package fiji.plugin.maars.cellboundaries;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/*
 * Allow to select current image as the image to process
 */
public class CurrentImageAction implements ActionListener {

	private CellsBoundaries cB;
	private CBParameters parameters;
	
	public CurrentImageAction(CellsBoundaries cB, CBParameters parameters) {
		this.cB = cB;
		this.parameters = parameters;
	}

	public void actionPerformed(ActionEvent arg0) {
		cB.getAlreadryOpenedImage();
		cB.resetFileNameField();
		cB.setFileNameField(parameters.getImageToAnalyze().getTitle());
	}
}
