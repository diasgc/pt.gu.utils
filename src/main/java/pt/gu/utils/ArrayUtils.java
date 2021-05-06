package pt.gu.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class ArrayUtils {

    @SafeVarargs
    public static <T> void addAll(List<T> list, T... elements){
        list.addAll(Arrays.asList(elements));
    }

    public static <T> void addIf(List<T> list, boolean condition, T element){
        if (condition)
            list.add(element);
    }

    public static <T> void addIf(List<T> dst, List<T> src, Predicate<T> filter){
        for(T t : src){
            if (filter.test(t))
                dst.add(t);
        }
    }

    public static class ListX<T> extends ArrayList<T> {

        @SafeVarargs
        public final ListX<T> add(T... e){
            if (e != null && e.length > 0)
                addAll(Arrays.asList(e));
            return this;
        }

        public ListX<T> add(boolean condition, T e){
            if (condition)
                add(e);
            return this;
        }
    }

    public static class BitArray extends ArrayList<Boolean> {

        public byte[] toByteArray(){
            byte[] data = new byte[(int)Math.ceil(size()/8.0)];
            for (int i = 0 ; i < size() ; i++){
                if (get(i)) data[i/8] |= 1 << (i % 8);
            }
            return data;
        }

        private byte[] toByteArray(int startBitOffset, int len){
            if (startBitOffset + len >= size())
                len = size() - startBitOffset;
            if (len < 0)
                return new byte[0];
            byte[] data = new byte[(int)Math.ceil(len/8.0)];
            for (int i = 0 ; i < len ; i++){
                if (get(i + startBitOffset)) data[i/8] |= 1 << (i % 8);
            }
            return data;
        }

        public BitArray add(long l, int bits){
            for (int i = 0 ; i < bits ; i++){
                add((l & 1) == 1);
                l >>=1;
            }
            return this;
        }

        public BitArray add(byte b){
            return add(b,8);
        }

        public BitArray add(short b){
            return add(b,16);
        }

        public BitArray add(int b){
            return add(b,32);
        }

        public BitArray add(long l){
            return add(l,64);
        }

        public byte getByte(int bitoffset){
            byte[] d = toByteArray(bitoffset,8);
            return d.length > 0 ? d[0] : 0;
        }

        public short getShort(int bitoffset, boolean le){
            byte[] d = toByteArray(bitoffset,16);
            return d.length > 0 ?
                    (le ?
                            (short) ((d[0] << 8) | d[1]) :
                            (short) ((d[1] << 8) | d[0])
                    ) :
                    0;
        }

        public int getInt(int bitoffset, boolean le){
            byte[] d = toByteArray(bitoffset,32);
            return d.length > 0 ?
                    (le ?
                            (d[0] << 24) | (d[1] << 16) | (d[2] << 8) | d[3] :
                            (d[3] << 24) | (d[2] << 16) | (d[1] << 8) | d[0]
                    ) :
                    0;
        }

        public long getLong(int bitoffset, boolean le){
            byte[] d = toByteArray(bitoffset,64);
            return d.length > 0 ?
                    (le ?
                            ((long)d[0] << 56) | ((long)d[1] << 48) | ((long)d[2] << 40) | ((long)d[3] << 32) | (d[4] << 24) | (d[5] << 16) | (d[6] << 8) | d[7] :
                            ((long)d[7] << 56) | ((long)d[6] << 48) | ((long)d[5] << 40) | ((long)d[4] << 32) | (d[3] << 24) | (d[2] << 16) | (d[1] << 8) | d[0]
                    ) :
                    0;
        }
    }
}
