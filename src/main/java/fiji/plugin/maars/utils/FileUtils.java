package fiji.plugin.maars.utils;

import java.io.File;
import java.io.IOException;

/**
* @author Tong LI, mail: tongli.bioinfo@gmail.com
* @version Nov 4, 2015
*/
public class FileUtils {

	public static boolean isFilenameValid(String file) {
		File f = new File(file);
		try {
		   f.getCanonicalPath();
		   return true;
		}
		catch (IOException e) {
		   return false;
		}
	}
}
