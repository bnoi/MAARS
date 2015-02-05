import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Opens MaarsSegmentationDialog when user click on "Segmentation" button
 * @author marie
 *
 */
public class OpenSegmentationDialogButtonAction implements ActionListener {
	
	private AllMaarsParameters parameters;
	
	/**
	 * Constructor :
	 * @param parameters : defaults parameters displayed in MaarsSegmentationDialog
	 */
	public OpenSegmentationDialogButtonAction(AllMaarsParameters parameters) {
		this.parameters = parameters;
	}

	/**
	 * Create and display dialog
	 */
	public void actionPerformed(ActionEvent e) {
		MaarsSegmentationDialog maarsSD = new MaarsSegmentationDialog(parameters);
		maarsSD.show();
	}
}
