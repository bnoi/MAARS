package fiji.plugin.maars.maarslib;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 10, 2015
 */

import java.awt.Color;
import java.io.FileWriter;
import java.io.IOException;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/*
 * MaarsParameters reads a configuration file written as a JsonObject,
 * then allows to access values thanks to all constants defined.
 * 
 * SEGMENTATION_PARAMETERS
 *    |
 *    +-----> CHANNEL
 *    +-----> NEW_MAX_WIDTH_FOR_CHANGE_SCALE
 *    +-----> NEW_MAX_HEIGTH_FOR_CHANGE_SCALE
 *    +-----> FRAME_NUMBER
 *    +-----> RANGE_SIZE_FOR_MOVIE
 *    +-----> STEP
 *    +-----> CELL_SIZE
 *    +-----> MINIMUM_CELL_AREA
 *    +-----> MEAN_GREY_VALUE
 *    +-----> SOLIDITY
 *    +-----> FILTER_MEAN_GREY_VALUE
 *    +-----> FILTER_SOLIDITY
 *    +-----> MAXIMUM_CELL_AREA
 *    
 * EXPLORATION_PARAMETERS
 *    |
 *    +-----> X_FIELD_NUMBER
 *    +-----> Y_FIELD_NUMBER
 *   
 * FLUO_ANALYSIS_PARAMETERS
 *    |
 *    +-----> FRAME_NUMBER
 *    +-----> RANGE_SIZE_FOR_MOVIE
 *    +-----> STEP
 *    +-----> SAVE_FLUORESCENT_MOVIES
 *    +-----> FLUO_CHANNELS
 *    		|
 *    		+----->USING
 *    				|
 *    				+----->channel name
 *    		+----->channel name
 *    				|
 *    				+-----> SPOT_RADIUS
 *    				+-----> MAXIMUM_NUMBER_OF_SPOT
 *    +-----> DYNAMIC
 *    +-----> TIME_LIMIT
 *    +-----> TIME_INTERVAL
 *    
 * GENERAL_ACQUISITION_PARAMETERS
 *    |
 *    +-----> SAVING_PATH
 *    +-----> CHANNEL_GROUP
 *    +-----> DEFAULT_CHANNEL_PARAMATERS
 * 					|
 * 					+-----> channel name
 * 								|
 * 								+-----> COLOR
 * 								+-----> EXPOSURE
 * 								+-----> SHUTTER
 * @author Tong LI
 *
 */
public class MaarsParameters {

	private String defaultParametersFile;
	Document doc;
	Element root;

	public static final String SEGMENTATION_PARAMETERS = "SEGMENTATION_PARAMETERS";
	public static final String FLUO_ANALYSIS_PARAMETERS = "FLUO_ANALYSIS_PARAMETERS";
	public static final String EXPLORATION_PARAMETERS = "EXPLORATION_PARAMETERS";

	public static final String RANGE_SIZE_FOR_MOVIE = "RANGE_SIZE_FOR_MOVIE";
	public static final String STEP = "STEP";
	public static final String SPOT_RADIUS = "SPOT_RADIUS";
	public static final String SAVE_FLUORESCENT_MOVIES = "SAVE_FLUORESCENT_MOVIES";
	public static final String MAXIMUM_NUMBER_OF_SPOT = "MAXIMUM_NUMBER_OF_SPOT";
	public static final String DYNAMIC = "DYNAMIC";

	public static final String X_FIELD_NUMBER = "X_FIELD_NUMBER";
	public static final String Y_FIELD_NUMBER = "Y_FIELD_NUMBER";

	public static final String CELL_SIZE = "CELL_SIZE";
	public static final String MINIMUM_CELL_AREA = "MINIMUM_CELL_AREA";
	public static final String MAXIMUM_CELL_AREA = "MAXIMUM_CELL_AREA";
	public static final String FILTER_MEAN_GREY_VALUE = "FILTER_MEAN_GREY_VALUE";
	public static final String MEAN_GREY_VALUE = "MEAN_GREY_VALUE";
	public static final String FILTER_SOLIDITY = "FILTER_SOLIDITY";
	public static final String SOLIDITY = "SOLIDITY";
	public static final String NEW_MAX_WIDTH_FOR_CHANGE_SCALE = "NEW_MAX_WIDTH_FOR_CHANGE_SCALE";
	public static final String NEW_MAX_HEIGTH_FOR_CHANGE_SCALE = "NEW_MAX_HEIGTH_FOR_CHANGE_SCALE";

	public static final String TIME_INTERVAL = "TIME_INTERVAL";
	public static final String TIME_LIMIT = "TIME_LIMIT";
	public static final String SAVING_PATH = "SAVING_PATH";

	public static final String SHUTTER = "SHUTTER";
	public static final String COLOR = "COLOR";
	public static final String EXPOSURE = "EXPOSURE";
	public static final String CHANNEL = "CHANNEL";
	public static final String FLUO_CHANNELS = "FLUO_CHANNELS";
	public static final String USING = "USING";
	public static final String GFP = "GFP";
	public static final String CFP = "CFP";
	public static final String TXRED = "TXRED";
	public static final String DAPI = "DAPI";
	public static final String FRAME_NUMBER = "FRAME_NUMBER";

	public static final String GENERAL_ACQUISITION_PARAMETERS = "GENERAL_ACQUISITION_PARAMETERS";

	public static final String DEFAULT_CHANNEL_PARAMATERS = "DEFAULT_CHANNEL_PARAMATERS";

	public static final String CHANNEL_GROUP = "CHANNEL_GROUP";

	/**
	 * Constructor of Element need path to configuration file
	 * 
	 * @param defaultParametersFile
	 * @throws IOException
	 */
	public MaarsParameters(String defaultParametersFile) {

		this.defaultParametersFile = defaultParametersFile;
		final SAXBuilder sb = new SAXBuilder();
		try {
			doc = sb.build(defaultParametersFile);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		root = (Element) doc.getContent(0);
	}

	/**
	 * Return Element containing all parameters
	 * 
	 * @return Element
	 */
	public Element getRootElement() {
		return root;
	}

	/**
	 * Write the parameters into the configuration file
	 * 
	 * @throws IOException
	 */
	public void save() throws IOException {
		doc.setContent(root);
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());
		xmlOutput.output(doc, new FileWriter(defaultParametersFile));
	}

	/**
	 * The few following colors are return as Color object : GREEN, CYAN, RED,
	 * BLUE, WHITE NB : return GRAY if unknown color
	 * 
	 * @param colorName
	 * @return Color
	 */
	public static Color getColor(String colorName) {
		if (colorName.equals("GREEN")) {
			return Color.GREEN;
		} else {
			if (colorName.equals("CYAN")) {
				return Color.CYAN;
			} else {
				if (colorName.equals("RED")) {
					return Color.RED;
				} else {
					if (colorName.equals("BLUE")) {
						return Color.BLUE;
					} else {
						if (colorName.equals("WHITE")) {
							return Color.WHITE;
						} else {
							return Color.GRAY;
						}
					}
				}
			}
		}
	}

	// Getter
	/**
	 * 
	 * @param xOrY
	 *            : is a final static string in MaarsParameters
	 * @return : the value of X or Y field
	 */
	public int getFieldNb(final String xOrY) {
		return Integer.parseInt(root.getChild(EXPLORATION_PARAMETERS)
				.getChildText(xOrY));
	}
	
	/**
	 * 
	 * @return analysis with dynamic or not
	 */
	public boolean useDynamic(){
		return Boolean.parseBoolean(root
				.getChild(FLUO_ANALYSIS_PARAMETERS)
				.getChildText(DYNAMIC));
	}

	/**
	 * 
	 * @return saving folder of MAARS output
	 */
	public String getSavingPath(){
		return root.getChild(GENERAL_ACQUISITION_PARAMETERS)
				.getChildText(SAVING_PATH);
	}
	
	/**
	 * 
	 * @return time limit of fluorescence acquisition for one acquisition
	 */
	public String getFluoParameter(final String parameter){
		return root
				.getChild(FLUO_ANALYSIS_PARAMETERS)
				.getChildText(parameter);
	}
	
	/**
	 * 
	 * @return time limit of fluorescence acquisition for one acquisition
	 */
	public String getSegmentationParameter(final String parameter){
		return root
				.getChild(SEGMENTATION_PARAMETERS)
				.getChildText(parameter);
	}
	
	/**
	 * 
	 * @return the name of channel group set in micromanager
	 */
	public String getChannelGroup(){
		return root.getChild(GENERAL_ACQUISITION_PARAMETERS)
				.getChildText(CHANNEL_GROUP);
	}
	
	/**
	 * 
	 * @param ch : GFP, CFP, DAPI, TXRED
	 * @return corresponding channel shutter
	 */
	public String getChShutter(String ch){
		return root.getChild(GENERAL_ACQUISITION_PARAMETERS)
		.getChild(DEFAULT_CHANNEL_PARAMATERS)
		.getChild(ch).getChildText(SHUTTER);
	}
	
	/**
	 * 
	 * @param ch : GFP, CFP, DAPI, TXRED
	 * @return corresponding channel color
	 */
	public String getChColor(String ch){
		return root.getChild(GENERAL_ACQUISITION_PARAMETERS)
		.getChild(DEFAULT_CHANNEL_PARAMATERS)
		.getChild(ch).getChildText(COLOR);
	}
	
	/**
	 * 
	 * @param ch : GFP, CFP, DAPI, TXRED
	 * @return corresponding channel color
	 */
	public String getChExposure(String ch){
		return root.getChild(GENERAL_ACQUISITION_PARAMETERS)
		.getChild(DEFAULT_CHANNEL_PARAMATERS)
		.getChild(ch).getChildText(EXPOSURE);
	}
	
	/**
	 * 
	 * @param ch: GFP, CFP, DAPI, TXRED
	 * @return 	MAXIMUM_NUMBER_OF_SPOT of corresponding channel
	 */
	public String getChMaxNbSpot(String ch){
		return root
				.getChild(FLUO_ANALYSIS_PARAMETERS).getChild(FLUO_CHANNELS).getChild(ch)
				.getChildText(MAXIMUM_NUMBER_OF_SPOT);
	}
	
	/**
	 * 
	 * @param ch: GFP, CFP, DAPI, TXRED
	 * @return 	SPOT_RADIUS of corresponding channel
	 */
	public String getChSpotRaius(String ch){
		return root
				.getChild(FLUO_ANALYSIS_PARAMETERS).getChild(FLUO_CHANNELS).getChild(ch)
				.getChildText(SPOT_RADIUS);
	}
	
	/**
	 * 
	 * @return get channels used for fluo analysis
	 */
	public String getUsingChannels(){
		return root.getChild(FLUO_ANALYSIS_PARAMETERS).getChild(FLUO_CHANNELS)
				.getChildText(USING);
	}
	
	////////////Setters
	/**
	 * update value of x or y field number of exloration
	 * @param xOrY
	 * @param value
	 */
	public void setFieldNb(final String xOrY, String value){
		root.getChild(EXPLORATION_PARAMETERS)
			.getChild(xOrY).setText(value);
	}
	
	/**
	 * set segmentation parameter
	 * 
	 * @param parameter
	 *            : static final String of MaarsParameters
	 * @param value
	 *            : corresponding value of parameter
	 */
	public void setSegmentationParameter(String parameter, String value) {
		root.getChild(SEGMENTATION_PARAMETERS).getChild(parameter)
				.setText(value);
	}

	/**
	 * set fluo analysis parameter
	 * 
	 * @param parameter
	 *            : static final String of MaarsParameters
	 * @param value
	 *            : corresponding value of parameter
	 */
	public void setFluoParameter(String parameter, String value) {
		root.getChild(FLUO_ANALYSIS_PARAMETERS).getChild(parameter)
				.setText(value);
	}

	/**
	 * set saving path
	 * 
	 * @param value
	 *            : corresponding value of parameter
	 */
	public void setSavingPath(String path) {
		root.getChild(GENERAL_ACQUISITION_PARAMETERS)
			.getChild(SAVING_PATH)
			.setText(path);
	}

	/**
	 * 
	 * @param ch: GFP, CFP, DAPI, TXRED
	 * @return 	MAXIMUM_NUMBER_OF_SPOT of corresponding channel
	 */
	public void setChMaxNbSpot(String ch, String maxNbSpot){
		root.getChild(FLUO_ANALYSIS_PARAMETERS).getChild(FLUO_CHANNELS)
		.getChild(ch)
		.getChild(MAXIMUM_NUMBER_OF_SPOT).setText(maxNbSpot);
	}
	
	/**
	 * 
	 * @param ch: GFP, CFP, DAPI, TXRED
	 * @return 	SPOT_RADIUS of corresponding channel
	 */
	public void setChSpotRaius(String ch, String spotRaidus){
		root.getChild(FLUO_ANALYSIS_PARAMETERS).getChild(FLUO_CHANNELS)
		.getChild(ch).getChild(SPOT_RADIUS)
		.setText(spotRaidus);
	}
	
	/**
	 * set channels to USING channel
	 * @param channels 
	 */
	public void setUsingChannels(String channels){
		root.getChild(FLUO_ANALYSIS_PARAMETERS).getChild(FLUO_CHANNELS)
				.getChild(USING).setText(channels);
	}

	
}