import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Opens MaarsMitosisMovieDialog when user click on "Parameters" button
 * @author marie
 *
 */
public class OpenMitosisMovieParamDialog implements ActionListener {
	
	private AllMaarsParameters parameters;
	
	/**
	 * Constructor :
	 * @param parameters : defaults parameters displayed in MaarsMitosisMovieDialog
	 */
	public OpenMitosisMovieParamDialog(AllMaarsParameters parameters) {
		this.parameters = parameters;
	}
	
	/**
	 * Create and display dialog
	 */
	public void actionPerformed(ActionEvent e) {
		System.out.println("action create MaarsMitosisMovieDialog");
		MaarsMitosisMovieDialog maarsMMD = new MaarsMitosisMovieDialog(parameters);
		System.out.println(" show MaarsMitosisMovieDialog");
		maarsMMD.show();
		System.out.println("Done");
	}
}
