package acquisition;

import mmcorej.CMMCore;

import org.micromanager.data.Datastore;
import org.micromanager.internal.MMStudio;

import fiji.plugin.maars.maarslib.MaarsParameters;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class SegAcquisition extends SuperClassAcquisition {

	private MMStudio mm;
	private CMMCore mmc;
	private MaarsParameters parameters;
	private double positionX;
	private double positionY;

	/**
	 * Constructor :
	 *
	 * @param mm
	 *            : graphical user interface of Micro-Manager
	 * @param mmc
	 *            : Core object of Micro-Manager
	 * @param parameters
	 *            : parameters used for algorithm
	 * @param positionX
	 *            : x field position (can be defined by ExplorationXYPositions)
	 * @param positionY
	 *            : y field position (can be defined by ExplorationXYPositions)
	 */
	public SegAcquisition(MMStudio mm, CMMCore mmc, MaarsParameters parameters,
			double positionX, double positionY) {
		super(mm, mmc, parameters, positionX, positionY);
	}
}
