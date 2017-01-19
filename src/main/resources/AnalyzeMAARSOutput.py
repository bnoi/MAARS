
# coding: utf-8

# In[262]:

#!/usr/bin/env python3
import numpy as np
import matplotlib.pyplot as plt
from os import path, mkdir, listdir
import pandas as pd
from numpy import diff, genfromtxt, round
from math import isnan, sqrt
from shutil import copyfile
from trackmate import trackmate_peak_import
from argparse import ArgumentParser
from re import match
from collections import deque
from scipy import stats
# get_ipython().magic('matplotlib inline')


idx = pd.IndexSlice

def distance(x1,y1,z1,x2,y2,z2):
    return  sqrt((x2-x1)**2 + (y2-y1)**2 + (z2-z1)**2)

def find_slope_change_point(anaphase_elongations, timeInterval,majorAxieLen, figureDir, cellNb, save):
    slopes= dict()
#   all in second
    minSegTimeLen = 180
    changePointMinInterval = 240;
#     sliding window size for slope detection
    minSegLen = int(minSegTimeLen/timeInterval)
#     normaliz spindle size with cell length
    normed_anaphase = anaphase_elongations/majorAxieLen
    
    fig, ax = plt.subplots(figsize=(15, 10))
    ax.plot(normed_anaphase.index[:minSegLen],normed_anaphase[:minSegLen], '-x', c  = "black", alpha = 0.5)
    ax.plot(normed_anaphase.index[-minSegLen:],normed_anaphase[-minSegLen:], '-x', c  = "black", alpha = 0.5)
    ax.plot(normed_anaphase.index[minSegLen:-minSegLen],normed_anaphase[minSegLen:-minSegLen], '-o', c  = "black")
    
    
    for i in range(len(normed_anaphase)):
        one_seg = normed_anaphase.iloc[i:i+minSegLen + 1]
        par = np.polyfit(one_seg.index, one_seg, 1 ,full = True)
        slope=par[0][0]
        slopes[one_seg.index[0]] = slope
        intercept=par[0][1]
#   show the fitted lines
        theo_line_x = np.arange(one_seg.index[0], one_seg.index[-1],1)
        theo_line_y = list()
        for x in theo_line_x:
            theo_line_y.append(x * slope + intercept)
        if (slope>=0):
            ax.plot(theo_line_x,theo_line_y, lw = 5, c='red', alpha = 0.2)
        else:
            ax.plot(theo_line_x,theo_line_y, lw = 5,c = 'blue', alpha = 0.2)
    slopes = pd.DataFrame(list(slopes.items()))
    slopes.set_index(0,drop=True, inplace=True)
    slopes = slopes.sort_index()
    slope_changes = pd.DataFrame(columns=['slope'])
    for i in range(slopes.index[minSegLen],slopes.index[-minSegLen]):
        current_slope_change = slopes.loc[i+minSegLen] - slopes.loc[i-minSegLen]
        slope_changes.loc[i] = current_slope_change[1]
        if current_slope_change[1] > 0:
            symbol = "^"
            c = "red"
        else:
            symbol= "s"
            c = "blue"
        ax.scatter(i, current_slope_change, marker=symbol, color = c)
#     for visualization
    slope_changes = slope_changes.sort_index()
    first_max_slope_change_index = int(slope_changes.idxmax())
    ax.axvline(first_max_slope_change_index, linestyle = "--", c = "grey", alpha = 0.8)
    ax.axhline(normed_anaphase.loc[first_max_slope_change_index]['SpLength'], linestyle = "--", c = "grey", alpha = 0.8)

    range_to_null = 480/timeInterval
    # try not to find the second max slope change close to the first one
    slope_changes.loc[first_max_slope_change_index-range_to_null:first_max_slope_change_index+range_to_null] = -np.inf
    maxInd = slope_changes.idxmax()['slope']
    maxInd = int(maxInd if not np.isnan(maxInd)  else 0)
    second_max_slope_change_index = 0
    if maxInd != 0 and slope_changes.loc[maxInd]['slope'] > 0:
        second_max_slope_change_index = maxInd
        ax.axvline(second_max_slope_change_index, linestyle = "--", c = "grey", alpha = 0.8)
        ax.axhline(normed_anaphase.loc[second_max_slope_change_index]['SpLength'], linestyle = "--", c = "grey", alpha = 0.8)
    ax.axhline(1 , c = 'red' , lw = 10)
    changePoints = "_".join(str(int(e)) for e in sorted([first_max_slope_change_index,second_max_slope_change_index]))
    plt.ylabel("Rescaled spindle length (to cell major axe)", fontsize=20)
    plt.xlabel("Change_Point(s)_" + changePoints, fontsize=20)
    plt.ylim(-0.05, 1)
    plt.tick_params(axis='both', which='major', labelsize=20)
    if save:
        plt.savefig(figureDir + str(cellNb) + "_slopChangePoints_" + changePoints + ".png",transparent = True, bbox_inches='tight')
    else:
        plt.show()
    plt.close(fig)
    
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
        self._minimumPeriod = 180
        self._acq_interval = 20
        self._fluo_suffix = "_FLUO/"
        self._mitosis_suffix = "_MITOSIS/"
        self._cropImgs = "croppedImgs"
        self._spots = "spots"
        self._tracks = "tracks"
        self._figs = "figs"
        self._features = "features"
        self._mitosisDir = self._baseDir + self._mitosis_suffix
        self._cellRois = self.load_rois()
        self._minSegLen = int(self._minimumPeriod / self._acq_interval)
        
    def set_calibration(self, cal):
        self._calibration = cal

    def set_minimumPeriod(self, minPeriod):
        self._minimumPeriod = minPeriod

    def set_acq_interval(self, acq_interval):
        self._acq_interval = acq_interval

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
            current_cell_nb = int(f.split("_")[0])
            if current_cell_nb not in all_cell_nbs:
                all_cell_nbs.append(current_cell_nb)
        return all_cell_nbs, features_dir
    
    def getMitoticCellNbs(self,features_dir,cellNbs,p,save_plot = True):
        anaphase_elongations = dict()
        minimumSegLength = self._minimumPeriod/self._acq_interval
        for cellNb in cellNbs:
            csvPath = features_dir + "/" + str(cellNb) + '_' + self._channel+'.csv'
            if path.lexists(csvPath): 
                oneCell = pd.DataFrame.from_csv(csvPath)
                spLens = oneCell['SpLength']
                if len(spLens[spLens>0]) > minimumSegLength:
                    minValue = pd.DataFrame.min(spLens)
                    maxValue = pd.DataFrame.max(spLens)
                    minIndex = spLens.idxmin()
                    maxIndex = spLens.idxmax()
                    if maxIndex < minIndex:
                        minIndex = spLens[:maxIndex].idxmin()
                    mitoregion = spLens.loc[minIndex:maxIndex]
                    mitoregion = mitoregion.interpolate()
#                     plt.axvline(minIndex)
#                     plt.axvline(maxIndex, c = "red")
#                     plt.plot(mitoregion.index, mitoregion)
#                     plt.xlim(-5,90)
#                     plt.show()
                    slope, intercept, r_value, p_value, std_err = stats.linregress(mitoregion.index,mitoregion)
                    if np.log10(p_value) < p:
                        if cellNb not in anaphase_elongations.keys():
                            nanlist = np.empty(maxIndex - minIndex + 2*self._minSegLen)
                            nanlist[:] = np.NAN
                            lowerNanlist = np.empty(self._minSegLen)
                            lowerNanlist[:] = minValue
                            upperNanlist = np.empty(self._minSegLen)
                            upperNanlist[:] = maxValue
                            lowerDataFrame = pd.DataFrame(data=lowerNanlist,index=np.arange(minIndex-self._minSegLen,minIndex,1), columns=['SpLength'])
                            upperDataFrame = pd.DataFrame(data=upperNanlist,index=np.arange(maxIndex,maxIndex + self._minSegLen,1), columns=['SpLength'])
                            extendedDataFrame = pd.DataFrame(data=nanlist,index=np.arange(minIndex-self._minSegLen,maxIndex+ self._minSegLen,1), columns=['SpLength'])
                            extendedDataFrame.update(mitoregion)
                            extendedDataFrame.update(lowerDataFrame)
                            extendedDataFrame.update(upperDataFrame)
                            extendedDataFrame = extendedDataFrame.interpolate()
                            anaphase_elongations[str(cellNb)+"_"+self._channel] = extendedDataFrame
                            if save_plot:
                                current_major_length = self._cellRois.loc[int(cellNb)]['Major'] * self._calibration
                                fig, ax = plt.subplots(figsize=(15, 8))
                                ax.axhline(current_major_length,  c= 'red', lw = 10)
                                plt.ylabel("Spindle Length ($μm$)", fontsize=20)
                                plt.tick_params(axis='both', which='major', labelsize=20)
                                plt.xlabel("Timepoint // interval " + str(self._acq_interval), fontsize=20)
                                plt.xlim(0, oneCell.index[-1])
                                plt.ylim(0, current_major_length)
                                plt.plot(extendedDataFrame.index, extendedDataFrame, "-o", c="black")
                                plt.savefig(self._mitosisDir + self._figs + "/" + str(cellNb) + "_elongation", transparent = True, bbox_inches='tight')
                                plt.close(fig)
        return anaphase_elongations
    
    def load_rois(self):
        csvPath = self._baseDir + '/BF_Results.csv' 
        return pd.DataFrame.from_csv(csvPath)
    
    def createOutputDirs(self):
        croppedImgsDir = self._mitosisDir + self._cropImgs + "/"
        spotsDir = self._mitosisDir + self._spots + "/"
        csvDir = self._mitosisDir + self._tracks + "/"
        featuresDir = self._mitosisDir + self._features + "/"
        figureDir = self._mitosisDir + self._figs + "/"
        if not path.isdir(self._mitosisDir):
            mkdir(self._mitosisDir)
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
        
    def getSPBTrack(self, cellNb, cell_major_length,figureDir, save):
        featurePath = self._baseDir + self._fluo_suffix + self._features +'/'+str(cellNb)+'_' + channel +'.csv'
        spotsPath = self._baseDir + self._fluo_suffix+ self._spots + '/' + str(cellNb)+'_' + channel +'.xml'
        concat_data = list()
        spAngToMajLabel = 'SpAngToMaj'
        frameLabel = 'Frame'
        xPos='x'
        yPos = 'y'
        last_spot_0_x_y = None
        last_spot_1_x_y = None
        if path.lexists(featurePath) and path.lexists(spotsPath):
            oneCellGeo = pd.DataFrame.from_csv(featurePath)
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
                                dis00 = distance(last_spot_0_x_y[0], last_spot_0_x_y[1], 0, current_spot_0_x, current_spot_0_y,0)
                                dis10 = distance(last_spot_1_x_y[0], last_spot_1_x_y[1], 0, current_spot_0_x, current_spot_0_y,0)
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
                            dis00 = distance(last_spot_0_x_y[0], last_spot_0_x_y[1], 0, current_spot_0_x, current_spot_0_y,0)
                            dis01 = distance(last_spot_0_x_y[0], last_spot_0_x_y[1], 0, current_spot_1_x, current_spot_1_y,0)
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
            concat_data = pd.DataFrame(concat_data, columns = [frameLabel, 'Major', spAngToMajLabel, xPos, yPos, 'Track'])
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
            plt.savefig(figureDir + str(cellNb) + "_SPBtracks",transparent = True, bbox_inches='tight')
        else:
            plt.show()
        plt.close(fig)
        return concat_data

    def analyse_each_cell(self, save, anaphase_elongations):
        for cellId in anaphase_elongations.keys():
            cellNb = cellId.split("_")[0]
            figureDir = self._mitosisDir + self._figs + "/"
            current_major_length = self._cellRois.loc[int(cellNb)]['Major'] * self._calibration
            find_slope_change_point(anaphase_elongations[cellId], self._acq_interval, current_major_length, figureDir, cellNb, save)
            
            d = self.getSPBTrack(cellNb, current_major_length, figureDir, save)
            d.to_csv(self._mitosisDir + self._tracks + "/d_" + str(cellNb) + ".csv", sep='\t')

    
    def copy_mitosis_files(self,anaphase_elongations, channels):
        for cellId in anaphase_elongations.keys():
            cellNb = cellId.split("_")[0]
            for ch in channels:
                if path.lexists(self._baseDir + self._fluo_suffix + self._cropImgs + "/" + str(cellNb) + "_" + ch + ".tif"):
                    copyfile(self._baseDir + self._fluo_suffix + self._cropImgs + "/" + str(cellNb) + "_" + ch + ".tif",  self._baseDir + self._mitosis_suffix + self._cropImgs + "/" + str(cellNb) + "_" + ch +".tif")
                    copyfile(self._baseDir + self._fluo_suffix + self._spots + "/" + str(cellNb) + "_" + ch + ".xml", self._baseDir + self._mitosis_suffix + self._spots + "/" + str(cellNb) + "_" + ch + ".xml")
                    copyfile(self._baseDir + self._fluo_suffix + self._features + "/" + str(cellNb) + "_" + ch + ".csv", self._baseDir + self._mitosis_suffix + self._features + "/" + str(cellNb) + "_" + ch + ".csv");


# In[263]:

if __name__ == '__main__':
    baseDir="/Volumes/Macintosh/curioData/MAARSdata/102/12-06-1/X0_Y0"
    channel ="CFP"
    launcher = getMitosisFiles(baseDir, channel)
    launcher.set_acq_interval(20)
    launcher.set_calibration(0.1075)
    launcher.set_minimumPeriod(200)
    launcher.createOutputDirs()
    # launcher.set_attributes_from_cmd_line()
    cellNbs, features_dir = launcher.getAllCellNumbers()
    anaphase_elongations = launcher.getMitoticCellNbs(features_dir,cellNbs, -11)
    launcher.copy_mitosis_files(anaphase_elongations, ["CFP", "GFP","TxRed","DAPI"])
    launcher.analyse_each_cell(True, anaphase_elongations)
    print("Done")


# In[ ]:



