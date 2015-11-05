package fiji.plugin.maars.segmentPombe.mainDialogActions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import fiji.plugin.maars.segmentPombe.SegPombeMainDialog;
import fiji.plugin.maars.segmentPombe.SegPombeParameters;
import ij.IJ;

/*
 * Allow to select current image as the image to process
 */
public class CurrentImageAction implements ActionListener {

	private SegPombeMainDialog mainDialog;
	private SegPombeParameters parameters;
	
	public CurrentImageAction(SegPombeMainDialog mainDialog, SegPombeParameters parameters) {
		this.mainDialog = mainDialog;
		this.parameters = parameters;
	}

	public void actionPerformed(ActionEvent arg0) {
		mainDialog.getAlreadryOpenedImage();
		mainDialog.resetFileNameField();
		mainDialog.setFileNameField(IJ.getImage().getTitle());
	}
}
