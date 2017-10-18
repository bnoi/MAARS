import maars.gui.MaarsMainDialog;
import maars.main.Maars_Interface;
import org.micromanager.MenuPlugin;
import org.micromanager.Studio;
import org.micromanager.internal.MMStudio;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.SciJavaPlugin;

/**
 * @author Tong LI, mail: tongli.bioinfo@gmail.com
 * @version Nov 21, 2015
 */
@Plugin(type = MenuPlugin.class)
public class MAARSPlugin implements org.micromanager.MenuPlugin, SciJavaPlugin {

   static final String VERSION_INFO = "1.0.0";
   static private final String COPYRIGHT_NOTICE = "BSD compatible CeCILL-B License, 2017";
   static private final String DESCRIPTION = "Micro-Manager plugin for Mitotic Analysis And Recording System";
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
      new MaarsMainDialog(mmStudio, Maars_Interface.loadParameters());
      Maars_Interface.copyDeps();
   }
}
