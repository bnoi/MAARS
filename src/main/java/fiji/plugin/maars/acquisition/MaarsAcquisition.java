package fiji.plugin.maars.acquisition;

import org.micromanager.data.Datastore;

/**
 * @author Tong LI, mail:tongli.bioinfo@gmail.com
 * @version Nov 19, 2015
 */
public interface MaarsAcquisition {

	/**
	 * Set shutter used for current acquisition
	 * 
	 * @param shutter
	 */
	public void setShutter(String shutter);

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
