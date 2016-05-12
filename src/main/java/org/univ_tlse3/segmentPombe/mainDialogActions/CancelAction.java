package org.univ_tlse3.segmentPombe.mainDialogActions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.univ_tlse3.segmentPombe.SegPombeMainDialog;

public class CancelAction implements ActionListener {

	private SegPombeMainDialog mainDialog;

	public CancelAction(SegPombeMainDialog mainDialog) {
		this.mainDialog = mainDialog;
	}

	public void actionPerformed(ActionEvent e) {
		mainDialog.hideMainWindow();
	}

}
