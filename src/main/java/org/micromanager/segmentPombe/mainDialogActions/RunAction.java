package org.micromanager.segmentPombe.mainDialogActions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.micromanager.segmentPombe.ParametersProcessor;
import org.micromanager.segmentPombe.SegPombe;
import org.micromanager.segmentPombe.SegPombeMainDialog;
import org.micromanager.segmentPombe.SegPombeParameters;

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
		ParametersProcessor process = new ParametersProcessor(mainDialog);
		if (process.checkParameters()) {
			process.updateParameters();
			SegPombeParameters parameters = process.getParameters();
			SegPombe segPombe = new SegPombe(parameters);
			segPombe.createCorrelationImage();
			segPombe.convertCorrelationToBinaryImage();
			segPombe.analyseAndFilterParticles();
			segPombe.showAndSaveResultsAndCleanUp();
		}
	}
}
