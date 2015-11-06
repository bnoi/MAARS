package fiji.plugin.maars.utils;

import java.io.File;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 4, 2015
 */
public class FileUtils {

	public static boolean isValid(String path) {
		File f = new File(path);
		if (f.exists()) {
			return true;
		} else {
			return false;
		}
	}
}
