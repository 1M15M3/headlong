package com.esaulpaugh.headlong.abi;

import com.esaulpaugh.headlong.TestUtils;
import com.esaulpaugh.headlong.abi.util.JsonUtils;
import com.esaulpaugh.headlong.util.FastHex;
import com.esaulpaugh.headlong.util.Strings;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class ABIJsonTest {

    private static final String RESOURCE = "tests/json/basic_abi_tests.json";

    private static final String TEST_CASES;

    static {
        try {
            TEST_CASES = TestUtils.readResourceAsString(ABIJsonTest.class, RESOURCE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static class ABITestCase {
        final String key;

        final JsonArray args;
        final JsonArray types;
        final String result;
        final Function function;

        private ABITestCase(String key, JsonArray args, String result, JsonArray types, Function function) {
            this.key = key;
            this.args = args;
            this.types = types;
            this.result = result;
            this.function = function;
        }

        private static ABITestCase forKey(String key) throws ParseException {

            JsonObject tests = JsonUtils.parseObject(TEST_CASES);
            Set<Map.Entry<String, JsonElement>> entries = tests.entrySet();

            JsonObject jsonObject = null;
            for (Map.Entry<String, JsonElement> e : entries) {
                if (key.equals(e.getKey())) {
                    jsonObject = e.getValue().getAsJsonObject();
                    System.out.println(jsonObject);
                    break;
                }
            }
            if (jsonObject == null) {
                throw new RuntimeException(key + " not found");
            }

            JsonArray args = JsonUtils.getArray(jsonObject, "args");
            String result = JsonUtils.getString(jsonObject, "result");
            JsonArray types = JsonUtils.getArray(jsonObject, "types");

            StringBuilder sb = new StringBuilder("test(");
            for (JsonElement type : types) {
                sb.append(type.getAsString()).append(',');
            }
            String tupleString = TupleTypeParser.completeTupleTypeString(sb);

            System.out.println(tupleString);

            return new ABITestCase(key, args, result, types, Function.parse(tupleString));
        }

        private void test(Object[] argsArray) {

            Tuple t = new Tuple(argsArray);
            ByteBuffer bb = function.encodeCall(t);

            System.out.println("expected:   " + result);
            System.out.println("actual:     " + Strings.encode(Arrays.copyOfRange(bb.array(), Function.SELECTOR_LEN, bb.limit())));

            Assert.assertArrayEquals(FastHex.decode(result), Arrays.copyOfRange(bb.array(), Function.SELECTOR_LEN, bb.limit()));
        }
    }

    @Test
    public void testGithubWikiTest() throws ParseException {

        ABITestCase testCase = ABITestCase.forKey("GithubWikiTest");

        Object[] argsArray = new Object[testCase.args.size()];
        argsArray[0] = TestUtils.parseBigInteger(testCase.args.get(0));
        argsArray[1] = TestUtils.parseIntArray(testCase.args.get(1).getAsJsonArray());
        argsArray[2] = TestUtils.parseBytesX(testCase.args.get(2).getAsString(), 10);
        argsArray[3] = TestUtils.parseBytes(testCase.args.get(3).getAsString());

        testCase.test(argsArray);
    }

    @Test
    public void testSingleInteger() throws ParseException {

        ABITestCase testCase = ABITestCase.forKey("SingleInteger");

        Object[] argsArray = new Object[testCase.args.size()];
        argsArray[0] = TestUtils.parseBigInteger(testCase.args.get(0));

        testCase.test(argsArray);
    }

    @Test
    public void testIntegerAndAddress() throws ParseException {

        ABITestCase testCase = ABITestCase.forKey("IntegerAndAddress");

        Object[] argsArray = new Object[testCase.args.size()];
        argsArray[0] = TestUtils.parseBigInteger(testCase.args.get(0));
        argsArray[1] = TestUtils.parseAddress(testCase.args.get(1));

        testCase.test(argsArray);
    }
}
