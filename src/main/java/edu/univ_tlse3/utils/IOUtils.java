package edu.univ_tlse3.utils;

import ij.IJ;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

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
}
