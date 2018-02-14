import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.SCIFIOImgPlus;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class  BFSegTest {
   private static Logger logger = LoggerFactory.getLogger(BFSegTest.class);
   @Parameterized.Parameters
   public static Collection<Object[]> prepareFiles() throws ImgIOException {
      String root = System.getProperty("user.dir") + "/src/main/resources/";
      ImgOpener imgOpener = new ImgOpener();
      ImgFactory<FloatType> factory = new ArrayImgFactory<>();
      List<SCIFIOImgPlus<FloatType>> bfimages =  imgOpener.openImgs( root + "BF.tif", factory, new FloatType());
      List<SCIFIOImgPlus<FloatType>> fluoimages =  imgOpener.openImgs( root + "FLUO.tif", factory, new FloatType());
      return Arrays.asList(new Object[][] {{bfimages.get(0), fluoimages.get(0)}});
   }
   @Parameterized.Parameter
   public SCIFIOImgPlus <FloatType> bfimg;

   @Parameterized.Parameter(1)
   public SCIFIOImgPlus <FloatType> fluoimg;

   @Test
   public void show(){
      ImageJFunctions.show( bfimg );
      ImageJFunctions.show( fluoimg );
//      OpService ops = new DefaultOpService();
//      FinalInterval finalInterval = new FinalInterval(0,0,5,5);
//
//      RandomAccessibleInterval<UnsignedByteType> view = Views.interval(bfimg.getImg(), finalInterval);
//      ImageJFunctions.show( view );
//      logger.info((int) ops.run(Ops.Math.Add.class, 1,2) + "");
//      int x = (int) ops.run("math.add", 1,1);
//      System.out.println(x);
//      RandomAccessibleInterval
//      logger.info(x + "");
//      ImageJFunctions.show( img. );
      try {
         Thread.sleep(100000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
   }
}
