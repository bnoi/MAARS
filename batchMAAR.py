#@OpService ops
#@String(choices={"fluoConfigurator", "batchSegmentation", "batchFluoAnalysis"}, style="radioButtonVertical") method
#@File(label="Select a directory", style="directory") d
if method=="fluoConfigurator":
    ops.run(method, d.getPath(), "maars_config.xml")
else:
    ops.run(method, d.getPath(), "maars_config.xml", "tiff")
