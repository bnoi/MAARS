package fiji.plugin.maars.segmentPombe;

import java.awt.Color;

import fiji.plugin.maars.utils.FileUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 5, 2015
 */
public class ParametersProcessing {

	SegPombeMainDialog mainDialog;
	SegPombeParameters parameters;

	private boolean unitsChecked;

	/*
	 * constructor 1: Call this constructor when using SegmentPomobe interface
	 */
	public ParametersProcessing(SegPombeMainDialog mainDialog) {
		this.mainDialog = mainDialog;
		this.parameters = new SegPombeParameters();
		this.unitsChecked = false;
	}
	
	/*
	 * constructor 2: Call this constructor when using MAARS 
	 */
	public ParametersProcessing(SegPombeParameters parameters) {
		this.parameters = parameters;
	}

	/*
	 * Check validity of all text fields
	 */
	public boolean checkParameters() {

		// check if saving path is valid
		System.out.println("Checking if saving path is valid");
		String savePath = mainDialog.getSaveDirTf().getText();
		if (!FileUtils.isValid(savePath)) {
			IJ.error("Invalid saving path");
			mainDialog.getSaveDirTf().setBackground(Color.ORANGE);
			return false;
		}
		System.out.println("...OK!");

		// check if image path is valid
		System.out.println("Checking if path to image is valid");
		String pathToImg = savePath + mainDialog.getImgNameTf().getText();
		if (!FileUtils.isValid(pathToImg)) {
			IJ.error("Invalid movie path");
			mainDialog.getImgNameTf().setBackground(Color.ORANGE);
			return false;
		}
		System.out.println("...OK!");
		
		// Check sigma value
		System.out.println("Checking if sigma value is valid");
		if ((Double) mainDialog.getTypicalSizeTf().getValue() <= 0) {
			IJ.error("Wrong parameter", "Sigma must be a positive not null value");
			return false;
		}
		System.out.println("...OK!");

		// Check new image size value
		System.out.println("Checking if new image size values are valid");
		if ((Integer) mainDialog.getMaxHeightTf().getValue() <= 0) {
			IJ.error("Wrong parameter", "Max height must be a positive not null value");
			return false;
		}
		System.out.println("...OK!");
		if ((Integer) mainDialog.getMaxWidthTf().getValue() <= 0) {
			IJ.error("Wrong parameter", "Max width must be a positive not null value");
			return false;
		}
		System.out.println("...OK!");

		// Check abnoraml cell shape value
		System.out.println("Checking if solidity value is valid");
		if ( (Double) mainDialog.getSolidityTf().getValue() <= 0 || (Double) mainDialog.getSolidityTf().getValue() > 1) {
			IJ.error("Wrong parameter", "Solidity must be between 0 and 1");
			return false;
		}
		System.out.println("...OK!");

		// Check minimum cell area
		System.out.println("Checking if minimum particle size is valid");
		if ((Double) mainDialog.getMinParticleSizeTf().getValue() <= 0) {
			IJ.error("Wrong parameter", "The minimum area must be a positive not null value");
			return false;
		}
		System.out.println("...OK!");

		// Check maximum cell area
		System.out.println("Checking if maximum particle size is valid");
		if ((Double) mainDialog.getMaxParticleSizeTf().getValue() <= 0) {
			IJ.error("Wrong parameter", "The maximum area must be a positive not null value");
			return false;
		}
		System.out.println("...OK!");

		// Check z focus value
		System.out.println("Checking if z focus value is valid");
		if ((Integer) mainDialog.getManualZFocusTf().getValue() <= 0) {
			IJ.error("Wrong parameter", "Focus slide must be a positive not null value");
			return false;
		}
		System.out.println("...OK!");

		// Check if none of the result ckeckBox is selected : in this case,
		// the user would not get any result
		System.out.println("Checking if none of the result ckeckBox is selected");
		boolean thereIsAResult = checkResultOptions();
		if (!thereIsAResult) {
			IJ.error("No result possible", "You have not selected a way to see your results");
			return false;
		}
		System.out.println("...OK!");

		return true;
	}

	/*
	 * Get all user chose parameters
	 */

	public void updateParameters() {
		int selectedIndex;
		double tmpDouble;
		int tmpInt;

		parameters.setSavingPath(mainDialog.getSaveDirTf().getText());
		parameters.setDirection(mainDialog.getDirection());
		parameters.setChangeScale(mainDialog.getChangeScaleCkb().getState());
		parameters.setFilterAbnormalShape(mainDialog.getFilterAbnormalShapeCkb().getState());
		parameters.setFiltrateWithMeanGrayValue(mainDialog.getFilterWithMeanGreyValueCkb().getState());
		parameters.setShowCorrelationImg(mainDialog.getShowCorrelationImgCkb().getState());
		parameters.showBinaryImg = mainDialog.getShowBinaryImgCkb().getState();
		parameters.showDataFrame = mainDialog.getShowDataFrameCkb().getState();
		parameters.showFocusImage = mainDialog.getShowFocusImageCkb().getState();
		parameters.saveCorrelationImg = mainDialog.getShowCorrelationImgCkb().getState();
		parameters.saveBinaryImg = mainDialog.getSaveBinaryImgCkb().getState();
		parameters.saveDataFrame = mainDialog.getSaveDataFrameCkb().getState();
		parameters.saveFocusImage = mainDialog.getSaveFocusImageCkb().getState();
		parameters.saveRoi = mainDialog.getSaveRoiCkb().getState();

		this.imgToAnalysis = IJ.getImage().duplicate();
		// if the unit chosen is a micron it must be converted
		System.out.println("Check if one of the unite used is micron");
		if (SegPombeParameters.MICRONS == mainDialog.getTypicalSizeUnitCombo().getSelectedIndex()
				|| SegPombeParameters.MICRONS == mainDialog.getMaxWidthUnitCombo().getSelectedIndex()
				|| SegPombeParameters.MICRONS == mainDialog.getMaxHeightUnitCombo().getSelectedIndex()
				|| SegPombeParameters.MICRONS == mainDialog.getMinParticleSizeUnitCombo().getSelectedIndex()
				|| SegPombeParameters.MICRONS == mainDialog.getMaxParticleSizeUnitCombo().getSelectedIndex()) {
			checkImgUnitsAndScale();
		}

		// Convert size into pixels
		selectedIndex = mainDialog.getTypicalSizeUnitCombo().getSelectedIndex();
		tmpDouble = (Double) mainDialog.getTypicalSizeTf().getValue();
		if (selectedIndex == SegPombeParameters.MICRONS && unitsChecked) {
			this.sigma = convertMicronToPixel(tmpDouble, SegPombeParameters.DEPTH)/SegPombeParameters.acquisitionStep;
			System.out.println("typical size is in micron, convert it in pixels : " + String.valueOf(this.sigma));
		} else if (selectedIndex == SegPombeParameters.PIXELS) {
			this.sigma = tmpDouble / SegPombeParameters.acquisitionStep;
		}

		this.solidity = (Double) mainDialog.getSolidityTf().getValue();

		// Covert minimum area if needed to
		selectedIndex = mainDialog.getMinParticleSizeUnitCombo().getSelectedIndex();
		tmpDouble = (Double) mainDialog.getMinParticleSizeTf().getValue();
		if (selectedIndex == SegPombeParameters.MICRONS && unitsChecked) {
			this.minParticleSize = tmpDouble * convertMicronToPixel(1, SegPombeParameters.WIDTH)
					* convertMicronToPixel(1, SegPombeParameters.HEIGHT);
			System.out.println("Cell Area is in micron, convert it in pixels : " + minParticleSize);
		} else {
			if (selectedIndex == SegPombeParameters.PIXELS) {
				this.minParticleSize = tmpDouble;
			}
		}

		// Covert maximum area if needed to
		selectedIndex = mainDialog.getMaxParticleSizeUnitCombo().getSelectedIndex();
		tmpDouble = (Double) mainDialog.getMaxParticleSizeTf().getValue();
		if (selectedIndex == SegPombeParameters.MICRONS && unitsChecked) {
			this.maxParticleSize = tmpDouble * convertMicronToPixel(1, SegPombeParameters.WIDTH)
					* convertMicronToPixel(1, SegPombeParameters.HEIGHT);
			System.out.println("Cell Area is in micron, convert it in pixels : " + maxParticleSize);
		} else {
			if (selectedIndex == SegPombeParameters.PIXELS) {
				this.maxParticleSize = tmpDouble;
			}
		}
		// If the user chose to change the scale
		System.out.println("Check if user wants to change scale");
		if (mainDialog.getChangeScaleCkb().getState()) {
			selectedIndex = mainDialog.getMaxWidthUnitCombo().getSelectedIndex();
			tmpInt = (Integer) mainDialog.getMaxWidthTf().getValue();
			if (selectedIndex == SegPombeParameters.MICRONS && unitsChecked) {
				this.maxWidth = convertMicronToPixel(tmpInt, SegPombeParameters.WIDTH);
				System.out.println("Width value is in micron, convert it in pixel : " + maxWidth);
			} else {
				if (selectedIndex == SegPombeParameters.PIXELS) {
					this.maxWidth = (int) tmpInt;
				}
			}

			selectedIndex = mainDialog.getMaxHeightUnitCombo().getSelectedIndex();
			tmpInt = (Integer) mainDialog.getMaxHeightTf().getValue();
			if (selectedIndex == SegPombeParameters.MICRONS && unitsChecked) {

				maxHeight = convertMicronToPixel(tmpInt, SegPombeParameters.HEIGHT);
				System.out.println("Height value is in micron, convert it in pixel : " + maxHeight);
			} else {
				if (selectedIndex == SegPombeParameters.PIXELS) {
					maxHeight = (int) tmpInt;
				}
			}
			// Then we can change scale
			System.out.println("Change scale");
			changeScale(maxWidth, maxHeight);
		}

		System.out.println("Check if user wants to precise z focus");
		if (mainDialog.getManualZFocusCkb().getState()) {
			this.focusSlide = (Integer) mainDialog.getManualZFocusTf().getValue() - 1;

		} else {
			this.focusSlide = (imgToAnalysis.getNSlices() / 2) - 1;
		}

		System.out.println("Check if user want to filter background using mean gray value");
		if (mainDialog.getFilterWithMeanGreyValueCkb().getState()) {
			this.meanGrayValue = (Double) mainDialog.getMeanGreyValueField().getValue();
		} else {
			this.meanGrayValue = 0;
		}

	}

	/**
	 * Method to shrink the image scale if it is bigger than supposed to the
	 * maximum width an height that are in pixels
	 */
	public void changeScale(int maxWidth, int maxHeight) {
		int newWidth;
		int newHeight;
		int newMaxParticleSize;
		int newMinParticleSize;
		Calibration newCal = new Calibration();
		newCal.setUnit("micron");
		ImagePlus img = imgToAnalysis;
		if (img.getWidth() > maxWidth) {
			System.out.println("Image width is greater than maximum width allowed");

			newWidth = maxWidth;
			newHeight = (int) img.getHeight() * maxWidth / img.getWidth();

			newMinParticleSize = (int) minParticleSize * maxWidth / img.getWidth();
			newMaxParticleSize = (int) maxParticleSize * maxWidth / img.getWidth();

			if (img.getCalibration().scaled()) {

				newCal.pixelWidth = parameters.getScale(SegPombeParameters.WIDTH) * img.getWidth() / maxWidth;
				newCal.pixelHeight = parameters.getScale(SegPombeParameters.HEIGHT) * img.getWidth() / maxWidth;
				newCal.pixelDepth = parameters.getScale(SegPombeParameters.DEPTH);
			}

			System.out.println("New values : w = " + newWidth + " h = " + newHeight);
			if (newHeight > maxHeight) {
				System.out.println("New height is still greater than maximum height allowed");
				newHeight = maxHeight;
				newWidth = (int) img.getWidth() * maxHeight / img.getHeight();

				newMinParticleSize = (int) minParticleSize * maxHeight / img.getHeight();
				newMaxParticleSize = (int) maxParticleSize * maxHeight / img.getHeight();

				if (img.getCalibration().scaled()) {
					newCal.pixelWidth = parameters.getScale(SegPombeParameters.WIDTH) * img.getHeight() / maxHeight;
					newCal.pixelHeight = parameters.getScale(SegPombeParameters.HEIGHT) * img.getHeight() / maxHeight;
					newCal.pixelDepth = parameters.getScale(SegPombeParameters.DEPTH);
				}

				System.out.println("New values : w = " + newWidth + " h = " + newHeight);

			}

			rescale(newWidth, newHeight, newMinParticleSize, newMaxParticleSize, newCal);
		} else {
			if (img.getHeight() > maxHeight) {
				System.out.println("Image height is greater than maximum width allowed");

				newHeight = maxHeight;
				newWidth = (int) img.getWidth() * maxHeight / img.getHeight();

				newMinParticleSize = (int) minParticleSize * maxHeight / img.getHeight();
				newMaxParticleSize = (int) maxParticleSize * maxHeight / img.getHeight();

				if (img.getCalibration().scaled()) {
					newCal.pixelWidth = parameters.getScale(SegPombeParameters.WIDTH) * img.getHeight() / maxHeight;
					newCal.pixelHeight = parameters.getScale(SegPombeParameters.HEIGHT) * img.getHeight() / maxHeight;
					newCal.pixelDepth = parameters.getScale(SegPombeParameters.DEPTH);
				}

				System.out.println("New values : w = " + newWidth + " h = " + newHeight);

				if (newWidth > maxWidth) {
					System.out.println("New Width is still greater than maximum height allowed");

					newWidth = maxWidth;
					newHeight = (int) img.getHeight() * maxWidth / img.getWidth();

					if (img.getCalibration().scaled()) {
						newCal.pixelWidth = parameters.getScale(SegPombeParameters.WIDTH) * img.getWidth() / maxWidth;
						newCal.pixelHeight = parameters.getScale(SegPombeParameters.HEIGHT) * img.getWidth() / maxWidth;
						newCal.pixelDepth = parameters.getScale(SegPombeParameters.DEPTH);
					}

					System.out.println("New values : w = " + newWidth + " h = " + newHeight);

					newMinParticleSize = (int) minParticleSize * maxWidth / img.getWidth();
					newMaxParticleSize = (int) maxParticleSize * maxWidth / img.getWidth();

				}

				rescale(newWidth, newHeight, newMinParticleSize, newMaxParticleSize, newCal);
			}
		}
	}

	/**
	 * Method to change size and scale of the image to analyze : need to compute
	 * parameters before so it can be coherent
	 */
	public void rescale(int newWidth, int newHeight, int newMinParticleSize, int newMaxParticleSize,
			Calibration newCal) {

		minParticleSize = newMinParticleSize;
		maxParticleSize = newMaxParticleSize;

		System.out.println("min area = " + newMinParticleSize + " max area = " + newMaxParticleSize);

		ImageStack newImgStack = new ImageStack(newWidth, newHeight);

		for (int slice = 0; slice < imgToAnalysis.getNSlices(); slice++) {
			imgToAnalysis.setZ(slice);
			newImgStack.addSlice(imgToAnalysis.getProcessor().resize(newWidth, newHeight));
		}

		ImagePlus newImagePlus = new ImagePlus("rescaled_" + imgToAnalysis.getTitle(), newImgStack);
		newImagePlus.setCalibration(newCal);

		parameters.setImageToAnalyze(newImagePlus);

		checkImgUnitsAndScale();
	}

	/**
	 * Method to check if the image is scaled and if the unit matches 'micron'
	 */
	public void checkImgUnitsAndScale() {
		System.out.println("Check if image is scaled");
		if (imgToAnalysis.getCalibration().scaled()) {

			if (imgToAnalysis.getCalibration().getUnit().equals("cm")) {
				imgToAnalysis.getCalibration().setUnit("micron");
				imgToAnalysis.getCalibration().pixelWidth = imgToAnalysis.getCalibration().pixelWidth * 10000;
				imgToAnalysis.getCalibration().pixelHeight = imgToAnalysis.getCalibration().pixelHeight * 10000;
			}

			System.out.println("Check if unit of calibration is micron");
			if (imgToAnalysis.getCalibration().getUnit().equals("micron")
					|| imgToAnalysis.getCalibration().getUnit().equals("µm")) {
				double[] scale = new double[3];
				scale[SegPombeParameters.WIDTH] = imgToAnalysis.getCalibration().pixelWidth;
				scale[SegPombeParameters.HEIGHT] = imgToAnalysis.getCalibration().pixelHeight;
				scale[SegPombeParameters.DEPTH] = imgToAnalysis.getCalibration().pixelDepth;

				parameters.setScale(scale);
				unitsChecked = true;
				System.out.println("Get and set calibration as scale");
			} else {
				IJ.error("Wrong scale unit",
						"The scale of your image must be in microns.\nTo change it you can go to Properties ...");
			}
		} else {
			IJ.error("No scale",
					"You must set a scale to your image\nif you want to enter measures in micron.\nYou can go to Properties ...");
		}
	}

	/**
	 * Method to get the state of all the result Option checkbox and return
	 * false if none of them are selected
	 */
	public boolean checkResultOptions() {

		return (mainDialog.getShowCorrelationImgCkb().getState() || mainDialog.getSaveBinaryImgCkb().getState()
				|| mainDialog.getShowDataFrameCkb().getState() || mainDialog.getSaveCorrelationImgCkb().getState()
				|| mainDialog.getSaveBinaryImgCkb().getState() || mainDialog.getSaveDataFrameCkb().getState()
				|| mainDialog.getShowFocusImageCkb().getState() || mainDialog.getSaveFocusImageCkb().getState()
				|| mainDialog.getSaveRoiCkb().getState());
	}

	/**
	 * Allows to convert width or height or depth of a size in microns to a size
	 * in pixels. To choose if you want to convert width or height you can use
	 * CellsBoundaries constants : WIDTH and HEIGHT and DEPTH. Return an int
	 * width or height in pixels.
	 */
	public int convertMicronToPixel(double micronSize, int widthOrHeightOrDepth) {
		int pixelSize = (int) Math.round(micronSize / parameters.getScale(widthOrHeightOrDepth));
		return pixelSize;
	}

	/**
	 * Allows to convert width or height or depth of a size in pixels to a size
	 * in microns. To choose if you want to convert width or height you can use
	 * CellsBoundaries constants : WIDTH and HEIGHT and DEPTH. Return an double
	 * width or height in microns.
	 */
	public double convertPixelToMicron(int pixelSize, int widthOrHeightOrDepth) {
		double micronSize = (double) parameters.getScale(widthOrHeightOrDepth) * pixelSize;

		return micronSize;
	}

	public SegPombeParameters initalizeParameters() {
		parameters.setImageToAnalyze(this.imgToAnalysis);
		parameters.setSavingPath(this.savingPath);
		parameters.setSigma(this.sigma);
		parameters.setChangeScale(this.changeScale);
		parameters.setMaxWidth(this.maxWidth);
		parameters.setMaxHeight(this.maxHeight);
		parameters.setFilterAbnormalShape(filterAbnormalShape);
		parameters.setSolidityThreshold(this.solidity);
		parameters.setFiltrateWithMeanGrayValue(filtrateWithMeanGrayValue);
		parameters.setMeanGreyValueThreshold(this.meanGrayValue);
		parameters.setMaxParticleSize(this.maxParticleSize);
		parameters.setMinParticleSize(this.minParticleSize);
		parameters.setDirection(this.direction);
		parameters.setFocusSlide(this.focusSlide);
		parameters.setShowCorrelationImg(this.showCorrelationImg);
		parameters.setShowBinaryImg(this.showBinaryImg);
		parameters.setShowDataFrame(this.showDataFrame);
		parameters.setShowFocusImage(this.showFocusImage);
		parameters.setShowCorrelationImg(this.showCorrelationImg);
		parameters.setSaveBinaryImg(this.saveBinaryImg);
		parameters.setSaveDataFrame(this.saveDataFrame);
		parameters.setSaveFocusImage(this.saveFocusImage);
		parameters.setSaveRoi(this.saveRoi);
		parameters.setSaveCorrelationImg(this.saveCorrelationImg);

		return parameters;
	}
	
	public SegPombeParameters getParameters(){
		return this.parameters;
	}

}
