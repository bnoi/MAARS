package maars.segmentPombe;

import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.records.listener.impl.LogRecordListener;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.RBM;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Random;

/**
 * Created by tong on 02/10/17.
 */
public class nd4j_test {
   private static Logger log = LoggerFactory.getLogger(nd4j_test.class);

   public static void main(String[] args) throws Exception{
      int height= 300;
      int width = 300;
      int channels = 3;
      int rngseed = 123;
      Random randNumGen = new Random(rngseed);
      int batchSize = 1;
      int outputNum = 2;

      File trainData =  new File("/home/tong/Dropbox/data/train");
      File testData =  new File("/home/tong/Dropbox/data/validation");

      //FileSplit(Path, allowed format, random)
      FileSplit train = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);
      FileSplit test = new FileSplit(testData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);

      ParentPathLabelGenerator labelMarker = new ParentPathLabelGenerator();
      ImageRecordReader recordReader = new ImageRecordReader(height,width,channels,labelMarker);
      recordReader.initialize(train);
      recordReader.setListeners(new LogRecordListener());

      DataSetIterator dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1 , outputNum);

      DataNormalization scaler = new ImagePreProcessingScaler(0,1);
      scaler.fit(dataIter);
      dataIter.setPreProcessor(scaler);

      for (int i =0; i<1; i++){
         DataSet ds = dataIter.next();
         System.out.println(ds);
         System.out.println(dataIter.getLabels());
      }

      NeuralNetConfiguration conf = new NeuralNetConfiguration.Builder()
            .iterations(100)
            .layer(new RBM.Builder().nIn(900000).nOut(2).build())
            .build();
//      MultiLayerNetwork network = new MultiLayerNetwork(conf);



   }
}
