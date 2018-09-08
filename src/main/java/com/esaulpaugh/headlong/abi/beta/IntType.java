package com.esaulpaugh.headlong.abi.beta;

import java.math.BigInteger;
import java.nio.ByteBuffer;

class IntType extends AbstractUnitType<Integer> {

    private static final String CLASS_NAME = Integer.class.getName();
//    private static final String ARRAY_CLASS_NAME_STUB = int[].class.getName().replaceFirst("\\[", "");

    static final int MAX_BIT_LEN = 32;

    IntType(String canonicalType, int bitLength, boolean signed) {
        super(canonicalType, bitLength, signed);
    }

    @Override
    String className() {
        return CLASS_NAME;
    }

    @Override
    Integer decode(ByteBuffer bb, byte[] unitBuffer) {
        bb.get(unitBuffer, 0, UNIT_LENGTH_BYTES);
        BigInteger bi = new BigInteger(unitBuffer);
        long longVal = bi.longValueExact();
        validateLongBitLen(longVal);
        return (int) longVal;
    }

    @Override
    void validate(Object object) {
        super.validate(object);
        final long longVal = ((Number) object).longValue();
        validateLongBitLen(longVal);
    }
}
