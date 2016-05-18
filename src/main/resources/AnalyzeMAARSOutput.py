
# coding: utf-8

# In[3]:

#!/usr/bin/env python3
import numpy as np
import matplotlib.pyplot as plt
from os import path, mkdir, listdir
from pandas import DataFrame, IndexSlice, DataFrame
from numpy import diff, genfromtxt, round
from math import isnan, sqrt
from shutil import copyfile
from trackmate import trackmate_peak_import
from argparse import ArgumentParser
from re import match
from collections import deque
from scipy import stats
idx = IndexSlice
#%matplotlib inline

def perf_measure(real, predict, cellNbs):
    TP = 0
    FP = 0
    FN = 0
    TN = 0
    for predictVal in predict: 
        if predictVal in real:
            TP += 1
        else:
            FP += 1
    for realVal in real:
        if realVal not in predict:
            FN += 1
    for i in cellNbs:
        if i not in predict and i not in real:
            TN += 1
    return(TP, FP, FN, TN)

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
        self._minimumPeriod = 100
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
        self._minimumPeriod = args.minimumPeriod
        self._acq_interval = args.acq_interval

    def distance(self, x1,y1,x2,y2):
        xd = x2-x1
        yd = y2-y1
        return  sqrt(xd*xd + yd*yd)

    def getAllCellNumbers(self):
        all_cell_nbs=list()
        features_dir = self._baseDir + '_FLUO/features'
        for f in listdir(features_dir):
            current_cell_nb = f.split("_")[0]
            if current_cell_nb not in all_cell_nbs:
                all_cell_nbs.append(current_cell_nb)
        return all_cell_nbs, features_dir
    
    def getMitosisCellNbs(self, channel,features_dir,cellNbs, p):
        mitosis_cellNbs = list()
        minimumSegLength = self._minimumPeriod/self._acq_interval
        for cellNb in cellNbs:
            csvPath = features_dir + "/" + str(cellNb) + '_' + channel+'.csv'
            if path.lexists(csvPath) : 
                oneCell = genfromtxt(csvPath, delimiter=',', names=True, dtype= float)
                spLens = oneCell['SpLength']
                frames = oneCell['Frame']
                if len(spLens[spLens>0]) > minimumSegLength:
                    rawSpLens = spLens
                    nans, x= self.nan_helper(spLens)
                    spLens[nans]= np.interp(x(nans), x(~nans), spLens[~nans])
                    maxValue = max(spLens)
                    firstMax = [i for i, j in enumerate(spLens) if j == maxValue][0]
                    for i in range(firstMax, len(spLens)):
                        spLens[i] = max(spLens)
                    mito_region = list()
                    f = list()
                    minValue = min(spLens)
                    lastMin = [i for i, j in enumerate(spLens) if j == minValue][-1]
                    for i in range(lastMin,firstMax+1):
                        mito_region.append(rawSpLens[i])
                        f.append(i)
                    if len(f)>0:
                        slope, intercept, r_value, p_value, std_err = stats.linregress(f, mito_region)
#                         print(r_value, p_value, std_err)
                        if p_value < 3*10**(-9):
                            if cellNb not in mitosis_cellNbs:
                                mitosis_cellNbs.append(cellNb)
        return mitosis_cellNbs
    
    def load_rois(self):
        csvPath = self._baseDir + '/BF_Results.csv' 
        return DataFrame.from_csv(csvPath)
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
            plt.savefig(figureDir + str(cellNb) + "_SPBtracks")
        else:
            plt.show()
        plt.close(fig)
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
                    if (slope>=0):
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
                if len(change_points_values) == 4:
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
                if doPlot and spLen[idx] > 0.15:
                    ax.axvline(frame[idx])
                    ax.axhline(spLen[idx])
                    plotedPoints.append(int(round(frame[idx])))
            changePoints = "_".join(str(e) for e in sorted(plotedPoints))
            plt.ylabel("Scaled spindle length (to cell major axe)", fontsize=20)
            plt.xlabel("Change_Point(s)_" + changePoints, fontsize=20)
            plt.xlim(0)
            plt.ylim(-0.05, 1)
            if save:
                plt.savefig(figureDir + str(cellNb) + "_slopChangePoints_" + changePoints)
            else:
                plt.show()
            plt.close(fig)

    def analyze(self, save, mitosis_cellNbs, allCellNbs, channels):
        major = 'Major'
        croppedImgsDir, spotsDir, csvDir, featuresDir, figureDir = self.createOutputDirs()
        cellRois = self.load_rois()
        for cellNb in mitosis_cellNbs:
            current_major_length = cellRois.loc[int(cellNb)][major] * self._calibration
            for ch in channels:
                if path.lexists(self._baseDir + "_FLUO/croppedImgs/" + str(cellNb) + "_" + ch + ".tif"):
                    copyfile(self._baseDir + "_FLUO/croppedImgs/" + str(cellNb) + "_" + ch + ".tif",  croppedImgsDir + str(cellNb) + "_" + ch +".tif")
                    copyfile(self._baseDir + "_FLUO/spots/" + str(cellNb) + "_" + ch + ".xml", spotsDir + str(cellNb) + "_" + ch + ".xml")
                    copyfile(self._baseDir + "_FLUO/features/" + str(cellNb) + "_" + ch + ".csv", featuresDir + str(cellNb) + "_" + ch + ".csv");
            cellFeaturesPath = featuresDir + str(cellNb) + '_' + channels[0]+'.csv'
            currentCellFeatures = genfromtxt(cellFeaturesPath, delimiter=',', names=True, dtype= float)
            spLens = currentCellFeatures['SpLength']
            frames = currentCellFeatures['Frame']
            nans, x= self.nan_helper(spLens)
            spLens[nans]= np.interp(x(nans), x(~nans), spLens[~nans])
            #
            fig, ax = plt.subplots(figsize=(15, 10))
            ax.plot(frames, spLens, '-o')
            ax.axhline(current_major_length,  c= 'red', lw = 10)
            plt.ylabel("Spindle Length ($μm$)", fontsize=20)
            plt.xlabel("Time (second)", fontsize=20)
            plt.tick_params(axis='both', which='major', labelsize=20)
            #ax.axvline(380, c='blue')
            #ax.axvline(500, c='red')
            #plt.xlabel("Frame // cell " + str(cellNb), fontsize=20)
            plt.xlim(0)
            plt.ylim(0,current_major_length)
            maxValue = max(spLens)
            firstMaxIndex = [i for i, j in enumerate(spLens) if j == maxValue][0]
            for i in range(firstMaxIndex, len(spLens)):
                spLens[i] = maxValue
            ax.plot(frames, spLens, '-o')
            mito_region = list()
            f = list()
            minValue = min(spLens)
            lastMin = [i for i, j in enumerate(spLens) if j == minValue][-1]
            for i in range(lastMin,firstMaxIndex+1):
                mito_region.append(spLens[i])
                f.append(frames[i])
            ax.plot(f, mito_region, '-o')
            if save:
                plt.savefig(figureDir + str(cellNb) + "_elongation", bbox_inches='tight')
            else:
                plt.show()
            plt.close(fig)
            
            #
            self.find_slope_change_point(spLens, frames, current_major_length, figureDir, cellNb, save)
            
            #
            d = self.analyzeSPBTrack(self._baseDir, cellNb, channels[0], figureDir, current_major_length, save)
            d.to_csv(csvDir + "/d_" + str(cellNb) + ".csv", sep='\t')
            
if __name__ == '__main__':
    baseDir="/home/tong/Documents/movies/102/60x/dynamic/25-03-1/X0_Y0"
    channels = ['CFP','GFP', 'TxRed', 'DAPI']
    launcher = getMitosisFiles(baseDir, channels[0])
    launcher.set_attributes_from_cmd_line()
    cellNbs, features_dir = launcher.getAllCellNumbers()
    cellNbs = [int(n) for n in cellNbs]
    #file = open(baseDir+"/anot")
    #data = file.readlines()
    #realList = [int(n) for n in data[0].split(',')]
    #tprs = list()
    #fprs = list()
    predictedList = launcher.getMitosisCellNbs(channels[0],features_dir,cellNbs,0)
    predictedList = [int(n) for n in predictedList]
    #tp, fp, fn, tn = perf_measure(realList,predictedList,cellNbs)
    #print('TP%s_FP%s_FN%s_TN%s' %(tp,fp,fn,tn))
    #print(tp+fp+fn+tn)
    launcher.analyze(True,predictedList,cellNbs, channels)


# In[ ]:



