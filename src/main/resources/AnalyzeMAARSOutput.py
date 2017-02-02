
# coding: utf-8

# In[45]:

#!/usr/bin/env python3
import numpy as np
import matplotlib.pyplot as plt
from os import path, mkdir, listdir
import pandas as pd
from math import sqrt
from shutil import copyfile
from trackmate import trackmate_peak_import
from argparse import ArgumentParser
from re import match
from collections import deque
from scipy import stats
import multiprocessing as mp
#get_ipython().magic('matplotlib inline')


idx = pd.IndexSlice

def load_rois():
    csvPath = baseDir + '/BF_Results.csv' 
    return pd.DataFrame.from_csv(csvPath)

def createOutputDirs(mitosisDir, cropImgs, spots, tracks, features, figs):
    croppedImgsDir = mitosisDir + cropImgs + "/"
    spotsDir = mitosisDir + spots + "/"
    tracksDir = mitosisDir + tracks + "/"
    featuresDir = mitosisDir + features + "/"
    figureDir = mitosisDir + figs + "/"
    if not path.isdir(mitosisDir):
        mkdir(mitosisDir)
    if not path.isdir(croppedImgsDir):
        mkdir(croppedImgsDir)
    if not path.isdir(spotsDir):
        mkdir(spotsDir)
    if not path.isdir(tracksDir):
        mkdir(tracksDir)
    if not path.isdir(featuresDir):
        mkdir(featuresDir)
    if not path.isdir(figureDir):
        mkdir(figureDir)

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
        plt.savefig(figureDir + path.sep + str(cellNb) + "_slopChangePoints_" + changePoints + ".png",transparent = True, bbox_inches='tight')
    else:
        plt.show()
    plt.close(fig)
    
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
        channel = args.channel
        calibration = args.calibration
        minimumPeriod = args.minimumPeriod
        acq_interval = args.acq_interval

def getAllCellNumbers(features_dir):
    all_cell_nbs=list()
    for f in listdir(features_dir):
        current_cell_nb = int(f.split("_")[0])
        if current_cell_nb not in all_cell_nbs:
            all_cell_nbs.append(current_cell_nb)
    return all_cell_nbs

def find_mitotic_region(featureOfOneCell, minSegLen, p, extended= True):
    spLens = featureOfOneCell['SpLength']
    if len(spLens[spLens > 0]) > minSegLen:
        minValue = pd.DataFrame.min(spLens)
        maxValue = pd.DataFrame.max(spLens)
        minIndex = spLens.idxmin()
        maxIndex = spLens.idxmax()
        if maxIndex < minIndex:
            if len(spLens[:maxIndex])==0:
                return
            minIndex = spLens[:maxIndex].idxmin()
        mitoregion = spLens.loc[minIndex:maxIndex]
        if len(mitoregion) ==0 :
            return
        mitoregion = mitoregion.interpolate()
#                     plt.axvline(minIndex)
#                     plt.axvline(maxIndex, c = "red")
#                     plt.plot(mitoregion.index, mitoregion)
#                     plt.xlim(-5,90)
#                     plt.show()
        if mitoregion.shape[0]< minSegLen:
            return
        slope, intercept, r_value, p_value, std_err = stats.linregress(mitoregion.index,mitoregion)
        if np.log10(p_value) < p:
            if extended:
                nanlist = np.empty(maxIndex - minIndex + 2*minSegLen)
                nanlist[:] = np.NAN
                lowerNanlist = np.empty(minSegLen)
                lowerNanlist[:] = minValue
                upperNanlist = np.empty(minSegLen)
                upperNanlist[:] = maxValue
                lowerDataFrame = pd.DataFrame(data=lowerNanlist,index=np.arange(minIndex-minSegLen,minIndex,1), columns=['SpLength'])
                upperDataFrame = pd.DataFrame(data=upperNanlist,index=np.arange(maxIndex,maxIndex + minSegLen,1), columns=['SpLength'])
                extendedDataFrame = pd.DataFrame(data=nanlist,index=np.arange(minIndex-minSegLen,maxIndex+ minSegLen,1), columns=['SpLength'])
                extendedDataFrame.update(mitoregion)
                extendedDataFrame.update(lowerDataFrame)
                extendedDataFrame.update(upperDataFrame)
                return extendedDataFrame.interpolate()
            else:
                return mitoregion
    return
    

def getMitoticElongation(cellRois, features_dir, cellNb, p, minSegLen, channel, mitosisFigDir, save_plot = True):
    csvPath = features_dir + "/" + str(cellNb) + '_' + channel+'.csv'
    if path.lexists(csvPath): 
        oneCell = pd.DataFrame.from_csv(csvPath)
        extendedDataFrame = find_mitotic_region(oneCell, minSegLen, p)
        if extendedDataFrame is None:
            return
        if save_plot:
            current_major_length = cellRois.loc[int(cellNb)]['Major'] * calibration
            fig, ax = plt.subplots(figsize=(15, 8))
            ax.axhline(current_major_length,  c= 'red', lw = 10)
            plt.ylabel("Spindle Length ($μm$)", fontsize=20)
            plt.tick_params(axis='both', which='major', labelsize=20)
            plt.xlabel("Timepoint // interval " + str(acq_interval), fontsize=20)
            plt.xlim(0, oneCell.index[-1])
            plt.ylim(0, current_major_length)
            plt.plot(extendedDataFrame.index, extendedDataFrame, "-o", c="black")
            plt.savefig(mitosisFigDir + str(cellNb) + "_elongation", transparent = True, bbox_inches='tight')
            plt.close(fig)
        return str(cellNb)+"_"+channel, extendedDataFrame
    else:
        return



def getSPBTrack( cellNb, cell_major_length,figureDir, save):
    featurePath = baseDir + fluo_suffix + features +'/'+str(cellNb)+'_' + channel +'.csv'
    spotsPath = baseDir + fluo_suffix+ spots + '/' + str(cellNb)+'_' + channel +'.xml'
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
    return cellNb, concat_data

def analyse_each_cell(pool,save, anaphase_elongations, figureDir, cellRois, mitosisDir):
    slope_change_tasks = list()
    SPBtrack_tasks = list()
    for cellId in anaphase_elongations.keys():
        cellNb = cellId.split("_")[0]
        current_major_length = cellRois.loc[int(cellNb)]['Major'] * calibration
        slope_change_tasks.append((anaphase_elongations[cellId], acq_interval, current_major_length, figureDir, cellNb, save))
        SPBtrack_tasks.append((cellNb, current_major_length, figureDir, save))
    for t in slope_change_tasks:
        pool.apply_async( find_slope_change_point, t )
    results = [pool.apply_async( getSPBTrack, t ) for t in SPBtrack_tasks]
    for result in results:
        res = result.get()
        res[1].to_csv(mitosisDir + tracks + path.sep + "d_" + str(res[0]) + ".csv", sep='\t')
    pool.close()
    pool.join()

def copy_mitosis_files(anaphase_elongations, channels, fluoDir, mitosisDir, cropImgs, spots, features):
    for cellId in anaphase_elongations.keys():
        cellNb = cellId.split("_")[0]
        for ch in channels:
            if path.lexists(fluoDir + cropImgs + path.sep + str(cellNb) + "_" + ch + ".tif"):
                copyfile(fluoDir + cropImgs + path.sep + str(cellNb) + "_" + ch + ".tif",  mitosisDir + cropImgs + path.sep + str(cellNb) + "_" + ch + ".tif")
                copyfile(fluoDir + spots + path.sep + str(cellNb) + "_" + ch + ".xml", mitosisDir + spots + path.sep + str(cellNb) + "_" + ch + ".xml")
                copyfile(fluoDir + features + path.sep + str(cellNb) + "_" + ch + ".csv", mitosisDir + features + path.sep + str(cellNb) + "_" + ch + ".csv");

def distance(x1,y1,z1,x2,y2,z2):
    return  sqrt((x2-x1)**2 + (y2-y1)**2 + (z2-z1)**2)                


# In[46]:

if __name__ == '__main__':
    baseDir="/media/tong/74CDBC0B2251059E/Starvation/Yes/10-11-1/X0_Y0"
    mitosis_suffix = "_MITOSIS" + path.sep
    fluo_suffix = "_FLUO" + path.sep
    cropImgs = "croppedImgs"
    spots = "spots"
    tracks = "tracks"
    figs = "figs"
    features = "features"
    channel ="CFP"
    calibration = 0.1075
    minimumPeriod = 180
    acq_interval = 20
    mitosisDir = baseDir+ mitosis_suffix
    mitosisFigDir = mitosisDir + figs + path.sep
    fluoDir = baseDir+ fluo_suffix
    features_dir = baseDir + fluo_suffix + features
    cropImgs_dir = baseDir + fluo_suffix + cropImgs
    minSegLen = int(minimumPeriod / acq_interval)
    
    # launcher.set_attributes_from_cmd_line()
    
    #-----------------------------------run the analysis-----------------------------------#
#     launcher.set_acq_interval(20)
#     launcher.set_calibration(0.1075)
#     launcher.set_minimumPeriod(200)
    pool = mp.Pool(mp.cpu_count())
    cellRois = load_rois()
    createOutputDirs(mitosisDir, cropImgs, spots, tracks, features, figs)
    cellNbs = getAllCellNumbers(features_dir)
    tasks = []
    for cellNb in cellNbs:
        tasks.append( (cellRois,features_dir, cellNb, -11, minSegLen, channel, mitosisFigDir) )
    results = [pool.apply_async( getMitoticElongation, t ) for t in tasks]
    anaphase_elongations= dict()
    for result in results:
        res = result.get()
        if res is None:
            continue
        anaphase_elongations[res[0]] =  res[1]
    copy_mitosis_files(anaphase_elongations, ["CFP", "GFP","TxRed","DAPI"], fluoDir, mitosisDir, cropImgs, spots, features)
    analyse_each_cell(pool, True, anaphase_elongations, mitosisFigDir, cellRois, mitosisDir)
    print("Done")


# In[ ]:



