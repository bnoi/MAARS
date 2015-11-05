package fiji.plugin.maars.segmentPombe.mainDialogActions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import fiji.plugin.maars.segmentPombe.ParametersProcessing;
import fiji.plugin.maars.segmentPombe.SegPombe;
import fiji.plugin.maars.segmentPombe.SegPombeMainDialog;
import fiji.plugin.maars.segmentPombe.SegPombeParameters;

public class RunAction implements ActionListener {

	private SegPombeMainDialog mainDialog;

	public RunAction(SegPombeMainDialog mainDialog) {
		this.mainDialog = mainDialog;
	}

	/**
	 * Action performed when run Button is triggered. It checks all parameters
	 * then run Algorithm.
	 */
	public void actionPerformed(ActionEvent e) {
		ParametersProcessing process = new ParametersProcessing(mainDialog);
		process.checkParameters();
		process.updateParameters();
		SegPombeParameters parameters = process.getFinalParameters();
		SegPombe segPombe = new SegPombe(parameters);
	}
	// String savingPath = cB.getFileNameField().getText();
	// this.parameters.setSavingPath(savingPath);
	// this.parameters.setWillFiltrateUnusualShape(cB.getFilterUnususalCkb().getState());
	// this.parameters.setWillFiltrateWithMeanGrayValue(cB.getFilterWithMeanGreyValueCkb().getState());
	// this.parameters.setWillSaveBinaryImg(cB.getWillSaveBinaryImgCkb().getState());
	// this.parameters.setWillSaveCorrelationImg(cB.getWillSaveCorrelationImgCkb().getState());
	// this.parameters.setWillSaveDataFrame(cB.getWillSaveDataFrameCkb().getState());
	// this.parameters.setWillSaveFocusImage(cB.getWillSaveFocusImageCkb().getState());
	// this.parameters.setWillSaveRoi(cB.getwillSaveRoiCkb().getState());
	// this.parameters.setWillShowBinaryImg(cB.getWillShowBinaryImgCkb().getState());
	// this.parameters.setWillShowCorrelationImg(cB.getWillShowCorrelationImgCkb().getState());
	// this.parameters.setWillShowDataFrame(cB.getWillShowDataFrameCkb().getState());
	// this.parameters.setWillChangeScale(cB.getWillChangeScaleCkb().getState());
	// this.parameters.setWillShowFocusImage(cB.getWillShowFocusImageCkb().getState());
}
