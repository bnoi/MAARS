#!/usr/bin/env python
import argparse
import numpy as np
# import matplotlib.pyplot as plt
import os
from pandas import DataFrame
from numpy import diff
from numpy import genfromtxt
import math
import pandas as pd
import trackmate
import shutil
idx = pd.IndexSlice

class getMitosisFiles(object):
    """
    @summary: collect files related to mitotic cell to root dir of acquisition 
        usage:  python getMitosisFiles.py baseDir channel
                --help
    @param baseDir
    @param channel
    """
    def __init__(self, baseDir = "",  channel = "", totalNbOfCell = int, calibration = float, gap_tolerance = float
        ,elongating_trend = float, minimumPeriod = int, acq_interval = int):
        self._baseDir = baseDir
        self._channel = channel
        self._calibration = calibration
        self._totalNbOfCell = totalNbOfCell
        self._gap_tolerance = gap_tolerance
        self._elongating_trend = elongating_trend
        self._minimumPeriod = minimumPeriod
        self._acq_interval = acq_interval

    def set_attributes_from_cmd_line(self):
        parser = argparse.ArgumentParser(description="collect files \
            related to mitotic cell to root dir of acquisition ")
        parser.add_argument("baseDir",
                            help="path to acquisition folder",
                            type=str)
        parser.add_argument("channel",
                            help="channel used to detect SPBs",
                            type=str)
        parser.add_argument("-calibration",
                            help="calibration of image",
                            type=float, default=0.1075)
        parser.add_argument("-totalNbOfCell",
                            help="totalNbOfCell",
                            type=int, default=1200)
        parser.add_argument("-gap_tolerance",
                            help="maximum percent of gap that can be accepted",
                            type=float, default=0.23)
        parser.add_argument("-elongating_trend",
                            help="percentage of elongating timepoint",
                            type=float, default=0.6)
        parser.add_argument("-minimumPeriod",
                            help="minimum time segment to be analyzed",
                            type=int, default=200)
        parser.add_argument("-acq_interval",
                            help="interval of fluo acquisition",
                            type=int, default=20)
        args = parser.parse_args()
        self._baseDir = args.baseDir
        self._channel = args.channel
        self._totalNbOfCell = args.totalNbOfCell
        self._calibration = args.calibration
        self._gap_tolerance = args.gap_tolerance
        self._elongating_trend = args.elongating_trend
        self._minimumPeriod = args.minimumPeriod
        self._acq_interval = args.acq_interval

    def distance(self, x1,y1,x2,y2):
        xd = x2-x1
        yd = y2-y1
        return  math.sqrt(xd*xd + yd*yd)

    def analyzeSPBTrack(self, baseDir, cellNb, channel):
        geoPath = baseDir + '/movie_X0_Y0_FLUO/features/'+str(cellNb)+'_' + channel +'.csv'
        spotsPath = baseDir+ '/movie_X0_Y0_FLUO/spots/'+str(cellNb)+'_' + channel +'.xml'
        concat_data = list()
        major = 'Major'
        spAngToMajLabel = 'SpAngToMaj'
        frameLabel = 'Frame'
        xPos='x'
        yPos = 'y'
        spAng2MajVal = 0
        csvPath = baseDir + '/movie_X0_Y0/BF_Results.csv'
        if os.path.lexists(csvPath) : 
            cellRois = pd.DataFrame.from_csv(csvPath)
        current_cell_major = cellRois.loc[cellNb + 1][major] * self._calibration
        last_spot_0_x_y = None
        last_spot_1_x_y = None
        if os.path.lexists(geoPath) and os.path.lexists(spotsPath):
            oneCellGeo = pd.DataFrame.from_csv(geoPath)
            oneCellSpots = trackmate.trackmate_peak_import(spotsPath, False)
            for i in range(0, oneCellGeo.index[-1] + 1):
                if i in oneCellGeo.index:
                    spAng2MajVal = oneCellGeo[spAngToMajLabel].loc[i]
                    current_spot_0_x = oneCellSpots.loc[idx[i,:],idx[xPos,yPos]].iloc[0][xPos]
                    current_spot_0_y = oneCellSpots.loc[idx[i,:],idx[xPos,yPos]].iloc[0][yPos]
                    if np.isnan(spAng2MajVal):
                        if last_spot_0_x_y == None:
                            concat_data.append(np.array([i,
                                                         current_cell_major,
                                                         spAng2MajVal,
                                                         current_spot_0_x,
                                                         current_spot_0_y, 0]))
                            concat_data.append(np.array([i,
                                                         current_cell_major,
                                                         spAng2MajVal,
                                                         np.nan,
                                                         np.nan, 1]))
                            last_spot_0_x_y = [current_spot_0_x, current_spot_0_y]  
                        else:
                            if last_spot_1_x_y == None:
                                concat_data.append(np.array([i,
                                                             current_cell_major,
                                                             spAng2MajVal,
                                                             current_spot_0_x,
                                                             current_spot_0_y, 0]))
                                concat_data.append(np.array([i,
                                                             current_cell_major,
                                                             spAng2MajVal,
                                                             np.nan,
                                                             np.nan, 1]))
                                last_spot_0_x_y = [current_spot_0_x, current_spot_0_y]  
                            else:
                                dis00 = self.distance(last_spot_0_x_y[0], last_spot_0_x_y[1], current_spot_0_x, current_spot_0_y)
                                dis10 = self.distance(last_spot_1_x_y[0], last_spot_1_x_y[1], current_spot_0_x, current_spot_0_y)
                                if dis00 < dis10:
                                    concat_data.append(np.array([i,
                                                                 current_cell_major,
                                                                 spAng2MajVal,
                                                                 current_spot_0_x,
                                                                 current_spot_0_y, 0]))
                                    concat_data.append(np.array([i,
                                                                 current_cell_major,
                                                                 spAng2MajVal,
                                                                 np.nan,
                                                                 np.nan, 1]))
                                    last_spot_0_x_y = [current_spot_0_x, current_spot_0_y]  
                                else:
                                    concat_data.append(np.array([i,
                                                                 current_cell_major,
                                                                 spAng2MajVal,
                                                                 current_spot_0_x,
                                                                 current_spot_0_y, 1]))
                                    concat_data.append(np.array([i,
                                                                 current_cell_major,
                                                                 spAng2MajVal,
                                                                 np.nan,
                                                                 np.nan, 0]))
                                    last_spot_1_x_y = [current_spot_0_x, current_spot_0_y]  
                    else:
                        current_spot_1_x = oneCellSpots.loc[idx[i,:],idx[xPos,yPos]].iloc[1][xPos]
                        current_spot_1_y = oneCellSpots.loc[idx[i,:],idx[xPos,yPos]].iloc[1][yPos]
                        if last_spot_0_x_y == None:
                            concat_data.append(np.array([i,
                                                         current_cell_major,
                                                         spAng2MajVal,
                                                         current_spot_0_x,
                                                         current_spot_0_y, 0]))
                            concat_data.append(np.array([i,
                                                         current_cell_major,
                                                         spAng2MajVal,
                                                         current_spot_1_x,
                                                         current_spot_1_y, 1]))
                            last_spot_0_x_y = [current_spot_0_x, current_spot_0_y]
                            last_spot_1_x_y = [current_spot_1_x, current_spot_1_y]
                        else:
                            dis00 = self.distance(last_spot_0_x_y[0], last_spot_0_x_y[1], current_spot_0_x, current_spot_0_y)
                            dis01 = self.distance(last_spot_0_x_y[0], last_spot_0_x_y[1], current_spot_1_x, current_spot_1_y)
                            if dis00 < dis01:
                                concat_data.append(np.array([i,
                                                             current_cell_major,
                                                             spAng2MajVal,
                                                             current_spot_0_x,
                                                             current_spot_0_y, 0]))
                                concat_data.append(np.array([i,
                                                             current_cell_major,
                                                             spAng2MajVal,
                                                             current_spot_1_x,
                                                             current_spot_1_y, 1]))
                                last_spot_0_x_y = [current_spot_0_x, current_spot_0_y]
                                last_spot_1_x_y = [current_spot_1_x, current_spot_1_y]
                            else:
                                concat_data.append(np.array([i,
                                                             current_cell_major,
                                                             spAng2MajVal,
                                                             current_spot_0_x,
                                                             current_spot_0_y, 1]))
                                concat_data.append(np.array([i,
                                                             current_cell_major,
                                                             spAng2MajVal,
                                                             current_spot_1_x,
                                                             current_spot_1_y, 0]))
                                last_spot_0_x_y = [current_spot_1_x, current_spot_1_y]
                                last_spot_1_x_y = [current_spot_0_x, current_spot_0_y]
                else:
                    concat_data.append(np.array([i,
                                                 current_cell_major,
                                                 spAng2MajVal,
                                                 np.nan,
                                                 np.nan, np.nan]))
                    concat_data.append(np.array([i,
                                                 current_cell_major,
                                                 spAng2MajVal,
                                                 np.nan,
                                                 np.nan, np.nan]))
            concat_data = pd.DataFrame(concat_data, columns = [frameLabel, major, spAngToMajLabel, xPos, yPos, 'Track'])
        else:
            print('File(s) do(es) not exist')
        # fig, ax = plt.subplots(figsize=(8, 5))
        # ax.scatter(concat_data[concat_data['Track'] ==0]['x'], -concat_data[concat_data['Track'] ==0]['y'], color = 'green')
        # ax.plot(concat_data[concat_data['Track'] ==0]['x'], -concat_data[concat_data['Track'] ==0]['y'], color = 'green')
        # ax.scatter(concat_data[concat_data['Track'] ==1]['x'], -concat_data[concat_data['Track'] ==1]['y'],color = 'red')
        # ax.plot(concat_data[concat_data['Track'] ==1]['x'], -concat_data[concat_data['Track'] ==1]['y'],color = 'red')
        return concat_data

    def analyze(self):
        iteration_nb = self._totalNbOfCell
        gap_tolerance = self._gap_tolerance
        elongating_trend = self._elongating_trend
        minimumPeriod = self._minimumPeriod
        acq_interval = self._acq_interval
        minimumSegLength = minimumPeriod/acq_interval
        acqDir = self._baseDir
        channels = [self._channel,'GFP', 'TxRed', 'DAPI']
        for x in range(0, iteration_nb):
            csvPath = acqDir + '/movie_X0_Y0_FLUO/features/'+str(x)+'_' +channels[0]+'.csv'
            if os.path.lexists(csvPath) : 
                oneCell = genfromtxt(csvPath, delimiter=',', names=True, dtype= [('Frame', 'i'), ('Phase', np.str, 10), ('NbOfSpotDetected', '<i8'), ('SpLength', 'f'), ('SpCenterX', 'f'), ('SpCenterY', 'f'), ('CellCenterToSpCenterAng', 'f'), ('SpAngToMaj', 'f'), ('SpCenterZ', 'f'), ('CellCenterToSpCenterLen', 'f')])
                spLen = oneCell['SpLength']
                frames = oneCell['Frame']
                if len(spLen[spLen>0]) > minimumSegLength:
                    segmentList = list()
                    segment = dict()
                    nanInSegmentCount = 0
                    for i in range(0,len(spLen)):
                        val = spLen[i]
                        if not math.isnan(val):
                            segment[frames[i]] = val
                        elif nanInSegmentCount < len(segment)*gap_tolerance:
                            segment[frames[i]] = val
                            nanInSegmentCount +=1
                        else:
                            if len(segment) > minimumSegLength:
                                segment = {k: segment[k] for k in segment if not math.isnan(k)}
                                segmentList.append(segment)
                            segment = dict()
                            nanInSegmentCount = 0
                    if not segmentList and len(segment) >minimumSegLength:
                        segment = {k: segment[k] for k in segment if not math.isnan(k)}
                        segmentList.append(segment)
                    for segment in segmentList:
                        diffSeg = diff(list(segment.values()))
                        if len(diffSeg[diffSeg>0]) > len(diffSeg) * elongating_trend:
                            # fig, ax = plt.subplots(figsize=(15, 10))
                            # diffedLength = diff(spLen)
                            # ax.plot(frames[0:-1], diffedLength , '-x')
                            # ax.plot(frames, spLen, '-o')
                            # ax.plot(list(segment.keys()), list(segment.values()), lw = 5)
                            # ax.plot(list(segment.keys())[0:-1], diffSeg, lw = 5)
                            # ax.axhline(0)
                            # plt.ylabel("Spindle Length ($um$)", fontsize=20)
                            # plt.xlabel("frame // cell " + str(x), fontsize=20)
                            # plt.xlim(0, 200)
                            # plt.ylim(-5, 30)
                            # plt.show()
                            d = self.analyzeSPBTrack(acqDir, x, channels[0])
                            # print("shrink % : " + str(len(diffSeg[diffSeg<0])/len(diffSeg)*100))
                            # print("mean speed : %s um/min " % ((list(segment.values())[-1] - list(segment.values())[0])/len(segment.keys())*15))
                            croppedImgsDir = acqDir + "/cropImgs/"
                            spotsDir = acqDir + "/spots/"
                            csvDir = acqDir + "/csv/"
                            if not os.path.isdir(croppedImgsDir):
                                os.mkdir(croppedImgsDir)
                            if not os.path.isdir(spotsDir):
                                os.mkdir(spotsDir)
                            if not os.path.isdir(csvDir):
                                os.mkdir(csvDir)
                            d.to_csv(csvDir + "/d_" + str(x) + ".csv", sep='\t')
                            for ch in channels:
                                if os.path.lexists(acqDir + "/movie_X0_Y0_FLUO/croppedImgs/" + str(x) + "_" + ch + ".tif"):
                                    shutil.copyfile(acqDir + "/movie_X0_Y0_FLUO/croppedImgs/" + str(x) + "_" + ch + ".tif",  croppedImgsDir + str(x) + "_" + ch +".tif");
                                    shutil.copyfile(acqDir + "/movie_X0_Y0_FLUO/spots/" + str(x) + "_" + ch + ".xml", spotsDir + str(x) + "_" + ch + ".xml");

if __name__ == '__main__':
    launcher = getMitosisFiles()
    launcher.set_attributes_from_cmd_line()
    launcher.analyze()
    print("Collection done")
