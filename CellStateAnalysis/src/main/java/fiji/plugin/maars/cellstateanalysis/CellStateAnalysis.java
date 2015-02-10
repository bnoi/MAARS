package fiji.plugin.maars.cellstateanalysis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import loci.formats.in.FluoviewReader;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.PlugIn;

public class CellStateAnalysis implements PlugIn {
	
	public void run(String arg) {
//		ImagePlus image = IJ.getImage().duplicate();
//		ImagePlus image = IJ.openImage("/run/media/marie/data_marie_1/data/bf_gfp/filmtest/movie_X-0.0_Y-0.0/rescaled_DUP_MMStack.omeFocusImage.tif");
//		String path = IJ.getImage().getOriginalFileInfo().directory;
//		String path = "/run/media/marie/data_marie_1/data/bf_gfp/filmtest/movie_X-0.0_Y-0.0/";
//		ImagePlus correlationImagePlus = IJ.openImage(path+IJ.getImage().getShortTitle()+"CorrelationImage.tif");
//		ImagePlus correlationImagePlus = IJ.openImage(path+"rescaled_DUP_MMStack.omeCorrelationImage.tif");
		/*
		FileWriter resWriter = null;
		try {
			resWriter = new FileWriter(path+"septNumber2.csv");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			resWriter.append("Cell/thresh");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		for (double thresh = 1.5; thresh > 0.9; thresh = thresh - 0.05) {
			try {
				resWriter.append(","+thresh);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			resWriter.append("\n");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		
		//ImagePlus fluoImg = IJ.openImage("/run/media/marie/data_marie_1/data/bf_gfp/movieGFP2_1/movieGFP2_1_MMStack.ome.tif");
		
		/*
		SetOfCells soc = new SetOfCells(image,
				correlationImagePlus,
				//fluoImg,
				1,//15,
				-1,
				path+"rescaled_DUP_MMStack.omeROI.zip",//path+IJ.getImage().getShortTitle()+"ROI.zip",
				path);
		
		
		ImagePlus fluoImg1 = IJ.openImage("/run/media/marie/data_marie_1/data/bf_gfp/filmtest/movie_X-0.0_Y-0.0GFP13/MMStack.ome.tif");
		
		soc.getCell(12).addFluoImage(fluoImg1);
		Spindle sp1 = soc.getCell(12).findFluoSpotTempFunction(false);
		System.out.println(sp1.getFeature());
		sp1.testFunction(fluoImg1).show();
		

		
		ImagePlus fluoImg2 = IJ.openImage("/run/media/marie/data_marie_1/data/bf_gfp/filmtest/movie_X-0.0_Y-0.0GFP13GFP20/MMStack.ome.tif");
		soc.getCell(19).addFluoImage(fluoImg2);
		Spindle sp2 = soc.getCell(19).findFluoSpotTempFunction(false);
		System.out.println(sp2.getFeature());
		sp2.testFunction(fluoImg2).show();//*/
		//correlationImagePlus.show();
		
		//fluoImg.show();
		//IJ.wait(5000);
		
		//soc.getCell(24).setFluoImage(fluoImg);
		/*soc.getCell(24).findFluoSpotTempFunction(true);
		IJ.wait(5000);
		soc.getCell(25).findFluoSpotTempFunction(true);
		IJ.wait(5000);
		soc.getCell(4).findFluoSpotTempFunction(true);
		IJ.wait(5000);
		*/
		/*
		ArrayList<Spindle> spindleList = new ArrayList<Spindle>();
		
		soc.shuffle();
		
		for(int i = 0; i < soc.length(); i++) {
			System.out.println(soc.getCell(i).getCellShapeRoi().getName());
			Spindle spindle = soc.getCell(i).findFluoSpotTempFunction(true);
			if (spindle.getFeature().equals("SPINDLE")) {
				spindleList.add(spindle);
			}
		}
		
		double width = 100;
		double heigth = 200;
		Calibration cal = new Calibration();
		cal.setUnit("micron");
		cal.pixelHeight = 20.0/heigth;
		cal.pixelWidth = 5.0/width;
		ImageStack imgStk = new ImageStack((int)width, (int)heigth);
		Iterator<Spindle> itr = spindleList.iterator();
		
		while (itr.hasNext()) {
			Spindle spindle = itr.next();
			imgStk.addSlice(spindle.getProcessorReferencedSpindle((int)width, (int)heigth, cal, false, false));
		}
		ImagePlus im = new ImagePlus("test",imgStk);
		im.setCalibration(cal);
		im.show();*/
		
		
	}
}
