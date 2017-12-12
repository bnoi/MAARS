#@OpService ops
#@String(choices={"fluoConfigurator", "batchSegmentation", "batchFluoAnalysis"}, style="radioButtonVertical") method
#@File(label="Select a directory", style="directory") d
ops.run(method, d.getPath(), "maars_config.xml")
