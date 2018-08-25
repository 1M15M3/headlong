package com.esaulpaugh.headlong.abi;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.esaulpaugh.headlong.abi.Function.FUNCTION_ID_LEN;

public class Encoder {

    private static final byte[] PADDING_192_BITS = new byte[24];

    public static ByteBuffer encodeFunctionCall(Function function, Object[] arguments) {

        System.out.println("requiredCanonicalization = " + function.requiredCanonicalization);

        final TupleType paramTypes = function.paramTypes;
        final Type[] types = paramTypes.types;
        final int expectedNumParams = types.length;

        if(arguments.length != expectedNumParams) {
            throw new IllegalArgumentException("arguments.length <> types.size(): " + arguments.length + " != " + types.length);
        }

        TupleType.checkTypes(types, arguments);

        int[] headLengths = new int[types.length];
        int encodingByteLen = TupleType.getLengthInfo(types, arguments, headLengths);

        final int allocation = FUNCTION_ID_LEN + encodingByteLen;

        System.out.println("allocating " + allocation);
        ByteBuffer outBuffer = ByteBuffer.wrap(new byte[allocation]); // ByteOrder.BIG_ENDIAN by default
        outBuffer.put(function.selector);

        Encoder.encodeTuple(paramTypes, arguments, headLengths, outBuffer);

        return outBuffer;
    }

    private static void encodeTuple(TupleType tupleType, Object[] values, int[] headLengths, ByteBuffer outBuffer) {
        List<Type> typeList = new LinkedList<>(Arrays.asList(tupleType.types));
        List<Object> valuesList = new LinkedList<>(Arrays.asList(values));

        encodeHeadsForTuple(typeList, valuesList, headLengths, outBuffer);
        encodeTailsForTuple(typeList, valuesList, outBuffer);
    }

    private static void encodeHeadsForTuple(List<Type> types, List<Object> values, int[] headLengths, ByteBuffer outBuffer) {

        int sum = 0;
        for (int i : headLengths) {
            sum += i;
        }

        int mark;
        Iterator<Type> ti;
        Iterator<Object> vi;
        for(ti = types.iterator(), vi = values.iterator(); ti.hasNext(); ) {
            Type type = ti.next();
            mark = outBuffer.position();
            encodeHead(type, vi.next(), outBuffer, sum, type.dynamic);
            sum += outBuffer.position() - mark;
            if(!type.dynamic) {
                ti.remove();
                vi.remove();
            }
        }
    }

    private static void encodeTailsForTuple(List<Type> types, List<Object> values, ByteBuffer outBuffer) {
        Iterator<Type> ti;
        Iterator<Object> vi;
        for(ti = types.iterator(), vi = values.iterator(); ti.hasNext(); ) {
            Type type = ti.next();
            encodeTail(type, vi.next(), outBuffer);
        }
    }

    // TODO switch(typeInt) for performance?
    private static void encodeHead(Type paramType, Object value, ByteBuffer dest, int tailOffset, boolean dynamic) {
        if(value instanceof String) { // dynamic
            insertStringHead(dest, tailOffset);
        } else if(value.getClass().isArray()) {
            if (value instanceof Object[]) {
                if(value instanceof BigInteger[]) {
                    insertBigIntsHead((BigInteger[]) value, dest, tailOffset, dynamic);
                } else {
                    insertObjectsHead(paramType, (Object[]) value, dest, tailOffset, dynamic);
                }
            } else if (value instanceof byte[]) {
                insertBytesHead((byte[]) value, dest, tailOffset, dynamic);
            } else if (value instanceof int[]) {
                insertIntsHead((int[]) value, dest, tailOffset, dynamic);
            } else if (value instanceof long[]) {
                insertLongsHead((long[]) value, dest, tailOffset, dynamic);
            } else if (value instanceof short[]) {
                insertShortsHead((short[]) value, dest, tailOffset, dynamic);
            } else if(value instanceof boolean[]) {
                insertBooleansHead((boolean[]) value, dest, tailOffset, dynamic);
            }
        } else if(value instanceof Tuple) {
            insertTupleHead(paramType, (Tuple) value, dest, tailOffset, dynamic);
        } else if (value instanceof Number) {
            if(value instanceof BigInteger) {
                insertInt(((BigInteger) value), dest);
            } else if(value instanceof BigDecimal) {
                insertInt(((BigDecimal) value).unscaledValue(), dest);
            } else {
                insertInt(((Number) value).longValue(), dest);
            }
        } else if(value instanceof Boolean) {
            insertBool((boolean) value, dest);
        }
    }

    /**
     * Only for dynamic types -- no Booleans, no Numbers
     *
     * @param paramType
     * @param value
     * @param dest
     */
    private static void encodeTail(Type paramType, Object value, ByteBuffer dest) {
        if(value instanceof String) { // dynamic
            insertBytes(((String) value).getBytes(StandardCharsets.UTF_8), dest);
        } else if(value.getClass().isArray()) {
            if (value instanceof Object[]) {
                if(value instanceof BigInteger[]) {
                    insertBigInts((BigInteger[]) value, dest);
                } else {
                    for (Object object : (Object[]) value) {
                        encodeTail(paramType, object, dest);
                    }
                }
            } else if (value instanceof byte[]) {
                insertBytes((byte[]) value, dest);
            } else if (value instanceof int[]) {
                insertInts((int[]) value, dest);
            } else if (value instanceof long[]) {
                insertLongs((long[]) value, dest);
            } else if (value instanceof short[]) {
                insertShorts((short[]) value, dest);
            } else if(value instanceof boolean[]) {
                insertBooleans((boolean[]) value, dest);
            }
        } else if(value instanceof Tuple) {
            TupleType tupleType;
            try {
                tupleType = (TupleType) paramType;
            } catch (ClassCastException cce) {
                throw new RuntimeException(cce);
            }
            Tuple tuple = (Tuple) value;
            int[] headLengths = TupleType.getHeadLengths(tupleType.types, tuple.elements);
            encodeTuple(tupleType, ((Tuple) value).elements, headLengths, dest);
        }
    }

    // ----------------------------------------------

    private static void insertStringHead(ByteBuffer dest, int tailOffset) {
        insertInt(tailOffset, dest);
    }

    private static void insertBytesHead(byte[] bytes, ByteBuffer dest, int tailOffset, boolean dynamic) {
        if(dynamic) {
            insertInt(tailOffset, dest);
        } else {
            insertBytes(bytes, dest);
        }
    }

    private static void insertBooleansHead(boolean[] bools, ByteBuffer dest, int tailOffset, boolean dynamic) {
        if(dynamic) {
            insertInt(tailOffset, dest);
        } else {
            insertBooleans(bools, dest);
        }
    }

    private static void insertShortsHead(short[] shorts, ByteBuffer dest, int tailOffset, boolean dynamic) {
        if(dynamic) {
            insertInt(tailOffset, dest);
        } else {
            insertShorts(shorts, dest);
        }
    }

    private static void insertIntsHead(int[] ints, ByteBuffer dest, int tailOffset, boolean dynamic) {
        if(dynamic) {
            insertInt(tailOffset, dest);
        } else {
            insertInts(ints, dest);
        }
    }

    private static void insertLongsHead(long[] ints, ByteBuffer dest, int tailOffset, boolean dynamic) {
        if(dynamic) {
            insertInt(tailOffset, dest);
        } else {
            insertLongs(ints, dest);
        }
    }

    private static void insertBigIntsHead(BigInteger[] ints, ByteBuffer dest, int tailOffset, boolean dynamic) {
        if(dynamic) {
            insertInt(tailOffset, dest);
        } else {
            insertBigInts(ints, dest);
        }
    }

    private static void insertTupleHead(Type tupleType, Tuple tuple, ByteBuffer dest, int tailOffset, boolean dynamic) {
        TupleType paramTypes;
        try {
            paramTypes = (TupleType) tupleType;
        } catch (ClassCastException cce) {
            throw new RuntimeException(cce);
        }
        if(dynamic) {
            insertInt(tailOffset, dest);
        } else {
            int[] headLengths = TupleType.getHeadLengths(paramTypes.types, tuple.elements);
            encodeTuple(paramTypes, tuple.elements, headLengths, dest);
        }
    }

    private static void insertObjectsHead(Type paramType, Object[] objects, ByteBuffer dest, int tailOffset, boolean dynamic) {
        if(dynamic) {
            insertInt(tailOffset, dest);
        } else {
            encodeTail(paramType, objects, dest);
        }
    }

    // -------------------------------------------------------------------------------------------------

    private static void insertBooleans(boolean[] bools, ByteBuffer dest) {
        insertInt(bools.length, dest);
        for (boolean e : bools) {
            insertBool(e, dest);
        }
    }

    private static void insertBytes(byte[] bytes, ByteBuffer dest) {
        insertInt(bytes.length, dest);
        dest.put(bytes);
        final int n = Integer.SIZE - bytes.length;
        for (int i = 0; i < n; i++) {
            dest.put((byte) 0);
        }
    }

    private static void insertShorts(short[] shorts, ByteBuffer dest) {
        insertInt(shorts.length, dest);
        for (short e : shorts) {
            insertInt(e, dest);
        }
    }

    private static void insertInts(int[] ints, ByteBuffer dest) {
        insertInt(ints.length, dest);
        for (int e : ints) {
            insertInt(e, dest);
        }
    }

    private static void insertLongs(long[] ints, ByteBuffer dest) {
        insertInt(ints.length, dest);
        for (long e : ints) {
            insertInt(e, dest);
        }
    }

    private static void insertBigInts(BigInteger[] bigInts, ByteBuffer dest) {
        insertInt(bigInts.length, dest);
        for (BigInteger e : bigInts) {
            insertInt(e, dest);
        }
    }

    // --------------------------

    private static void insertInt(long val, ByteBuffer dest) {
        dest.put(PADDING_192_BITS);
        dest.putLong(val);
    }

    private static void insertInt(BigInteger bigGuy, ByteBuffer dest) {
        byte[] arr = bigGuy.toByteArray();
        final int lim = 32 - arr.length;
        for (int i = 0; i < lim; i++) {
            dest.put((byte) 0);
        }
        dest.put(arr);
    }

    private static void insertBool(boolean bool, ByteBuffer dest) {
        insertInt(bool ? 1L : 0L, dest);
    }
}
