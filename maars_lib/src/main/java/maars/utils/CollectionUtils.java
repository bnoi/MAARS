package maars.utils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by tong on 04/05/17.
 */
public class CollectionUtils {
   public static int size(Iterable itr) {
      if (itr instanceof Collection)
         return ((Collection<?>) itr).size();

      // else iterate

      int i = 0;
      for (Object obj : itr) i++;
      return i;
   }

   public static ArrayList toArrayList(Iterable itr) {
      ArrayList arrayList = new ArrayList();
      for (Object obj : itr) {
         arrayList.add(obj);
      }
      return arrayList;
   }
}
