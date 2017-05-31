package edu.univ_tlse3.cellstateanalysis;

import ij.IJ;

import java.io.File;
import java.io.IOException;

public class PythonPipeline {
   public static final String ANALYSING_SCRIPT_NAME = "processMitosis.py";
   public static final String TRACKMATE_LOADER_NAME = "tmXmlToDataFrame.py";
   public static final String COLOCAL_SCRIPT_NAME = "kt_spb_colocalisation.py";

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

//   /**
//    * @param pattern the pattern to search for
//    * @param line    String to modify
//    * @param value   the string to be inserted
//    * @return modified string
//    */
//   private static String replaceSubString(String pattern, String line, String value) {
//      Pattern p = Pattern.compile(pattern);
//      Matcher m = p.matcher(line);
//      line = m.replaceFirst(value);
//      return line;
//   }

//   public static ArrayList<String> getPythonScript(String acqDir, String channel, String calibration, String minimumPeriod, String interval) {
//      BufferedReader bfr = FileUtils.getBufferReaderOfScript(ANALYSING_SCRIPT_NAME);
//      ArrayList<String> script = new ArrayList<>();
//      Boolean changeParam = false;
//      String pattern = "if __name__ == '__main__':";
//      String patternForDir = ".*baseDir =.*";
//      String patternForChannel = ".*channel =.*";
//      String patternForAcqInt = ".*acq_interval =.*";
//      String patternForCal = ".*calibration =.*";
//      String patternForMinPerio = ".*minimumPeriod =.*";
//      String intPat = "\\d+";
//      String decimalPat = "0\\.\\d+";
//      try {
//         while (bfr.ready()) {
//            String line = bfr.readLine();
//            if (Pattern.matches(pattern, line) || changeParam) {
//               if (Pattern.matches(patternForDir, line)) {
//                  line = line.replaceFirst(line.split("\"")[1], acqDir);
//               } else if (Pattern.matches(patternForChannel, line)) {
//                  line = line.replaceFirst(line.split("\"")[1], channel);
//               } else if (Pattern.matches(patternForAcqInt, line)) {
//                  line = replaceSubString(intPat, line, interval);
//               } else if (Pattern.matches(patternForCal, line)) {
//                  line = replaceSubString(decimalPat, line, calibration);
//               } else if (Pattern.matches(patternForMinPerio, line)) {
//                  line = replaceSubString(intPat, line, minimumPeriod);
//               }
//               changeParam = true;
//            }
//            script.add(line);
//         }
//      } catch (IOException e) {
//         IOUtils.printErrorToIJLog(e);
//      }
//      return script;
//   }
//
//   public static void savePythonScript(String pathToMitosisDir, ArrayList<String> script) {
//      FileUtils.createFolder(pathToMitosisDir);
//      FileUtils.writeScript(pathToMitosisDir + ANALYSING_SCRIPT_NAME, script);
//   }

   public static void main(String[] args) {
//      new ImageJ();
//      String newPath = FileUtils.convertPathToLinuxType("/Volumes/Macintosh/curioData/MAARSdata/102/15-06-2/BF_1");
//      System.out.println(newPath);
//      String[] cmd = new String[]{PythonPipeline.getPythonDefaultPathInConda(), MaarsParameters.DEPS_DIR +
//            PythonPipeline.ANALYSING_SCRIPT_NAME, newPath, "CFP", "0.1065", "30" };
//      ArrayList cmds = new ArrayList();
//      cmds.add(String.join(" ",cmd));
//      String bashPath = "/Users/tongli/Desktop/script.sh";
//      FileUtils.writeScript(bashPath,cmds);
//      PythonPipeline.runPythonScript(cmd, "/Users/tongli/Desktop/");
   }
}