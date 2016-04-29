
# coding: utf-8

# In[3]:

#!/usr/bin/env python3
import numpy as np
import matplotlib.pyplot as plt
from os import path, mkdir, listdir
from pandas import DataFrame, IndexSlice, DataFrame
from numpy import diff
from numpy import genfromtxt
from math import isnan, sqrt
from shutil import copyfile
from trackmate import trackmate_peak_import
from argparse import ArgumentParser
from re import match
from collections import deque
idx = IndexSlice
#get_ipython().magic('matplotlib inline')

class getMitosisFiles(object):
    """
    @summary: collect files related to mitotic cell to root dir of acquisition 
        usage:  python getMitosisFiles.py segDir channel
                --help
    @param baseDir
    @param channel
    """
    def __init__(self, baseDir = str,  channel = str):
        self._baseDir = baseDir
        self._channel = channel
        self._calibration = 0.1075
        self._gap_tolerance = 0.3
        self._elongating_trend = 0.6
        self._minimumPeriod = 200
        self._acq_interval = 20

    def set_attributes_from_cmd_line(self):
        parser = ArgumentParser(description="collect files             related to mitotic cell to root dir of acquisition ")
        parser.add_argument("baseDir",
                            help="path to acquisition folder",
                            type=str)
        parser.add_argument("channel",
                            help="channel used to detect SPBs",
                            type=str)
        parser.add_argument("-calibration",
                            help="calibration of image",
                            type=float, default=0.1075)
        parser.add_argument("-gap_tolerance",
                            help="maximum percent of gap that can be accepted",
                            type=float, default=0.3)
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
        self._calibration = args.calibration
        self._gap_tolerance = args.gap_tolerance
        self._elongating_trend = args.elongating_trend
        self._minimumPeriod = args.minimumPeriod
        self._acq_interval = args.acq_interval

    def distance(self, x1,y1,x2,y2):
        xd = x2-x1
        yd = y2-y1
        return  sqrt(xd*xd + yd*yd)

    def getAllCellNnumbers(self):
        all_cell_nbs=list()
        features_dir = self._baseDir + '_FLUO/features'
        for f in listdir(features_dir):
            current_cell_nb = f.split("_")[0]
            if current_cell_nb not in all_cell_nbs:
                all_cell_nbs.append(current_cell_nb)
        return all_cell_nbs, features_dir
    
    def getSegList(self,minimumSegLength, spLen, frames):
        segmentList = list()
        segment = dict()
        nanInSegmentCount = 0
        for i in range(0,len(spLen)):
            val = spLen[i]
            if not isnan(val):
                segment[frames[i]] = val
            elif nanInSegmentCount < len(segment) * self._gap_tolerance:
                segment[frames[i]] = val
                nanInSegmentCount +=1
            else:
                if len(segment) > minimumSegLength:
                    segment = {k: segment[k] for k in segment if not isnan(segment[k])}
                    segmentList.append(segment)
                segment = dict()
                nanInSegmentCount = 0
        if not segmentList and len(segment) >minimumSegLength:
            segment = {k: segment[k] for k in segment if not isnan(segment[k])}
            segmentList.append(segment)
        return segmentList
    
    def getMitosisCellNbs(self, channel):
        mitosis_cellNbs = list()
        minimumSegLength = self._minimumPeriod/self._acq_interval
        cellNbs, features_dir = self.getAllCellNnumbers()
        for cellNb in cellNbs:
            csvPath = features_dir + "/" + cellNb + '_' + channel+'.csv'
            if path.lexists(csvPath) : 
                oneCell = genfromtxt(csvPath, delimiter=',', names=True, dtype= float)
                spLens = oneCell['SpLength']
                if len(spLens[spLens>0]) > minimumSegLength:
                    end = np.where(spLens == np.nanmax(spLens))[0]
                    spLens = spLens[:end+1]
                    if len(spLens) > minimumSegLength:
                        frames = oneCell['Frame'][:end+1]
                        segmentList = self.getSegList(minimumSegLength, spLens, frames)
                        for segment in segmentList:
                            diffSeg = diff(list(segment.values()))
                            if len(diffSeg[diffSeg>0]) > len(diffSeg) * self._elongating_trend:
                                if cellNb not in mitosis_cellNbs:
                                    mitosis_cellNbs.append(cellNb)
        return mitosis_cellNbs
    
    def nan_helper(self, y):
        """Helper to handle indices and logical indices of NaNs.

        Input:
            - y, 1d numpy array with possible NaNs
        Output:
            - nans, logical indices of NaNs
            - index, a function, with signature indices= index(logical_indices),
              to convert logical indices of NaNs to 'equivalent' indices
        Example:
            >>> # linear interpolation of NaNs
            >>> nans, x= nan_helper(y)
            >>> y[nans]= np.interp(x(nans), x(~nans), y[~nans])
        """

        return np.isnan(y), lambda z: z.nonzero()[0]
    
    def findKeyWithSmallestVal(self, dic):
        smallest_value = np.inf
        smallest_key = np.inf
        for k in dic:
            if dic[k] < smallest_value:
                smallest_value = dic[k]
                smallest_key = k
        return smallest_key
    
    def createOutputDirs(self):
        mitosisDir = self._baseDir + "_MITOSIS/"
        croppedImgsDir = mitosisDir + "cropImgs/"
        spotsDir = mitosisDir + "spots/"
        csvDir = mitosisDir + "csv/"
        featuresDir = mitosisDir + "features/"
        figureDir = mitosisDir + "figs/"
        if not path.isdir(mitosisDir):
            mkdir(mitosisDir)
        if not path.isdir(croppedImgsDir):
            mkdir(croppedImgsDir)
        if not path.isdir(spotsDir):
            mkdir(spotsDir)
        if not path.isdir(csvDir):
            mkdir(csvDir)
        if not path.isdir(featuresDir):
            mkdir(featuresDir)
        if not path.isdir(figureDir):
            mkdir(figureDir)
        return croppedImgsDir, spotsDir, csvDir, featuresDir, figureDir
        
    def analyzeSPBTrack(self, baseDir, cellNb, channel, figureDir, cell_major_length, save):
        geoPath = baseDir + '_FLUO/features/'+str(cellNb)+'_' + channel +'.csv'
        spotsPath = baseDir+ '_FLUO/spots/'+str(cellNb)+'_' + channel +'.xml'
        concat_data = list()
        spAngToMajLabel = 'SpAngToMaj'
        frameLabel = 'Frame'
        xPos='x'
        yPos = 'y'
        last_spot_0_x_y = None
        last_spot_1_x_y = None
        if path.lexists(geoPath) and path.lexists(spotsPath):
            oneCellGeo = DataFrame.from_csv(geoPath)
            oneCellSpots = trackmate_peak_import(spotsPath, False)
            for i in range(0, oneCellGeo.index[-1] + 1):
                if i in oneCellGeo.index:
                    spAng2MajVal = oneCellGeo[spAngToMajLabel].loc[i]
                    current_spot_0_x = oneCellSpots.loc[idx[i,:],idx[xPos,yPos]].iloc[0][xPos]
                    current_spot_0_y = oneCellSpots.loc[idx[i,:],idx[xPos,yPos]].iloc[0][yPos]
                    if np.isnan(spAng2MajVal):
                        if last_spot_0_x_y == None:
                            concat_data.append(np.array([i,
                                                         cell_major_length,
                                                         spAng2MajVal,
                                                         current_spot_0_x,
                                                         current_spot_0_y, 0]))
                            concat_data.append(np.array([i,
                                                         cell_major_length,
                                                         spAng2MajVal,
                                                         np.nan,
                                                         np.nan, 1]))
                            last_spot_0_x_y = [current_spot_0_x, current_spot_0_y]  
                        else:
                            if last_spot_1_x_y == None:
                                concat_data.append(np.array([i,
                                                             cell_major_length,
                                                             spAng2MajVal,
                                                             current_spot_0_x,
                                                             current_spot_0_y, 0]))
                                concat_data.append(np.array([i,
                                                             cell_major_length,
                                                             spAng2MajVal,
                                                             np.nan,
                                                             np.nan, 1]))
                                last_spot_0_x_y = [current_spot_0_x, current_spot_0_y]  
                            else:
                                dis00 = self.distance(last_spot_0_x_y[0], last_spot_0_x_y[1], current_spot_0_x, current_spot_0_y)
                                dis10 = self.distance(last_spot_1_x_y[0], last_spot_1_x_y[1], current_spot_0_x, current_spot_0_y)
                                if dis00 < dis10:
                                    concat_data.append(np.array([i,
                                                                 cell_major_length,
                                                                 spAng2MajVal,
                                                                 current_spot_0_x,
                                                                 current_spot_0_y, 0]))
                                    concat_data.append(np.array([i,
                                                                 cell_major_length,
                                                                 spAng2MajVal,
                                                                 np.nan,
                                                                 np.nan, 1]))
                                    last_spot_0_x_y = [current_spot_0_x, current_spot_0_y]  
                                else:
                                    concat_data.append(np.array([i,
                                                                 cell_major_length,
                                                                 spAng2MajVal,
                                                                 current_spot_0_x,
                                                                 current_spot_0_y, 1]))
                                    concat_data.append(np.array([i,
                                                                 cell_major_length,
                                                                 spAng2MajVal,
                                                                 np.nan,
                                                                 np.nan, 0]))
                                    last_spot_1_x_y = [current_spot_0_x, current_spot_0_y]  
                    else:
                        current_spot_1_x = oneCellSpots.loc[idx[i,:],idx[xPos,yPos]].iloc[1][xPos]
                        current_spot_1_y = oneCellSpots.loc[idx[i,:],idx[xPos,yPos]].iloc[1][yPos]
                        if last_spot_0_x_y == None:
                            concat_data.append(np.array([i,
                                                         cell_major_length,
                                                         spAng2MajVal,
                                                         current_spot_0_x,
                                                         current_spot_0_y, 0]))
                            concat_data.append(np.array([i,
                                                         cell_major_length,
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
                                                             cell_major_length,
                                                             spAng2MajVal,
                                                             current_spot_0_x,
                                                             current_spot_0_y, 0]))
                                concat_data.append(np.array([i,
                                                             cell_major_length,
                                                             spAng2MajVal,
                                                             current_spot_1_x,
                                                             current_spot_1_y, 1]))
                                last_spot_0_x_y = [current_spot_0_x, current_spot_0_y]
                                last_spot_1_x_y = [current_spot_1_x, current_spot_1_y]
                            else:
                                concat_data.append(np.array([i,
                                                             cell_major_length,
                                                             spAng2MajVal,
                                                             current_spot_0_x,
                                                             current_spot_0_y, 1]))
                                concat_data.append(np.array([i,
                                                             cell_major_length,
                                                             spAng2MajVal,
                                                             current_spot_1_x,
                                                             current_spot_1_y, 0]))
                                last_spot_0_x_y = [current_spot_1_x, current_spot_1_y]
                                last_spot_1_x_y = [current_spot_0_x, current_spot_0_y]
                else:
                    concat_data.append(np.array([i,
                                                 cell_major_length,
                                                 np.nan,
                                                 np.nan,
                                                 np.nan, np.nan]))
                    concat_data.append(np.array([i,
                                                 cell_major_length,
                                                 np.nan,
                                                 np.nan,
                                                 np.nan, np.nan]))
            concat_data = DataFrame(concat_data, columns = [frameLabel, 'Major', spAngToMajLabel, xPos, yPos, 'Track'])
        else:
            print('File(s) do(es) not exist')
        fig, ax = plt.subplots(figsize=(15, 10))
        ax.scatter(concat_data[concat_data['Track'] ==0]['x'], -concat_data[concat_data['Track'] ==0]['y'], color = 'green')
        ax.plot(concat_data[concat_data['Track'] ==0]['x'], -concat_data[concat_data['Track'] ==0]['y'], color = 'green')
        ax.scatter(concat_data[concat_data['Track'] ==1]['x'], -concat_data[concat_data['Track'] ==1]['y'],color = 'red')
        ax.plot(concat_data[concat_data['Track'] ==1]['x'], -concat_data[concat_data['Track'] ==1]['y'],color = 'red')
        plt.ylabel("Absolute y in original fluo image (pixel)", fontsize=20)
        plt.xlabel("Absolute x in original fluo image (pixel)", fontsize=20)
        if save:
            plt.savefig(figureDir + cellNb + "_track")
        else:
            plt.show()
        return concat_data
    
    def find_slope_change_point(self, spLen, frame, majorAxieLen, figureDir, cellNb, save):
        fig, ax = plt.subplots(figsize=(15, 10))
        slopes= dict()
        minSegTimeLen = 180
        timeInterval = 20
        changePointMinimalLen = 20;
        minSegLen = int(minSegTimeLen/timeInterval)
        idxQueue = deque()
        queue = deque()
        spLen = spLen/majorAxieLen
        nans, x= self.nan_helper(spLen)
        spLen[nans]= np.interp(x(nans), x(~nans), spLen[~nans])
        if len(spLen) > 2*minSegLen:
            for i in range(0, len(spLen)):
                queue.append(spLen[i])
                idxQueue.append(frame[i])
                if len(queue) > minSegLen:
                    idxQueue.popleft()
                    queue.popleft()
                    par = np.polyfit(idxQueue, queue, 1 ,full = True)
                    slope=par[0][0]
                    slopes[frame[i]] = slope
                    intercept=par[0][1]
                    theo_line1_x = np.arange(frame[i]- minSegLen, frame[i],1)
                    theo_line1_y = []
                    for x in theo_line1_x:
                        theo_line1_y.append(x * slope + intercept)
                    if (slope>0):
                        ax.plot(theo_line1_x,theo_line1_y, lw = 1 + i*0.1, c='red')
                    else:
                        ax.plot(theo_line1_x,theo_line1_y, lw = 1 + i*0.1, c = 'blue')
            diffrences = dict()
            slopesKeys = list(slopes.keys())
            for x in range(int(minSegLen), len(slopesKeys)):
                diffrences[slopesKeys[x- minSegLen]] = slopes[slopesKeys[x]] - slopes[slopesKeys[x - minSegLen]]
            diffValues = DataFrame.from_dict(diffrences, 'index')
            positiveDiffValues = diffValues[diffValues>=0]
            ax.plot(list(diffrences.keys()), positiveDiffValues, lw = 5, c = 'red')
            negativeDiffValues = diffValues[diffValues<0]
            ax.plot(list(diffrences.keys()), negativeDiffValues, lw = 5, c = 'blue')
            change_points_values = dict()
            for f in diffrences.keys():
                idx = 0
                for y in range(0, len(frame)):
                    if frame[y] == f:
                        idx = y
                change_points_values[f] = diffrences[f]
                if len(change_points_values) == 3:
                    key_to_pop = self.findKeyWithSmallestVal(change_points_values)
                    change_points_values.pop(key_to_pop, None)
            ax.plot(frame, spLen, '-o')
            ax.axhline(0)
            ax.axhline(1 , c = 'red' , lw = 10)
            doPlot = True
            plotedPoints = list()
            for p in list(change_points_values.keys()):
                idx = 0
                for y in range(0, len(frame)):
                    if frame[y] == p:
                        idx = y
                for point in plotedPoints:
                    if np.abs(point - idx) < changePointMinimalLen:
                        doPlot = False
                    else:
                        doPlot = True
                if doPlot:
                    ax.axvline(frame[idx])
                    ax.axhline(spLen[idx])
                plotedPoints.append(idx)

            plt.ylabel("Scaled spindle length (to cell major axe)", fontsize=20)
            plt.xlabel("Frame ", fontsize=20)
            plt.xlim(0)
            plt.ylim(-0.2, 1)
            if save:
                plt.savefig(figureDir + cellNb + "_changePoints")
            else:
                plt.show()
        else:
            idx = 0
        return spLen[idx]

    def analyze(self, save):
        major = 'Major'
        channels = ['CFP','GFP', 'TxRed', 'DAPI']
        mitosis_cellNbs = self.getMitosisCellNbs(channels[0])
        croppedImgsDir, spotsDir, csvDir, featuresDir, figureDir = self.createOutputDirs()
        csvPath = self._baseDir + '/BF_Results.csv' 
        cellRois = DataFrame.from_csv(csvPath)
        for cellNb in mitosis_cellNbs:
            current_major_length = cellRois.loc[int(cellNb)][major] * self._calibration
            for ch in channels:
                if path.lexists(self._baseDir + "_FLUO/croppedImgs/" + cellNb + "_" + ch + ".tif"):
                    copyfile(self._baseDir + "_FLUO/croppedImgs/" + cellNb + "_" + ch + ".tif",  croppedImgsDir + cellNb + "_" + ch +".tif")
                    copyfile(self._baseDir + "_FLUO/spots/" + cellNb + "_" + ch + ".xml", spotsDir + cellNb + "_" + ch + ".xml")
                    copyfile(self._baseDir + "_FLUO/features/" + cellNb + "_" + ch + ".csv", featuresDir + cellNb + "_" + ch + ".csv");
            cellFeaturesPath = featuresDir + cellNb + '_' + channels[0]+'.csv'
            oneCellFeatures = genfromtxt(cellFeaturesPath, delimiter=',', names=True, dtype= float)
            spLens = oneCellFeatures['SpLength']
            end = np.where(spLens == np.nanmax(spLens))[-1]
            spLens = spLens[:end+1]
            print(cellNb)
            if len(spLens) > self._minimumPeriod/self._acq_interval:
                frames = oneCellFeatures['Frame']
                frames = frames[:end+1]
                diffedLengths = diff(spLens)
                #
                fig, ax = plt.subplots(figsize=(15, 10))
                ax.axhline(current_major_length,  c= 'red', lw = 10)
                ax.plot(frames[1:], diffedLengths , '-x')
                ax.plot(frames, spLens, '-o')
                ax.axhline(0)
                plt.ylabel("Spindle Length ($μm$)", fontsize=20)
                plt.xlabel("Frame // cell " + cellNb, fontsize=20)
                plt.xlim(0)
                plt.ylim(-2,current_major_length)
                if save:
                    plt.savefig(figureDir + cellNb + "_elongation")
                else:
                    plt.show()
                #
                self.find_slope_change_point(spLens, frames, current_major_length, figureDir, cellNb, save)
                #
                d = self.analyzeSPBTrack(self._baseDir, cellNb, channels[0], figureDir, current_major_length, save)
                d.to_csv(csvDir + "/d_" + cellNb + ".csv", sep='\t')
            
if __name__ == '__main__':
    launcher = getMitosisFiles("/Volumes/Macintosh/curioData/102/25-03-1/X0_Y0", "CFP")
    launcher.set_attributes_from_cmd_line()
    launcher.analyze(True)
#     plt.hist(change_point_lengths)
    print("Collection done")


# In[ ]:




# In[ ]:



