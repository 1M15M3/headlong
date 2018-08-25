package com.esaulpaugh.headlong.rlp.example;

import com.esaulpaugh.headlong.rlp.DecodeException;

public interface RLPAdapter<T> {

    default T decode(byte[] rlp) throws DecodeException {
        return decode(rlp, 0);
    }

    T decode(byte[] rlp, int index) throws DecodeException;

    byte[] encode(T t);

}