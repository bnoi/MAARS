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

        static public final String VERSION_INFO = "1.0.0";
        static private final String COPYRIGHT_NOTICE = "BSD License, 2015";
        static private final String DESCRIPTION = "Micro-Manager plugin for Mitotic Analysing And Recording System";
        static private final String NAME = "MAARS";

	@Override
	public void setContext(Studio mmStudio) {
            // TODO Auto-generated method stub
            this.mmStudio = (MMStudio) mmStudio;
            this.mmc = mmStudio.core();
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
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            } else {
                inStream = getClass().getResourceAsStream("/maars_default_config.xml");
            }
            parameters = new MaarsParameters(inStream);
            new MaarsMainDialog(mmStudio, mmc, parameters).show();
	}
}
