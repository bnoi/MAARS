package maars.segmentPombe.mainDialogActions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import maars.segmentPombe.SegPombeMainDialog;

public class CancelAction implements ActionListener {

	private SegPombeMainDialog mainDialog;

	public CancelAction(SegPombeMainDialog mainDialog) {
		this.mainDialog = mainDialog;
	}

	public void actionPerformed(ActionEvent e) {
		mainDialog.hideMainWindow();
	}

}
