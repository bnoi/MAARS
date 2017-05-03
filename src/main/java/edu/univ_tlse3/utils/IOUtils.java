package edu.univ_tlse3.utils;

import ij.IJ;

import java.io.*;
import java.util.Properties;

/**
 * Created by tongli on 27/12/2016.
 */
public class IOUtils {
    public static void printErrorToIJLog(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter ps = new PrintWriter(sw);
        exception.printStackTrace(ps);
        IJ.error(sw.toString());
        try {
            sw.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        ps.close();
    }

    public static void writeToFile(String filePath, Properties properties){
        try (PrintWriter out = new PrintWriter(filePath)) {
            properties.store(out,"");
        } catch (FileNotFoundException e) {
            IOUtils.printErrorToIJLog(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
