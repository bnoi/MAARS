package org.micromanager.maars;

import org.micromanager.MenuPlugin;
import org.micromanager.Studio;
import org.micromanager.gui.MaarsMainDialog;
import org.micromanager.internal.MMStudio;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

import mmcorej.CMMCore;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.micromanager.utils.FileUtils;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 21, 2015
 */
@Plugin(type = MenuPlugin.class)
public class MAARSPlugin implements MenuPlugin, SciJavaPlugin {

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
		return "Main";
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
		String configFileName = "maars_config.xml";
		InputStream inStream = null;
		if (FileUtils.isValid(configFileName)) {
			try {
				inStream = new FileInputStream(configFileName);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else {
			inStream = ClassLoader.getSystemResourceAsStream("org/micromanager/" + configFileName);
		}
		parameters = new MaarsParameters(inStream);
		new MaarsMainDialog(mmStudio, mmc, parameters).show();
	}
}
