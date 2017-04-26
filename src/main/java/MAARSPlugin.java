import edu.univ_tlse3.gui.MaarsMainDialog;
import edu.univ_tlse3.maars.MaarsParameters;
import edu.univ_tlse3.utils.FileUtils;
import edu.univ_tlse3.utils.IOUtils;
import ij.IJ;
import org.micromanager.MenuPlugin;
import org.micromanager.Studio;
import org.micromanager.internal.MMStudio;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 21, 2015
 */
@Plugin(type = MenuPlugin.class)
public class MAARSPlugin implements org.micromanager.MenuPlugin, SciJavaPlugin {

    private static final String VERSION_INFO = "1.0.0";
    static private final String COPYRIGHT_NOTICE = "BSD compatible CeCILL-B License, 2017";
    static private final String DESCRIPTION = "Micro-Manager plugin for Mitotic Analysing And Recording System";
    static private final String NAME = "MAARS";
    private MMStudio mmStudio;

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
                IOUtils.printErrorToIJLog(e);
            }

        } else {
            inStream = FileUtils.getInputStreamOfScript("maars_default_config.xml");
        }
        MaarsParameters parameters = new MaarsParameters(inStream);
        new MaarsMainDialog(mmStudio, parameters).setVisible(true);
        if (!FileUtils.exists(IJ.getDirectory("plugins") + "/MAARS_deps")) {
            FileUtils.createFolder(IJ.getDirectory("plugins") + "/MAARS_deps");
        }
    }
}
