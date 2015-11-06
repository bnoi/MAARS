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
		if (process.checkParameters()) {
			process.updateParameters();
			SegPombeParameters parameters = process.initalizeParameters();
			SegPombe segPombe = new SegPombe(parameters);
			segPombe.createCorrelationImage();
			segPombe.convertCorrelationToBinaryImage();
			segPombe.analyseAndFilterParticles();
			segPombe.showAndSaveResultsAndCleanUp();
		}
	}
}
