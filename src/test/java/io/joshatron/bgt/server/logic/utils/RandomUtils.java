package io.joshatron.bgt.server.logic.utils;

import java.util.Random;

public class RandomUtils {

    public static String generateTest(int length) {
        Random rand = new Random();
        char[] letters = new char[]{'A','B','C','D','E','F','G','H','J','K','L','M','N','P','Q','R','S',
                'T','U','V','W','X','Y','Z','0','1','2','3','4','5','6','7','8','9'};

        StringBuilder id = new StringBuilder();
        for(int i = 0; i < length; i++) {
            id.append(letters[rand.nextInt(letters.length)]);
        }

        return id.toString();
    }
}
