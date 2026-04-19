package com.indralokbright.util;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class NumberToWordsUtil {

    private static final String[] ONES = {
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] TENS = {
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public String convert(BigDecimal amount) {
        if (amount == null) return "Zero Rupees Only";
        long rupees = amount.longValue();
        int paise = amount.remainder(BigDecimal.ONE).multiply(new BigDecimal(100)).intValue();

        StringBuilder result = new StringBuilder();
        result.append("Rupees ");
        result.append(convertLong(rupees));

        if (paise > 0) {
            result.append(" and ").append(convertInt(paise)).append(" Paise");
        }
        result.append(" Only");
        return result.toString();
    }

    private String convertLong(long number) {
        if (number == 0) return "Zero";
        if (number < 0) return "Minus " + convertLong(-number);

        String result = "";

        if (number / 10000000 > 0) {
            result += convertInt((int)(number / 10000000)) + " Crore ";
            number %= 10000000;
        }
        if (number / 100000 > 0) {
            result += convertInt((int)(number / 100000)) + " Lakh ";
            number %= 100000;
        }
        if (number / 1000 > 0) {
            result += convertInt((int)(number / 1000)) + " Thousand ";
            number %= 1000;
        }
        if (number > 0) {
            result += convertInt((int) number);
        }
        return result.trim();
    }

    private String convertInt(int number) {
        if (number == 0) return "";
        if (number < 20) return ONES[number];
        if (number < 100) {
            return TENS[number / 10] + (number % 10 != 0 ? " " + ONES[number % 10] : "");
        }
        return ONES[number / 100] + " Hundred" + (number % 100 != 0 ? " " + convertInt(number % 100) : "");
    }
}
