package com.esaulpaugh.headlong.abi.beta;

import java.nio.ByteBuffer;

import static com.esaulpaugh.headlong.abi.beta.AbstractUnitType.UNIT_LENGTH_BYTES;
import static com.esaulpaugh.headlong.abi.beta.util.ClassNames.toFriendly;

// TODO support model classes à la Student.java
abstract class StackableType<V> {

    static final int TYPE_CODE_BOOLEAN = 0;
    static final int TYPE_CODE_BYTE = 1;
    static final int TYPE_CODE_SHORT = 2;
    static final int TYPE_CODE_INT = 3;
    static final int TYPE_CODE_LONG = 4;
    static final int TYPE_CODE_BIG_INTEGER = 5;
    static final int TYPE_CODE_BIG_DECIMAL = 6;

    static final int TYPE_CODE_ARRAY = 7;
    static final int TYPE_CODE_TUPLE = 8;

    static final StackableType<?>[] EMPTY_TYPE_ARRAY = new StackableType<?>[0];

    final String canonicalType;

    final boolean dynamic;

    StackableType(String canonicalType, boolean dynamic) {
        this.canonicalType = canonicalType;
        this.dynamic = dynamic;
    }

    abstract String className();

    abstract String arrayClassNameStub();

    abstract int typeCode();

    abstract int byteLength(Object value);

    int validate(Object value) {
        final String expectedClassName = className();

        // will throw NPE if argument null
        if(!expectedClassName.equals(value.getClass().getName())) {
            boolean isAssignable;
            try {
                isAssignable = Class.forName(expectedClassName).isAssignableFrom(value.getClass());
            } catch (ClassNotFoundException cnfe) {
                isAssignable = false;
            }
            if(!isAssignable) {
                throw new IllegalArgumentException("class mismatch: "
                        + value.getClass().getName()
                        + " not assignable to "
                        + expectedClassName
                        + " (" + toFriendly(value.getClass().getName()) + " not instanceof " + toFriendly(expectedClassName) + "/" + canonicalType + ")");
            }
        }

        return UNIT_LENGTH_BYTES;
    }

    /**
     *
     * @param buffer    the buffer containing the encoded data
     * @param unitBuffer a buffer of length {@link AbstractUnitType#UNIT_LENGTH_BYTES} in which to store intermediate values
     * @return  the decoded value
     */
    abstract V decode(ByteBuffer buffer, byte[] unitBuffer);

    static byte[] newUnitBuffer() {
        return new byte[UNIT_LENGTH_BYTES];
    }
}
