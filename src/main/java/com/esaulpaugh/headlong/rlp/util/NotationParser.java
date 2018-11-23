package com.esaulpaugh.headlong.rlp.util;

import com.esaulpaugh.headlong.util.Strings;

import java.util.ArrayList;
import java.util.List;

import static com.esaulpaugh.headlong.util.Strings.HEX;

/**
 * Decodes RLP object notation as defined by the {@link Notation} class.
 */
public class NotationParser {

    private static final int OBJECT_ARRAY = 0;
    private static final int STRING = 1;

    private static final int OBJECT_ARRAY_PREFIX_LEN = Notation.OBJECT_ARRAY_PREFIX.length();
    private static final int OBJECT_ARRAY_SUFFIX_LEN = Notation.OBJECT_ARRAY_SUFFIX.length();
    private static final int STRING_PREFIX_LEN = Notation.STRING_PREFIX.length();
    private static final int STRING_SUFFIX_LEN = Notation.STRING_SUFFIX.length();

    /**
     * Returns the object hierarchy represented by the notation.
     * @param notation
     * @return
     */
    public static List<Object> parse(String notation) {
        List<Object> topLevelObjects = new ArrayList<>(); // a sequence (as in encodeSequentially)
        int[] pair = new int[2];
        parse(notation, 0, notation.length(), topLevelObjects, pair);
        return topLevelObjects;
    }

    private static int parse(String notation, int i, final int end, List<Object> parent, int[] pair) {

        int nextArrayEnd;

        int nextObjectIndex;

        int datumStart;
        int datumEnd;
        while (i < end) {

            nextArrayEnd = notation.indexOf(Notation.OBJECT_ARRAY_SUFFIX, i);
            if(nextArrayEnd == -1) {
                nextArrayEnd = Integer.MAX_VALUE;
            }

            if(!findNextObject(notation, i, pair)) {
                return Integer.MAX_VALUE;
            }

            nextObjectIndex = pair[0];

            if(nextArrayEnd < nextObjectIndex) {
                return nextArrayEnd + OBJECT_ARRAY_SUFFIX_LEN;
            }

            switch (/* nextObjectType */ pair[1]) {
            case STRING:
                datumStart = nextObjectIndex + STRING_PREFIX_LEN;
                datumEnd = notation.indexOf(Notation.STRING_SUFFIX, datumStart);
                parent.add(Strings.decode(notation.substring(datumStart, datumEnd), HEX));
                i = datumEnd + STRING_SUFFIX_LEN;
                break;
            case OBJECT_ARRAY:
                datumStart = nextObjectIndex + OBJECT_ARRAY_PREFIX_LEN;
                List<Object> childList = new ArrayList<>();
                i = parse(notation, datumStart, end, childList, pair);
                parent.add(childList);
//                break;
//            default: /* do nothing */
            }
        }

        return end + OBJECT_ARRAY_SUFFIX_LEN;
    }

    private static boolean findNextObject(String notation, int i, int[] pair) { // Pair<Integer, Integer>
        int o = notation.indexOf(Notation.OBJECT_ARRAY_PREFIX, i);
        int s = notation.indexOf(Notation.STRING_PREFIX, i);

        if(s == -1) {
            if(o == -1) {
                return false;
            }
            pair[0] = o;
            pair[1] = OBJECT_ARRAY;
            return true;
        }
        if(o == -1) {
            pair[0] = s;
            pair[1] = STRING;
            return true;
        }
        if(o < s) {
            pair[0] = o;
            pair[1] = OBJECT_ARRAY;
            return true;
        }
        pair[0] = s;
        pair[1] = STRING;
        return true;
    }
}
