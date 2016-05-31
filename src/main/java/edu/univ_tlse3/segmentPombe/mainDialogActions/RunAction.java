package edu.univ_tlse3.segmentPombe.mainDialogActions;

import edu.univ_tlse3.segmentPombe.ParametersProcessor;
import edu.univ_tlse3.segmentPombe.SegPombe;
import edu.univ_tlse3.segmentPombe.SegPombeMainDialog;
import edu.univ_tlse3.segmentPombe.SegPombeParameters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
