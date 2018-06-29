import maars.utils.ImgUtils;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.records.listener.impl.LogRecordListener;
import org.datavec.api.split.FileSplit;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

@RunWith(Parameterized.class)
public class  ClassificationTest {
   private static Logger logger = LoggerFactory.getLogger(ClassificationTest.class);

   @Parameterized.Parameters
   public static Collection<Object[]> prepareFiles() throws IOException {
      // image information
      // 300*300
      // RGB (will be in gray scale
      int height =300;
      int width = 300;
      int channels = 1;
      int rngseed = 123;
      Random randNumGen = new Random(rngseed);
      int batchSize = 1;
      int outputNum = 2;

      String root = System.getProperty("user.dir") + File.separator + "src"+ File.separator + "main"+ File.separator + "resources"+ File.separator;
      FileSplit train = ImgUtils.loadImages(root + "dl" + File.separator + "train_spb", randNumGen);
      FileSplit validate = ImgUtils.loadImages(root + "dl" + File.separator + "validate_spb", randNumGen);
      assert train.length() == 6;
      assert validate.length() == 6;

      ParentPathLabelGenerator labelGenerator = new ParentPathLabelGenerator();
      ImageRecordReader recordReader = new ImageRecordReader(height, width, channels, labelGenerator);

      // add a listener, to extract the name
      recordReader.initialize(train);
      recordReader.setListeners(new LogRecordListener());

      DataSetIterator traindataIter =  new RecordReaderDataSetIterator(recordReader, batchSize, 1, outputNum);
      for (int i = 0; i < 3; i++){
         DataSet ds = traindataIter.next();
         logger.info(ds + "");
         logger.info(traindataIter.getLabels() + "");
      }
      return Arrays.asList(new Object[][] {{traindataIter}});
   }
   @Parameterized.Parameter
   public DataSetIterator traindataIter;

//   @Parameterized.Parameter(1)
//   public DataSetIterator fluoimg;

//   @Test
//   public void heihei(){
//   }
}
