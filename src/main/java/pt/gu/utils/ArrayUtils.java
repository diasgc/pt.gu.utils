package pt.gu.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.util.Function;
import androidx.collection.ArrayMap;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class ArrayUtils {

    @SafeVarargs
    public static <T> void addAll(List<T> list, @Nullable T... elements){
        if (elements != null && elements.length > 0)
            list.addAll(Arrays.asList(elements));
    }

    @SafeVarargs
    public static <T> void addIf(List<T> list, boolean condition, T... elements){
        if (condition)
            addAll(list,elements);
    }

    public static <T> void addIf(List<T> dst, List<T> src, Predicate<T> filter){
        for(T t : src){
            if (filter.test(t))
                dst.add(t);
        }
    }

    //region Primivites toArray functions

    public static byte[] toArray(Collection<Byte> collection, byte valueOfNull){
        final byte[] out = new byte[collection.size()];
        int i = 0;
        for (Byte e : collection)
            out[i++] = e == null ? valueOfNull : e;
        return out;
    }

    public static int[] toArray(Collection<Integer> collection, int valueOfNull){
        final int[] out = new int[collection.size()];
        int i = 0;
        for (Integer e : collection)
            out[i++] = e == null ? valueOfNull : e;
        return out;
    }

    public static long[] toArray(Collection<Long> collection, long valueOfNull){
        final long[] out = new long[collection.size()];
        int i = 0;
        for (Long e : collection)
            out[i++] = e == null ? valueOfNull : e;
        return out;
    }

    public static float[] toArray(Collection<Float> collection, float valueOfNull){
        final float[] out = new float[collection.size()];
        int i = 0;
        for (Float e : collection)
            out[i++] = e == null ? valueOfNull : e;
        return out;
    }

    public static double[] toArray(Collection<Double> collection, double valueOfNull){
        final double[] out = new double[collection.size()];
        int i = 0;
        for (Double e : collection)
            out[i++] = e == null ? valueOfNull : e;
        return out;
    }

    //endregion


    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new ArrayList<>(map.entrySet());
        list.sort(Map.Entry.comparingByValue());

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }

    public static <K,V> Map<V, K> swapMap(Map<K, V> map) {
        Map<V,K> out = new ArrayMap<>();
        List<V> vl = new ArrayList<>(map.values());
        List<K> kl = new ArrayList<>(map.keySet());
        for (int i = 0 ; i < map.size() ; i++){
            out.put(vl.get(i),kl.get(i));
        }
        return out;
    }

    @SafeVarargs
    @NonNull
    public static <T> List<T> listOf(@Nullable T... elements) {
        List<T> l = new ArrayList<>();
        if (elements != null){
            l.addAll(Arrays.asList(elements));
        }
        return l;
    }

    public static <T> List<T> asList(Iterator<T> i) {
        List<T> list = new ArrayList<>();
        while (i.hasNext())
            list.add(i.next());
        return list;
    }

    public static <T> boolean isEmpty(List<T> array) {
        return array == null || array.size() == 0;
    }

    @SafeVarargs
    public static <T> boolean isEmpty(T... array) {
        return array == null || array.length == 0;
    }

    public static class ListX<T> extends ArrayList<T> {

        @SafeVarargs
        public final ListX<T> add(T... e){
            if (e != null && e.length > 0)
                addAll(Arrays.asList(e));
            return this;
        }

        @SafeVarargs
        public final ListX<T> add(boolean condition, T... e){
            if (condition)
                add(e);
            return this;
        }

        public <E> E[] toArrayOf(Function<T,E> fn) {
            ArrayList<E> out = new ArrayList<>();
            for (T i : this)
                out.add(fn.apply(i));
            Class<?> cls = out.getClass().getComponentType();
            return cls == null ? null : out.toArray((E[]) Array.newInstance(cls,size()));
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
