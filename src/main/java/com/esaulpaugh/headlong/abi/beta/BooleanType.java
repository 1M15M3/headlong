package com.esaulpaugh.headlong.abi.beta;

import java.math.BigInteger;
import java.util.Arrays;

class BooleanType extends AbstractInt256Type<Boolean> {

    static final String CLASS_NAME = Boolean.class.getName();
    static final String CLASS_NAME_ELEMENT = boolean[].class.getName().replaceFirst("\\[", "");

    BooleanType(String canonicalAbiType, String className) {
        super(canonicalAbiType, className, 1, false);
    }

    @Override
    Boolean decode(byte[] buffer, int index) {
        BigInteger bi = new BigInteger(Arrays.copyOfRange(buffer, index, index + INT_LENGTH_BYTES));
        switch (bi.byteValueExact()) {
        case 0: return Boolean.FALSE;
        case 1: return Boolean.TRUE;
        default: throw new ArithmeticException("expected value 0 or 1");
        }
    }
}
