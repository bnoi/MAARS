package org.micromanager.segmentPombe.mainDialogActions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.micromanager.segmentPombe.SegPombeMainDialog;

public class CancelAction implements ActionListener {

	private SegPombeMainDialog mainDialog;

	public CancelAction(SegPombeMainDialog mainDialog) {
		this.mainDialog = mainDialog;
	}

	public void actionPerformed(ActionEvent e) {
		mainDialog.hideMainWindow();
	}

}
