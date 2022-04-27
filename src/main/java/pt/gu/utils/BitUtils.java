package pt.gu.utils;

import java.io.IOException;
import java.io.OutputStream;

public class BitUtils {

    public static boolean matchFlag(int flag, int matchingMask, int notMatchingMask){
        return  ((flag & matchingMask) == matchingMask) && (notMatchingMask == 0 || ((flag & notMatchingMask) != notMatchingMask));
    }

    public static boolean matchFlag(int flag, int matchingMask, boolean value){
        return ((flag & matchingMask) == matchingMask) ^ !value;
    }

    public static boolean getFlag(int flags, int flag){
        return (flags & flag) == flag;
    }

    public static boolean testAny(int flags, int test){
        return (flags & test) != 0;
    }

    public static boolean getFlag(long flags, int flag){
        return (flags & flag) == flag;
    }

    public static int toggleFlag(int flags, int flag) {
        flags ^= flag;
        return flags;
    }

    public static int setFlag(int flags, int flag, boolean value) {
        flags = value ? flags | flag : flags & ~flag;
        return flags;
    }

    public static int countBits(int data, int startBit, int endBit) {
        int d = data;
        int r = 0;
        d = d >> startBit;
        for (int i = startBit ; i < endBit - startBit ; i++) {
            if ((d & 1) == 1)
                r++;
            d = d >> 1;
        }
        return r;
    }

    public static class IntFlagBuilder {

        int mFlag = 0;

        public IntFlagBuilder(){}

        public IntFlagBuilder(int flags){
            mFlag = flags;
        }

        public IntFlagBuilder set(int flag){
            mFlag |= flag;
            return this;
        }

        public IntFlagBuilder reset(int flag){
            mFlag &= ~flag;
            return this;
        }

        public IntFlagBuilder setIf(boolean condition, int flag){
            if (condition)
                mFlag |= flag;
            return this;
        }

        public IntFlagBuilder resetIf(boolean condition, int flag){
            if (condition)
                mFlag &= ~flag;
            return this;
        }

        public int get(){
            return mFlag;
        }
    }


    public static final class BitOutputStream implements AutoCloseable {

        private OutputStream out;
        private long bitBuffer;
        private int bitBufferLen;
        public int crc8;
        public int crc16;


        public BitOutputStream(OutputStream out) {
            this.out = out;
            bitBuffer = 0;
            bitBufferLen = 0;
            resetCrcs();
        }


        public void resetCrcs() {
            crc8 = 0;
            crc16 = 0;
        }


        public void alignToByte() throws IOException {
            writeInt((64 - bitBufferLen) % 8, 0);
        }


        public int writeInt(int n, int val) throws IOException {
            bitBuffer = (bitBuffer << n) | (val & ((1L << n) - 1));
            bitBufferLen += n;
            while (bitBufferLen >= 8) {
                bitBufferLen -= 8;
                int b = (int) (bitBuffer >>> bitBufferLen) & 0xFF;
                out.write(b);
                crc8 ^= b;
                crc16 ^= b << 8;
                for (int i = 0; i < 8; i++) {
                    crc8 = (crc8 << 1) ^ ((crc8 >>> 7) * 0x107);
                    crc16 = (crc16 << 1) ^ ((crc16 >>> 15) * 0x18005);
                }
            }
            return n;
        }

        public int writeString(String string) throws IOException{
            byte[] b =string.getBytes();
            out.write(b);
            return b.length * 8;
        }


        public void close() throws IOException {
            out.close();
        }
    }
}
