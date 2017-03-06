package edu.univ_tlse3.cellstateanalysis;

import edu.univ_tlse3.utils.FileUtils;
import edu.univ_tlse3.utils.IOUtils;

import ij.IJ;
import org.micromanager.internal.utils.ReportingUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PythonPipeline {
   private static final String SCRIPT_NAME = "AnalyzeMAARSOutput.py";
   private static final String TRACKMATE_NAME = "trackmate.py";
   private static final String PATH2PYTHONSCRIPTS = IJ.getDirectory("plugins") + "MAARS_deps" + File.separator;
//   private static final String PATH2PYTHONSCRIPTS = "/home/tong/Documents/code/ImageJ/plugins/MAARS_deps" + File.separator;
//private static final String PATH2PYTHONSCRIPTS = "/Applications/ImageJ/plugins/MAARS_deps" + File.separator;

   public static ArrayList<String> getPythonScript(String acqDir, String channel, String calibration, String minimumPeriod, String interval) {
      BufferedReader bfr = FileUtils.getBufferReaderOfScript(SCRIPT_NAME);
      ArrayList<String> script = new ArrayList<>();
      Boolean changeParam = false;
      String pattern = "if __name__ == '__main__':";
      String patternForDir = ".*baseDir=.*";
      String patternForChannel = ".*channel =.*";
      String patternForAcqInt = ".*acq_interval =.*";
      String patternForCal = ".*calibration =.*";
      String patternForMinPerio = ".*minimumPeriod =.*";
      String intPat = "\\d+";
      String decimalPat = "0\\.\\d+";
      try {
         while (bfr.ready()) {
            String line = bfr.readLine();
            if (Pattern.matches(pattern, line) || changeParam) {
               if (Pattern.matches(patternForDir, line)) {
                  line = line.replaceFirst(line.split("\"")[1], acqDir);
               } else if (Pattern.matches(patternForChannel, line)) {
                  line = line.replaceFirst(line.split("\"")[1], channel);
               } else if (Pattern.matches(patternForAcqInt, line)) {
                  line = replaceSubString(intPat, line, interval);
               } else if (Pattern.matches(patternForCal, line)) {
                  line = replaceSubString(decimalPat, line, calibration);
               } else if (Pattern.matches(patternForMinPerio, line)) {
                  line = replaceSubString(intPat, line, minimumPeriod);
               }
               changeParam = true;
            }
            script.add(line);
         }
      } catch (IOException e) {
         IOUtils.printErrorToIJLog(e);
      }
      return script;
   }

   public static void savePythonScript(ArrayList<String> script) {
      FileUtils.createFolder(PATH2PYTHONSCRIPTS);
      FileUtils.copyScriptDependency(PATH2PYTHONSCRIPTS, TRACKMATE_NAME);
      ReportingUtils.logMessage(PATH2PYTHONSCRIPTS + SCRIPT_NAME);
      FileUtils.writeScript(PATH2PYTHONSCRIPTS + SCRIPT_NAME, script);
   }

   /**
    *
    */
   public static void runPythonScript() {
      String[] cmd = new String[]{PythonPipeline.getPythonDefaultPathInConda(), PATH2PYTHONSCRIPTS + SCRIPT_NAME};
      ProcessBuilder probuilder = new ProcessBuilder().inheritIO().redirectErrorStream(true).command(cmd);
      File pythonLog = new File(PATH2PYTHONSCRIPTS + "pythonPipeline_log.txt");
      try {
         probuilder.redirectOutput(ProcessBuilder.Redirect.appendTo(pythonLog));
         Process process = probuilder.start();
         process.waitFor();
         process.destroyForcibly();
         if (!process.isAlive()){
            IJ.log("Python pipeline ended");
         }
      } catch (IOException | InterruptedException e) {
         IJ.log(e.getMessage());
      }
   }

   private static String getPythonDefaultPathInConda() {
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

   /**
    *
    * @param pattern the pattern to search for
    * @param line String to modify
    * @param value   the string to be inserted
    * @return modified string
    */
   private static String replaceSubString(String pattern, String line, String value){
      Pattern p = Pattern.compile(pattern);
      Matcher m = p.matcher(line);
      line = m.replaceFirst(value);
      return line;
   }

   public static void main(String[] args) {
//       String newPath = FileUtils.convertPathToLinuxType("/Volumes/Macintosh/curioData/MAARSdata/102/12-06-1/X0_Y0-------");
//       ReportingUtils.logMessage(newPath);
//       ArrayList<String> script = PythonPipeline.getPythonScript(newPath, "GFP", "0.1", "300","30");
//       PythonPipeline.savePythonScript(script);
//       PythonPipeline.runPythonScript();
   //todo it will be cool if one day anaconda support jython. Though not possible for now. The codes below is tested with jython
// /      ReportingUtils.logMessage(PythonPipeline.class.getProtectionDomain().getCodeSource().getLocation().getPath().substring(1)
//              + "AnalyzeMAARSOutput.py");
// ScriptEngineManager manager = new ScriptEngineManager();
//      java.util.List<ScriptEngineFactory> factories = manager.getEngineFactories();
//      for (ScriptEngineFactory f : factories) {
//         System.out.println("egine name:" + f.getEngineName());
//         System.out.println("engine version:" + f.getEngineVersion());
//         System.out.println("language name:" + f.getLanguageName());
//         System.out.println("language version:" + f.getLanguageVersion());
//         System.out.println("names:" + f.getNames());
//         System.out.println("mime:" + f.getMimeTypes());
//         System.out.println("extension:" + f.getExtensions());
//         System.out.println("-----------------------------------------------");
//      }
//      ScriptEngine python = new ScriptEngineManager().getEngineByName("python");
//      try {
//         python.eval("import sys");
//         python.eval("print sys.version_info");
//
//      } catch (ScriptException e) {
//         IOUtils.printErrorToIJLog(e);;
//      }
//
//      final PythonInterpreter interpreter = new PythonInterpreter();
//      interpreter.exec("print \"Python - Hello, world!\"");
//
//      PyObject result = interpreter.eval("2 + 3");
//      System.out.println(result.toString());
//      ClassLoader loader = java.lang.ClassLoader.getSystemClassLoader();
//      InputStream stream = loader.getResourceAsStream("AnalyzeMAARSOutput.py");
//      BufferedReader reader = BufferedReader(InputStreamReader(stream));
//
//      String script = "";
//      line = reader.readLine()
//      while (line != None) :
//      script += line + "\n"
//      line = reader.readLine()
//      ReportingUtils.logMessage(stream.toString());
//      System.out.println();
//        PythonPipeline.getMitosisFiles("/Volumes/Macintosh/curioData/102/25-03-1/X0_Y0", "CFP", "0.1075",
//                "200", "20");
    }
}