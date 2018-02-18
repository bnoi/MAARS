# coding: utf-8
# !/usr/bin/env python3

import cellh5
import cellh5write
import multiprocessing as mp
import numpy as np
import pandas as pd
import tifffile
from argparse import ArgumentParser
from os import path, mkdir, listdir
from scipy import stats
from scipy.spatial import ConvexHull
from shutil import copyfile
import TMxml2dflib

pos_x = "POSITION_X"
pos_y = "POSITION_Y"
poleSuffix = "_pole"
ktSuffix = "_kt"

def createOutputDirs(mitosisDir, outputDirs):
    for targetDir in outputDirs:
        if not path.exists(mitosisDir + targetDir + path.sep):
            mkdir(mitosisDir + targetDir + path.sep)

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
    parser.add_argument("pos",
                        help="position name",
                        type=str)
    parser.add_argument("bf_Prefix",
                        help="bright field prefix",
                        type=str)
    parser.add_argument("fluo_Prefix",
                        help="fluo field prefix",
                        type=str)
    parser.add_argument('--ch5', dest='ch5', action='store_true', help="save into cellh5")
    parser.add_argument('--no-ch5', dest='ch5', action='store_false', help="not save into cellh5")
    parser.set_defaults(ch5=True)
    parser.add_argument("-minimumPeriod",
                        help="minimum time segment to be analyzed",
                        type=int, default=200)
    return parser.parse_args()

def getAllCellNumbers(features_dir):
    return pd.Series([int(f.split("_")[0]) for f in listdir(features_dir)]).unique()

def extendMitoRegion(mitoregion, extendLen):
    dlist = extendLen*[mitoregion.iloc[0]] + list(mitoregion) + extendLen*[mitoregion.iloc[-1]]
    indlist = list(np.arange(mitoregion.index[0]- extendLen, mitoregion.index[0])) + list(mitoregion.index) + list(np.arange(mitoregion.index[-1], mitoregion.index[-1]+extendLen))
    return pd.DataFrame(dlist, index=indlist,columns={"SpLength"}).interpolate()

def find_mitotic_region(featureOfOneCell, minSegLen, p):
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
        if mitoregion.shape[0] < minSegLen:
            return
        slope, intercept, r_value, p_value, std_err = stats.linregress(mitoregion.index, mitoregion)
        if np.log10(p_value) < p:
            return mitoregion
    return


def getMitoticElongation(features_dir, cellNb, p, minSegLen, channel):
    csvPath = features_dir + str(cellNb) + '_' + channel + '.csv'
    if path.lexists(csvPath):
        oneCell = pd.read_csv(csvPath)
        elongationRegion = find_mitotic_region(oneCell, minSegLen, p)
        if elongationRegion is None:
            return
        return str(cellNb) + "_" + channel, elongationRegion
    else:
        return


def analyse_each_cell(pool, minSegLen, elongationRegions, cellRois, mitosisDir):
    slope_change_tasks = list()
    for cellId in elongationRegions.keys():
        cellNb = cellId.split("_")[0]
        current_major_length = cellRois.loc[int(cellNb)]['Major'] * calibration
        slope_change_tasks.append(
            (elongationRegions[cellId], minSegLen, acq_interval, current_major_length, cellNb))
    timePoints = dict()
    jobs = [pool.apply_async(find_slope_change_point, t) for t in slope_change_tasks]
    for job in jobs:
        res = job.get()
        timePoints[res[0]] = res[1:]
    return pd.DataFrame.from_dict(timePoints).T


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
        print(cell_id, elongation)
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

def find_slope_change_point(elongation, minSegLen, timeInterval, majorAxieLen, cellNb):
    elongation = extendMitoRegion(elongation, minSegLen)
    slopes = dict()
    #     normaliz spindle size with cell length
    normed_anaphase = elongation / majorAxieLen
    for i in range(len(normed_anaphase)):
        one_seg = normed_anaphase.iloc[i:i + minSegLen]
        par = np.polyfit(one_seg.index, one_seg, 1, full=True)
        slope = par[0][0]
        slopes[one_seg.index[0]] = slope
    slopes = pd.DataFrame.from_dict(slopes).T
    slope_changes = pd.DataFrame(columns=['slopeDiff'])
    for i in range(len(slopes.index) - 2 * minSegLen):
        current_slope_change = slopes.loc[slopes.index[i + 2*minSegLen]] - slopes.loc[slopes.index[i + minSegLen]]
        slope_changes.loc[slopes.index[i]] = current_slope_change[0]
    slope_changes = slope_changes.sort_index()
    first_max_slope_change_index = int(slope_changes.idxmax()) if int(slope_changes.idxmax()) >= 0 else normed_anaphase.index[minSegLen]
    return (str(cellNb), normed_anaphase.index[minSegLen], first_max_slope_change_index, normed_anaphase.index[-minSegLen])

    # range_to_null = 480 / timeInterval
    # # try not to find the second max slope change close to the first one
    # slope_changes.loc[
    # first_max_slope_change_index - range_to_null:first_max_slope_change_index + range_to_null] = -np.inf
    # maxInd = slope_changes.idxmax()['slopeDiff']
    # maxInd = int(maxInd if not np.isnan(maxInd)  else 0)
    # second_max_slope_change_index = normed_anaphase.index[minSegLen]
    # if maxInd > normed_anaphase.index[minSegLen] and slope_changes.loc[maxInd]['slopeDiff'] > 0:
    #     second_max_slope_change_index = maxInd
    # line = str(cellNb) + ","
    # line += str(normed_anaphase.index[minSegLen]) + ","
    # line += ",".join(str(int(e)) for e in sorted([first_max_slope_change_index, second_max_slope_change_index]))
    # line += "," + str(normed_anaphase.index[-minSegLen]) + "\n"
    # return line

############################## ch5 methods######################################
# def ch5writeChDef(ciw):
#     c_def = CH5ImageChannelDefinition()
#     c_def.add_row(channel_name="BF", description='bright-field', is_physical=True,
#                   voxel_size=(0.0645,0.0645,0.3), color="#aabbcc")
#     ciw.write_definition(c_def)

def ch5writeRegDef(crw):
    r_def = cellh5write.CH5ImageRegionDefinition()
    r_def.add_row(region_name='cell', channel_idx='0')
    r_def.add_row(region_name='cell', channel_idx='1')
    r_def.add_row(region_name='ktspot', channel_idx='1')
    r_def.add_row(region_name='spbspot', channel_idx='1')
    crw.write_definition(r_def)

############################## colocalisation methods ##########################
def dist(x1,y1,x2,y2):
    return np.sqrt((x1-x2)**2 + (y1-y2)**2)

def noKtOutsidePoles(spotsInFrame, radius):
    for i in spotsInFrame.index:
        if np.min(dist(spotsInFrame[pos_x + poleSuffix], spotsInFrame[pos_y + poleSuffix], \
            spotsInFrame.loc[i][pos_x + ktSuffix], spotsInFrame.loc[i][pos_y + ktSuffix])) > radius:
            return False
    return True

def getkts2polesStats(spotsInFrame):
    distances = pd.Series()
    for i in spotsInFrame.index:
        distances = distances.append(dist(spotsInFrame[pos_x + poleSuffix], spotsInFrame[pos_y + poleSuffix], \
            spotsInFrame.loc[i][pos_x + ktSuffix], spotsInFrame.loc[i][pos_y + ktSuffix]))
    return distances

# def generate_circle_coords(radius):
#     an = np.linspace(0, 2*np.pi, 100)
#     xs = radius *np.cos(an)
#     ys = radius *np.sin(an)
#     return (xs, ys)

def getSpotGeos(poleSpots, ktSpots, radius = 0.25):
    if poleSpots is not None and ktSpots is not None:
        spots = poleSpots[[pos_x, pos_y]].join(ktSpots[[pos_x, pos_y]], how="outer", lsuffix=poleSuffix, rsuffix=ktSuffix)
        geos = pd.DataFrame(columns = ["phase", "poleNb", "ktNb", "poleCenter_X","poleCenter_Y",\
            "ktCenter_X","ktCenter_Y", "poleCenter2KtCenter_X", "poleCenter2KtCenter_Y", \
            "ktXpos_std", "ktYpos_std", "kts2ktCenter_mean","kts2ktCenter_std", "kts2ktCenter_max","kts2ktCenter_min", \
            "convexHull_area",
            "kts2poleCenter_mean","kts2poleCenter_std", "kts2poleCenter_max","kts2poleCenter_min", \
            "kts2poles_mean", "kts2poles_std", "kts2poles_max", "kts2poles_min", \
            "kt2Sp_mean", "kt2Sp_std", "kt2Sp_max", "kt2Sp_min", \
            "proj2SpCenter_mean", "proj2SpCenter_std","proj2SpCenter_max","proj2SpCenter_min"])
        for f in spots.index.levels[0]:
            geos.loc[f] = computeGeometries(spots.loc[f])
        return geos
    else:
        return

def getDotProjectionOnSp(sp1x, sp1y, sp2x, sp2y, dotx, doty):
    px = sp2x-sp1x
    py = sp2y-sp1y
    dAB = px*px + py*py
    u = ((dotx - sp1x) * px + (doty - sp1y) * py) / dAB
    return pd.concat([sp1x + u * px, sp1y + u * py], axis=1)

def computeGeometries(spotsInFrame):
    # import matplotlib.pyplot as plt
    statFeatures = ["mean","std","max","min"]
    spotsInFrame = spotsInFrame.astype(np.float)
    spotCounts = spotsInFrame.count()
    maxSpotNb = np.max(spotCounts)
    phase = 0
    spotStats = spotsInFrame.describe()
    poleCenterX, poleCenterY = spotStats[[pos_x + poleSuffix, pos_y + poleSuffix]].loc["mean"]
    ktCenterX, ktCenterY = spotStats[[pos_x + ktSuffix, pos_y + ktSuffix]].loc["mean"]
    params = [spotCounts[0], spotCounts[2], poleCenterX, poleCenterY, ktCenterX, \
        ktCenterY, poleCenterX - ktCenterX, poleCenterY - ktCenterY, \
        spotStats[pos_x + ktSuffix].loc["std"], spotStats[pos_y + ktSuffix].loc["std"]]
    if maxSpotNb >= 2:
        phase = 1
        kt2ktCenter = dist(spotsInFrame[pos_x + ktSuffix], spotsInFrame[pos_y + ktSuffix],\
            ktCenterX, ktCenterY)
        params += kt2ktCenter.describe()[statFeatures].tolist()
        if spotCounts[2] > 2 :
            convexHull = ConvexHull(np.array(spotsInFrame[[pos_x + ktSuffix, pos_y + ktSuffix]]), incremental=False)
            params += [convexHull.area]
        else:
            params += [None]
        if spotCounts[0] ==  2:
            kt2poleCenter = dist(spotsInFrame[pos_x + ktSuffix], spotsInFrame[pos_y + ktSuffix],\
                poleCenterX,poleCenterY)
            projDots = getDotProjectionOnSp(\
                spotsInFrame.loc[0][pos_x + poleSuffix], spotsInFrame.loc[0][pos_y + poleSuffix],
                spotsInFrame.loc[1][pos_x + poleSuffix], spotsInFrame.loc[1][pos_y + poleSuffix],
                spotsInFrame[pos_x + ktSuffix], spotsInFrame[pos_y + ktSuffix])
            kts2Sp = dist(spotsInFrame[pos_x + ktSuffix], spotsInFrame[pos_y + ktSuffix], projDots[0], projDots[1])
            if noKtOutsidePoles(spotsInFrame, 0.25):
                phase = 2
            proj2SpCenter = dist(projDots[0], projDots[1], poleCenterX, poleCenterY)
            params += kt2poleCenter.describe()[statFeatures].tolist() + \
                getkts2polesStats(spotsInFrame).describe()[statFeatures].tolist() + \
                kts2Sp.describe()[statFeatures].tolist() + proj2SpCenter.describe()[statFeatures].tolist()
            # ax = spotsInFrame.plot(pos_x + poleSuffix, pos_y + poleSuffix, c='red',kind="scatter")
            # spotsInFrame.plot(pos_x + ktSuffix, pos_y + ktSuffix, c='green',kind="scatter", ax=ax)
            # plt.scatter(poleCenterX, poleCenterY, c="blue")
            # plt.scatter(ktCenterX, ktCenterY, c="y")
        else:
            params += [None] * 16
    else:
        params += [None] * 21
    # plt.show()
    return [phase] + params

def isInMitosis(cellNb, features_dir, channel):
    csvPath = features_dir + str(cellNb) + '_' + channel + '.csv'
    if path.lexists(csvPath):
        oneCell = pd.read_csv(csvPath)
        spLens = oneCell['SpLength']
        if len(spLens[spLens > 0]) > 0:
            return str(cellNb) + "_" + channel, spLens
        else:
            return
    else:
        return


if __name__ == '__main__':
    args = set_attributes_from_cmd_line()
    baseDir = args.baseDir
    channel = args.channel
    calibration = args.calibration
    minimumPeriod = args.minimumPeriod
    acq_interval = args.acq_interval
    pos = args.pos
    bfprefix = args.bf_Prefix
    fluoprefix = args.fluo_Prefix
    ch5 = args.ch5

    # user won't need to change
    date = baseDir.split("/")[-2]
    mitosis_suffix = "Mitosis" + path.sep
    fluo = fluoprefix + "_FluoAnalysis" + path.sep
    seg = bfprefix + "_SegAnalysis" + path.sep
    posPrefix = pos + path.sep
    cropImgs = "croppedImgs" + path.sep
    spots = "spots" + path.sep
    figs = "figs" + path.sep
    features = "features" + path.sep
    mitosisDir = baseDir + path.sep + mitosis_suffix + posPrefix
    mitosisFigDir = mitosisDir + figs
    fluoDir = baseDir + path.sep + fluo + posPrefix
    features_dir = fluoDir + features
    minSegLen = int(minimumPeriod / acq_interval)
    chs = ["CFP","GFP"]

    # -----------------------------------run the analysis-----------------------------------#
    pool = mp.Pool(mp.cpu_count())
    cellRois = pd.read_csv(baseDir + path.sep + seg +posPrefix + 'Results.csv')
    cellNbs = getAllCellNumbers(features_dir)
    tasks = []
    isStatic=True
    if isStatic:
        for cellNb in cellNbs:
            tasks.append((cellNb, features_dir, channel))
        results = [pool.apply_async(isInMitosis, t) for t in tasks]
    else:
        for cellNb in cellNbs:
            tasks.append((features_dir, cellNb, -4, minSegLen, channel))
        results = [pool.apply_async(getMitoticElongation, t) for t in tasks]
    elongationRegions = dict()
    for result in results:
        res = result.get()
        if res is None:
            continue
        elongationRegions[res[0]] = res[1]
    if isStatic:
        timePoints = None
    else:
        timePoints = analyse_each_cell(pool, minSegLen, elongationRegions, cellRois, mitosisDir)
    if ch5:
        description = ("cell", "cell_shape_features")
        with cellh5write.CH5FileWriter(mitosisDir + "mitosisAnalysis.ch5") as cfw:
            for cellNb in [k.split("_")[0] for k in elongationRegions.keys()]:
                cdata = list()
                for c in chs:
                    cdata.append(tifffile.imread(fluoDir + cropImgs + str(cellNb) + "_" + c + ".tif"))
                cdata = np.array(cdata)
                while len(cdata.shape) < 5:
                    cdata = np.expand_dims(cdata, axis=0)
                shape = cdata.shape
                cpw = cfw.add_position(cellh5.CH5PositionCoordinate(pos, date, cellNb))

                crw = cpw.add_label_image(shape=shape, dtype=np.int16)
                for c in range(shape[0]):
                    for t in range(shape[1]):
                        for z in range(shape[2]):
                            crw.write(cdata[c,t,z,:,:], c=c, t=t, z=z)
                crw.finalize()

                regObjs = list()

                cow_cell = cpw.add_region_object('cell')
                for t in range(shape[2]):
                    cow_cell.write(t=t, object_labels=np.array([cellNb]))
                cow_cell.finalize()
                regObjs.append(cow_cell)

                cfew_cell_features = cpw.add_object_feature_matrix(object_name=description[0],
                    feature_name=description[1], n_features=len(cellRois.columns),
                    dtype=np.float16)
                cfew_cell_features.write(np.expand_dims(cellRois.loc[int(cellNb)], axis=0))
                cfew_cell_features.finalize()

                cfewSpotMats = list()
                featuresOfCurrentCell = {chs[0]: None, chs[1]: None}
                spotsOfCurrentCell = {chs[0]: None, chs[1]: None}
                for c in chs:
                    ##### save features data#######
                    curId = str(cellNb) + "_" + c
                    try:
                        features = pd.read_csv(features_dir + curId + ".csv")
                    except :
                        continue
                    # cow_feature = cpw.add_region_object(c + '_features')
                    # regObjs.append(cow_feature)
                    # for t in features['Frame']:
                    #     cow_feature.write(t=t, object_labels=np.array([t]))
                    # cow_feature.finalize()
                    if c == channel:
                        features = pd.merge(features,
                            pd.DataFrame({"Frame":elongationRegions[curId].index, "mitoRegion":elongationRegions[curId]})
                            , on='Frame', how='outer')
                    else:
                        features = pd.merge(features,
                            pd.DataFrame({"Frame":features.index, "mitoRegion":np.empty(len(features.index))})
                            , on='Frame', how='outer')
                    changePoints = pd.DataFrame({"Frame":features.index,"ChangePoints":np.empty(len(features.index))})
                    if timePoints != None:
                        for chPoiInd in timePoints.loc[str(cellNb)].values:
                            changePoints.loc[chPoiInd] = [1, chPoiInd]
                    features = pd.merge(features, changePoints, on='Frame', how='outer')
                    featuresOfCurrentCell[c] = features
                    ##### save spot data#######
                    spotsData = TMxml2dflib.getAllSpots(fluoDir + spots + str(cellNb) + "_" + c + ".xml")
                    print(spotsData.shape[0])
                    if spotsData.shape[0] > 1:
                        spotsData.drop('name', axis=1, inplace=True)
                        cow_spot = cpw.add_region_object(c + '_spot')
                        for t in spotsData.index.levels[0]:
                                cow_spot.write(t=t, object_labels=spotsData.loc[t]['ID'])
                        cow_spot.finalize()

                        regObjs.append(cow_spot)

                        cfew_spot_features = cpw.add_object_feature_matrix(object_name=c + '_spot',
                            feature_name=c + '_spot_features', n_features=len(spotsData.columns),
                            dtype=np.float16)
                        cfew_spot_features.write(spotsData.astype(np.float16))
                        cfew_spot_features.finalize()

                        spotsOfCurrentCell[c] = spotsData
                        cfewSpotMats.append(cfew_spot_features)
                geos = getSpotGeos(spotsOfCurrentCell[channel], spotsOfCurrentCell["GFP"])
                if geos is not None:
                    geos['Frame'] = list(geos.index)
                    features = pd.merge(featuresOfCurrentCell[channel], geos, on='Frame', how='outer')
                    features.set_index('Frame', inplace=True)
                    features.sort_index(inplace=True)
                cfewFeatureMats = list()
                for c in chs:
                    cfew_features = cpw.add_object_feature_matrix(object_name=c + '_features',
                        feature_name=c + '_features', n_features=len(features.columns),
                        dtype=np.float16)
                    cfew_features.write(features.astype(np.float16))
                    cfew_features.finalize()
                    cfewFeatureMats.append(cfew_features)
            for regobj in regObjs:
                regobj.write_definition()
            # cfew_cell_bounding.write_definition()
            cfew_cell_features.write_definition(list(cellRois.columns))
            for cf in cfewSpotMats:
                cf.write_definition(list(spotsData.columns))
            for cf in cfewFeatureMats:
                cf.write_definition(list(features.columns))
            ch5writeRegDef(crw)
    elif timePoints != None:
            createOutputDirs(mitosisDir, [cropImgs, spots, features, figs])
            copy_mitosis_files(elongationRegions, ["CFP", "GFP", "TxRed", "DAPI"],
                fluoDir, mitosisDir, cropImgs, spots,features)
            pd.DataFrame.from_dict(elongationRegions).to_csv(mitosisDir + "mitosis_elongations.csv")
            timePoints.to_csv(mitosisDir + "mitosis_time_board.csv")
            print(elongationRegions)
            savePlots(elongationRegions, cellRois, calibration, timePoints)
    pool.close()
    pool.join()
    print("Done")
