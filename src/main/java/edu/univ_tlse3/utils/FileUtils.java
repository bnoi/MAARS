package edu.univ_tlse3.utils;

import edu.univ_tlse3.cellstateanalysis.PythonPipeline;
import ij.IJ;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 4, 2015
 */
public class FileUtils {

   /**
    * test if the path exists
    *
    * @param path path to test
    * @return : true or false
    */
   public static boolean exists(String path) {
      return new File(path).exists();
   }

   /**
    * Convert an unix path in windows path if program is running on windows OS
    *
    * @param unixPath path to be converted
    * @return String path
    */
   public static String convertPath(String unixPath) {
      String path = unixPath;
      if (IJ.isWindows() && path.contains("/")) {
         path = path.replace("/", "\\\\");
      }
      return path;
   }

   /**
    * Convert an windows path in unix path
    *
    * @param path path to be converted
    * @return String path
    */
   public static String convertPathToLinuxType(String path) {
      return path.replace("\\", "/");
   }

   /**
    * if current path do not exists, create a new one
    *
    * @param pathToFluoDir folder to create
    * @return succeed to create de dir or not
    */
   public static Boolean createFolder(String pathToFluoDir) {
      File fluoDir = new File(pathToFluoDir);
      return fluoDir.mkdirs();
   }

   /**
    *
    * @param path where to write the script
    * @param scriptInArray script stored in the ArrayList line by line
    */
   public static void writeScript(String path, ArrayList<String> scriptInArray){
      try {
         Files.write(Paths.get(path), scriptInArray, Charset.forName("UTF-8"));
      } catch (IOException e) {
         IJ.error(e.toString());
      }
   }

   /**
    *
    * @param scriptName
    * @return
    */
   public static BufferedReader getBufferReaderOfScript(String scriptName){
      InputStream pythonScript = getInputStreamOfScript(scriptName);
      return new BufferedReader(new InputStreamReader(pythonScript));
   }

   /**
    *
    * @param scriptName
    * @return
    */
   public static InputStream getInputStreamOfScript(String scriptName){
      ClassLoader classLoader = PythonPipeline.class.getClassLoader();
      return classLoader.getResourceAsStream(scriptName);
   }

   /**
    *
    */
   public static void copyScriptDependency(String folderPath, String scriptName){
      BufferedReader bfr = getBufferReaderOfScript(scriptName);
      ArrayList<String> script = new ArrayList<>();
      try {
         while (bfr.ready()) {
            script.add(bfr.readLine());
         }
      } catch (IOException e) {
         IJ.error(e.toString());
      }
      FileUtils.writeScript(folderPath + scriptName, script);
   }
}
