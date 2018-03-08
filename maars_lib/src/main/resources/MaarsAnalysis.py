# coding: utf-8
# !/usr/bin/env python3

from maarsanalyzer.io import loader
import pandas as pd
from maarsanalyzer import Plotting
from maarsanalyzer.MaarsAnalyzer import MaarsAnalyzer

if __name__ == '__main__':
    args = loader.set_attributes_from_cmd_line()
    analyser = MaarsAnalyzer(args.baseDir, args.pos, args.bf_Prefix, args.fluo_Prefix,\
        args.minimumPeriod, args.acq_interval, targetCh ="CFP", toCh5=True, isstatic=args.isstatic)
    # analyser = MaarsAnalyzer("/media/tong/screening/Data/07_03_18",\
    #     "wt-1", "BF_WT_1", "FLUO_WT_2", 200, 30, targetCh ="CFP", toCh5=True, isstatic=True)
    mitoFilter = analyser.getMitosisFilter()
    mitoCellNbs = analyser.getMitoCellNbs(mitoFilter)
    dict_id_spLens = analyser.getElongations(mitoCellNbs)
    timeTable = pd.DataFrame(columns = ["0", "1", "start", "end"])
    for cellId in dict_id_spLens.keys():
        timeTable.loc[cellId] = ( None, None, dict_id_spLens[cellId].index[0], dict_id_spLens[cellId].index[-1])
    if not analyser.isstatic():
        slopesChanges = analyser.getMaxSlopes(dict_id_spLens)
        for cellId in dict_id_spLens.keys():
            timeTable.set_value(cellId, "0", slopesChanges.loc[cellId]["0"])
            timeTable.set_value(cellId, "1", slopesChanges.loc[cellId]["1"])
        ##########plotting#################
        Plotting.plotElong(dict_id_spLens)
    analyser.writeReport(mitoCellNbs, dict_id_spLens, timeTable)
    # pathToXmls = [maarscsts.FLUO_SPOT + str(c) + "_" + args.channel + '.xml' for c in mitoticCellNbs]
    # # Track SPBs
    #
    # for ind, xmlpath in zip(mitoticCellNbs, pathToXmls):
    #     spotXY = loader.getAllSpots(xmlpath)[[maarsConsts.POS_X, maarsConsts.POS_Y]]
    #     spotsInRegion = spotXY.loc[region.loc[ind][0]:region.loc[ind][1]]
    #     print(spotsInRegion)
        # reshaped =spLens.values.reshape(len(spLens),1)
        # fftmsd = msd_fft(reshaped)
        # fig = figure(title=p.split("/")[-1], y_range=(0.0001,10**2) , y_axis_type="log")#
        # fig.line(spLens.index, fftmsd, color= "blue")
        # row.append(fig)
    analyser.shutdown()
    print("Done")
