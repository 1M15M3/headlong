package com.esaulpaugh.headlong.rlp.util;

import java.util.Arrays;
import java.util.Random;

public final class FastHex {

    private static final int[] ENCODE_TABLE = new int[1 << Byte.SIZE];
    private static final int[] DECODE_TABLE = new int[1 << Byte.SIZE];

    static {
        final int[] ints = new int[] {
                '0', '1', '2', '3',
                '4', '5', '6', '7',
                '8', '9', 'A', 'B',
                'C', 'D', 'E', 'F' };
        for (int i = 0; i < ENCODE_TABLE.length; i++) {
            int val = (ints[(i & 0xF0) >>> 4] << 8) | ints[i & 0x0F];
            ENCODE_TABLE[i] = val;
        }

        Arrays.fill(DECODE_TABLE, -1);

        DECODE_TABLE['0'] = 0;
        DECODE_TABLE['1'] = 1;
        DECODE_TABLE['2'] = 2;
        DECODE_TABLE['3'] = 3;
        DECODE_TABLE['4'] = 4;
        DECODE_TABLE['5'] = 5;
        DECODE_TABLE['6'] = 6;
        DECODE_TABLE['7'] = 7;
        DECODE_TABLE['8'] = 8;
        DECODE_TABLE['9'] = 9;
        DECODE_TABLE['A'] = 10;
        DECODE_TABLE['B'] = 11;
        DECODE_TABLE['C'] = 12;
        DECODE_TABLE['D'] = 13;
        DECODE_TABLE['E'] = 14;
        DECODE_TABLE['F'] = 15;
        DECODE_TABLE['a'] = 10;
        DECODE_TABLE['b'] = 11;
        DECODE_TABLE['c'] = 12;
        DECODE_TABLE['d'] = 13;
        DECODE_TABLE['e'] = 14;
        DECODE_TABLE['f'] = 15;
    }

    public static String encode(byte[] buffer, final int offset, final int length) {
        final int end = offset + length;
        char[] chars = new char[length << 1];
        for (int i = offset, j = 0; i < end; i++, j+=2) {
            int x = ENCODE_TABLE[buffer[i] & 0xFF];
            chars[j] = (char) (x >>> Byte.SIZE);
            chars[j + 1] = (char) (x & 0xFF);
        }
        return new String(chars);
    }

    public static byte[] decode(String hex, final int offset, final int length) {
        if ((length & 0b1) != 0) {
            throw new IllegalArgumentException("length must be a multiple of two");
        }
        final int bytesLen = length >> 1;
        byte[] bytes = new byte[bytesLen];
        for (int i = 0, j = offset; i < bytesLen; i++, j+=2) {
            int a = DECODE_TABLE[hex.charAt(j)];
            if (a == -1) {
                throw new IllegalArgumentException("illegal char @ " + j);
            }
            int b = DECODE_TABLE[hex.charAt(j + 1)];
            if (b == -1) {
                throw new IllegalArgumentException("illegal char @ " + (j + 1));
            }
            bytes[i] = (byte) ((a << 4) | b);
        }
        return bytes;
    }

    public static void main(String[] args0) {
        byte[] test = new byte[] { -128, -127, -126, -1, 0, 1, 2, 3, 4, 5 };
        String s = encode(test, 0, test.length);
        System.out.println(s);
        byte[] bb = decode(s, 0, s.length());
        System.out.println(Arrays.toString(bb) + " " + Arrays.equals(bb, test));


//        System.out.println(org.spongycastle.util.encoders.Hex.toHexString(test, 0, test.length));

        Random r = new Random();
        final int n = 1000;
        byte[][] randoms = new byte[n][];
        for (int i = 0; i < n; i++) {
            byte[] x = new byte[r.nextInt(100)];
            r.nextBytes(x);
            randoms[i] = x;
        }

        long start, end;

        String str, str2;
        byte[] bytes = null;

        for (byte[] rando : randoms) {
            str = encode(rando, 0, rando.length);
            bytes = decode(str, 0, str.length()); // ---------- 1.76
//            bytes = org.spongycastle.util.encoders.Hex.decode(str);

//            str2 = org.spongycastle.util.encoders.Hex.toHexString(rando, 0, rando.length);
//            if(str.toLowerCase().equals(str2)) {
////                System.out.println("good in the hood");
//            } else {
//                throw new Error("no good " + str + " " + str2);
//            }
        }
        start = System.nanoTime();
        for (byte[] rando : randoms) {
            str = encode(rando, 0, rando.length); // 0.88
            bytes = decode(str, 0, str.length()); // 2.14
//            bytes = org.spongycastle.util.encoders.Hex.decode(str); // 4.18
        }
        end = System.nanoTime();

        System.out.println((end - start) / 1000000.0 + " " + Arrays.toString(bytes));

        byte[] x = decode("BB", 0, 2);
        System.out.println(x.length + " " + Arrays.toString(x));
    }
}

