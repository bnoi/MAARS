package maars.acquisition;

import mmcorej.CMMCore;

import org.micromanager.data.Datastore;
import org.micromanager.data.DatastoreFrozenException;
import org.micromanager.data.Image;
import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.ReportingUtils;

import ij.ImagePlus;
import maars.MaarsParameters;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class FluoAcquisition extends SuperClassAcquisition {

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
	 * @param frame
	 *            :current frame
	 * @param channel
	 *            : current channel
	 */
	public FluoAcquisition(MMStudio mm, CMMCore mmc,
			MaarsParameters parameters, double positionX, double positionY) {
		super(mm, mmc, parameters, positionX, positionY);
	}

	public ImagePlus acquire(int frame, String channelName){
		return super.acquire(frame, channelName);
	}
}
