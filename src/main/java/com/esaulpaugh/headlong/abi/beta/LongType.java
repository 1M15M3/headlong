package com.esaulpaugh.headlong.abi.beta;

import java.math.BigInteger;
import java.nio.ByteBuffer;

class LongType extends AbstractUnitType<Long> {

    private static final String CLASS_NAME = Long.class.getName();
    private static final String ARRAY_CLASS_NAME_STUB = long[].class.getName().replaceFirst("\\[", "");

    LongType(String canonicalType, int bitLength, boolean signed) {
        super(canonicalType, bitLength, signed);
    }

    @Override
    String className() {
        return CLASS_NAME;
    }

    @Override
    String arrayClassNameStub() {
        return ARRAY_CLASS_NAME_STUB;
    }

    @Override
    Long decode(ByteBuffer bb, byte[] unitBuffer) {
        bb.get(unitBuffer, 0, UNIT_LENGTH_BYTES);
        BigInteger bi = new BigInteger(unitBuffer);
        long l = bi.longValueExact();
        validateLongBitLen(l);
        return l;
    }

    @Override
    void validate(Object object) {
        super.validate(object);
        final long longVal = ((Number) object).longValue();
        validateLongBitLen(longVal);
    }
}
