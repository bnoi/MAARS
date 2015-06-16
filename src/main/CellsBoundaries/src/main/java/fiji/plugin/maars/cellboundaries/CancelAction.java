package fiji.plugin.maars.cellboundaries;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CancelAction implements ActionListener {

	private CellsBoundaries cB;

	public CancelAction(CellsBoundaries cB) {
		this.cB = cB;
	}

	public void actionPerformed(ActionEvent e) {
		cB.hideMainWindow();
	}

}
