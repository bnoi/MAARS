# coding: utf-8
# !/usr/bin/env python3

from maarsanalyzer.io import loader
from maarsanalyzer import Plotting
from maarsanalyzer.MaarsAnalyzer import MaarsAnalyzer

if __name__ == '__main__':
    args = loader.set_attributes_from_cmd_line()
    analyser = MaarsAnalyzer(args.baseDir, args.pos, args.bf_Prefix, args.fluo_Prefix,\
        args.minimumPeriod, args.acq_interval, args.calibration, targetCh ="CFP", toCh5=True, isstatic=args.isstatic)
    mitoFilter = analyser.getMitosisFilter()
    mitoCellNbs = analyser.getMitoCellNbs(mitoFilter)
    dict_id_spLens = analyser.getElongations(mitoCellNbs)
    timeTable = analyser.getTimeTable(dict_id_spLens)
    analyser.writeReport(mitoCellNbs, dict_id_spLens, timeTable)
    ##########plotting#################
    if not analyser.isstatic():
        Plotting.plotElong(dict_id_spLens)
    analyser.shutdown()
    print("Done")
