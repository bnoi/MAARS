#@OpService ops
#@File(label="Select a directory", style="directory") d
#@String(label="Folder pattern", value="Pos0_T*") pattern
#@String(label="Image name", value="MMStack.ome.tif") imageName
import os
import re
import shutil
from ij import IJ

bfDir = str(d) + os.sep + "BFs"

if not os.path.exists(bfDir):
	os.mkdir(bfDir)

for fs in os.listdir(str(d)):
	if re.search(pattern, fs):
		shutil.move(str(d)+ os.sep + fs + os.sep + imageName,
		bfDir+ os.sep + fs.split("_")[1] + "_" + imageName)
IJ.log("Migration done")