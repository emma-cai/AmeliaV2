package bpn;

import bpn.preparing.FeatureExtraction;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
* Created by qingqingcai on 4/29/15.
*/
 public class FeatureExtractionTest {

    @Test
    public void TestGenerateFeatures() {

        String input = "Remedy Ticket number: ${ebond_number}";
        String expected = "remedy ticket number: ebond_number";
        String actual = FeatureExtraction.regExp(input);
        assertEquals(expected, actual);

        input = "The invoice number is ${invoice_number}. The invoice amount is ${invoice_amount}. The date is ${input2display} ";
        expected = "the invoice number is invoice_number. the invoice amount is invoice_amount. the date is input2display";
        actual = FeatureExtraction.regExp(input);
        assertEquals(expected, actual);

        input = "I'm looking up your account.";
        expected = "i 'm looking up your account.";
        actual = FeatureExtraction.regExp(input);
        assertEquals(expected, actual);

        input = "Great I've confirmed your account information Ergun Ekici";
        expected = "great i 've confirmed your account information ergun ekici";
        actual = FeatureExtraction.regExp(input);
        assertEquals(expected, actual);

        input = "Let's start by checking your touchscreen interface";
        expected = "let 's start by checking your touchscreen interface";
        actual = FeatureExtraction.regExp(input);
        assertEquals(expected, actual);
    }
}
