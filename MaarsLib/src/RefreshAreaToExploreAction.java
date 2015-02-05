import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Action performed when user click on "Refresh" button
 * @author marie
 *
 */
public class RefreshAreaToExploreAction implements ActionListener {
	
	private MaarsMainDialog maarsMD;
	
	/**
	 * Constructor : 
	 * @param maarsMD : dialog containing "Refresh" button
	 */
	public RefreshAreaToExploreAction(MaarsMainDialog maarsMD) {
		this.maarsMD = maarsMD;
	}
	
	/**
	 * Update number of field scanned by the program
	 */
	public void actionPerformed(ActionEvent e) {
		maarsMD.refreshNumField();
	}
}
