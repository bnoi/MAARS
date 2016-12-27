package edu.univ_tlse3.maars;

/**
 * This class stores all the parameters that are needed to run MAARS
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 10, 2015
 */

import edu.univ_tlse3.utils.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/*
 * MaarsParameters reads a configuration file written as a XML,
 * then allows to access values thanks to all constants defined.
 * 
 * SEGMENTATION_PARAMETERS
 *    |
 *    +-----> SKIP
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
 *    +-----> USING
 *    +-----> DYNAMIC
 *    +-----> TIME_LIMIT
 *    +-----> TIME_INTERVAL
 *    +-----> DO_ANALYSIS
 *    +-----> ANALYSIS_OPTIONS
 *    		|
 *    		+-----> DO_MITOSIS_RATIO
 *    		+----->
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
 * 							   +-----> SPOT_RADIUS
 *    				         +-----> MAXIMUM_NUMBER_OF_SPOT
 *    				         +-----> QUALITY
 * MITOSIS_DETECTION_PARAMETERS
 *    |
 *    +-----MINIMUM_DURATION
 *    +-----DETECTION_CHANNEL
 * @author Tong LI && Marie
 *
 */
public class MaarsParameters {

   private static final String SEGMENTATION_PARAMETERS = "SEGMENTATION_PARAMETERS";
   private static final String FLUO_ANALYSIS_PARAMETERS = "FLUO_ANALYSIS_PARAMETERS";
   private static final String EXPLORATION_PARAMETERS = "EXPLORATION_PARAMETERS";
   private static final String MITOSIS_DETECTION_PARAMETERS = "MITOSIS_DETECTION_PARAMETERS";
   private static final String MINIMUM_DURATION = "MINIMUM_DURATION";
   private static final String DETECTION_CHANNEL = "DETECTION_CHANNEL";
   public static final String RANGE_SIZE_FOR_MOVIE = "RANGE_SIZE_FOR_MOVIE";
   public static final String STEP = "STEP";
   private static final String SPOT_RADIUS = "SPOT_RADIUS";
   public static final String SAVE_FLUORESCENT_MOVIES = "SAVE_FLUORESCENT_MOVIES";
   private static final String MAXIMUM_NUMBER_OF_SPOT = "MAXIMUM_NUMBER_OF_SPOT";
   private static final String QUALITY = "QUALITY";
   public static final String DYNAMIC = "DYNAMIC";
   public static final String X_FIELD_NUMBER = "X_FIELD_NUMBER";
   public static final String Y_FIELD_NUMBER = "Y_FIELD_NUMBER";
   static final String CELL_SIZE = "CELL_SIZE";
   public static final String MINIMUM_CELL_AREA = "MINIMUM_CELL_AREA";
   public static final String MAXIMUM_CELL_AREA = "MAXIMUM_CELL_AREA";
   public static final String FILTER_MEAN_GREY_VALUE = "FILTER_MEAN_GREY_VALUE";
   public static final String MEAN_GREY_VALUE = "MEAN_GREY_VALUE";
   public static final String FILTER_SOLIDITY = "FILTER_SOLIDITY";
   public static final String SOLIDITY = "SOLIDITY";
   static final String NEW_MAX_WIDTH_FOR_CHANGE_SCALE = "NEW_MAX_WIDTH_FOR_CHANGE_SCALE";
   static final String NEW_MAX_HEIGTH_FOR_CHANGE_SCALE = "NEW_MAX_HEIGTH_FOR_CHANGE_SCALE";
   public static final String TIME_INTERVAL = "TIME_INTERVAL";
   public static final String TIME_LIMIT = "TIME_LIMIT";
   private static final String SAVING_PATH = "SAVING_PATH";
   public static final String DO_ANALYSIS = "DO_ANALYSIS";
   public static final String ANALYSIS_OPTIONS = "ANALYSIS_OPTIONS";
   public static final String DO_MITOSIS_RATIO = "DO_MITOSIS_RATIO";
   public static final String DO_INTERPHASE_RATIO = "DO_INTERPHASE_RATIO";
   public static final String DO_METAPHASE_RATIO = "DO_METAPHASE_RATIO";
   public static final String DO_FIND_MEROTELY = "DO_FIND_MEROTELY";
   private static final String SHUTTER = "SHUTTER";
   private static final String COLOR = "COLOR";
   private static final String EXPOSURE = "EXPOSURE";
   public static final String CHANNEL = "CHANNEL";
   private static final String USING = "USING";
   public static final String GFP = "GFP";
   public static final String CFP = "CFP";
   public static final String TXRED = "TXRED";
   public static final String DAPI = "DAPI";
   private static final String GENERAL_ACQUISITION_PARAMETERS = "GENERAL_ACQUISITION_PARAMETERS";
   private static final String DEFAULT_CHANNEL_PARAMATERS = "DEFAULT_CHANNEL_PARAMATERS";
   private static final String CHANNEL_GROUP = "CHANNEL_GROUP";
   public static final String X_POS = "X_POS";
   public static final String Y_POS = "Y_POS";
   public static final String FRAME = "FRAME";
   public static final String CUR_CHANNEL = "CHANNEL";
   public static final String CUR_MAX_NB_SPOT = "CUR_MAX_NB_SPOT";
   public static final String CUR_SPOT_RADIUS = "CUR_SPOT_RADIUS";
   private static final String SKIP = "SKIP";
   private Document doc;
   private Element root;
   private String[] allColors = {"GREEN","CYAN","RED", "BLUE", "WHITE","GRAY"};

   /**
    * Constructor of Element need path to configuration file
    *
    * @param defaultParametersStream input stream conaining xml file information
    */
   public MaarsParameters(InputStream defaultParametersStream) {

      final SAXBuilder sb = new SAXBuilder();
      try {
         try {
            doc = sb.build(defaultParametersStream);
         } catch (IOException e) {
            IOUtils.printErrorToIJLog(e);
         }
      } catch (JDOMException e) {
         IOUtils.printErrorToIJLog(e);
      }
      root = (Element) doc.getContent(0);
   }

   /**
    * empty
    */
   public MaarsParameters() {
   }

   /**
    * The few following colors are return as Color object : GREEN, CYAN, RED,
    * BLUE, WHITE NB : return GRAY if unknown color
    *
    * @param colorName name of the color
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
    * Write the parameters into the configuration file
    *
    * @throws IOException error than can not write xml file
    */
   public void save() throws IOException {
      doc.setContent(root);
      XMLOutputter xmlOutput = new XMLOutputter();
      xmlOutput.setFormat(Format.getPrettyFormat());
      xmlOutput.output(doc, new FileWriter("maars_config.xml"));
   }

   /**
    * Write the parameters into the configuration file
    *
    * @throws IOException error than can not write xml file
    */
   public void save(String path) throws IOException {
      doc.setContent(root);
      XMLOutputter xmlOutput = new XMLOutputter();
      xmlOutput.setFormat(Format.getPrettyFormat());
      xmlOutput.output(doc, new FileWriter(path + File.separator + "maars_config.xml"));
   }

   // Getter

   /**
    * @param xOrY : is a final static string in MaarsParameters
    * @return : the value of X or Y field
    */
   public int getFieldNb(final String xOrY) {
      return Integer.parseInt(root.getChild(EXPLORATION_PARAMETERS).getChildText(xOrY));
   }

   /**
    * @return analysis with dynamic or not
    */
   public boolean useDynamic() {
      return Boolean.parseBoolean(root.getChild(FLUO_ANALYSIS_PARAMETERS).getChildText(DYNAMIC));
   }

   /**
    * @return saving folder of MAARS output
    */
   public String getSavingPath() {
      return root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChildText(SAVING_PATH);
   }

   /**
    * set saving path
    *
    * @param path : corresponding value of parameter
    */
   public void setSavingPath(String path) {
      root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(SAVING_PATH).setText(path);
   }

   /**
    * @param parameter name of fluo parameter
    * @return time limit of fluorescence acquisition for one acquisition
    */
   public String getFluoParameter(final String parameter) {
      return root.getChild(FLUO_ANALYSIS_PARAMETERS).getChildText(parameter);
   }

   /**
    * @param parameter name of fluo parameter
    * @return time limit of fluorescence acquisition for one acquisition
    */
   public String getSegmentationParameter(final String parameter) {
      return root.getChild(SEGMENTATION_PARAMETERS).getChildText(parameter);
   }

   /**
    * @return the name of channel group set in micromanager
    */
   public String getChannelGroup() {
      return root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChildText(CHANNEL_GROUP);
   }


   /**
    * @param ch : GFP, CFP, DAPI, TXRED
    * @return corresponding channel shutter
    */
   public String getChShutter(String ch) {
      return root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch)
              .getChildText(SHUTTER);
   }

   /**
    * @param ch : GFP, CFP, DAPI, TXRED
    * @return corresponding channel color
    */
   public String getChColor(String ch) {
      return root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch)
              .getChildText(COLOR);
   }

   /**
    * @param ch : GFP, CFP, DAPI, TXRED
    * @return corresponding channel color
    */
   public String getChExposure(String ch) {
      return root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch)
              .getChildText(EXPOSURE);
   }

//   /**
//    * @param ch : GFP, CFP, DAPI, TXRED
//    * @return corresponding channel color
//    */
//   public String getchColor(String ch) {
//      return root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch)
//              .getChildText(COLOR);
//   }
//
//   /**
//    * @param ch : GFP, CFP, DAPI, TXRED
//    * @return corresponding channel color
//    */
//   public String getchShutter(String ch) {
//      return root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch)
//              .getChildText(SHUTTER);
//   }

   /**
    * @param ch: GFP, CFP, DAPI, TXRED
    * @return MAXIMUM_NUMBER_OF_SPOT of corresponding channel
    */
   public String getChMaxNbSpot(String ch) {
      return root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch)
              .getChildText(MAXIMUM_NUMBER_OF_SPOT);
   }

   /**
    * @param ch: GFP, CFP, DAPI, TXRED
    * @return SPOT_RADIUS of corresponding channel
    */
   public String getChSpotRaius(String ch) {
      return root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch).getChildText(SPOT_RADIUS);
   }

   /**
    * @param ch: GFP, CFP, DAPI, TXRED
    * @return SPOT_RADIUS of corresponding channel
    */
   public String getChQuality(String ch) {
      return root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch).getChildText(QUALITY);
   }

   /**
    *@return list of all channels
    */
   public List<String> getAllChannels(){
      ArrayList<String> channelNames = new ArrayList<>();
      for (Element e: root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChildren()){
         channelNames.add(e.getName());
      }
      return channelNames;
   }

    /**
     * @return get channels used for fluo analysis
     */
    public String getUsingChannels() {
        return root.getChild(FLUO_ANALYSIS_PARAMETERS).getChildText(USING);
    }

   /**
    *
    * @return
    */
    public String getSkipSegmentation(){
       return root.getChild(SEGMENTATION_PARAMETERS).getChildText(SKIP);
    }

    /**
     *
     * @return the parameter minimum mitosis duration
     */
    public String getMinimumMitosisDuration(){
        return root.getChild(MITOSIS_DETECTION_PARAMETERS).getChildText(MINIMUM_DURATION);
    }

    /**
     *
     * @return the channel to use for mitosis detection
     */
    public String getDetectionChForMitosis(){
        return root.getChild(MITOSIS_DETECTION_PARAMETERS).getChildText(DETECTION_CHANNEL);
    }

   //////////// Setters

    /**
     *
     * @param duration minimum mitosis duration (sec)
     */
    public void setMinimumMitosisDuration(String duration){
        root.getChild(MITOSIS_DETECTION_PARAMETERS).getChild(MINIMUM_DURATION).setText(duration);
    }

    /**
     *
     * @param chForMitosis the channel to detection mitosis
     */
    public void setDetectionChForMitosis(String chForMitosis){
        root.getChild(MITOSIS_DETECTION_PARAMETERS).getChild(DETECTION_CHANNEL).setText(chForMitosis);
    }

    /**
     * @param channelGroup channel group name
     */
    public void setChannelGroup(String channelGroup){
        root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(CHANNEL_GROUP).setText(channelGroup);
    }

   /**
    * @param ch : GFP, CFP, DAPI, TXRED
    * @param exposure exposure of the corresonding channel
    */
   public void setChExposure(String ch, String exposure) {
      root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch)
              .getChild(EXPOSURE).setText(exposure);
   }

   /**
    * @param ch : GFP, CFP, DAPI, TXRED
    * @param color
    */
   public void setChColor(String ch, String color) {
      root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch)
              .getChild(COLOR).setText(color);
   }

   /**
    * @param ch : GFP, CFP, DAPI, TXRED
    * @param shutter
    */
   public void setChShutter(String ch, String shutter) {
      root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch)
              .getChild(SHUTTER).setText(shutter);
   }

   /**
    * set channels to USING channel
    *
    * @param channels channels that are being using for acquisitions
    */
   public void setUsingChannels(String channels) {
      root.getChild(FLUO_ANALYSIS_PARAMETERS).getChild(USING).setText(channels);
   }

   /**
    * update value of x or y field number of exloration
    *
    * @param xOrY  dimension to be updated
    * @param value new value
    */
   public void setFieldNb(final String xOrY, String value) {
      root.getChild(EXPLORATION_PARAMETERS).getChild(xOrY).setText(value);
   }

   /**
    * set segmentation parameter
    *
    * @param parameter : static final String of MaarsParameters
    * @param value     : corresponding value of parameter
    */
   public void setSegmentationParameter(String parameter, String value) {
      root.getChild(SEGMENTATION_PARAMETERS).getChild(parameter).setText(value);
   }

   /**
    * set fluo analysis parameter
    *
    * @param parameter : static final String of MaarsParameters
    * @param value     : corresponding value of parameter
    */
   public void setFluoParameter(String parameter, String value) {
      root.getChild(FLUO_ANALYSIS_PARAMETERS).getChild(parameter).setText(value);
   }

   /**
    * @param ch        GFP, CFP, DAPI, TXRED
    * @param maxNbSpot maximum number of spot for corresponding channel
    */
   public void setChMaxNbSpot(String ch, String maxNbSpot) {
      root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch).getChild(MAXIMUM_NUMBER_OF_SPOT)
              .setText(maxNbSpot);
   }

   /**
    * @param ch         GFP, CFP, DAPI, TXRED
    * @param spotRaidus spotRaidus for corresponding channel
    */
   public void setChSpotRaius(String ch, String spotRaidus) {
      root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch).getChild(SPOT_RADIUS)
              .setText(spotRaidus);
   }

   /**
    * @param ch      GFP, CFP, DAPI, TXRED
    * @param quality quality of spots for corresponding channel
    */
   public void setChQuality(String ch, String quality) {
      root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChild(ch).getChild(QUALITY).setText(quality);
   }

   /**
    *
    * @param bfChannel
    */
   public void setSegChannel(String bfChannel){
      root.getChild(SEGMENTATION_PARAMETERS).getChild(CHANNEL).setText(bfChannel);
   }

   public void setSkipSegmentation(Boolean skip){
      root.getChild(SEGMENTATION_PARAMETERS).getChild(SKIP).setText(String.valueOf(skip));
   }

   /**
    *
    * @return availiableColors
    */
   public String[] availiableColors(){
      return allColors;
   }
   /**
    * @param root the dataset of this class
    */
   private void setRoot(Element root) {
      this.root = root;
   }

   public void addChannel(String newChannel){
      Element anotherChannel = root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).getChildren().get(2);
      List<Element> attributes = new ArrayList<>();
      for (Element e : anotherChannel.getChildren()){
         attributes.add(e.clone());
      }
      Element newChannelElement = new Element(newChannel);
      newChannelElement.addContent(attributes);
      root.getChild(GENERAL_ACQUISITION_PARAMETERS).getChild(DEFAULT_CHANNEL_PARAMATERS).addContent(newChannelElement);
   }

   /**
    * duplicate this object
    *
    * @return the a duplicate version of this class
    */
   public MaarsParameters duplicate() {
      Element newRoot = root.clone();
      MaarsParameters newParams = new MaarsParameters();
      newParams.setRoot(newRoot);
      return newParams;
   }
}