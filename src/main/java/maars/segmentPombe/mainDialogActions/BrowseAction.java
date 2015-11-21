package maars.segmentPombe.mainDialogActions;

import ij.ImagePlus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import maars.segmentPombe.SegPombeMainDialog;
import maars.segmentPombe.SegPombeParameters;
import loci.formats.FormatException;
import loci.plugins.LociImporter;
import loci.plugins.in.DisplayHandler;
import loci.plugins.in.ImagePlusReader;
import loci.plugins.in.ImportProcess;
import loci.plugins.in.Importer;
import loci.plugins.in.ImporterOptions;

/**
 * 
 * @author Tong LI
 *
 */

public class BrowseAction implements ActionListener {

	private LociImporter lociImpt;
	private Importer impt;
	private ImportProcess imptProcess = null;
	private ImagePlus imgPlus = null;
	private SegPombeMainDialog mainDialog;
	private SegPombeParameters parameters;

	/**
	 * 
	 * @param cB
	 */
	public BrowseAction(SegPombeMainDialog mainDialog, SegPombeParameters parameters) {
		this.mainDialog = mainDialog;
		this.parameters = parameters;
	}

	/**
	 * 
	 */
	public void actionPerformed(ActionEvent e) {
		// create importer with loci library
		lociImpt = new LociImporter();
		impt = new Importer(lociImpt);

		// Create import process, its is empty for now but will get all
		// informations about movie and importation
		try {
			imptProcess = new ImportProcess();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		// Show dialog so the user can import/load the movie of his choice,
		// process get informations needed
		try {
			impt.showDialogs(imptProcess);
		} catch (FormatException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}

		/*
		 * Get the set of images (as ImagePlus array) corresponding to the
		 * loaded movie
		 * 
		 * TODO : if the movie include other dimension than just z, modify
		 * Importer Options to get only z images
		 */
		// create image reader and all tools needed to get the set of images
		ImagePlusReader imPlusReader = new ImagePlusReader(imptProcess);
		ImporterOptions imptOptions = imptProcess.getOptions();
		DisplayHandler dispHandler = new DisplayHandler(imptProcess);

		// get set of images

		try {
			imgPlus = impt.readPixels(imPlusReader, imptOptions, dispHandler)[0];
		} catch (FormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		parameters.setImageToAnalyze(imgPlus);
		mainDialog.resetFileNameField();
		mainDialog.setFileNameField(imptProcess.getIdName());
		mainDialog.setPathDirField(imgPlus.getOriginalFileInfo().directory);

	}

}
