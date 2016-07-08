
# coding: utf-8

# In[4]:

#!/usr/bin/env python3
import numpy as np
import matplotlib.pyplot as plt
from os import path, mkdir, listdir
from pandas import DataFrame, IndexSlice
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

def findKeyWithSmallestVal(dic):
    smallest_value = np.inf
    smallest_key = np.inf
    for k in dic:
        if dic[k] < smallest_value:
            smallest_value = dic[k]
            smallest_key = k
    return smallest_key

def find_slope_change_point(frames, spLens, majorAxieLen, figureDir, cellNb, save):
    slopes= dict()
#   all in second
    minSegTimeLen = 180
    timeInterval = 20
    changePointMinInterval = 240;
    
#     sliding window size for slope detection
    minSegLen = int(minSegTimeLen/timeInterval)
#     normaliz spindle size with cell length
    normed_spLens = spLens/majorAxieLen
    idxQueue = deque()
    queue = deque()
    
#     fill nan numbers in spindle detection time point
    nans, x= nan_helper(normed_spLens)
    normed_spLens[nans]= np.interp(x(nans), x(~nans), normed_spLens[~nans])
    
#     ensure to have at least two slope change values
    if len(normed_spLens) > 2*minSegLen:
        fig, ax = plt.subplots(figsize=(15, 10))
        for i in range(0, len(normed_spLens)):
            queue.append(normed_spLens[i])
            idxQueue.append(frames[i])
            if len(queue) == minSegLen:
                idxQueue.popleft()
                queue.popleft()
                par = np.polyfit(idxQueue, queue, 1 ,full = True)
                slope=par[0][0]
                slopes[frames[i]] = slope
                intercept=par[0][1]
#                 show the fitted lines
                theo_line1_x = np.arange(frames[i]- minSegLen, frames[i],1)
                theo_line1_y = []
                for x in theo_line1_x:
                    theo_line1_y.append(x * slope + intercept)
                if (slope>=0):
                    ax.plot(theo_line1_x,theo_line1_y, lw = 1 + i*0.1, c='red')
                else:
                    ax.plot(theo_line1_x,theo_line1_y, lw = 1 + i*0.1, c = 'blue')
        diffrences = dict()
        slopesFrames = list(slopes.keys())
        for x in range(int(minSegLen), len(slopesFrames)):
            diffrences[slopesFrames[x- minSegLen]] = slopes[slopesFrames[x]] - slopes[slopesFrames[x - minSegLen]]
#         for visualization
        diff_f = list(diffrences.keys())
        diffValues = DataFrame.from_dict(diffrences, 'index')
        upper = np.ma.masked_where(diffValues < 0, diffValues)
        lower = np.ma.masked_where(diffValues >= 0, diffValues)
        plt.plot(diff_f, lower, 'bs', diff_f, upper, 'r^', lw = 5)
        
#         find the 2 maximum change points
        change_points_values = list()
        while len(change_points_values) <2 and len(diffrences) > 0 :
            maxInd = max(diffrences, key=diffrences.get)
            change_points_values.append(maxInd)
            new_diff = diffrences.copy()
            for ind in diffrences.keys():
                if ind <  maxInd + changePointMinInterval / timeInterval and ind > maxInd - changePointMinInterval / timeInterval:
                    new_diff.pop(ind,None)
            diffrences = new_diff
        ax.plot(frames, normed_spLens, '-o')
        ax.axhline(1 , c = 'red' , lw = 10)
        doPlot = True
        for p in change_points_values:
            ind = 0
            for y in range(0, len(frames)):
                if frames[y] == p:
                    ind = y
            ax.axvline(frames[ind])
            ax.axhline(normed_spLens[ind])
        changePoints = "_".join(str(int(e)) for e in sorted(change_points_values))
        plt.ylabel("Scaled spindle length (to cell major axe)", fontsize=20)
        plt.xlabel("Change_Point(s)_" + changePoints, fontsize=20)
        plt.xlim(0)
        plt.ylim(-0.05, 1)
        if save:
            plt.savefig(figureDir + str(cellNb) + "_slopChangePoints_" + changePoints + ".png")
        else:
            plt.show()
        plt.close(fig)
        return diffrences
    
def distance(x1,y1,x2,y2):
    xd = x2-x1
    yd = y2-y1
    return  sqrt(xd*xd + yd*yd)

def nan_helper(y):
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
        self._fluo_suffix = "_FLUO/"
        self._mitosis_suffix = "_MITOSIS/"
        self._cropImgs = "croppedImgs"
        self._spots = "spots"
        self._csv = "csv"
        self._figs = "figs"
        self._features = "features"
        

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

    def getAllCellNumbers(self):
        all_cell_nbs=list()
        features_dir = self._baseDir + self._fluo_suffix + self._features
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
                    minValue = np.nanmin(spLens)
                    maxValue = np.nanmax(spLens)
                    nans, x= nan_helper(spLens)
                    spLens[nans]= np.interp(x(nans), x(~nans), spLens[~nans])
                    lastMinIndex = np.where(spLens == minValue)[0][-1]
                    firstMaxIndex = np.where(spLens == maxValue)[0][-1]
                    for i in range(firstMaxIndex, len(spLens)):
                        spLens[i] = max(spLens)
                    mito_region = list()
                    f = list()
                    if (firstMaxIndex - lastMinIndex) * self._acq_interval > 120:
                        for i in range(lastMinIndex,firstMaxIndex+1):
                            mito_region.append(spLens[i])
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
    
    def createOutputDirs(self):
        mitosisDir = self._baseDir + self._mitosis_suffix
        croppedImgsDir = mitosisDir + self._cropImgs + "/"
        spotsDir = mitosisDir + self._spots + "/"
        csvDir = mitosisDir + self._csv + "/"
        featuresDir = mitosisDir + self._features + "/"
        figureDir = mitosisDir + self._figs + "/"
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
        return mitosisDir
        
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
                                dis00 = distance(last_spot_0_x_y[0], last_spot_0_x_y[1], current_spot_0_x, current_spot_0_y)
                                dis10 = distance(last_spot_1_x_y[0], last_spot_1_x_y[1], current_spot_0_x, current_spot_0_y)
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
                            dis00 = distance(last_spot_0_x_y[0], last_spot_0_x_y[1], current_spot_0_x, current_spot_0_y)
                            dis01 = distance(last_spot_0_x_y[0], last_spot_0_x_y[1], current_spot_1_x, current_spot_1_y)
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

    def analyze(self, save, mitosis_cellNbs, allCellNbs, channel):
        all_mitoregion_spLens = dict()
        all_slope_changes = dict()
        major = 'Major'
        mitosisDir = self.createOutputDirs()
        cellRois = self.load_rois()
        for cellNb in mitosis_cellNbs:
            fig, ax = plt.subplots(figsize=(15, 8))
            current_major_length = cellRois.loc[int(cellNb)][major] * self._calibration
            cellFeaturesPath = mitosisDir + self._features + "/" + str(cellNb) + '_' + channel+'.csv'
            currentCellFeatures = genfromtxt(self._baseDir + self._fluo_suffix + self._features + "/"  + str(cellNb) + "_" + channel + ".csv", delimiter=',', names=True, dtype= float)
            spLens = currentCellFeatures['SpLength']
            frames = currentCellFeatures['Frame']
            
            ax.axhline(current_major_length,  c= 'red', lw = 10)
            plt.ylabel("Spindle Length ($μm$)", fontsize=20)
            plt.tick_params(axis='both', which='major', labelsize=20)
            plt.xlabel("Frame // cell " + str(cellNb), fontsize=20)
            plt.xlim(0, len(spLens))
            plt.ylim(0, current_major_length)
            maxValue = np.nanmax(spLens)
            firstMaxIndex = np.where(spLens == maxValue)[0]
            for i in range(firstMaxIndex, len(spLens)):
                spLens[i] = maxValue
#             ax.plot(frames, spLens, '-o')
            mito_region = dict()
            minValue = np.nanmin(spLens)
            lastMinIndex = np.where(spLens == minValue)[-1]
            for i in range(lastMinIndex,firstMaxIndex+1):
                mito_region[frames[i]] = spLens[i]
            ax.plot(list(mito_region.keys()), list(mito_region.values()), "-o")
            all_mitoregion_spLens[cellNb] = mito_region
            #
            figureDir = mitosisDir + self._figs + "/"
            all_slope_changes[cellNb] = find_slope_change_point(frames, spLens, current_major_length, figureDir, cellNb, save)
            
            #
            d = self.analyzeSPBTrack(self._baseDir, cellNb, channel, figureDir, current_major_length, save)
            d.to_csv(mitosisDir + self._csv + "/d_" + str(cellNb) + ".csv", sep='\t')
            if save:
                plt.savefig(figureDir + str(cellNb) + "_elongation", bbox_inches='tight')
            else:
                plt.show()
            plt.close(fig)
        return all_mitoregion_spLens, all_slope_changes
    
    def pick_mitosis_files(self,mitosis_cellNbs, channels):
        for cellNb in mitosis_cellNbs:
            for ch in channels:
                if path.lexists(self._baseDir + self._fluo_suffix + self._cropImgs + "/" + str(cellNb) + "_" + ch + ".tif"):
                    copyfile(self._baseDir + self._fluo_suffix + self._cropImgs + "/" + str(cellNb) + "_" + ch + ".tif",  self._baseDir + self._mitosis_suffix + self._cropImgs + "/" + str(cellNb) + "_" + ch +".tif")
                    copyfile(self._baseDir + self._fluo_suffix + self._spots + "/" + str(cellNb) + "_" + ch + ".xml", self._baseDir + self._mitosis_suffix + self._spots + "/" + str(cellNb) + "_" + ch + ".xml")
                    copyfile(self._baseDir + self._fluo_suffix + self._features + "/" + str(cellNb) + "_" + ch + ".csv", self._baseDir + self._mitosis_suffix + self._features + "/" + str(cellNb) + "_" + ch + ".csv");
if __name__ == '__main__':
    
    baseDir="/Volumes/Macintosh/curioData/102/11-06-1/X0_Y0"
    channels = ['CFP','GFP', 'TxRed', 'DAPI']
    launcher = getMitosisFiles(baseDir, channels[0])
    launcher.set_attributes_from_cmd_line()
    cellNbs, features_dir = launcher.getAllCellNumbers()
    cellNbs = [int(n) for n in cellNbs]
    predictedList = launcher.getMitosisCellNbs(channels[0],features_dir,cellNbs,0)
    predictedList = [int(n) for n in predictedList]
    all_mitoregion_spLens, all_slope_changes = launcher.analyze(True,predictedList,cellNbs, channels[0])
    launcher.pick_mitosis_files(predictedList, channels)
    print("Done")


# In[ ]:



