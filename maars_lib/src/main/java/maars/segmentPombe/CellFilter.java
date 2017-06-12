package maars.segmentPombe;

/**
 * Created by tongli on 27/04/2017.
 */
public class CellFilter {
   int parameter_;
   double min_;
   double max_;

   public CellFilter(int parameter, double min, double max) {
      parameter_ = parameter;
      min_ = min;
      max_ = max;
   }
}
