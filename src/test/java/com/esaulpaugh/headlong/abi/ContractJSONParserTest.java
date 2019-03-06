package com.esaulpaugh.headlong.abi;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.junit.Test;

import java.text.ParseException;
import java.util.List;

public class ContractJSONParserTest {

    private static final String JSON_ARRAY = "[\n" +
            "  {\n" +
            "    \"type\":\"event\",\n" +
            "    \"inputs\": [\n" +
            "     {\"name\":\"a\",\"type\":\"bytes\",\"indexed\":true},\n" +
            "     {\"name\":\"b\",\"type\":\"uint\",\"indexed\":false}\n" +
            "    ],\n" +
            "    \"name\":\"an_event\"\n" +
            "  },\n" +
            "  {\n" +
            "    \"name\": \"func\",\n" +
            "    \"type\": \"function\",\n" +
            "    \"inputs\": [\n" +
            "      {\n" +
            "        \"name\": \"aa\",\n" +
            "        \"type\": \"tuple\",\n" +
            "        \"components\": [\n" +
            "          {\n" +
            "            \"name\": \"aa_d\",\n" +
            "            \"type\": \"decimal\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"name\": \"aa_f\",\n" +
            "            \"type\": \"fixed\"\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"bb\",\n" +
            "        \"type\": \"fixed[]\",\n" +
            "        \"components\": [\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"name\": \"cc\",\n" +
            "        \"type\": \"tuple\",\n" +
            "        \"components\": [\n" +
            "          {\n" +
            "            \"name\": \"cc_uint\",\n" +
            "            \"type\": \"uint256\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"name\": \"cc_int_arr\",\n" +
            "            \"type\": \"int[]\"\n" +
            "          },\n" +
            "          {\n" +
            "            \"name\": \"cc_tuple_arr\",\n" +
            "            \"type\": \"tuple[]\",\n" +
            "            \"components\": [\n" +
            "              {\n" +
            "                \"name\": \"cc_tuple_arr_int_eight\",\n" +
            "                \"type\": \"int8\"\n" +
            "              },\n" +
            "              {\n" +
            "                \"name\": \"cc_tuple_arr_uint_forty\",\n" +
            "                \"type\": \"uint40\"\n" +
            "              }\n" +
            "            ]\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ],\n" +
            "    \"outputs\": []\n" +
            "  }\n" +
            "]";

    @Test
    public void testGetFunctions() throws ParseException {

        List<Function> functions = ContractJSONParser.getFunctions(JSON_ARRAY);

        for(Function f : functions) {
            System.out.println(f.getName() + " : " + f.canonicalSignature);
        }
    }

    @Test
    public void testGetEvents() {
        List<JsonObject> events = ContractJSONParser.getEvents(JSON_ARRAY);

        for(JsonObject eventObj : events) {
            System.out.println(eventObj.get("name") + ", " + eventObj.get("type"));
            JsonArray inputs = eventObj.get("inputs").getAsJsonArray();
            for (JsonElement element : inputs) {
                JsonObject input = (JsonObject) element;
                System.out.println("  " + input.get("name") + ", " + input.get("type") + ", " + input.get("indexed"));
            }
        }
    }

}
