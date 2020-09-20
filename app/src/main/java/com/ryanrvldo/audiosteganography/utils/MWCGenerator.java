package com.ryanrvldo.audiosteganography.utils;

import com.ryanrvldo.audiosteganography.model.PseudoRandomNumber;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class MWCGenerator {

    public static Integer[] getXN(PseudoRandomNumber randomNumber) {
        Integer[] XN = new Integer[randomNumber.getLength()];
        Map<BigInteger, Boolean> tempXN = new HashMap<>();

        BigInteger xn1 = randomNumber.getX0();
        BigInteger cn1 = randomNumber.getC0();
        for (int i = 0; i < randomNumber.getLength(); i++) {
            BigInteger tmp = randomNumber.getA().multiply(xn1).add(cn1);
            xn1 = tmp.mod(randomNumber.getB());
            cn1 = BigInteger.valueOf(Math.floorDiv(tmp.intValue(), randomNumber.getB().intValue()));

            if (tempXN.size() != 0) {
                while (tempXN.containsKey(xn1)) {
                    xn1 = xn1.add(BigInteger.ONE);
                    if (xn1.equals(randomNumber.getB())) xn1 = BigInteger.ZERO;
                }
            }
            tempXN.put(xn1, true);
            XN[i] = xn1.intValue();
        }
        return XN;
    }
}
