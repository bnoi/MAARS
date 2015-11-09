package fiji.plugin.maars.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;


import fiji.plugin.trackmate.Model;
import fiji.plugin.trackmate.io.TmXmlWriter;

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
	
	public static ArrayList<File> listFilesForFolder( File folder) {
		ArrayList<File> list = new ArrayList<File>();
	    for ( File fileEntry : folder.listFiles()) {
	        if (fileEntry.isDirectory()) {
	            listFilesForFolder(fileEntry);
	        } else {
	            list.add(fileEntry);
	        }
	    }
	    return list;
	}
	
	public static void writeSpotFeatures(File file, Model model){
		File newFile = new File(file.getParentFile() + "_" + file.getName() +".xml");

		TmXmlWriter writer = new TmXmlWriter( newFile , model.getLogger() );
		writer.appendModel( model );
		try {
			writer.writeToFile();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
