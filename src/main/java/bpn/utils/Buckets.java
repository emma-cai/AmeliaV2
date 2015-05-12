package bpn.utils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by qingqingcai on 5/11/15.
 */
public class Buckets {

    public static ArrayList<String> FOCUSED_ON =
            new ArrayList<>(Arrays.asList("GREETING", "CLOSING",
                    "ACKNOWLEDGE", "ACCEPT", "WAIT", /**"STATEMENT",**/
                    "TRANSFER", "THANKING", "THANKING_REPLY",
                    "APOLOGY", "APOLOGY_REPLY"));
}
