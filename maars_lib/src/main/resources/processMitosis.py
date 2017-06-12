# coding: utf-8
# !/usr/bin/env python3

import matplotlib.pyplot as plt
import multiprocessing as mp
import numpy as np
import pandas as pd
from argparse import ArgumentParser
from os import path, mkdir, listdir
from scipy import stats
from shutil import copyfile


def load_rois(posNb):
    csvPath = baseDir + '/BF_Pos'+str(posNb)+'_Results.csv'
    return pd.DataFrame.from_csv(csvPath)


def createOutputDirs(mitosisDir, cropImgs, spots, features, figs):
    croppedImgsDir = mitosisDir + cropImgs + "/"
    spotsDir = mitosisDir + spots + "/"
    featuresDir = mitosisDir + features + "/"
    figureDir = mitosisDir + figs + "/"
    if not path.isdir(mitosisDir):
        mkdir(mitosisDir)
    if not path.isdir(croppedImgsDir):
        mkdir(croppedImgsDir)
    if not path.isdir(spotsDir):
        mkdir(spotsDir)
    if not path.isdir(featuresDir):
        mkdir(featuresDir)
    if not path.isdir(figureDir):
        mkdir(figureDir)


def find_slope_change_point(elongation, minSegLen, timeInterval, majorAxieLen, cellNb):
    slopes = dict()
    #     normaliz spindle size with cell length
    normed_anaphase = elongation / majorAxieLen

    for i in range(len(normed_anaphase)):
        one_seg = normed_anaphase.iloc[i:i + minSegLen + 1]
        par = np.polyfit(one_seg.index, one_seg, 1, full=True)
        slope = par[0][0]
        slopes[one_seg.index[0]] = slope
    slopes = pd.DataFrame(list(slopes.items()))
    slopes.set_index(0, drop=True, inplace=True)
    slopes = slopes.sort_index()
    slope_changes = pd.DataFrame(columns=['slope'])
    for i in range(slopes.index[minSegLen], slopes.index[-minSegLen]):
        current_slope_change = slopes.loc[i + minSegLen] - slopes.loc[i - minSegLen]
        slope_changes.loc[i] = current_slope_change[1]
    slope_changes = slope_changes.sort_index()
    first_max_slope_change_index = int(slope_changes.idxmax()) if int(slope_changes.idxmax()) >= 0 else \
        normed_anaphase.index[minSegLen]

    range_to_null = 480 / timeInterval
    # try not to find the second max slope change close to the first one
    slope_changes.loc[
    first_max_slope_change_index - range_to_null:first_max_slope_change_index + range_to_null] = -np.inf
    maxInd = slope_changes.idxmax()['slope']
    maxInd = int(maxInd if not np.isnan(maxInd)  else 0)
    second_max_slope_change_index = normed_anaphase.index[minSegLen]
    if maxInd > normed_anaphase.index[minSegLen] and slope_changes.loc[maxInd]['slope'] > 0:
        second_max_slope_change_index = maxInd
    line = str(cellNb) + ","
    line += str(normed_anaphase.index[minSegLen]) + ","
    line += ",".join(str(int(e)) for e in sorted([first_max_slope_change_index, second_max_slope_change_index]))
    line += "," + str(normed_anaphase.index[-minSegLen]) + "\n"
    return line


def set_attributes_from_cmd_line():
    parser = ArgumentParser(
        description="Find mitotic cells and put related data into a folder")
    parser.add_argument("baseDir",
                        help="path to acquisition folder",
                        type=str)
    parser.add_argument("channel",
                        help="channel used to detect SPBs",
                        type=str)
    parser.add_argument("calibration",
                        help="calibration of image",
                        type=float)
    parser.add_argument("acq_interval",
                        help="interval of fluo acquisition",
                        type=float)
    parser.add_argument("-minimumPeriod",
                        help="minimum time segment to be analyzed",
                        type=int, default=200)
    return parser.parse_args()


def getAllCellNumbers(features_dir):
    return pd.Series([int(f.split("_")[0]) for f in listdir(features_dir)]).unique()


def find_mitotic_region(featureOfOneCell, minSegLen, p, extended=True):
    spLens = featureOfOneCell['SpLength']
    if len(spLens[spLens > 0]) > minSegLen:
        minValue = pd.Series.min(spLens)
        maxValue = pd.Series.max(spLens)
        minIndex = spLens.idxmin()
        maxIndex = spLens.idxmax()
        if maxIndex < minIndex:
            if len(spLens[:maxIndex]) == 0:
                return
            minIndex = spLens[:maxIndex].idxmin()
        mitoregion = spLens.loc[minIndex:maxIndex]
        if len(mitoregion) == 0:
            return
        mitoregion = mitoregion.interpolate()
        #                     plt.axvline(minIndex)
        #                     plt.axvline(maxIndex, c = "red")
        #                     plt.plot(mitoregion.index, mitoregion)
        #                     plt.xlim(-5,90)
        #                     plt.show()
        if mitoregion.shape[0] < minSegLen:
            return
        slope, intercept, r_value, p_value, std_err = stats.linregress(mitoregion.index, mitoregion)
        if np.log10(p_value) < p:
            if extended:
                nanlist = np.empty(maxIndex - minIndex + 2 * minSegLen)
                nanlist[:] = np.NAN
                lowerNanlist = np.empty(minSegLen)
                lowerNanlist[:] = minValue
                upperNanlist = np.empty(minSegLen)
                upperNanlist[:] = maxValue
                lowerDataFrame = pd.DataFrame(data=lowerNanlist, index=np.arange(minIndex - minSegLen, minIndex, 1),
                                              columns=['SpLength'])
                upperDataFrame = pd.DataFrame(data=upperNanlist, index=np.arange(maxIndex, maxIndex + minSegLen, 1),
                                              columns=['SpLength'])
                extendedDataFrame = pd.DataFrame(data=nanlist,
                                                 index=np.arange(minIndex - minSegLen, maxIndex + minSegLen, 1),
                                                 columns=['SpLength'])
                extendedDataFrame.update(mitoregion)
                extendedDataFrame.update(lowerDataFrame)
                extendedDataFrame.update(upperDataFrame)
                return extendedDataFrame.interpolate()
            else:
                return mitoregion
    return


def getMitoticElongation(features_dir, cellNb, p, minSegLen, channel):
    csvPath = features_dir + "/" + str(cellNb) + '_' + channel + '.csv'
    if path.lexists(csvPath):
        oneCell = pd.DataFrame.from_csv(csvPath)
        elongationRegion = find_mitotic_region(oneCell, minSegLen, p)
        if elongationRegion is None:
            return
        return str(cellNb) + "_" + channel, elongationRegion
    else:
        return


def analyse_each_cell(pool, minSegLen, elongationRegions, cellRois, mitosisDir):
    slope_change_tasks = list()
    f = open(mitosisDir + "mitosis_time_board.csv", "w+")
    for cellId in elongationRegions.keys():
        cellNb = cellId.split("_")[0]
        current_major_length = cellRois.loc[int(cellNb)]['Major'] * calibration
        slope_change_tasks.append(
            (elongationRegions[cellId], minSegLen, acq_interval, current_major_length, cellNb))
    time_board = ""
    jobs = [pool.apply_async(find_slope_change_point, t) for t in slope_change_tasks]
    for job in jobs:
        line = job.get()
        time_board += line
        f.write(line)
    f.close()
    return time_board


def copy_mitosis_files(elongationRegions, channels, fluoDir, mitosisDir, cropImgs, spots, features):
    for cellId in elongationRegions.keys():
        cellNb = cellId.split("_")[0]
        for ch in channels:
            if path.lexists(fluoDir + cropImgs + path.sep + str(cellNb) + "_" + ch + ".tif"):
                copyfile(fluoDir + cropImgs + path.sep + str(cellNb) + "_" + ch + ".tif",
                         mitosisDir + cropImgs + path.sep + str(cellNb) + "_" + ch + ".tif")
                copyfile(fluoDir + spots + path.sep + str(cellNb) + "_" + ch + ".xml",
                         mitosisDir + spots + path.sep + str(cellNb) + "_" + ch + ".xml")
                copyfile(fluoDir + features + path.sep + str(cellNb) + "_" + ch + ".csv",
                         mitosisDir + features + path.sep + str(cellNb) + "_" + ch + ".csv");


def savePlots(elongationRegions, cellRois, calibration, time_board):
    for cell_id, elongation in elongationRegions.items():
        # print(cell_id, elongation)
        cellNb = int(cell_id.split("_")[0])
        current_major_length = cellRois.loc[int(cellNb)]['Major'] * calibration
        fig, ax = plt.subplots(figsize=(15, 8))
        ax.axhline(current_major_length, c='red', lw=10)
        ax.axvline(int(time_board.loc[str(cellNb)][1]), c='red')
        ax.axvline(int(time_board.loc[str(cellNb)][2]), c='black', linestyle=":")
        ax.axvline(int(time_board.loc[str(cellNb)][3]), c='black', linestyle=":")
        ax.axvline(int(time_board.loc[str(cellNb)][4]), c='red')
        plt.ylabel("Spindle Length ($μm$)", fontsize=20)
        plt.tick_params(axis='both', which='major', labelsize=20)
        plt.xlabel("Timepoint // interval " + str(acq_interval), fontsize=20)
        plt.ylim(0, current_major_length)
        plt.plot(elongation.index, elongation, "-o", c="black")
        plt.savefig(mitosisFigDir + str(cellNb), transparent=True, bbox_inches='tight')
        plt.close(fig)


def saveAllElongations(mitosisDir, elongationRegions):
    elongationRegionsDf = pd.DataFrame()
    for cell_id in elongationRegions:
        elongationRegionsDf = elongationRegionsDf.append(
            elongationRegions[cell_id].rename(index=int, columns={"SpLength": cell_id}).T)
    elongationRegionsDf.to_csv(mitosisDir + "mitosis_elongations.csv")
    return elongationRegionsDf


if __name__ == '__main__':
    # example
    # baseDir = "/Volumes/Macintosh/curioData/MAARSdata/102/12-06-1/BF_1"
    # channel = "CFP"
    # calibration = 0.10650000410025016
    # minimumPeriod = 200
    # acq_interval = 20
    args = set_attributes_from_cmd_line()
    baseDir = args.baseDir
    channel = args.channel
    calibration = args.calibration
    minimumPeriod = args.minimumPeriod
    acq_interval = args.acq_interval

    # user won't need to change
    mitosis_suffix = "_MITOSIS" + path.sep
    fluo_suffix = "FLUO_1" + path.sep
    cropImgs = "croppedImgs"
    spots = "spots"
    figs = "figs"
    features = "features"
    mitosisDir = baseDir + mitosis_suffix
    mitosisFigDir = mitosisDir + figs + path.sep
    fluoDir = "/".join(baseDir.split("/")[:-1]) + path.sep + fluo_suffix
    features_dir = fluoDir + features
    cropImgs_dir = fluoDir + cropImgs
    minSegLen = int(minimumPeriod / acq_interval)

    # -----------------------------------run the analysis-----------------------------------#
    pool = mp.Pool(mp.cpu_count())
    cellRois = load_rois(0)
    createOutputDirs(mitosisDir, cropImgs, spots, features, figs)
    cellNbs = getAllCellNumbers(features_dir)
    tasks = []
    for cellNb in cellNbs:
        tasks.append((features_dir, cellNb, -4, minSegLen, channel))
    results = [pool.apply_async(getMitoticElongation, t) for t in tasks]
    elongationRegions = dict()
    for result in results:
        res = result.get()
        if res is None:
            continue
        elongationRegions[res[0]] = res[1]
    copy_mitosis_files(elongationRegions, ["CFP", "GFP", "TxRed", "DAPI"], fluoDir, mitosisDir, cropImgs, spots,
                       features)
    time_board = analyse_each_cell(pool, minSegLen, elongationRegions, cellRois, mitosisDir)
    times = pd.DataFrame([cell.split(",") for cell in time_board.split("\n")])
    times = times.set_index(0)
    elongationRegionsDf = saveAllElongations(mitosisDir, elongationRegions)
    savePlots(elongationRegions, cellRois, calibration, times)
    pool.close()
    pool.join()
    print("Done")


    #############
    # plots
    # fig, ax = plt.subplots(figsize=(15, 10))
    # ax.plot(normed_anaphase.index[:minSegLen], normed_anaphase[:minSegLen], '-x', c="black", alpha=0.5)
    # ax.plot(normed_anaphase.index[-minSegLen:], normed_anaphase[-minSegLen:], '-x', c="black", alpha=0.5)
    # ax.plot(normed_anaphase.index[minSegLen:-minSegLen], normed_anaphase[minSegLen:-minSegLen], '-o', c="black")
    #   show the fitted lines
    # theo_line_x = np.arange(one_seg.index[0], one_seg.index[-1], 1)
    # theo_line_y = list()
    # for x in theo_line_x:
    #     theo_line_y.append(x * slope + intercept)
    # if (slope >= 0):
    #     ax.plot(theo_line_x, theo_line_y, lw=5, c='red', alpha=0.2)
    # else:
    #     ax.plot(theo_line_x, theo_line_y, lw=5, c='blue', alpha=0.2)
    #
    # if current_slope_change[1] > 0:
    #     symbol = "^"
    #     c = "red"
    # else:
    #     symbol = "s"
    #     c = "blue"
    # ax.scatter(i, current_slope_change, marker=symbol, color=c)
    #
    # ax.axvline(first_max_slope_change_index, linestyle="--", c="grey", alpha=0.8)
    # ax.axhline(normed_anaphase.loc[first_max_slope_change_index]['SpLength'], linestyle="--", c="grey", alpha=0.8)
    #
    #     ax.axvline(second_max_slope_change_index, linestyle="--", c="grey", alpha=0.8)
    #     ax.axhline(normed_anaphase.loc[second_max_slope_change_index]['SpLength'], linestyle="--", c="grey", alpha=0.8)
    # ax.axhline(1, c='red', lw=10)
    #
    # plt.ylabel("Rescaled spindle length (to cell major axe)", fontsize=20)
    # plt.xlabel("Change_Point(s)_" + changePoints, fontsize=20)
    # plt.ylim(-0.05, 1)
    # plt.tick_params(axis='both', which='major', labelsize=20)
    # if save:
    #     plt.savefig(figureDir + path.sep + str(cellNb) + "_slopChangePoints_" + changePoints + ".png", transparent=True,
    #                 bbox_inches='tight')
    # else:
    #     plt.show()
    # plt.close(fig)
    ###
    # mean_mask=[1/3,1/3,1/3]
    # for idx in elongationRegionsDf.index:
    #     f,ax = plt.subplots()
    #     ax.plot(elongationRegionsDf.loc[idx])
    #     ax.plot(np.diff(np.diff(np.convolve(elongationRegionsDf.loc[idx],mean_mask),3),3))
    #     ax.plot(np.convolve(elongationRegionsDf.loc[idx],mean_mask))
    #     plt.show()

    ########deprecated
    # SPBtrack_tasks = list()
    #        SPBtrack_tasks.append(
    #            (baseDir, fluo_suffix, features, channel, spots, cellNb, current_major_length, figureDir, save))
    # results = [pool.apply_async(getSPBTrack, t) for t in SPBtrack_tasks]
    # for result in results:
    #     res = result.get()
    #     res[1].to_csv(mitosisDir + tracks + path.sep + "d_" + str(res[0]) + ".csv", sep='\t')
