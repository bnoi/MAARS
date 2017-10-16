//package maars.segmentPombe;
//
////import org.datavec.api.io.filters.BalancedPathFilter;
////import org.datavec.api.io.labels.ParentPathLabelGenerator;
////import org.datavec.api.split.FileSplit;
////import org.datavec.api.split.InputSplit;
////import org.datavec.image.loader.ImageLoader;
//import org.bytedeco.javacpp.opencv_core.*;
//import org.bytedeco.javacpp.opencv_highgui;
//import org.bytedeco.javacpp.opencv_highgui.*;
//import org.bytedeco.javacpp.opencv_imgcodecs;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.File;
//import java.util.Random;
//
///**
// * Created by tong on 02/10/17.
// */
//public class nd4j_test {
//   private static Logger log = LoggerFactory.getLogger(nd4j_test.class);
//
//   public static void main(String[] args) throws Exception{
//      int rngseed = 123;
//      Random randNumGen = new Random(rngseed);
//      int batchSize = 1;
//      int outputNum = 2;
//      String p = "/Users/tongli/Desktop/100x/Mitosis/795_wt/figs/102.png";
//      File parentDir =  new File(p);
//      Mat img = opencv_imgcodecs.imread(p);
//      opencv_highgui.namedWindow("Linear Blend", opencv_highgui.WINDOW_AUTOSIZE); // Create Window
//
//      opencv_highgui.startWindowThread();
//      opencv_highgui.namedWindow("preview");
//      opencv_highgui.imshow("test", img);
//      opencv_highgui.waitKey(0);
////      FileSplit filesInDir = new FileSplit(parentDir, ImageLoader.ALLOWED_FORMATS, randNumGen);
////      ParentPathLabelGenerator labelMaker = new ParentPathLabelGenerator();
////      BalancedPathFilter pathFilter = new BalancedPathFilter(randNumGen, ImageLoader.ALLOWED_FORMATS, labelMaker);
////
////      InputSplit[] filesInDirSplit = filesInDir.sample(pathFilter, 20, 50);
////      InputSplit trainData = filesInDirSplit[0];
////      InputSplit testData = filesInDirSplit[1];
////      System.out.println(trainData.length());
////      System.out.println(testData.length());
//
//
////      NativeImageLoader loader = new NativeImageLoader();
//
////      System.out.println(loader.asMatrix(trainData).shape());
//
//      //FileSplit(Path, allowed format, random)
////      FileSplit train = new FileSplit(trainData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);
////      FileSplit test = new FileSplit(testData, NativeImageLoader.ALLOWED_FORMATS, randNumGen);
//
////      ParentPathLabelGenerator labelMarker = new ParentPathLabelGenerator();
////      VideoRecordReader recordReader = new VideoRecordReader(height,width);
////      recordReader.initialize(train);
////      recordReader.setListeners(new LogRecordListener());
////
////      DataSetIterator dataIter = new RecordReaderDataSetIterator(recordReader, batchSize, 1 , outputNum);
//
////      DataNormalization scaler = new ImagePreProcessingScaler(0,1);
////      scaler.fit(dataIter);
////      dataIter.setPreProcessor(scaler);
////
////      for (int i =0; i<1; i++){
////         DataSet ds = dataIter.next();
////         System.out.println(ds);
////         System.out.println(dataIter.getLabels());
////      }
//
//
//      //      INDArray out = totalArray.get(NDArrayIndex.point(0), NDArrayIndex.point(0), NDArrayIndex.all());
////      System.out.println(out);
////      INDArray on = totalArray.get(NDArrayIndex.point(365), NDArrayIndex.point(193), NDArrayIndex.all());
////      System.out.println(on);
////      XYSeries outData = new XYSeries("Out");
////      for (int i=0; i<out.length(); i++){
////         outData.add(i, out.getDouble(i));
////      }
////      final XYSeriesCollection data = new XYSeriesCollection(outData);
////      XYSeries oneData = new XYSeries("on");
////      for (int i=0; i<on.length(); i++){
////         oneData.add(i, on.getDouble(i));
////      }
////
////      data.addSeries(oneData);
////      final JFreeChart chart = ChartFactory.createXYLineChart(
////            "XY Series Demo",
////            "X",
////            "Y",
////            data,
////            PlotOrientation.VERTICAL,
////            true,
////            true,
////            false
////      );
//      //
////      final ChartPanel chartPanel = new ChartPanel(chart);
////      chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
////      ApplicationFrame frame = new ApplicationFrame("test");
////      frame.setContentPane(chartPanel);
////      frame.setVisible(true);
////      RefineryUtilities.centerFrameOnScreen(frame);
//   }
//}
