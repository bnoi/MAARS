package org.micromanager.segmentPombe.mainDialogActions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.micromanager.segmentPombe.SegPombeMainDialog;

import ij.IJ;

/*
 * Allow to select current image as the image to process
 */
public class CurrentImageAction implements ActionListener {

	private SegPombeMainDialog mainDialog;
	
	public CurrentImageAction(SegPombeMainDialog mainDialog) {
		this.mainDialog = mainDialog;
	}

	public void actionPerformed(ActionEvent arg0) {
		mainDialog.getAlreadryOpenedImage();
		mainDialog.resetFileNameField();
		mainDialog.setFileNameField(IJ.getImage().getTitle());
	}
}
