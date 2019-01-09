package test;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FastutilTest {
    public static void main(String[] args) {
        IntList fastutilList = new IntArrayList();
      //  List<Integer> fastutilList = new ArrayList<Integer>();
        long st = new Date().getTime();
        for(int i=0;i<100000;i++){
            fastutilList.add(i);
        }
        long et = new Date().getTime();
        System.out.println(et-st);
        System.out.println(fastutilList.size());

    }
}
