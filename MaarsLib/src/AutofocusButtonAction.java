import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ActionListener that open autofocus Dialog
 * @author marie
 *
 */
public class AutofocusButtonAction  implements ActionListener {
	
	private MaarsMainDialog maarsD;
	
	/**
	 * Constructor use object MaarsMainDialog
	 * @param maarsD
	 */
	public AutofocusButtonAction (MaarsMainDialog maarsD) {
		this.maarsD = maarsD;
	}
	public void actionPerformed(ActionEvent e) {
		maarsD.getGui().showAutofocusDialog();
	}
}
