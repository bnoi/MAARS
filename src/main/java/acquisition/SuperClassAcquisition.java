package acquisition;

import mmcorej.CMMCore;

import org.micromanager.data.Datastore;
import org.micromanager.internal.MMStudio;

import fiji.plugin.maars.maarslib.MaarsParameters;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public class SuperClassAcquisition {

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
	public SuperClassAcquisition(MMStudio mm, CMMCore mmc,
			MaarsParameters parameters, double positionX, double positionY) {
		this.mm = mm;
		this.mmc = mmc;
		this.parameters = parameters;
		this.positionX = positionX;
		this.positionY = positionY;
	}

	/**
	 * Set shutter used for current acquisition
	 * 
	 * @param shutter
	 */
	public void setShutter(){
		try {
			mmc.setShutterDevice();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * set visualization color while acquisition
	 * 
	 * @param color
	 */
	public void setChColor(String color);

	/**
	 * set channel exposure while acquisition
	 * 
	 * @param exposure
	 */
	public void setChExposure(String exposure);

	/**
	 * Set range of acquisition
	 * 
	 * @param range
	 */
	public void setAcqRange(double range);

	/**
	 * Set step of acquisition
	 * 
	 * @param step
	 */
	public void setAcqStep(double step);

	/**
	 * create a Datastore (a .tiff file with metadata in most of the case)
	 * 
	 * @param pathToMovie
	 * @return
	 */
	public Datastore createDataStore(String pathToMovie);

	/**
	 * Set DataStore metadata by using parameters in class
	 */
	public Datastore setDatastoreMetadata();

	/**
	 * clean up (ex. clear ROI selected, close previously opened display
	 * windows)
	 */
	public void cleanUp();

	/**
	 * Do acquisition
	 */
	public void acquire();
}
