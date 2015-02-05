import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Opens MaarsFluoAnalysisDialog when user click on "Fluorescent analysis" button
 * @author marie
 *
 */
public class OpenFluoAnalysisDialogAction implements ActionListener {
	
	private AllMaarsParameters parameters;
	
	/**
	 * Constructor :
	 * @param parameters : defaults parameters displayed in MaarsFluoAnalysisDialog
	 */
	public OpenFluoAnalysisDialogAction(AllMaarsParameters parameters) {
		this.parameters = parameters;
	}
	
	/**
	 * Create and display dialog
	 */
	public void actionPerformed(ActionEvent e) {
		MaarsFluoAnalysisDialog maarsFDA = new MaarsFluoAnalysisDialog(parameters);
		maarsFDA.show();
	}
}
