package maars.utils;

import ij.IJ;
import maars.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

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
    * @param pathToDir folder to create
    * @return succeed to create de dir or not
    */
   public static Boolean createFolder(String pathToDir) {
      if (!exists(pathToDir)) {
         File fluoDir = new File(pathToDir);
         return fluoDir.mkdirs();
      }
      return false;
   }

   /**
    * @param path          where to write the script
    * @param scriptInArray script stored in the ArrayList line by line
    */
   public static void writeScript(String path, ArrayList<String> scriptInArray) {
      try {
         Files.write(Paths.get(path), scriptInArray, Charset.forName("UTF-8"));
      } catch (IOException e) {
         IOUtils.printErrorToIJLog(e);
      }
   }

   /**
    * @param scriptName path to script
    * @return its bufferedReader
    */
   public static BufferedReader getBufferReaderOfScript(String scriptName) {
      InputStream pythonScript = getInputStreamOfScript(scriptName);
      return new BufferedReader(new InputStreamReader(pythonScript));
   }

   /**
    * @param scriptName path to script
    * @return its inputstream
    */
   public static InputStream getInputStreamOfScript(String scriptName) {
      ClassLoader classLoader = FileUtils.class.getClassLoader();
      return classLoader.getResourceAsStream(scriptName);
   }

   /**
    * @param folderPath folder to copy scripts
    * @param scriptName name of script
    */
   public static void copy(String folderPath, String scriptName) {
      BufferedReader bfr = getBufferReaderOfScript(scriptName);
      ArrayList<String> script = new ArrayList<>();
      try {
         while (bfr.ready()) {
            script.add(bfr.readLine());
         }
      } catch (IOException e) {
         IOUtils.printErrorToIJLog(e);
      }
      FileUtils.writeScript(folderPath + scriptName, script);
   }

   public static boolean containsTiffFile(String path){
      boolean hasTiffFile = false;
      for (String f : new File(path).list()){
         if (f.endsWith(".tiff") || f.endsWith(".tif")){
            hasTiffFile = true;
         }
      }
      return hasTiffFile;
   }

   public static ArrayList<String> getTiffWithPattern(String path, String pattern) {
      ArrayList<String> names = new ArrayList<>();
      File folder = new File(path);
      File[] listOfFiles = folder.listFiles();
      for (int i = 0; i < listOfFiles.length; i++) {
         if (listOfFiles[i].isFile()) {
            if (Pattern.matches(pattern, listOfFiles[i].getName())) {
               names.add(listOfFiles[i].getName());
            }
         }
      }
      return names;
   }

   public static void recursiveRemove(String folderPath) {
      Path directory = Paths.get(folderPath);
      try {
         Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
               Files.delete(file);
               return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
               Files.delete(dir);
               return FileVisitResult.CONTINUE;
            }
         });
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static HashMap<String, String[]> readTable(String path) {
      HashMap<String, String[]> table = new HashMap<>();
      try {
         BufferedReader in = new BufferedReader(new FileReader(path));
         String str;
         String[] splited;
         while ((str = in.readLine()) != null) {
            splited = str.split(",");
            table.put(splited[0], Arrays.copyOfRange(splited, 1, splited.length));
         }
         in.close();
      } catch (IOException e) {
         IOUtils.printErrorToIJLog(e);
      }
      return table;
   }
}
