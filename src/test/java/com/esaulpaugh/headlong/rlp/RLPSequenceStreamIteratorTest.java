package com.esaulpaugh.headlong.rlp;

import com.esaulpaugh.headlong.TestUtils;
import com.esaulpaugh.headlong.rlp.exception.DecodeException;
import com.esaulpaugh.headlong.rlp.exception.UnrecoverableDecodeException;
import com.esaulpaugh.headlong.util.FastHex;
import com.esaulpaugh.headlong.util.Strings;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.esaulpaugh.headlong.util.Strings.HEX;
import static com.esaulpaugh.headlong.util.Strings.UTF_8;

public class RLPSequenceStreamIteratorTest {

    private static final byte TEST_BYTE = 0x79;
    private static final byte[] TEST_BYTES = new byte[] { 0x04, 0x03, 0x02 };
    private static final String TEST_STRING = "\u0009\u0009\u0030\u0031";

    @Test
    public void testStream() throws Throwable {
        ReceiveStreamThread thread = new ReceiveStreamThread();
        thread.start();
        thread.join();
        Throwable t = thread.throwable;
        if(t != null) {
            throw t;
        }
    }

    @Test
    public void testUnrecoverable() throws Throwable {

        Class<? extends DecodeException> clazz = UnrecoverableDecodeException.class;

        final PipedOutputStream pos = new PipedOutputStream();
        final PipedInputStream pis = new PipedInputStream(pos, 512);

        RLPSequenceStreamIterator iter = new RLPSequenceStreamIterator(RLPDecoder.RLP_STRICT, pis);

        pos.write(0x81);
        pos.write(0x00);

        TestUtils.assertThrown(clazz, "invalid rlp for single byte @ 0", iter::hasNext);

        iter = new RLPSequenceStreamIterator(RLPDecoder.RLP_STRICT, pis);

        pos.write(0xf8);
        pos.write(0x37);

        for (int i = 0; i < 3; i++) {
            TestUtils.assertThrown(
                    clazz,
                    "long element data length must be 56 or greater; found: 55 for element @ 0",
                    iter::hasNext
            );
        }
    }

    private static class ReceiveStreamThread extends Thread {

        private final Object receiver = new Object();
        private final Object sender;

        private long zero;
        private final PipedOutputStream pos;
        private final AtomicBoolean canReceive;

        private int readNum = -1;
        private final SendStreamThread senderThread;

        private Throwable throwable;

        public ReceiveStreamThread() {
            this.zero = System.nanoTime();
            this.pos = new PipedOutputStream();
            this.canReceive = new AtomicBoolean(false);
            this.senderThread = new SendStreamThread(zero, pos, receiver, canReceive);
            this.sender = senderThread.getSender();
        }

        public Throwable getThrowable() {
            return throwable;
        }

        @Override
        public void run() {
            try {
                final PipedInputStream pis = new PipedInputStream(pos, 512);

                RLPSequenceStreamIterator iter = new RLPSequenceStreamIterator(RLPDecoder.RLP_STRICT, pis);

                senderThread.setPriority(Thread.MAX_PRIORITY);
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

                senderThread.start();
                waitForNotifiedSender();

                assertNoNext(iter);

                waitForNotifiedSender();

                assertReadSuccess(iter);
                Assert.assertArrayEquals(new byte[] { TEST_BYTE }, iter.next().data());
                assertNoNext(iter);

                waitForNotifiedSender();

                for (byte b : TEST_BYTES) {
                    assertReadSuccess(iter);
                    Assert.assertArrayEquals(timestamp(zero), new byte[] { b }, iter.next().data());
                }
                assertNoNext(iter);

                waitForNotifiedSender();

                assertNoNext(iter);

                waitForNotifiedSender();

                assertNoNext(iter);

                waitForNotifiedSender();

                assertReadSuccess(iter);
                Assert.assertTrue(iter.hasNext());
                Assert.assertTrue(iter.hasNext());
                Assert.assertEquals(TEST_STRING, iter.next().asString(UTF_8));
                assertReadSuccess(iter);
                Assert.assertTrue(iter.hasNext());
                Assert.assertArrayEquals(new byte[] { TEST_BYTE }, iter.next().data());
                TestUtils.assertThrown(NoSuchElementException.class, iter::next);
                assertNoNext(iter);
                Assert.assertFalse(iter.hasNext());
                Assert.assertFalse(iter.hasNext());
                TestUtils.assertThrown(NoSuchElementException.class, iter::next);

                senderThread.join();

            } catch (Throwable io) {
                throwable = io;
            } finally {
                canReceive.set(false);
            }
        }

        private void waitForNotifiedSender() throws InterruptedException {
            notifySender();
            synchronized (receiver) {
                while(!canReceive.get()) {
                    receiver.wait();
                }
            }
        }

        private void notifySender() {
            synchronized (sender) {
                canReceive.set(false);
                sender.notify();
            }
        }

        private void assertNoNext(RLPSequenceStreamIterator iter) throws IOException, UnrecoverableDecodeException {
            RLPSequenceStreamIteratorTest.assertNoNext(zero, ++readNum, iter);
        }

        private void assertReadSuccess(RLPSequenceStreamIterator iter) throws IOException, UnrecoverableDecodeException {
            RLPSequenceStreamIteratorTest.assertReadSuccess(zero, ++readNum, iter);
        }
    }

    private static class SendStreamThread extends Thread {

        private final Object sender = new Object();

        private final long zero;
        private final OutputStream os;
        private final Object receiver;
        private final AtomicBoolean canReceive;

        private SendStreamThread(long zero, OutputStream os, Object receiver, AtomicBoolean canReceive) {
            this.zero = zero;
            this.os = os;
            this.receiver = receiver;
            this.canReceive = canReceive;
        }

        private Object getSender() {
            return sender;
        }

        @Override
        public void run() {
            try {
                waitForReceiver();
                write(TEST_BYTE);
                waitForReceiver();
                for (byte b : TEST_BYTES) {
                    write(b);
                }
                waitForReceiver();
                byte[] rlpString = RLPEncoder.encode(Strings.decode(TEST_STRING, UTF_8));
                int i = 0;
                write(rlpString[i++]);
                waitForReceiver();
                write(rlpString[i++]);
                waitForReceiver();
                while(i < rlpString.length) {
                    write(rlpString[i++]);
                }
                write(TEST_BYTE);
                notifyReceiver();
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }

        private void waitForReceiver() throws InterruptedException {
            notifyReceiver();
            synchronized (sender) {
                while(canReceive.get()) {
                    sender.wait();
                }
            }
        }

        private void notifyReceiver() {
            synchronized (receiver) {
                canReceive.set(true);
                receiver.notify();
            }
        }

        private void write(byte b) throws IOException {
            os.write(b);
            logWrite(zero, FastHex.encodeToString(b));
        }
    }

    private static void assertReadSuccess(long zero, int readNum, RLPSequenceStreamIterator iter) throws IOException, UnrecoverableDecodeException {
        Assert.assertTrue("no next() found, " + timestamp(zero), iter.hasNext());
        logRead(zero, readNum, true);
    }

    private static void assertNoNext(long zero, int readNum, RLPSequenceStreamIterator iter) throws IOException, UnrecoverableDecodeException {
        if(iter.hasNext()) {
            throw new AssertionError("unexpected next(): " + iter.next().asString(HEX) + ", " + timestamp(zero));
        }
        logRead(zero, readNum, false);
    }

    private static void logWrite(long zero, String message) {
        System.out.println(timestamp(zero) + "\u0009write " + message);
    }

    private static void logRead(long zero, int readNum, boolean success) {
        System.out.println(timestamp(zero) + "\u0009read " + (success ? "success, #" + readNum : "failure"));
    }

    private static String timestamp(long zero) {
        double t = (System.nanoTime() - zero) / 1000000.0;
        String tString = String.valueOf(t);
        StringBuilder sb = new StringBuilder("t=");
        sb.append(tString);
        int n = 10 - tString.length();
        for (int i = 0; i < n; i++) {
            sb.append('0');
        }
        return sb.toString();
    }
}
