package maars.segmentPombe;

import ij.IJ;
import ij.ImagePlus;
import org.datavec.api.io.labels.ParentPathLabelGenerator;
import org.datavec.api.records.listener.impl.LogRecordListener;
import org.datavec.api.split.FileSplit;
import org.datavec.image.loader.ImageLoader;
import org.datavec.image.loader.NativeImageLoader;
import org.datavec.image.recordreader.ImageRecordReader;
import org.datavec.image.recordreader.VideoRecordReader;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.dataset.api.preprocessor.DataNormalization;
import org.nd4j.linalg.dataset.api.preprocessor.ImagePreProcessingScaler;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by tong on 02/10/17.
 */
public class nd4j_test {
   private static Logger log = LoggerFactory.getLogger(nd4j_test.class);

   public static void main(String[] args) throws Exception{
      int height= 2160;
      int width = 2560;
      int channels = 3;
      int rngseed = 123;
      Random randNumGen = new Random(rngseed);
      int batchSize = 1;
      int outputNum = 2;

      ImagePlus img = IJ.openImage("/media/tong/MAARSData/MAARSData/mod20/12-06-1/BF_1/_1_MMStack_Pos0.ome.tif");
//      File testData =  new File("/home/tong/Dropbox/data/validation");
      System.out.println(Arrays.toString(img.getDimensions()));
      INDArray totalArray = Nd4j.create(height, width, img.getDimensions()[3]);
      for (int i=0; i<img.getDimensions()[3];i++){
         img.setZ(i);
         INDArray array = Nd4j.create(img.getProcessor().getFloatArray());
         totalArray.put(new INDArrayIndex[]{NDArrayIndex.all(), NDArrayIndex.all(), NDArrayIndex.point(i)}, array);
      }
      System.out.println(totalArray.get(NDArrayIndex.point(0), NDArrayIndex.point(0), NDArrayIndex.all()));

//      NativeImageLoader loader = new NativeImageLoader(height, width,channels);
//      System.out.println(loader.asMatrix(trainData).shape());

      //FileSplit(Path, allowed format, random)
//      FileSplit train = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);
//      FileSplit test = new FileSplit(testData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);

//      ParentPathLabelGenerator labelMarker = new ParentPathLabelGenerator();
//      VideoRecordReader recordReader = new VideoRecordReader(height,width);
//      recordReader.initialize(train);
//      recordReader.setListeners(new LogRecordListener());
//
//      DataSetIterator dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1 , outputNum);

//      DataNormalization scaler = new ImagePreProcessingScaler(0,1);
//      scaler.fit(dataIter);
//      dataIter.setPreProcessor(scaler);
//
//      for (int i =0; i<1; i++){
//         DataSet ds = dataIter.next();
//         System.out.println(ds);
//         System.out.println(dataIter.getLabels());
//      }
   }
}
