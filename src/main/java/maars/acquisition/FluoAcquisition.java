//package maars.acquisition;
//
//import mmcorej.CMMCore;
//
//import org.micromanager.data.Datastore;
//import org.micromanager.data.DatastoreFrozenException;
//import org.micromanager.data.Image;
//import org.micromanager.internal.MMStudio;
//import org.micromanager.internal.utils.ReportingUtils;
//
//import fiji.plugin.maars.maarslib.MaarsParameters;
//
///**
// * @author Tong LI, mail:tongli.bioinfo@gmail.com
// * @version Nov 19, 2015
// */
//public class FluoAcquisition extends SuperClassAcquisition {
//
//	private MMStudio mm;
//	private CMMCore mmc;
//	private double range;
//	private double step;
//	private int sliceNumber;
//
//	/**
//	 * Constructor :
//	 * 
//	 * @param mm
//	 *            : graphical user interface of Micro-Manager
//	 * @param mmc
//	 *            : Core object of Micro-Manager
//	 * @param parameters
//	 *            : parameters used for algorithm
//	 * @param positionX
//	 *            : x field position (can be defined by ExplorationXYPositions)
//	 * @param positionY
//	 *            : y field position (can be defined by ExplorationXYPositions)
//	 * @param frame
//	 *            :current frame
//	 * @param channel
//	 *            : current channel
//	 */
//	public FluoAcquisition(MMStudio mm, CMMCore mmc,
//			MaarsParameters parameters, double positionX, double positionY, int frame, String channel) {
//		super(mm, mmc, parameters, positionX, positionY, frame, channel);
//		mm.
//	}
//
//	public void acquire() {
//		super.cleanUp();
//		super.setShutter();
//		super.setChExposure();
//		Datastore ds = super.createDataStore();
//		super.setDatastoreMetadata(ds);
//		try {
//			mmc.setShutterOpen(true);
//			mmc.waitForSystem();
//		} catch (Exception e) {
//			ReportingUtils.logMessage("Can not open shutter");
//			e.printStackTrace();
//		}
//		double zFocus = 0;
//		try {
//			zFocus = mmc.getPosition(mmc.getFocusDevice());
//		} catch (Exception e) {
//			ReportingUtils.logMessage("could not get z current position");
//			e.printStackTrace();
//		}
//		ReportingUtils.logMessage("-> z focus is " + zFocus);
//		ReportingUtils.logMessage("... start acquisition");
//		/*
//		 * important to add 2 µm. Depends on different microscopes. in our case,
//		 * the z-focus position is not in the middle of z range. It is often
//		 * lower than the real medium plan. So we add 2. This parameter needs to
//		 * be defined by testing on your own microscope.
//		 */
//		double z = zFocus - (range / 2) + 2;
//		for (int k = 0; k <= sliceNumber; k++) {
//			ReportingUtils.logMessage("- set focus device at position " + z);
//			try {
//				mmc.setPosition(mmc.getFocusDevice(), z);
//				// mmc.waitForDevice(mmc.getFocusDevice());
//			} catch (Exception e) {
//				ReportingUtils.logMessage("could not set focus device at position");
//			}
//			mm.getSnapLiveManager().snap(true);
//			z = z + step;
//		}
//		ReportingUtils.logMessage("--- Acquisition done.");
//		for (Image img : mm.getSnapLiveManager().snap(false)) {
//			try {
//				ds.putImage(img);
//			} catch (IllegalArgumentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (DatastoreFrozenException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		ds.freeze();
//		ds.close();
//		mm.getDisplayManager().closeAllDisplayWindows(false);
//		try {
//			mmc.setPosition(mmc.getFocusDevice(), zFocus);
//			mmc.setShutterOpen(false);
//		} catch (Exception e) {
//			ReportingUtils.logMessage("could not set focus device back to position and close shutter");
//			e.printStackTrace();
//		}
//		
//	}
//}
