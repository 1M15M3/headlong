package com.esaulpaugh.headlong.util;

import java.util.Arrays;

/**
 * Uses a larger encoding table to speed up encoding.
 */
public final class FastHex {

    private static final int NIBBLE_BITS = Byte.SIZE / 2;

    // Byte values index directly into the encoding table (size 256) whose elements contain two char values each,
    // encoded together as an int.
    private static final int[] ENCODE_TABLE = new int[1 << Byte.SIZE];

    // Char values index directly into the decoding table (size 256).
    private static final byte[] DECODE_TABLE = new byte[1 << Byte.SIZE];

    private static final byte NO_MAPPING = -1;

    static {
        final int[] ints = new int[] {
                '0', '1', '2', '3',
                '4', '5', '6', '7',
                '8', '9', 'a', 'b',
                'c', 'd', 'e', 'f' };
        for (int i = 0; i < ENCODE_TABLE.length; i++) {
            ENCODE_TABLE[i] = (ints[(i & 0xF0) >>> NIBBLE_BITS] << Byte.SIZE) | ints[i & 0x0F];
        }

        Arrays.fill(DECODE_TABLE, NO_MAPPING);

        DECODE_TABLE['0'] = 0x00;
        DECODE_TABLE['1'] = 0x01;
        DECODE_TABLE['2'] = 0x02;
        DECODE_TABLE['3'] = 0x03;
        DECODE_TABLE['4'] = 0x04;
        DECODE_TABLE['5'] = 0x05;
        DECODE_TABLE['6'] = 0x06;
        DECODE_TABLE['7'] = 0x07;
        DECODE_TABLE['8'] = 0x08;
        DECODE_TABLE['9'] = 0x09;
        DECODE_TABLE['A'] = DECODE_TABLE['a'] = 0x0a;
        DECODE_TABLE['B'] = DECODE_TABLE['b'] = 0x0b;
        DECODE_TABLE['C'] = DECODE_TABLE['c'] = 0x0c;
        DECODE_TABLE['D'] = DECODE_TABLE['d'] = 0x0d;
        DECODE_TABLE['E'] = DECODE_TABLE['e'] = 0x0e;
        DECODE_TABLE['F'] = DECODE_TABLE['f'] = 0x0f;
    }

    public static String encodeToString(byte b) {
        return encodeToString(new byte[] { b });
    }

    public static String encodeToString(byte[] buffer) {
        return encodeToString(buffer, 0, buffer.length);
    }

    public static String encodeToString(byte[] buffer, int off, final int len) {
        final int end = off + len;
        char[] chars = new char[len << 1];
        for (int j = 0; off < end; off++, j+=2) {
            int hexPair = ENCODE_TABLE[buffer[off] & 0xFF];
            chars[j] = (char) (hexPair >>> Byte.SIZE); // left char
            chars[j+1] = (char) (hexPair & 0xFF); // right char
        }
        return new String(chars);
    }

    public static byte[] encodeToBytes(byte[] buffer, int off, final int len) {
        final int end = off + len;
        byte[] bytes = new byte[len << 1];
        for (int j = 0; off < end; off++, j+=2) {
            int hexPair = ENCODE_TABLE[buffer[off] & 0xFF];
            bytes[j] = (byte) (hexPair >>> Byte.SIZE); // left
            bytes[j+1] = (byte) (hexPair & 0xFF); // right
        }
        return bytes;
    }

    public static byte[] decode(String hex) {
        return decode(hex, 0, hex.length());
    }

    public static byte[] decode(String hex, int offset, int length) {
        return decode(hex.getBytes(Utils.CHARSET_ASCII), offset, length);
    }

    public static byte[] decode(byte[] hexBytes, int off, final int len) {
        if ((len & 0x01) != 0) {
            throw new IllegalArgumentException("length must be a multiple of two");
        }
        final int bytesLen = len >> 1;
        byte[] bytes = new byte[bytesLen];
        for (int i = 0; i < bytesLen; i++, off+=2) {
            byte left = DECODE_TABLE[hexBytes[off]];
            if (left == NO_MAPPING) {
                throw new IllegalArgumentException("illegal val @ " + off);
            }
            byte right = DECODE_TABLE[hexBytes[off+1]];
            if (right == NO_MAPPING) {
                throw new IllegalArgumentException("illegal val @ " + (off + 1));
            }
            bytes[i] = (byte) ((left << NIBBLE_BITS) | right);
        }
        return bytes;
    }
}

