package com.esaulpaugh.headlong.rlp.util;

import com.esaulpaugh.headlong.rlp.DecodeException;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

public class IntegersTest {

    @Test
    public void putGetByte_fast() throws DecodeException {
        byte[] one = new byte[1];
        for (int j = 0; j < 1_000_000; j++) {
            for (int i = -128; i < 128; i++) {
                byte b = (byte) i;
                int n = Integers.putByte(b, one, 0);
                byte r = Integers.getByte(one, 0, n);
                Assert.assertEquals(b, r);
            }
        }
    }

    @Test
    public void putGetShort_fast() throws DecodeException {
        byte[] two = new byte[2];
        for (int j = 0; j < 10_000; j++) {
            for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
                short s = (short) i;
                int n = Integers.putShort(s, two, 0);
                short r = Integers.getShort(two, 0, n);
                Assert.assertEquals(s, r);
            }
        }
    }

    @Test
    public void putGetInt_fast() throws DecodeException {
        byte[] four = new byte[4];
        for (long lo = Integer.MIN_VALUE; lo <= Integer.MAX_VALUE; lo++) {
            int i = (int) lo;
            int n = Integers.putInt(i, four, 0);
            int r = Integers.getInt(four, 0, n);
            Assert.assertEquals(i, r);
        }
    }

    @Test
    public void putGetByte_slow() throws DecodeException {
        byte[] one = new byte[1];
        for (int j = 0; j < 1_000_000; j++) {
            for (int i = -128; i < 128; i++) {
                byte b = (byte) i;
                int n = Integers.putLong(0xFFL & b, one, 0);
                byte r = Integers.getByte(one, 0, n);
                Assert.assertEquals(b, r);
            }
        }
    }

    @Test
    public void putGetShort_slow() throws DecodeException {
        byte[] two = new byte[2];
        for (int j = 0; j < 10_000; j++) {
            for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
                short s = (short) i;
                int n = Integers.putLong(0xFFFFL & s, two, 0);
                short r = Integers.getShort(two, 0, n);
                Assert.assertEquals(s, r);
            }
        }
    }

    /* ignored because it takes 16 seconds on laptop */
    // TODO forkjoin
//    @Ignore
    @Test
    public void putGetInt_slow() throws DecodeException {
        byte[] four = new byte[4];
        for (long lo = Integer.MIN_VALUE; lo <= Integer.MAX_VALUE; lo++) {
            int i = (int) lo;
            int n = Integers.putLong(0xFFFFFFFFL & i, four, 0);
            int r = Integers.getInt(four, 0, n);
            Assert.assertEquals(i, r);
        }
    }

    @Test
    public void putGetLong() throws DecodeException {
        Random rand = new Random(new SecureRandom().nextLong());
        byte[] eight = new byte[8];
        final long lim = Long.MAX_VALUE - (long) Math.pow(2.0, 24);
        for (long i = Long.MAX_VALUE; i >= lim; i--) {
            long lo = rand.nextLong();
            int n = Integers.putLong(lo, eight, 0);
            long r = Integers.getLong(eight, 0, n);
            Assert.assertEquals(lo, r);
        }
    }

    @Test
    public void lenByte() {
        for (int i = Byte.MIN_VALUE; i <= Byte.MAX_VALUE; i++) {
            byte b = (byte) i;
            int len = Integers.len(b);
            Assert.assertEquals(b == 0 ? 0 : 1, len);
        }
    }

    @Test
    public void lenShort() {
        for (int i = Short.MIN_VALUE; i <= Short.MAX_VALUE; i++) {
            short s = (short) i;
            int len = Integers.len(s);
            Assert.assertEquals(
                    s == 0
                            ? 0
                            : s > 0 && s < 256
                            ? 1
                            : 2,
                    len
            );
        }
    }

    @Test
    public void lenInt() {
        for (long lo = Integer.MIN_VALUE; lo <= Integer.MAX_VALUE; lo++) {
            int i = (int) lo;
            int len = Integers.len(i);
            Assert.assertEquals(
                    i == 0
                            ? 0
                            : i > 0 && i < 256
                            ? 1
                            : i >= 256 && i < 65536
                            ? 2
                            : i >= 65536 && i < 16777216
                            ? 3
                            : 4,
                    len
            );
        }
    }

    @Test
    public void lenLong() {
        Random rand = new Random(new SecureRandom().nextLong());

        for (int i = 0; i < Short.MAX_VALUE; i++) {
            long lo = rand.nextLong();
            int len = Integers.len(lo);
            Assert.assertEquals(
                    lo == 0
                            ? 0
                            : lo > 0 && lo < 256
                            ? 1
                            : lo >= 256 && lo < 65536
                            ? 2
                            : lo >= 65536 && lo < 16777216
                            ? 3
                            : lo >= 16777216 && lo < 4_294_967_296L
                            ? 4
                            : lo >= 4_294_967_296L && lo < 1_099_511_627_776L
                            ? 5
                            : lo >= 1_099_511_627_776L && lo < 281_474_976_710_656L
                            ? 6
                            : lo >= 281_474_976_710_656L && lo < 72_057_594_037_927_936L
                            ? 7
                            : 8,
                    len
            );
        }
    }

    @Test
    public void insertBytes() {
        byte[] ten = new byte[10];

        final byte a = 1, b = 11, c = 111, d = 9, e = 99, f = -1, g = -100, h = 64;

        Arrays.fill(ten, (byte) 0);
        Integers.insertBytes(0, ten, 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, a, b, c, d);
        Assert.assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, ten);

        Arrays.fill(ten, (byte) 0);
        Integers.insertBytes(1, ten, 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, a, b, c, d);
        Assert.assertArrayEquals(new byte[] { 0, d, 0, 0, 0, 0, 0, 0, 0, 0 }, ten);

        Arrays.fill(ten, (byte) 0);
        Integers.insertBytes(2, ten, 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, a, b, c, d);
        Assert.assertArrayEquals(new byte[] { 0, c, d, 0, 0, 0, 0, 0, 0, 0 }, ten);
        Arrays.fill(ten, (byte) 0);

        Arrays.fill(ten, (byte) 0);
        Integers.insertBytes(3, ten, 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, a, b, c, d);
        Assert.assertArrayEquals(new byte[] { 0, b, c, d, 0, 0, 0, 0, 0, 0 }, ten);

        Arrays.fill(ten, (byte) 0);
        Integers.insertBytes(4, ten, 1, (byte) 0, (byte) 0, (byte) 0, (byte) 0, a, b, c, d);
        Assert.assertArrayEquals(new byte[] { 0, a, b, c, d, 0, 0, 0, 0, 0 }, ten);

        Arrays.fill(ten, (byte) 0);
        Integers.insertBytes(5, ten, 1, (byte) 0, (byte) 0, (byte) 0, a, b, c, d, e);
        Assert.assertArrayEquals(new byte[] { 0, a, b, c, d, e, 0, 0, 0, 0 }, ten);

        Arrays.fill(ten, (byte) 0);
        Integers.insertBytes(6, ten, 1, (byte) 0, (byte) 0, a, b, c, d, e, f);
        Assert.assertArrayEquals(new byte[] { 0, a, b, c, d, e, f, 0, 0, 0 }, ten);

        Arrays.fill(ten, (byte) 0);
        Integers.insertBytes(7, ten, 1, (byte) 0, a, b, c, d, e, f, g);
        Assert.assertArrayEquals(new byte[] { 0, a, b, c, d, e, f, g, 0, 0 }, ten);

        Arrays.fill(ten, (byte) 0);
        Integers.insertBytes(8, ten, 1, a, b, c, d, e, f, g, h);
        Assert.assertArrayEquals(new byte[] { 0, a, b, c, d, e, f, g, h, 0 }, ten);

        Arrays.fill(ten, (byte) 0);
        byte[] src = new byte[4];
        Random rand = new Random(new SecureRandom().nextLong());
        rand.nextBytes(src);
        Integers.insertBytes(3, ten, ten.length - 3, (byte) 0, src[1], src[2], src[3]);
        Assert.assertArrayEquals(new byte[] { 0, 0, 0, 0, 0, 0, 0, src[1], src[2], src[3] }, ten);
    }

    @Test
    public void putGetBigInt() {
        byte[] dest = new byte[17];
        Arrays.fill(dest, (byte) -1);
        Random rand = new Random(new SecureRandom().nextLong());

        final int lim = Short.MAX_VALUE * 10;
        for(int i = 0; i < lim; i++) {
            BigInteger big = BigInteger.valueOf(rand.nextLong()).multiply(BigInteger.valueOf(Long.MAX_VALUE));
            int n = Integers.putBigInt(big, dest, 0);
            BigInteger r = Integers.getBigInt(dest, 0, n);
            Assert.assertEquals(big, r);
        }
    }
}
