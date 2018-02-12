import ij.ImageJ;
import io.scif.config.SCIFIOConfig;
import io.scif.img.ImgIOException;
import io.scif.img.ImgOpener;
import io.scif.img.SCIFIOImgPlus;
import maars.headless.batchSegmentation.DefaultBatchSegmentation;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.cell.CellImg;
import net.imglib2.img.cell.CellImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class  BFSegTest {
   private static Logger logger = LoggerFactory.getLogger(BFSegTest.class);
   @Parameterized.Parameters
   public static < T extends NumericType< T > & NativeType< T > > Collection<Object[]> prepareFiles() throws ImgIOException {
      String root = System.getProperty("user.dir") + "/src/main/resources/";
      ImgOpener imgOpener = new ImgOpener();
      SCIFIOConfig config = new SCIFIOConfig();
      config.imgOpenerSetImgModes( SCIFIOConfig.ImgMode.PLANAR );
      List<SCIFIOImgPlus<?>> bfimages =  imgOpener.openImgs( root + "BF.tif", config);
      List<SCIFIOImgPlus<?>> fluoimages =  imgOpener.openImgs( root + "FLUO.tif", config);

      return Arrays.asList(new Object[][] {{bfimages.get(0), fluoimages.get(0)}});
   }
   @Parameterized.Parameter
   public SCIFIOImgPlus bfimg;

   @Parameterized.Parameter(1)
   public SCIFIOImgPlus fluoimg;

   @Test
   public void show(){
      ImageJFunctions.show( bfimg );
      ImageJFunctions.show( fluoimg );
      try {
         Thread.sleep(10000);
      } catch (InterruptedException e) {
         e.printStackTrace();
      }
   }
}
