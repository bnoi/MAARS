package org.univ_tlse3.resultSaver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.univ_tlse3.utils.FileUtils;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import loci.plugins.LociExporter;

public class MAARSImgSaver {
	String pathToFluoDir;
	String croppedImgDir;
	ImagePlus mergedFullFieldImg;

	public MAARSImgSaver(String pathToFluoDir, ImagePlus mergedFullFieldImg) {
		this.pathToFluoDir = pathToFluoDir;
		this.croppedImgDir = pathToFluoDir + File.separator + "croppedImgs" + File.separator;
		this.mergedFullFieldImg = mergedFullFieldImg;
		if (!new File(croppedImgDir).exists()) {
			new File(croppedImgDir).mkdirs();
		}
	}

	public String getCroppedImgDir() {
		return this.croppedImgDir;
	}

	/**
	 * 
	 * @param croppedImgSet
	 * @return cropped images base directory
	 */
	public void saveCroppedImgs(HashMap<String, ImagePlus> croppedImgSet, int cellNb) {
		for (String s : croppedImgSet.keySet()) {
			String pathToCroppedImg = croppedImgDir + String.valueOf(cellNb) + "_" + s;
			ImagePlus imp = croppedImgSet.get(s);
			IJ.run(imp, "Enhance Contrast", "saturated=0.35");
			IJ.saveAsTiff(imp, pathToCroppedImg);
		}
	}

	public void exportChannelBtf(Boolean splitChannel, ArrayList<String> arrayChannels) {
		LociExporter lociExporter = new LociExporter();
		if (splitChannel) {
			for (String channel : arrayChannels) {
				final String btfPath = pathToFluoDir + File.separator + channel + ".ome.btf";
				if (!FileUtils.exists(btfPath)) {
					ImageStack currentStack = new ImageStack(mergedFullFieldImg.getWidth(),
							mergedFullFieldImg.getHeight());
					for (int j = 1; j <= mergedFullFieldImg.getImageStack().size(); j++) {
						if (mergedFullFieldImg.getStack().getSliceLabel(j).equals(channel)) {
							currentStack.addSlice(mergedFullFieldImg.getStack().getProcessor(j));
						}
					}
					final String macroOpts = "outfile=[" + btfPath
							+ "] splitz=[0] splitc=[0] splitt=[0] compression=[Uncompressed]";
					ImagePlus currentChImp = new ImagePlus(channel, currentStack);
					currentChImp.setCalibration(mergedFullFieldImg.getCalibration());
					lociExporter.setup(macroOpts, currentChImp);
					lociExporter.run(null);
				}
			}
		} else {
			String btfPath = pathToFluoDir + "merged.ome.btf";
			if (!FileUtils.exists(btfPath)) {
				final String macroOpts = "outfile=[" + btfPath
						+ "] splitz=[0] splitc=[0] splitt=[0] compression=[Uncompressed]";
				lociExporter.setup(macroOpts, mergedFullFieldImg);
				lociExporter.run(null);
			}
		}
	}
}
