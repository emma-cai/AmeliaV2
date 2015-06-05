package utils;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by qingqingcai on 6/5/15.
 */
public class LangTools {

    @Test
    public void testRegularExpression() {

        String regular = "^(When|when|What time|what time) .*$";

        String input = "What time did he go to school?";
        assertTrue(input.matches(regular));

        input = "When did he go to school?";
        assertTrue(input.matches(regular));

        input = "Where did he find the bag?";
        assertTrue(input.matches(regular));
    }
}
