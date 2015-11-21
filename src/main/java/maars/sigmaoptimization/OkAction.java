package maars.sigmaoptimization;

import ij.gui.GenericDialog;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * What happens when user click on "ok" button
 * 
 * @author marie
 *
 */
public class OkAction implements ActionListener {

	private SigmaOptimization so;

	/**
	 * Constructor :
	 * 
	 * @param so
	 */
	public OkAction(SigmaOptimization so) {
		this.so = so;
	}

	/**
	 * Get and set parameters of algorithm and run it
	 */
	public void actionPerformed(ActionEvent e) {
		GenericDialog dialog = so.getDialog();
		so.setLowerSigma(dialog.getNextNumber());
		so.setUpperSigma(dialog.getNextNumber());
		so.setStep(dialog.getNextNumber());
		so.setPath(dialog.getNextString());
		so.setZFocus((int) dialog.getNextNumber());
		so.setDirection((int) dialog.getNextNumber());
		dialog.setVisible(false);
	}

}
