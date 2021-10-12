package minicpbp.util;

import minicpbp.engine.core.IntVar;

public class ArrayUtil {


    public static IntVar[] append(IntVar[] a1, IntVar a) {
        IntVar[] newArray = new IntVar[a1.length +1];
        System.arraycopy(a1,0,newArray, 0,a1.length);
        newArray[a1.length] = a;
        return newArray;
    }

    public static IntVar[] append(IntVar a, IntVar[] a1) {
        IntVar[] newArray = new IntVar[a1.length +1];
        System.arraycopy(a1,0,newArray, 1,a1.length);
        newArray[0] = a;
        return newArray;
    }

    public static IntVar[] append(IntVar[] a1, IntVar[] a2) {
        IntVar[] newArray = new IntVar[a1.length +a2.length];
        System.arraycopy(a1,0,newArray, 0,a1.length);
        System.arraycopy(a2,0,newArray, a1.length,a2.length);
        return newArray;
    }
}
