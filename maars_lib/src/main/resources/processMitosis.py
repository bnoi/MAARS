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
import tifffile
import cellh5
import TMxml2dflib

def createOutputDirs(mitosisDir, cropImgs, spots, features, figs):
    for targetDir in [cropImgs, spots, features, figs]:
        d = mitosisDir + targetDir + path.sep
        if not path.isdir(d):
            mkdir(d)

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
    parser.add_argument("-ch5",
                        help="save into cellh5",
                        type=bool, default=True)
    parser.add_argument("-minimumPeriod",
                        help="minimum time segment to be analyzed",
                        type=int, default=200)
    return parser.parse_args()


def getAllCellNumbers(features_dir):
    return pd.Series([int(f.split("_")[0]) for f in listdir(features_dir)]).unique()

def extendMitoRegion(mitoregion, extendLen):
    dlist = extendLen*[mitoregion.iloc[0]] + list(mitoregion) + extendLen*[mitoregion.iloc[-1]]
    indlist = list(np.arange(mitoregion.index[0]- extendLen, mitoregion.index[0])) +        list(mitoregion.index) + list(np.arange(mitoregion.index[-1], mitoregion.index[-1]+extendLen))
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
        oneCell = pd.DataFrame.from_csv(csvPath)
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
    return timePoints


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


# def savePlots(elongationRegions, cellRois, calibration, time_board):
#     for cell_id, elongation in elongationRegions.items():
#         # print(cell_id, elongation)
#         cellNb = int(cell_id.split("_")[0])
#         current_major_length = cellRois.loc[int(cellNb)]['Major'] * calibration
#         fig, ax = plt.subplots(figsize=(15, 8))
#         ax.axhline(current_major_length, c='red', lw=10)
#         ax.axvline(int(time_board.loc[str(cellNb)][1]), c='red')
#         ax.axvline(int(time_board.loc[str(cellNb)][2]), c='black', linestyle=":")
#         ax.axvline(int(time_board.loc[str(cellNb)][3]), c='black', linestyle=":")
#         ax.axvline(int(time_board.loc[str(cellNb)][4]), c='red')
#         plt.ylabel("Spindle Length ($μm$)", fontsize=20)
#         plt.tick_params(axis='both', which='major', labelsize=20)
#         plt.xlabel("Timepoint // interval " + str(acq_interval), fontsize=20)
#         plt.ylim(0, current_major_length)
#         plt.plot(elongation.index, elongation, "-o", c="black")
#         plt.savefig(mitosisFigDir + str(cellNb), transparent=True, bbox_inches='tight')
#         plt.close(fig)

############################## ch5 methods######################################
def ch5writeChDef(ciw):
    c_def = CH5ImageChannelDefinition()
    c_def.add_row(channel_name="BF", description='bright-field', is_physical=True,
                  voxel_size=(0.0645,0.0645,0.3), color="#aabbcc")
    ciw.write_definition(c_def)

def ch5writeRegDef(crw):
    r_def = cellh5.CH5ImageRegionDefinition()
    r_def.add_row(region_name='cell', channel_idx='0')
    r_def.add_row(region_name='cell', channel_idx='1')
    r_def.add_row(region_name='ktspot', channel_idx='1')
    r_def.add_row(region_name='spbspot', channel_idx='1')
    crw.write_definition(r_def)

############################## colocalisation methods ##########################
def dist(x1,y1,x2,y2):
    return np.sqrt((x1-x2)**2 + (y1-y2)**2)

def get_outside_spots(x_base, y_base, xs, ys, radius):
    spots_outside_xs = list()
    spots_outside_ys = list()
    for i in range(0,len(xs)):
        if dist(x_base, y_base,xs[i],ys[i]) > radius:
            spots_outside_xs.append(xs[i])
            spots_outside_ys.append(ys[i])
    return spots_outside_xs,spots_outside_ys

def change_label(df, index, col, value):
    for j in df.loc[index]:
        df.set_value(index,col, value)

def generate_circle_coords(radius):
    an = np.linspace(0, 2*np.pi, 100)
    xs = radius *np.cos(an)
    ys = radius *np.sin(an)
    return (xs, ys)

def find_phases_of_one_cell(poleSpots, ktSpots, radius = 0.25):
    pos_x = "POSITION_X"
    pos_y = "POSITION_Y"
    for j in [pos_x, pos_y]:
        poleSpots[j] = poleSpots[j].astype(np.float)
        ktSpots[j] = ktSpots[j].astype(np.float)
    merged_frameNb = sorted(list(poleSpots.index.levels[0]) + list(set(list(ktSpots.index.levels[0])) - set(list(poleSpots.index.levels[0]))))
    phase_label = np.empty(merged_frameNb[-1]+1)
    poleDotNb = np.empty(merged_frameNb[-1]+1)
    ktDotNb = np.empty(merged_frameNb[-1]+1)
    n_pole = None
    n_kt = None
    for x in merged_frameNb:
        skip=False
        if x in poleSpots.index.levels[0]:
            current_frame_poles = poleSpots.loc[x]
            n_pole = len(current_frame_poles)
            poleDotNb[x] = n_pole
            if n_pole >1:
                phase_label[x]= 1
        else:
            skip = True
        if x in ktSpots.index.levels[0]:
            current_frame_kts = ktSpots.loc[x]
            n_kt = len(current_frame_kts)
            ktDotNb[x] = n_kt
            if n_kt>1:
                phase_label[x]= 1
        else:
            skip=True
        if skip or n_pole<2:
            continue
        spots_outside = (list(current_frame_kts[pos_x]), list(current_frame_kts[pos_y]))
        for i in range(0,len(current_frame_poles[pos_x])):
            spots_outside = get_outside_spots(current_frame_poles[pos_x].iloc[i], current_frame_poles[pos_y].iloc[i],spots_outside[0],spots_outside[1],radius)
        if len(spots_outside[0])==0:
            phase_label[x]= 2
        else:
            phase_label[x]= 1
    phase_d = pd.DataFrame.from_dict({'pole_dotNb':poleDotNb, 'kt_dotNb':ktDotNb, 'phase':phase_label})
    phase_d = phase_d[(phase_d.T != 0).any()]
    return phase_d

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
    cellRois = pd.DataFrame.from_csv(baseDir + path.sep + seg +posPrefix + 'Results.csv')
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

    timePoints = pd.DataFrame.from_dict(analyse_each_cell(pool, minSegLen, elongationRegions, cellRois, mitosisDir)).T
    if ch5:
        description = ("cell", "cell_shape_features")
        with cellh5.CH5FileWriter(mitosisDir + "mitosisAnalysis.ch5") as cfw:
            for cellNb in [k.split("_")[0] for k in elongationRegions.keys()]:
                cdata = list()
                for c in chs:
                    cdata.append(
                    tifffile.imread(baseDir + path.sep + fluo + posPrefix + cropImgs + str(cellNb) + "_" + c + ".tif"))
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
                regObjs.append(cow_cell)
                for t in range(shape[2]):
                    cow_cell.write(t=t, object_labels=np.array([cellNb]))
                cow_cell.finalize()
                # cfew_cell_bounding = cpw.add_object_bounding_box(object_name="cell_boundaries")
                # cfew_cell_bounding.write(cellRois.loc[int(cellNb)][["X","Y","Min","Max"]].values.astype(np.int16).flatten().reshape((-1,4)))
                # cfew_cell_bounding.finalize()
                cfew_cell_features = cpw.add_object_feature_matrix(object_name=description[0],
                    feature_name=description[1], n_features=len(cellRois.columns),
                    dtype=np.float32)
                cfew_cell_features.write(np.expand_dims(cellRois.loc[int(cellNb)], axis=0))
                cfew_cell_features.finalize()


                cfewSpotMats = list()
                cfewFeatureMats = list()
                featuresOfCurrentCell = {chs[0]: None, chs[1]:None}
                spotsOfCurrentCell = {chs[0]: None, chs[1]:None}
                for c in chs:
                    ##### save features data#######
                    curId = str(cellNb) + "_" + c
                    features = pd.read_csv(features_dir + curId + ".csv")
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
                    for chPoiInd in timePoints.loc[str(cellNb)].values:
                        changePoints.loc[chPoiInd] = [1, chPoiInd]
                    features = pd.merge(features, changePoints, on='Frame', how='outer')
                    featuresOfCurrentCell[c] = features
                    ##### save spot data#######
                    spotsData = TMxml2dflib.getAllSpots(baseDir + path.sep + fluo + posPrefix + spots + str(cellNb) + "_" + c + ".xml")
                    del spotsData["name"]
                    cow_spot = cpw.add_region_object(c + '_spot')
                    regObjs.append(cow_spot)
                    for t in spotsData.index.levels[0]:
                        cow_spot.write(t=t, object_labels=spotsData.loc[t]['ID'])
                    cow_spot.finalize()
                    cfew_spot_features = cpw.add_object_feature_matrix(object_name=c + '_spot',
                        feature_name=c + '_spot_features', n_features=len(spotsData.columns),
                        dtype=np.float32)
                    cfew_spot_features.write(spotsData.astype(np.float32))
                    cfew_spot_features.finalize()
                    spotsOfCurrentCell[c] = spotsData
                    cfewSpotMats.append(cfew_spot_features)
                table = find_phases_of_one_cell(spotsOfCurrentCell[channel], spotsOfCurrentCell["GFP"])
                for c in chs:
                    table['Frame'] = list(table.index)
                    features = pd.merge(featuresOfCurrentCell[c], table, on='Frame', how='outer')
                    features.set_index('Frame', inplace=True)
                    features.sort_index(inplace=True)
                    cfew_features = cpw.add_object_feature_matrix(object_name=c + '_features',
                        feature_name=c + '_features', n_features=len(features.columns),
                        dtype=np.float32)
                    cfew_features.write(features.astype(np.float32))
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
    else:
        createOutputDirs(mitosisDir, cropImgs, spots, features, figs)
        copy_mitosis_files(elongationRegions, ["CFP", "GFP", "TxRed", "DAPI"],
            fluoDir, mitosisDir, cropImgs, spots,features)
        pd.DataFrame.from_dict(elongationRegions).to_csv(mitosisDir + "mitosis_elongations.csv")
        timePoints.to_csv(mitosisDir + "mitosis_time_board.csv")
        # savePlots(elongationRegions, cellRois, calibration, times)
    pool.close()
    pool.join()
    print("Done")
