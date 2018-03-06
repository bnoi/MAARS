# coding: utf-8
# !/usr/bin/env python3

from maarsanalyzer.io import loader
import pandas as pd
from maarsanalyzer import Plotting
from maarsanalyzer.MaarsAnalyzer import MaarsAnalyzer

if __name__ == '__main__':
    args = loader.set_attributes_from_cmd_line()
    analyser = MaarsAnalyzer(args.baseDir, args.pos, args.bf_Prefix, args.fluo_Prefix,\
        args.minimumPeriod, args.acq_interval, targetCh ="CFP", toCh5=True)
    # analyser = MaarsAnalyzer("/media/tong/screening/20_10_17",\
    #     "dam1", "BF_1", "FLUO_1", 200, 30, targetCh ="CFP", toCh5=True)
    mitoFilter = analyser.getMitosisFilter()
    mitoCellNbs = analyser.getMitoCellNbs(mitoFilter)
    dict_id_spLens = analyser.getElongations(mitoCellNbs)
    mitoRange = pd.DataFrame(columns = ["start", "end"])
    for cellId in dict_id_spLens.keys():
        interpolated_spLens = dict_id_spLens[cellId]
        mitoRange.loc[cellId] = [interpolated_spLens.index[0], interpolated_spLens.index[-1]]
    slopeChanges = analyser.getMaxSlopes(dict_id_spLens)
    slopeChanges.insert(2, "start", mitoRange["start"])
    slopeChanges.insert(3, "end", mitoRange["end"])
    analyser.writeReport(mitoCellNbs, dict_id_spLens, slopeChanges)
    ##########plotting#################
    Plotting.plotElong(dict_id_spLens)
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
