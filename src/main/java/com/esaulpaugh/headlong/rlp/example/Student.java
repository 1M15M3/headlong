package com.esaulpaugh.headlong.rlp.example;

import com.esaulpaugh.headlong.rlp.DecodeException;
import com.esaulpaugh.headlong.rlp.RLPCodec;
import com.esaulpaugh.headlong.rlp.RLPItem;
import com.esaulpaugh.headlong.rlp.util.FloatingPoint;
import com.esaulpaugh.headlong.rlp.util.Integers;
import com.esaulpaugh.headlong.rlp.util.Strings;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.esaulpaugh.headlong.rlp.util.Strings.UTF_8;

public class Student implements RLPEncodeable {

    private String name;
    private float gpa;
    private BigInteger publicKey;
    private BigDecimal balance;

    public Student(String name, float gpa, BigInteger publicKey, BigDecimal balance) {
        this.name = name;
        this.gpa = gpa;
        this.publicKey = publicKey;
        this.balance = balance;
    }

    // 2051 ms
    public Student(byte[] rlp) throws DecodeException {
        RLPItem item = RLPCodec.wrap(rlp);
        this.name = item.asString(UTF_8);
        item = RLPCodec.wrap(rlp, item.endIndex);
        this.gpa = item.asFloat();
        item = RLPCodec.wrap(rlp, item.endIndex);
        this.publicKey = item.asBigInt();
        item = RLPCodec.wrap(rlp, item.endIndex);
        BigInteger intVal = item.asBigInt();
        item = RLPCodec.wrap(rlp, item.endIndex);
        this.balance = new BigDecimal(intVal, item.asInt());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getGpa() {
        return gpa;
    }

    public void setGpa(float gpa) {
        this.gpa = gpa;
    }

    public BigInteger getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(BigInteger publicKey) {
        this.publicKey = publicKey;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return name + ", " + gpa + ", " + publicKey + ", $" + balance;
    }

    @Override
    public int hashCode() {
        return name == null ? 31 : name.hashCode()
                * Float.hashCode(gpa)
                * (publicKey == null ? 31 : publicKey.hashCode())
                * (balance == null ? 31 : balance.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Student)) {
            return false;
        }

        Student other = (Student) obj;

        if (name == null ? other.name != null : !name.equals(other.name)) {
            return false;
        }
        if (gpa != other.gpa) {
            return false;
        }
        if (publicKey == null ? other.publicKey != null : !publicKey.equals(other.publicKey)) {
            return false;
        }
        if(balance == null ? other.balance != null : !balance.equals(other.balance)) {
            return false;
        }

        return true;
    }

    @Override
    public Object[] toObjectArray() {
        return new Object[] {
                Strings.decode(name, UTF_8),
                FloatingPoint.toBytes(gpa),
                publicKey.toByteArray(),
                balance.unscaledValue().toByteArray(),
                Integers.toBytes(balance.scale())
        };
    }

    @Override
    public byte[] toRLP() {
        return RLPCodec.encodeSequentially(toObjectArray());
    }

    @Override
    public void toRLP(byte[] dest, int destIndex) {
        RLPCodec.encodeSequentially(toObjectArray(), dest, destIndex);
    }
}
