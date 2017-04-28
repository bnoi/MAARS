import edu.univ_tlse3.cellstateanalysis.SetOfCells;
import edu.univ_tlse3.display.SOCVisualizer;
import edu.univ_tlse3.maars.MAARSNoAcq;
import edu.univ_tlse3.maars.MaarsParameters;
import edu.univ_tlse3.utils.IOUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tongli on 28/04/2017.
 */
public class GuiFreeRun {
    public static void main(String[] args) {
        String configFileName = "/Volumes/Macintosh/curioData/102/60x/26-10-1/maars_config.xml";
        InputStream inStream = null;
        try {
            inStream = new FileInputStream(configFileName);
        } catch (FileNotFoundException e) {
            IOUtils.printErrorToIJLog(e);
        }
        MaarsParameters parameters = new MaarsParameters(inStream);
        SetOfCells soc = new SetOfCells();
        SOCVisualizer socVisualizer = new SOCVisualizer();
        socVisualizer.createGUI(soc);
        ExecutorService es = Executors.newSingleThreadExecutor();
        es.execute(new MAARSNoAcq(parameters, socVisualizer, soc));
        es.shutdown();
    }
}
