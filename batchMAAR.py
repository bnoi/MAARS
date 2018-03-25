#@OpService ops
#@String(choices={"fluoConfigurator", "batchSegmentation", "batchFluoAnalysis"}, style="radioButtonVertical") method
#@File(label="Select a directory", style="directory") d
#@String(value="tif") suffix
if method=="fluoConfigurator":
    ops.run(method, d.getPath(), "maars_config.xml")
else:
    ops.run(method, d.getPath(), "maars_config.xml", suffix)
