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
	public MaarsParameters(String defaultParametersFile) throws IOException {

		this.defaultParametersFile = defaultParametersFile;
		final SAXBuilder sb = new SAXBuilder();
		try {
			doc = sb.build(defaultParametersFile);
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		root =  (Element) doc.getContent(0);
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

	/**
	 * update segmentation parameter
	 * 
	 * @param parameter
	 *            : static final String of MaarsParameters
	 * @param value
	 *            : corresponding value of parameter
	 */
	static public void updateSegmentationParameter(Element rootElement, String parameter, String value) {
		rootElement.getChild(MaarsParameters.SEGMENTATION_PARAMETERS)
			.removeChild(parameter);
		rootElement.getChild(MaarsParameters.SEGMENTATION_PARAMETERS)
			.addContent(new Element(parameter).setText(value));
	}

	/**
	 * update fluo analysis parameter
	 * 
	 * @param parameter
	 *            : static final String of MaarsParameters
	 * @param value
	 *            : corresponding value of parameter
	 */
	static public void updateFluoParameter(Element rootElement, String parameter, String value) {
		rootElement.getChild(MaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.removeChild(parameter);
		rootElement.getChild(MaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.addContent(new Element(parameter).setText(value));
	}

	/**
	 * update general parameters
	 * 
	 * @param paramObject
	 *            : current MaarsParameters object
	 * @param parameter
	 *            : static final String of MaarsParameters
	 * @param value
	 *            : corresponding value of parameter
	 */
	static public void updateGeneralParameter(MaarsParameters paramObject, String parameter, String value) {
		paramObject.getParametersAsJsonObject().get(MaarsParameters.GENERAL_ACQUISITION_PARAMETERS).getAsJsonObject()
				.remove(parameter);

		paramObject.getParametersAsJsonObject().get(MaarsParameters.GENERAL_ACQUISITION_PARAMETERS).getAsJsonObject()
				.addProperty(parameter, value);
	}

	public static void updateFluoChannel(MaarsParameters paramObject, String channelName, int maxNbSpot,
			double spotRaius) {
		JsonObject channelObj = paramObject.getParametersAsJsonObject().get(MaarsParameters.FLUO_ANALYSIS_PARAMETERS)
				.getAsJsonObject().get(MaarsParameters.FLUO_CHANNELS).getAsJsonObject().get(channelName)
				.getAsJsonObject();
		channelObj.remove(MaarsParameters.SPOT_RADIUS);
		channelObj.remove(MaarsParameters.MAXIMUM_NUMBER_OF_SPOT);
		channelObj.addProperty(MaarsParameters.SPOT_RADIUS, spotRaius);
		channelObj.addProperty(MaarsParameters.MAXIMUM_NUMBER_OF_SPOT, maxNbSpot);
		paramObject.getParametersAsJsonObject().get(MaarsParameters.FLUO_ANALYSIS_PARAMETERS).getAsJsonObject()
				.get(MaarsParameters.FLUO_CHANNELS).getAsJsonObject().remove(channelName);
		paramObject.getParametersAsJsonObject().get(MaarsParameters.FLUO_ANALYSIS_PARAMETERS).getAsJsonObject()
				.get(MaarsParameters.FLUO_CHANNELS).getAsJsonObject().add(channelName, channelObj);

	}
}