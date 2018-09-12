package com.esaulpaugh.headlong.abi;

import com.esaulpaugh.headlong.abi.util.Utils;

import java.math.BigInteger;
import java.nio.ByteBuffer;

class BooleanType extends AbstractUnitType<Boolean> {

    private static final long serialVersionUID = -437935895006302627L;

    static final String CLASS_NAME = Boolean.class.getName();
    static final String ARRAY_CLASS_NAME_STUB = Utils.getNameStub(boolean[].class);

    BooleanType() {
        super("bool", 1, true);
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
    int typeCode() {
        return TYPE_CODE_BOOLEAN;
    }

    @Override
    Boolean decode(ByteBuffer bb, byte[] unitBuffer) {
        bb.get(unitBuffer, 0, UNIT_LENGTH_BYTES);
        BigInteger bi = new BigInteger(unitBuffer);
        validateBigIntBitLen(bi);
        switch (bi.byteValue()) {
        case 0: return Boolean.FALSE;
        case 1: return Boolean.TRUE;
        default: throw new ArithmeticException("expected value 0 or 1");
        }
    }
}
