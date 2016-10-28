package edu.univ_tlse3.utils;

import ij.IJ;

import java.io.File;
import java.io.IOException;
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
    */
   public static void writeScript(String path, ArrayList<String> scriptInArray){
      try {
         Files.write(Paths.get(path), scriptInArray, Charset.forName("UTF-8"));
      } catch (IOException e) {
         e.printStackTrace();
      }
   }
}
