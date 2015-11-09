import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedByteType;
 
public class CreateAndDisplay
{
    public static void main( final String[] args )
    {
        final Img< UnsignedByteType > img = new ArrayImgFactory< UnsignedByteType >()
            .create( new long[] { 400, 320 }, new UnsignedByteType() );
        ImageJFunctions.show( img );
    }
}