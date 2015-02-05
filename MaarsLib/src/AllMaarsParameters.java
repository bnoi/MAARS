import ij.IJ;

import java.awt.Color;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * AllMaarsParameters reads a configuration file written as a JsonObject,
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
 *    +-----> SPOT_RADIUS
 *    +-----> SAVE_FLUORESCENT_MOVIES
 *    +-----> CHANNEL
 *    +-----> MAXIMUM_NUMBER_OF_SPOT
 *    +-----> FIND_BEST_MITOSIS_IN_FIELD
 *    
 * MITOSIS_MOVIE_PARAMETERS
 *    |
 *    +-----> START_MOVIE_CONDITIONS
 *    |				|
 *    |   			+-----> CONDITIONS
 *    |				|		|
 *    |				|		+-----> ABSOLUTE_MINIMUM_SPINDLE_SIZE
 *    |				|		+-----> RELATIVE_SPINDLE_ANGLE
 *    |				|		+-----> ABSOLUTE_MAXIMUM_SPINDLE_SIZE
 *    |				|
 *    |				+-----> VALUES
 *    |						|
 *    |						+-----> ABSOLUTE_MINIMUM_SPINDLE_SIZE
 *    |						+-----> RELATIVE_SPINDLE_ANGLE
 *    |						+-----> ABSOLUTE_MAXIMUM_SPINDLE_SIZE
 *    |
 *    +-----> END_MOVIE_CONDITIONS
 *    |			|
 *    |			+-----> CONDITIONS
 *    |			|			|
 *    |			|			+-----> ABSOLUTE_MINIMUM_SPINDLE_SIZE
 *    |			|			+-----> RELATIVE_MAXIMUM_SPINDLE_SIZE
 *    |			|			+-----> RELATIVE_SPINDLE_ANGLE
 *    |			|			+-----> TIME_LIMIT
 *    |			|			+-----> GROWING_SPINDLE
 *    |			|
 *    |			+-----> VALUES
 *    |						|
 *    |						+-----> ABSOLUTE_MINIMUM_SPINDLE_SIZE
 *    |						+-----> RELATIVE_MAXIMUM_SPINDLE_SIZE
 *    |						+-----> RELATIVE_SPINDLE_ANGLE
 *    |						+-----> TIME_LIMIT
 *    |						+-----> GROWING_SPINDLE
 *    |
 *    +-----> FRAME_NUMBER
 *    +-----> TIME_INTERVAL
 *    +-----> MARGIN_AROUD_CELL
 *    +-----> RANGE_SIZE_FOR_MOVIE
 *    +-----> SAVING_PATH
 *    +-----> CHANNEL
 *    
 * GENERAL_ACQUISITION_PARAMETERS
 *    |
 *    +-----> CHANNEL_GROUP
 *    +-----> DEFAULT_CHANNEL_PARAMATERS
 * 					|
 * 					+-----> channel name
 * 								|
 * 								+-----> COLOR
 * 								+-----> EXPOSURE
 * 								+-----> SHUTTER
 * @author marie
 *
 */
public class AllMaarsParameters {

	private String defaultParametersFile;
	JsonObject parametersJObject;
	
	public static final String SEGMENTATION_PARAMETERS = "SEGMENTATION_PARAMETERS";
	public static final String FLUO_ANALYSIS_PARAMETERS = "FLUO_ANALYSIS_PARAMETERS";
	public static final String EXPLORATION_PARAMETERS = "EXPLORATION_PARAMETERS";
	public static final String MITOSIS_MOVIE_PARAMETERS = "MITOSIS_MOVIE_PARAMETERS";

	public static final String RANGE_SIZE_FOR_MOVIE = "RANGE_SIZE_FOR_MOVIE";
	public static final String STEP = "STEP";
	public static final String SPOT_RADIUS = "SPOT_RADIUS";
	public static final String SAVE_FLUORESCENT_MOVIES = "SAVE_FLUORESCENT_MOVIES";
	public static final String MAXIMUM_NUMBER_OF_SPOT = "MAXIMUM_NUMBER_OF_SPOT";
	public static final String FIND_BEST_MITOSIS_IN_FIELD = "FIND_BEST_MITOSIS_IN_FIELD";

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
	
	
	public static final String START_MOVIE_CONDITIONS = "START_MOVIE_CONDITIONS";
	public static final String END_MOVIE_CONDITIONS = "END_MOVIE_CONDITIONS";
	public static final String TIME_INTERVAL = "TIME_INTERVAL";
	public static final String MARGIN_AROUD_CELL = "MARGIN_AROUD_CELL";
	
	public static final String CONDITIONS = "CONDITIONS";
	public static final String VALUES = "VALUES";
	public static final String RELATIVE_MAXIMUM_SPINDLE_SIZE = "RELATIVE_MAXIMUM_SPINDLE_SIZE";
	public static final String ABSOLUTE_MINIMUM_SPINDLE_SIZE = "ABSOLUTE_MINIMUM_SPINDLE_SIZE";
	public static final String ABSOLUTE_MAXIMUM_SPINDLE_SIZE = "ABSOLUTE_MAXIMUM_SPINDLE_SIZE";
	public static final String RELATIVE_SPINDLE_ANGLE= "RELATIVE_SPINDLE_ANGLE";
	public static final String TIME_LIMIT = "TIME_LIMIT";
	public static final String GROWING_SPINDLE= "GROWING_SPINDLE";
	public static final String SAVING_PATH = "SAVING_PATH";
	
	public static final String SHUTTER = "SHUTTER";
	public static final String COLOR = "COLOR";
	public static final String EXPOSURE = "EXPOSURE";
	public static final String CHANNEL = "CHANNEL";
	public static final String FRAME_NUMBER = "FRAME_NUMBER";
	
	public static final String GENERAL_ACQUISITION_PARAMETERS = "GENERAL_ACQUISITION_PARAMETERS";
	
	public static final String DEFAULT_CHANNEL_PARAMATERS = "DEFAULT_CHANNEL_PARAMATERS";
	
	public static final String CHANNEL_GROUP = "CHANNEL_GROUP";
	
	/**
	 * Constructor of Object need path to configuration file
	 * @param defaultParametersFile
	 * @throws IOException
	 */
	public AllMaarsParameters(String defaultParametersFile) throws IOException {
		
		this.defaultParametersFile = defaultParametersFile;
		
		Reader fReader =  null;
		fReader = new FileReader(defaultParametersFile);
		JsonParser jParser = new JsonParser();
		JsonElement jElement = jParser.parse(fReader);
		
		parametersJObject = jElement.getAsJsonObject();
	}
	
	/**
	 * Return JsonObject containing all parameters
	 * @return JsonObject
	 */
	public JsonObject getParametersAsJsonObject() {
		return parametersJObject;
	}
	
	/**
	 * Write the parameters in the configuration file
	 * @throws IOException
	 */
	public void save() throws IOException {
		FileWriter fWriter = new FileWriter(defaultParametersFile);
		fWriter.write(parametersJObject.toString());
		fWriter.close();
	}
	
	/**
	 * Convert an unix path in windows path if program is running on windows OS
	 * @param unixPath
	 * @return String path
	 */
	public static String convertPath(String unixPath) {
		String path = unixPath;
		System.out.println("program running on windows : "+IJ.isWindows());
		System.out.println("path is containing '/' : "+path.contains("/"));
		if (IJ.isWindows() && path.contains("/")) {
			path = path.replace("/", "\\\\");
		}
		return path;
	}
	
	/**
	 * The few following colors are return as Color object : GREEN, CYAN, RED, BLUE, WHITE
	 * NB : return GRAY if unknown color
	 * @param colorName
	 * @return Color
	 */
	public static Color getColor(String colorName) {
		if (colorName.equals("GREEN")) {
			return Color.GREEN;
		}
		else {
		if (colorName.equals("CYAN")) {
			return Color.CYAN;
		}
		else {
		if (colorName.equals("RED")) {
			return Color.RED;
		}
		else {
		if (colorName.equals("BLUE")) {
			return Color.BLUE;
		}
		else {
		if(colorName.equals("WHITE")) {
			return Color.WHITE;
		}
		else {
			return Color.GRAY;
		}
		}
		}
		}
		}
	}
}
