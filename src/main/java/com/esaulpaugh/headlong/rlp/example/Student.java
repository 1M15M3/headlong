package com.esaulpaugh.headlong.rlp.example;

import com.esaulpaugh.headlong.rlp.DecodeException;
import com.esaulpaugh.headlong.rlp.RLPEncoder;
import com.esaulpaugh.headlong.rlp.RLPItem;
import com.esaulpaugh.headlong.rlp.util.FloatingPoint;
import com.esaulpaugh.headlong.rlp.util.Integers;
import com.esaulpaugh.headlong.util.Strings;

import java.math.BigDecimal;
import java.math.BigInteger;

import static com.esaulpaugh.headlong.rlp.RLPDecoder.RLP_STRICT;
import static com.esaulpaugh.headlong.util.Strings.UTF_8;

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

    public Student(byte[] rlp, int index) throws DecodeException {
        RLPItem item = RLP_STRICT.wrap(rlp, index);
        this.name = item.asString(UTF_8);
        item = RLP_STRICT.wrap(rlp, item.endIndex);
        this.gpa = item.asFloat();
        item = RLP_STRICT.wrap(rlp, item.endIndex);
        this.publicKey = item.asBigInt();
        item = RLP_STRICT.wrap(rlp, item.endIndex);
        BigInteger intVal = item.asBigInt();
        item = RLP_STRICT.wrap(rlp, item.endIndex);
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
//        return Arrays.deepHashCode(new Object[]{ name, gpa, publicKey, balance });

        return name == null ? 31 : name.hashCode()
                * Float.floatToIntBits(gpa)
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
        if (Math.abs(gpa - other.gpa) > 0.00005) {
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
        return RLPEncoder.encodeSequentially(toObjectArray());
    }

    @Override
    public void toRLP(byte[] dest, int destIndex) {
        RLPEncoder.encodeSequentially(toObjectArray(), dest, destIndex);
    }
}
