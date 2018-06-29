#@OpService ops
#@String(choices={"fluoConfigurator", "batchSegmentation", "batchFluoAnalysis", "find_septums(experimental)"}, style="radioButtonVertical") method
#@File(label="Select a directory", style="directory") d
#@String(value="tif") suffix
import xml.etree.ElementTree as ET
import os
p = ET.parse(d.getPath() + "/maars_config.xml")
fluo_prefix = p.findtext("FLUO_ANALYSIS_PARAMETERS/FLUO_PREFIX")
bf_prefix = p.findtext("SEGMENTATION_PARAMETERS/SEG_PREFIX")
def findSeptum(root, show, pos, n = 4):
	from ij import IJ
	corrImg = IJ.openImage(root + "/%s_SegAnalysis/%s/CorrelationImage.tif"% (bf_prefix, pos))
	IJ.run(corrImg, "8-bit", "");
	#estimate_width extend_line
	# these parameters can be added also
	cmd = "line_width=10 high_contrast=250 low_contrast=50  show_junction_points show_ids add_to_manager make_binary method_for_overlap_resolution=NONE sigma=3 lower_threshold=0 upper_threshold=1.36 minimum_line_length=30 maximum=60"

	if show:
		cmd += " displayresults"
	IJ.run(corrImg, "Ridge Detection", cmd);
		
	binarylineImg = IJ.getImage()
	IJ.run(binarylineImg, "Invert", "");
	binaryImg = IJ.openImage(root + "/%s_SegAnalysis/%s/BinaryImage.tif" % (bf_prefix, pos))
	binaryImg.show()
	IJ.run("Add Image...", "x=0 y=0 opacity=100 zero");
	binaryImg.hide()
	binarylineImg.hide()
	imp2 = binaryImg.flatten();
	IJ.run(imp2, "8-bit", "");
	for i in range(n):
		IJ.run(imp2, "Erode", "");
	for j in range(n):
		IJ.run(imp2, "Dilate", "");
	IJ.saveAsTiff(imp2, d.getPath() + "/%s_SegAnalysis/%s/BinaryImage_with_sep.tif" % (bf_prefix, pos));


def is_segmentation_done():
	return os.path.exists(d.getPath() + "/%s_SegAnalysis" % bf_prefix)

def find_positions():
	return os.listdir(d.getPath() + "/%s_SegAnalysis" % bf_prefix)

if method=="fluoConfigurator":
    ops.run(method, d.getPath(), "maars_config.xml")
elif method == "batchSegmentation":
	ops.run(method, d.getPath(), "maars_config.xml", suffix, False)
elif method == "find_septums(experimental)":
	if is_segmentation_done():
		pos_list = find_positions()
		for pos in pos_list:
			findSeptum(d.getPath(), False, pos)
	else:
		print("Do segmentation first")
elif method == "batchFluoAnalysis":
    ops.run(method, d.getPath(), "maars_config.xml", suffix)
else:
	print("Don't know what you want")


