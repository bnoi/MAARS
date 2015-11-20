package fiji.plugin.maars;
import java.awt.event.ActionListener;
import java.util.HashMap;

import mmcorej.CMMCore;

import org.micromanager.MenuPlugin;
import org.micromanager.Studio;
import org.micromanager.internal.MMStudio;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

import fiji.plugin.maars.maarslib.MaarsAcquisitionForSegmentation;
import fiji.plugin.maars.maarslib.MaarsMainDialog;
import fiji.plugin.maars.maarslib.MaarsParameters;
import fiji.plugin.maars.maarslib.MaarsSegmentation;

/**
 *@author Tong LI, mail:tongli.bioinfo@gmail.com
 *@version Nov 20, 2015
 */
@Plugin(type = MenuPlugin.class)
public class MAARS implements MenuPlugin,SciJavaPlugin{
	private MMStudio mmStudio;
	private CMMCore mmc;
	private MaarsParameters parameters;
	public MAARS(MMStudio mm, CMMCore mmc, MaarsParameters parameters){
//		autofocus = mm.getAutofocus();
//
//		ExplorationXYPositions explo = new ExplorationXYPositions(mmc, parameters);
//		for (int i = 0; i < explo.length(); i++) {
//			print("x : "+explo.getX(i)+" y : "+explo.getY(i));
//			double xPos = explo.getX(i);
//			double yPos = explo.getY(i);
//			
//			mm.core().setXYPosition(xPos,yPos);
//			
//			MaarsAcquisitionForSegmentation mas = new MaarsAcquisitionForSegmentation(mm,mmc, parameters,
//					(double) 0,
//					(double) 0);
//			mmc.waitForDevice(mmc.getXYStageDevice());
//			autofocus.fullFocus();

//			HashMap params = mas.getParametersFromConf(parameters);
//			mas.setParameters(params);
//			//--------------------------BF acquisition-----------------------------//
//			mas.acquire(true);
			//--------------------------segmentation-----------------------------//
//			MaarsSegmentation ms = new MaarsSegmentation(parameters,
//				0,
//				0);
//			ms.segmentation();
//			if(ms.roiDetected()){
//			//----------------if got ROI, start fluo-acquisition --------//
//				MaarsFluoAnalysis mfa = new MaarsFluoAnalysis(param, ms.getSegPombeParam(), xPos, yPos);
//				MaarsAcquisitionForFluoAnalysis mafa = new MaarsAcquisitionForFluoAnalysis(md,
//																xPos,
//																yPos,
//																mfa.getSetOfCells());
//	//////////////////////////multiple snapshot per field//////////////////////////////
//				if(param.useDynamic()) {
//					double timeInterval = 
//						Double.parseDouble(param.getFluoParameter(MaarsParameters.TIME_INTERVAL));
//					double startTime = System.currentTimeMillis();
//					int frame = 0;
//					double timeLimit = 
//						Double.parseDouble(param.getFluoParameter(MaarsParameters.TIME_LIMIT))
//						* 60 * 1000;
//					while (System.currentTimeMillis() - startTime <= timeLimit)
//						{
//							double beginAcq = System.currentTimeMillis();
//
//							String channels = param.getUsingChannels();
//							String[] arrayChannels = channels.split(",", -1);
//							for (String channel:arrayChannels) {
//								ImagePlus fluoImage = mafa.acquire(true, frame, channel);
//								new FluoAnalyzer(mfa, fluoImage, channel, frame).start();
//							}
//							mfa.getSetOfCells().closeRoiManager();
//							frame++;
//							double acqTook = System.currentTimeMillis() - beginAcq;
//							Thread.sleep((long) (timeInterval - acqTook));	
//							acqNameFluo = null;
//							fluoImage = null;	
//						}
//	////////////////////////////one snapshot per field/////////////////////////////////																			
//				}else {
//					int frame = 0;
//					String channels = param.getUsingChannels();
//					String[] arrayChannels = channels.split(",", -1);
//					for (String channel:arrayChannels) {
//						ImagePlus fluoImage = mafa.acquire(true, frame, channel);
//						new FluoAnalyzer(mfa, fluoImage, channel, frame).start();
//					}
//					mfa.getSetOfCells().closeRoiManager();
//					acqNameFluo = null;
//					fluoImage = null;
//				}
//	///////////////////////////save cropped images//////////////////////////////////////
//				if (Boolean.parseBoolean(param.getFluoParameter(
//					MaarsParameters.SAVE_FLUORESCENT_MOVIES))) {
//					mfa.saveCroppedImgs();
//				}
//				// close roi manager
//				// mfa.getSetOfCells().closeRoiManager();
//				mas.setParameters(params);
//			}
//		}
//		mmc.setAutoShutter(true);
//		mmc.waitForDevice(mmc.getShutterDevice());
//		print("end "+System.currentTimeMillis());
//		print("it took "+(System.currentTimeMillis()-start));
//		print("DONE.");
//	}else{
//		print("Session aborted, click 'OK' to start analyse.");
//	}
	}

	@Override
	public String getCopyright() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHelpText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "MAARS";
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "1.0";
	}

	@Override
	public void setContext(Studio mmStudio) {
		// TODO Auto-generated method stub
		this.mmStudio = (MMStudio) mmStudio;
		this.mmc = mmStudio.core();
		String base_dir = "/home/tong/Documents/code/MAARS/";
		parameters = new MaarsParameters(base_dir + "maars_config.xml");
		
	}

	@Override
	public String getSubMenu() {
		return null;
	}

	@Override
	public void onPluginSelected() {
		new MaarsMainDialog(mmStudio, mmc, parameters).show();
	}
}
