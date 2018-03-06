package maars.cellAnalysis;

import ij.IJ;

import java.io.File;
import java.io.IOException;

public class PythonPipeline {
   public static final String ANALYSING_SCRIPT_NAME = "MaarsAnalysis.py";

   /**
    * @param cmd           command to execute
    * @param logFileName   log directory
    */
   public static void runPythonScript(String[] cmd, String logFileName) {
      ProcessBuilder probuilder = new ProcessBuilder().inheritIO().redirectErrorStream(true).command(cmd);
      File pythonLog = new File(logFileName);
      try {
         probuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(pythonLog));
         Process process = probuilder.start();
         process.waitFor();
         process.destroyForcibly();
         if (!process.isAlive()) {
            IJ.log("Python extension ended (please check the log file for further information)");
         }
      } catch (IOException | InterruptedException e) {
         IJ.log(e.getMessage());
      }
   }

   public static String getPythonDefaultPathInConda() {
      String osName = System.getProperty("os.name");
      String pythonPath = "";
      File condaDir;
      String condaDirPattern = "(\\w+)(conda)(\\w+)";
      String sep = File.separator;
      switch (osName) {
         case "Linux":
            condaDir = new File(sep + "home" + sep + System.getProperty("user.name"));
            for (String dir : condaDir.list()) {
               if (dir.matches(condaDirPattern)) {
                  pythonPath = condaDir + sep + dir + sep + "bin" + sep + "python";
               }
            }
            break;
         case "Mac OS X":
            condaDir = new File(sep + "Users" + sep + System.getProperty("user.name"));
            for (String dir : condaDir.list()) {
               if (dir.matches(condaDirPattern)) {
                  pythonPath = condaDir + sep + dir + sep + "bin" + sep + "python";
               }
            }
            break;
         default:
            condaDir = new File(sep + "C:" + sep + "Users" + sep + System.getProperty("user.name"));
            for (String dir : condaDir.list()) {
               if (dir.matches(condaDirPattern)) {
                  pythonPath = condaDir + sep + dir + sep + "python";
               }
            }
            break;
      }
      System.out.println(pythonPath);
      return pythonPath;
   }
}