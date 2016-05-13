package org.univ_tlse3.utils;

import java.io.File;
import ij.IJ;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 4, 2015
 */
public class FileUtils {

	/**
	 * test if the path exists
	 * 
	 * @param path
	 * path to test
	 * @return : true or false
	 */
	public static boolean exists(String path) {
		return new File(path).exists();
	}

	/**
	 * Convert an unix path in windows path if program is running on windows OS
	 * 
	 * @param unixPath
	 * path to be converted
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
	 * @param pathToFluoDir
	 * folder to create
	 */
	public static void createFolder(String pathToFluoDir) {
		File fluoDir = new File(pathToFluoDir);
		fluoDir.mkdirs();
	}
}
