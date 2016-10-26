package edu.univ_tlse3.segmentPombe;

import edu.univ_tlse3.utils.FileUtils;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;

import java.awt.*;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 5, 2015
 */
public class ParametersProcessor {

    private SegPombeMainDialog mainDialog;
    private SegPombeParameters parameters;
    private ImagePlus imgToAnalysis;

    private boolean unitsChecked;

    /*
     * constructor 1: Call this constructor when using SegmentPomobe interface
     */
    public ParametersProcessor(SegPombeMainDialog mainDialog) {
        this.mainDialog = mainDialog;
        this.parameters = new SegPombeParameters();
        this.unitsChecked = false;
    }

    /*
     * constructor 2: Call this constructor when using MAARS
     */
    public ParametersProcessor(SegPombeParameters parameters) {
        this.parameters = parameters;
        this.imgToAnalysis = parameters.getImageToAnalyze();
    }

    /*
     * Check validity of all text fields, this method is called only when using
     * segpombe plugin
     */
    public boolean checkParameters() {

        // check if saving path is valid
        IJ.log("Checking if saving path is valid");
        String savePath = mainDialog.getSaveDirTf().getText();
        if (!FileUtils.exists(savePath)) {
            IJ.error("Invalid saving path");
            mainDialog.getSaveDirTf().setBackground(Color.ORANGE);
            return false;
        }
        IJ.log("...OK!");

        // check if image path is valid
        IJ.log("Checking if path to image is valid");
        String pathToImg = savePath + mainDialog.getImgNameTf().getText();
        if (!FileUtils.exists(pathToImg)) {
            IJ.error("Invalid movie path");
            mainDialog.getImgNameTf().setBackground(Color.ORANGE);
            return false;
        }
        IJ.log("...OK!");

        // Check sigma value
        IJ.log("Checking if sigma value is valid");
        if ((Double) mainDialog.getTypicalSizeTf().getValue() <= 0) {
            IJ.error("Wrong parameter", "Sigma must be a positive not null value");
            return false;
        }
        IJ.log("...OK!");

        // Check new image size value
        IJ.log("Checking if new image size values are valid");
        if ((Integer) mainDialog.getMaxHeightTf().getValue() <= 0) {
            IJ.error("Wrong parameter", "Max height must be a positive not null value");
            return false;
        }
        IJ.log("...OK!");
        if ((Integer) mainDialog.getMaxWidthTf().getValue() <= 0) {
            IJ.error("Wrong parameter", "Max width must be a positive not null value");
            return false;
        }
        IJ.log("...OK!");

        // Check abnoraml cell shape value
        IJ.log("Checking if solidity value is valid");
        if ((Double) mainDialog.getSolidityTf().getValue() <= 0 || (Double) mainDialog.getSolidityTf().getValue() > 1) {
            IJ.error("Wrong parameter", "Solidity must be between 0 and 1");
            return false;
        }
        IJ.log("...OK!");

        // Check minimum cell area
        IJ.log("Checking if minimum particle size is valid");
        if ((Double) mainDialog.getMinParticleSizeTf().getValue() <= 0) {
            IJ.error("Wrong parameter", "The minimum area must be a positive not null value");
            return false;
        }
        IJ.log("...OK!");

        // Check maximum cell area
        IJ.log("Checking if maximum particle size is valid");
        if ((Double) mainDialog.getMaxParticleSizeTf().getValue() <= 0) {
            IJ.error("Wrong parameter", "The maximum area must be a positive not null value");
            return false;
        }
        IJ.log("...OK!");

        // Check z focus value
        IJ.log("Checking if z focus value is valid");
        if ((Integer) mainDialog.getManualZFocusTf().getValue() <= 0) {
            IJ.error("Wrong parameter", "Focus slide must be a positive not null value");
            return false;
        }
        IJ.log("...OK!");

        // Check if none of the result ckeckBox is selected : in this case,
        // the user would not get any result
        IJ.log("Checking if none of the result ckeckBox is selected");
        boolean thereIsAResult = checkResultOptions();
        if (!thereIsAResult) {
            IJ.error("No result possible", "You have not selected a way to see your results");
            return false;
        }
        IJ.log("...OK!");

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
        parameters.setShowBinaryImg(mainDialog.getShowBinaryImgCkb().getState());
        parameters.setShowDataFrame(mainDialog.getShowDataFrameCkb().getState());
        parameters.setShowFocusImage(mainDialog.getShowFocusImageCkb().getState());
        parameters.setSaveCorrelationImg(mainDialog.getShowCorrelationImgCkb().getState());
        parameters.setSaveBinaryImg(mainDialog.getSaveBinaryImgCkb().getState());
        parameters.setSaveDataFrame(mainDialog.getSaveDataFrameCkb().getState());
        parameters.setSaveFocusImage(mainDialog.getSaveFocusImageCkb().getState());
        parameters.setSaveRoi(mainDialog.getSaveRoiCkb().getState());

        this.imgToAnalysis = IJ.getImage().duplicate();
        // if the unit chosen is a micron it must be converted
        IJ.log("Check if one of the unite used is micron");
        if (SegPombeParameters.MICRONS == mainDialog.getTypicalSizeUnitCombo().getSelectedIndex()
                || SegPombeParameters.MICRONS == mainDialog.getMaxWidthUnitCombo().getSelectedIndex()
                || SegPombeParameters.MICRONS == mainDialog.getMaxHeightUnitCombo().getSelectedIndex()
                || SegPombeParameters.MICRONS == mainDialog.getMinParticleSizeUnitCombo().getSelectedIndex()
                || SegPombeParameters.MICRONS == mainDialog.getMaxParticleSizeUnitCombo().getSelectedIndex()) {
            checkImgUnitsAndScale();
        }

        // Convert size into pixels
        selectedIndex = mainDialog.getTypicalSizeUnitCombo().getSelectedIndex();
        float tmpFloat = (Float) mainDialog.getTypicalSizeTf().getValue();
        if (selectedIndex == SegPombeParameters.MICRONS && unitsChecked) {
            parameters.setSigma(convertMicronToPixel(tmpFloat, SegPombeParameters.DEPTH));
            IJ.log(
                    "typical size is in micron, convert it in pixels : " + String.valueOf(parameters.getSigma()));
        } else if (selectedIndex == SegPombeParameters.PIXELS) {
            parameters.setSigma(tmpFloat);
        }

        parameters.setSolidityThreshold((Double) mainDialog.getSolidityTf().getValue());

        // Covert minimum area if needed to
        selectedIndex = mainDialog.getMinParticleSizeUnitCombo().getSelectedIndex();
        tmpDouble = (Double) mainDialog.getMinParticleSizeTf().getValue();
        if (selectedIndex == SegPombeParameters.MICRONS && unitsChecked) {
            parameters.setMinParticleSize(tmpDouble * convertMicronToPixel(1, SegPombeParameters.WIDTH)
                    * convertMicronToPixel(1, SegPombeParameters.HEIGHT));
            IJ.log("Cell Area is in micron, convert it in pixels : "
                    + String.valueOf(parameters.getMinParticleSize()));
        } else {
            if (selectedIndex == SegPombeParameters.PIXELS) {
                parameters.setMinParticleSize(tmpDouble);
            }
        }

        // Covert maximum area if needed to
        selectedIndex = mainDialog.getMaxParticleSizeUnitCombo().getSelectedIndex();
        tmpDouble = (Double) mainDialog.getMaxParticleSizeTf().getValue();
        if (selectedIndex == SegPombeParameters.MICRONS && unitsChecked) {
            parameters.setMaxParticleSize(tmpDouble * convertMicronToPixel(1, SegPombeParameters.WIDTH)
                    * convertMicronToPixel(1, SegPombeParameters.HEIGHT));
            IJ.log("Cell Area is in micron, convert it in pixels : "
                    + String.valueOf(parameters.getMaxParticleSize()));
        } else {
            if (selectedIndex == SegPombeParameters.PIXELS) {
                parameters.setMaxParticleSize(tmpDouble);
            }
        }
        // If the user chose to change the scale
        IJ.log("Check if user wants to change scale");
        if (mainDialog.getChangeScaleCkb().getState()) {
            selectedIndex = mainDialog.getMaxWidthUnitCombo().getSelectedIndex();
            tmpInt = (Integer) mainDialog.getMaxWidthTf().getValue();
            if (selectedIndex == SegPombeParameters.MICRONS && unitsChecked) {
                parameters.setMaxWidth(convertMicronToPixel(tmpInt, SegPombeParameters.WIDTH));
                IJ.log(
                        "Width value is in micron, convert it in pixel : " + String.valueOf(parameters.getMaxWidth()));
            } else {
                if (selectedIndex == SegPombeParameters.PIXELS) {
                    parameters.setMaxWidth(tmpInt);
                }
            }

            selectedIndex = mainDialog.getMaxHeightUnitCombo().getSelectedIndex();
            tmpInt = (Integer) mainDialog.getMaxHeightTf().getValue();
            if (selectedIndex == SegPombeParameters.MICRONS && unitsChecked) {

                parameters.setMaxHeight(convertMicronToPixel(tmpInt, SegPombeParameters.HEIGHT));
                IJ.log("Height value is in micron, convert it in pixel : "
                        + String.valueOf(parameters.getMaxHeight()));
            } else {
                if (selectedIndex == SegPombeParameters.PIXELS) {
                    parameters.setMaxHeight(tmpInt);
                }
            }
            // Then we can change scale
            IJ.log("Change scale");
            changeScale(parameters.getMaxWidth(), parameters.getMaxHeight());
        }

        IJ.log("Check if user wants to precise z focus");
        if (mainDialog.getManualZFocusCkb().getState()) {
            parameters.setFocusSlide((Integer) mainDialog.getManualZFocusTf().getValue());

        } else {
            parameters.setFocusSlide((imgToAnalysis.getNSlices() / 2) - 1);
        }

        IJ.log("Check if user want to filter background using mean gray value");
        if (mainDialog.getFilterWithMeanGreyValueCkb().getState()) {
            parameters.setMeanGreyValueThreshold((Double) mainDialog.getMeanGreyValueField().getValue());
        } else {
            parameters.setMeanGreyValueThreshold(0);
        }

    }

    /**
     * Method to shrink the image scale if it is bigger than supposed to the
     * maximum width an height that are in pixels
     * @param maxHeight new height
     * @param maxWidth  new width
     */
    public void changeScale(int maxWidth, int maxHeight) {
        int newWidth;
        int newHeight;
        int newMaxParticleSize;
        int newMinParticleSize;
        Calibration newCal = new Calibration();
        newCal.setUnit("micron");
        ImagePlus img = imgToAnalysis;
        IJ.log("Before Width : " + String.valueOf(img.getWidth()) + ", Before Height : "
                + String.valueOf(img.getHeight()));
        if (img.getWidth() > maxWidth) {
            IJ.log("Image width is greater than maximum width allowed");

            newWidth = maxWidth;
            newHeight = img.getHeight() * maxWidth / img.getWidth();

            newMinParticleSize = (int) parameters.getMinParticleSize() * maxWidth / img.getWidth();
            newMaxParticleSize = (int) parameters.getMaxParticleSize() * maxWidth / img.getWidth();

            if (img.getCalibration().scaled()) {

                newCal.pixelWidth = parameters.getScale(SegPombeParameters.WIDTH) * img.getWidth() / maxWidth;
                newCal.pixelHeight = parameters.getScale(SegPombeParameters.HEIGHT) * img.getWidth() / maxWidth;
                newCal.pixelDepth = parameters.getScale(SegPombeParameters.DEPTH);
            }

            IJ.log("New values : w = " + newWidth + " h = " + newHeight);
            if (newHeight > maxHeight) {
                IJ.log("New height is still greater than maximum height allowed");
                newHeight = maxHeight;
                newWidth = img.getWidth() * maxHeight / img.getHeight();

                newMinParticleSize = (int) parameters.getMinParticleSize() * maxHeight / img.getHeight();
                newMaxParticleSize = (int) parameters.getMaxParticleSize() * maxHeight / img.getHeight();

                if (img.getCalibration().scaled()) {
                    newCal.pixelWidth = parameters.getScale(SegPombeParameters.WIDTH) * img.getHeight() / maxHeight;
                    newCal.pixelHeight = parameters.getScale(SegPombeParameters.HEIGHT) * img.getHeight() / maxHeight;
                    newCal.pixelDepth = parameters.getScale(SegPombeParameters.DEPTH);
                }

                IJ.log("New values : w = " + newWidth + " h = " + newHeight);

            }

            rescale(newWidth, newHeight, newMinParticleSize, newMaxParticleSize, newCal);
        } else {
            if (img.getHeight() > maxHeight) {
                IJ.log("Image height is greater than maximum width allowed");

                newHeight = maxHeight;
                newWidth = img.getWidth() * maxHeight / img.getHeight();

                newMinParticleSize = (int) parameters.getMinParticleSize() * maxHeight / img.getHeight();
                newMaxParticleSize = (int) parameters.getMaxParticleSize() * maxHeight / img.getHeight();

                if (img.getCalibration().scaled()) {
                    newCal.pixelWidth = parameters.getScale(SegPombeParameters.WIDTH) * img.getHeight() / maxHeight;
                    newCal.pixelHeight = parameters.getScale(SegPombeParameters.HEIGHT) * img.getHeight() / maxHeight;
                    newCal.pixelDepth = parameters.getScale(SegPombeParameters.DEPTH);
                }

                IJ.log("New values : w = " + newWidth + " h = " + newHeight);

                if (newWidth > maxWidth) {
                    IJ.log("New Width is still greater than maximum height allowed");

                    newWidth = maxWidth;
                    newHeight = img.getHeight() * maxWidth / img.getWidth();

                    if (img.getCalibration().scaled()) {
                        newCal.pixelWidth = parameters.getScale(SegPombeParameters.WIDTH) * img.getWidth() / maxWidth;
                        newCal.pixelHeight = parameters.getScale(SegPombeParameters.HEIGHT) * img.getWidth() / maxWidth;
                        newCal.pixelDepth = parameters.getScale(SegPombeParameters.DEPTH);
                    }

                    IJ.log("New values : w = " + newWidth + " h = " + newHeight);

                    newMinParticleSize = (int) parameters.getMinParticleSize() * maxWidth / img.getWidth();
                    newMaxParticleSize = (int) parameters.getMaxParticleSize() * maxWidth / img.getWidth();

                }

                rescale(newWidth, newHeight, newMinParticleSize, newMaxParticleSize, newCal);
            }
        }
    }

    /**
     * Method to change size and scale of the image to analyze : need to compute
     * parameters before so it can be coherent
     */
    private void rescale(int newWidth, int newHeight, int newMinParticleSize, int newMaxParticleSize,
                         Calibration newCal) {

        parameters.setMinParticleSize(newMinParticleSize);
        parameters.setMaxParticleSize(newMaxParticleSize);

        IJ.log("min area = " + newMinParticleSize + " max area = " + newMaxParticleSize);

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
        IJ.log("Check if image is scaled");
        if (imgToAnalysis.getCalibration().scaled()) {

            if (imgToAnalysis.getCalibration().getUnit().equals("cm")) {
                imgToAnalysis.getCalibration().setUnit("micron");
                imgToAnalysis.getCalibration().pixelWidth = imgToAnalysis.getCalibration().pixelWidth * 10000;
                imgToAnalysis.getCalibration().pixelHeight = imgToAnalysis.getCalibration().pixelHeight * 10000;
            }

            IJ.log("Check if unit of calibration is micron");
            if (imgToAnalysis.getCalibration().getUnit().equals("micron")
                    || imgToAnalysis.getCalibration().getUnit().equals("µm")) {
                double[] scale = new double[3];
                scale[SegPombeParameters.WIDTH] = imgToAnalysis.getCalibration().pixelWidth;
                scale[SegPombeParameters.HEIGHT] = imgToAnalysis.getCalibration().pixelHeight;
                scale[SegPombeParameters.DEPTH] = imgToAnalysis.getCalibration().pixelDepth;

                parameters.setScale(scale);
                unitsChecked = true;
                IJ.log("Get and set calibration as scale");
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
    private boolean checkResultOptions() {

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
    private int convertMicronToPixel(double micronSize, int widthOrHeightOrDepth) {
        return (int) Math.round(micronSize / parameters.getScale(widthOrHeightOrDepth));
    }

    /**
     * Allows to convert width or height or depth of a size in pixels to a size
     * in microns. To choose if you want to convert width or height you can use
     * CellsBoundaries constants : WIDTH and HEIGHT and DEPTH.
     * @param pixelSize     a size in pixels
     * @param widthOrHeightOrDepth Dimension
     * @return Return an double width or height in microns.
     */
    public double convertPixelToMicron(int pixelSize, int widthOrHeightOrDepth) {

        return parameters.getScale(widthOrHeightOrDepth) * pixelSize;
    }

    public SegPombeParameters getParameters() {
        return parameters;
    }

}
