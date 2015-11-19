package acquisition;

import mmcorej.CMMCore;

import org.micromanager.data.Datastore;
import org.micromanager.internal.MMStudio;

import fiji.plugin.maars.maarslib.MaarsParameters;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class FluoAcquisition implements MaarsAcquisition {

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
	public FluoAcquisition(MMStudio mm, CMMCore mmc,
			MaarsParameters parameters, double positionX, double positionY) {
		this.mm = mm;
		this.mmc = mmc;
		this.parameters = parameters;
		this.positionX = positionX;
		this.positionY = positionY;
	}

	@Override
	public void setShutter(String shutter) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setChColor(String color) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setChExposure(String exposure) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAcqRange(double range) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setAcqStep(double step) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Datastore createDataStore(String pathToMovie) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Datastore setDatastoreMetadata() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void cleanUp() {
		try {
			mmc.clearROI();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mm.getDisplayManager().closeAllDisplayWindows(false);
	}

	@Override
	public void acquire() {
		this.cleanUp();
		
	}


}
