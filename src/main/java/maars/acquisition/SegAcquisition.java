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
public class SegAcquisition extends SuperClassAcquisition {

//	private MMStudio mm;
//	private CMMCore mmc;;
//	private double range;
//	private double step;
//	private int sliceNumber;

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
	public SegAcquisition(MMStudio mm, CMMCore mmc, MaarsParameters parameters, double positionX, double positionY) {
		super(mm, mmc, parameters, positionX, positionY);
//		this.range = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.RANGE_SIZE_FOR_MOVIE));
//		this.step = Double.parseDouble(parameters.getFluoParameter(MaarsParameters.STEP));
//		this.sliceNumber = (int) Math.round(range / step);
	}
	
	public ImagePlus acquire(String channelName){
		return super.acquire(0, channelName);
	}
}
