import org.micromanager.MenuPlugin;
import org.micromanager.Studio;
import org.univ_tlse3.gui.MaarsMainDialog;
import org.micromanager.internal.MMStudio;
import org.univ_tlse3.maars.MaarsParameters;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.univ_tlse3.utils.FileUtils;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 21, 2015
 */
@Plugin(type = MenuPlugin.class)
public class MAARSPlugin implements org.micromanager.MenuPlugin, SciJavaPlugin {

	private MMStudio mmStudio;

	private static final String VERSION_INFO = "1.0.0";
	static private final String COPYRIGHT_NOTICE = "BSD License, 2016";
	static private final String DESCRIPTION = "Micro-Manager plugin for Mitotic Analysing And Recording System";
	static private final String NAME = "MAARS";

	@Override
	public void setContext(Studio mmStudio) {
		this.mmStudio = (MMStudio) mmStudio;
	}

	@Override
	public String getCopyright() {
		return COPYRIGHT_NOTICE;
	}

	@Override
	public String getHelpText() {
		return DESCRIPTION;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public String getVersion() {
		return VERSION_INFO;
	}

	@Override
	public String getSubMenu() {
		return "";
	}

	@Override
	public void onPluginSelected() {
		String configFileName = "maars_config.xml";
		InputStream inStream = null;
		if (FileUtils.exists(configFileName)) {
			try {
				inStream = new FileInputStream(configFileName);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

		} else {
			inStream = getClass().getResourceAsStream(File.separator + "maars_default_config.xml");
		}
		MaarsParameters parameters = new MaarsParameters(inStream);
		new MaarsMainDialog(mmStudio, parameters).show();
	}
}
