package maars;

import org.micromanager.MenuPlugin;
import org.micromanager.Studio;
import org.micromanager.internal.MMStudio;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

import maars.gui.MaarsMainDialog;
import mmcorej.CMMCore;

/**
* @author Tong LI, mail: tongli.bioinfo@gmail.com
* @version Nov 21, 2015
*/
@Plugin(type = MenuPlugin.class)
public class MAARSPlugin implements MenuPlugin,SciJavaPlugin {

	private MMStudio mmStudio;
	private CMMCore mmc;
	private MaarsParameters parameters;
	
	@Override
	public String getCopyright() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHelpText() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "MAARS";
	}

	@Override
	public String getVersion() {
		// TODO Auto-generated method stub
		return "1.0";
	}

	@Override
	public void setContext(Studio mmStudio) {
		// TODO Auto-generated method stub
		this.mmStudio = (MMStudio) mmStudio;
		this.mmc = mmStudio.core();
	}

	@Override
	public String getSubMenu() {
		return "MAARS";
	}

	@Override
	public void onPluginSelected() {
		String base_dir = "/Volumes/Macintosh/mm_source/micro-manager/plugins/MAARS/";
//		String base_dir = "/home/tong/Documents/code/MAARS/";
		parameters = new MaarsParameters(base_dir + "maars_config.xml");
		new MaarsMainDialog(mmStudio, mmc, parameters).show();
	}

}
