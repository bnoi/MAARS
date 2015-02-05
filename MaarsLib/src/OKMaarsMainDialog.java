import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Action performed when user clicks on "ok" button of MaarsMainDialog
 * @author marie
 *
 */
public class OKMaarsMainDialog implements ActionListener{
	
	private MaarsMainDialog maarsMD;
	
	/**
	 * Constructor :
	 * @param maarsMD : dialog containing button clicked
	 */
	public OKMaarsMainDialog(MaarsMainDialog maarsMD) {
		this.maarsMD = maarsMD;
	}
	
	/**
	 * - update and save parameters changed
	 * - remember that user has clicked on "ok"
	 * - hide dialog
	 */
	public void actionPerformed(ActionEvent e) {
		maarsMD.refreshNumField();
		maarsMD.saveParameters();
		maarsMD.setOkClicked(true);
		maarsMD.hide();
	}
}
