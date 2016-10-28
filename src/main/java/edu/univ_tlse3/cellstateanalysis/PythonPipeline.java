package edu.univ_tlse3.cellstateanalysis;

import edu.univ_tlse3.utils.FileUtils;
import ij.IJ;
import org.micromanager.internal.utils.ReportingUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PythonPipeline {
   public static final String SCRIPT_NAME = "AnalyzeMAARSOutput.py";
   public static final String TRACKMATE_NAME = "trackmate.py";
   public static final String PATH2PYTHONSCRIPTS = "plugins" + File.separator + "MAARS_deps" + File.separator;

   public static ArrayList<String> getPythonScript(String acqDir, String channel, String calibration, String minimumPeriod, String interval) {
      BufferedReader bfr = getBufferReaderOfScript(File.separator + SCRIPT_NAME);
      ArrayList<String> script = new ArrayList<String>();
      Boolean changeParam = false;
      String pattern = "if __name__ == '__main__':";
      String patternForDir = ".*baseDir=.*";
      String patternForChannel = ".*channel =.*";
      String patternForAcqInt = ".*launcher.set_acq_interval.*";
      String patternForCal = ".*launcher.set_calibration.*";
      String patternForMinPerio = ".*launcher.set_minimumPeriod.*";
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
//                ReportingUtils.logMessage(line);
               changeParam = true;
            }
            script.add(line);
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
      return script;
   }

   public static BufferedReader getBufferReaderOfScript(String scriptPathWithJar){
      InputStream pythonScript = PythonPipeline.class.getResourceAsStream(scriptPathWithJar);
      return new BufferedReader(new InputStreamReader(pythonScript));
   }

   public static void savePythonScript(ArrayList<String> script) {
//         if (PythonPipeline.class.getProtectionDomain().getCodeSource().getLocation().getPath().substring(1).endsWith(".jar")){
//         if(PythonPipeline.class.getProtectionDomain().getCodeSource().getLocation().getPath().endsWith(".jar")){
      FileUtils.createFolder(PATH2PYTHONSCRIPTS);
      copyScriptDependency();
      ReportingUtils.logMessage(PATH2PYTHONSCRIPTS + SCRIPT_NAME);
      FileUtils.writeScript(PATH2PYTHONSCRIPTS + SCRIPT_NAME, script);
   }

   /**
    *
    */
   public static void copyScriptDependency(){
      ReportingUtils.logMessage(PythonPipeline.class.getProtectionDomain().getCodeSource().getLocation().getPath());
      BufferedReader bfr = getBufferReaderOfScript(File.separator + TRACKMATE_NAME);
      ArrayList<String> script = new ArrayList<String>();
      try {
         while (bfr.ready()) {
            script.add(bfr.readLine());
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
      FileUtils.writeScript(PATH2PYTHONSCRIPTS + TRACKMATE_NAME, script);
   }

   /**
    *
    * @return
    */
   public static void runPythonScript() {
      ProcessBuilder probuilder;
      Process process;
      BufferedReader in;
      BufferedReader stdError;
      String[] cmd;
      if (System.getProperty("os.name").matches("(Windows)(.+)")) {
         cmd = new String[]{PythonPipeline.getPythonDefaultPathInConda(), PATH2PYTHONSCRIPTS + SCRIPT_NAME};
      } else {
         cmd = new String[]{PythonPipeline.getPythonDefaultPathInConda(),PATH2PYTHONSCRIPTS + SCRIPT_NAME};
      }

      probuilder = new ProcessBuilder(cmd);
      try {
         process = probuilder.start();
         in = new BufferedReader(new InputStreamReader(process.getInputStream()));
         stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
         String s;
         while ((s = in.readLine()) != null) {
            IJ.log(s);
         }
         while ((s = stdError.readLine()) != null) {
            IJ.log(s);
         }
      } catch (IOException e) {
         IJ.log(e.getMessage());
      }
   }

   private static String getPythonDefaultPathInConda() {
      String osName = System.getProperty("os.name");
      String pythonPath = "";
      File condaDir;
      String condaDirPattern = "(\\w+)(conda)(\\w+)";
      String sep = File.separator;
      if (osName.equals("Linux")) {
         condaDir = new File(sep + "home" + sep + System.getProperty("user.name"));
         for (String dir : condaDir.list()) {
            if (dir.matches(condaDirPattern)) {
               pythonPath = condaDir + sep + dir + sep + "bin" + sep + "python";
            }
         }
      } else if (osName.equals("Mac OS X")) {
         condaDir = new File(sep + "Users" + sep + System.getProperty("user.name"));
         for (String dir : condaDir.list()) {
            if (dir.matches(condaDirPattern)) {
               pythonPath = condaDir + sep + dir + sep + "bin" + sep + "python";
            }
         }
      } else {
         condaDir = new File(sep + "C:" + sep + "Users" + sep + System.getProperty("user.name"));
         for (String dir : condaDir.list()) {
            if (dir.matches(condaDirPattern)) {
               pythonPath = condaDir + sep + dir + sep + "python";
            }
         }
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
   public static String replaceSubString(String pattern, String line, String value){
      Pattern p = Pattern.compile(pattern);
      Matcher m = p.matcher(line);
      line = m.replaceFirst(value);
      return line;
   }

    public static void main(String[] args) {
//       ArrayList<String> script = PythonPipeline.getPythonScript("/home/tong/Documents/movies/26-10-1/X0_Y0", "CFP", "0.1075", "200","20");
//       PythonPipeline.savePythonScript(script);
//       PythonPipeline.copyScriptDependency();
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
//         e.printStackTrace();
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