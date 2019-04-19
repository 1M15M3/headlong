package com.esaulpaugh.headlong.util;

import com.esaulpaugh.headlong.TestUtils;
import com.esaulpaugh.headlong.abi.MonteCarloTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;

import static com.esaulpaugh.headlong.util.Strings.*;

public class StringsTest {

    @Test
    public void utf8() {
        Random rand = new Random(MonteCarloTest.getSeed(System.nanoTime()));
        for (int j = 0; j < 20_000; j++) {
            byte[] x = new byte[rand.nextInt(400)];
            for (int i = 0; i < x.length; i++) {
//                x[i] = (byte) (r.nextInt(95) + 32);
                x[i] = (byte) rand.nextInt(128);
            }
            String s = Strings.encode(x, UTF_8);
            byte[] y = Strings.decode(s, UTF_8);
            Assert.assertArrayEquals(x, y);
        }
    }

    @Test
    public void hex() {
        Random rand = new Random(MonteCarloTest.getSeed(System.nanoTime()));
        for (int i = 0; i < 20_000; i++) {
            byte[] x = new byte[rand.nextInt(400)];
            rand.nextBytes(x);
            String s = Strings.encode(x, HEX);
            byte[] y = Strings.decode(s, HEX);
            Assert.assertArrayEquals(x, y);
        }
    }

    @Test
    public void padding() {
        Random rand = new Random(MonteCarloTest.getSeed(System.nanoTime()));
        java.util.Base64.Encoder encoder = java.util.Base64.getEncoder();
        for(int j = 0; j < 160; j++) {
            byte[] x = new byte[j];
            for (int i = 0; i < 100; i++) {
                rand.nextBytes(x);
                String s = Strings.encode(x, BASE64);
                String s2 = encoder.encodeToString(x);
                Assert.assertEquals(encodedLen(x.length, true), s.length());
                Assert.assertEquals(s2, s);
            }
        }
    }

    @Test
    public void noPadding() {
        Random rand = new Random(MonteCarloTest.getSeed(System.nanoTime()));
        for(int j = 3; j < 160; j++) {
            byte[] x = new byte[j];
            for (int i = 0; i < 100; i++) {
                rand.nextBytes(x);
                int offset = rand.nextInt(x.length / 3);
                int len = rand.nextInt(x.length / 2);
                String s = com.migcomponents.migbase64.Base64.encodeToString(x, offset, len, false, DONT_PAD);
                Assert.assertEquals(encodedLen(len, false), s.length());
            }
        }
    }

    @Test
    public void tryDecodeBase64() throws Throwable {
        TestUtils.assertThrown(UnsupportedOperationException.class, () -> Strings.decode("", BASE64));
    }

    private static int encodedLen(int numBytes, boolean padding) {
        if(padding) {
            return numBytes / 3 * 4 + (numBytes % 3 > 0 ? 4 : 0);
        }
//        return (int) StrictMath.ceil(inputLen * 4 / 3d);
        int estimated = numBytes / 3 * 4;
        int mod = numBytes % 3;
        if(mod == 0) {
            return estimated;
        }
        if(mod == 1) {
            return estimated + 2;
        }
        return estimated + 3;
    }
}
