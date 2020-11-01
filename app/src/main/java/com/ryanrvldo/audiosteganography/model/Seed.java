package com.ryanrvldo.audiosteganography.model;

import java.math.BigInteger;

public class Seed {
    private final BigInteger a;
    private final BigInteger b;
    private final BigInteger c0;
    private final BigInteger x0;
    private final int length;

    public Seed(int a, int b, int c0, int x0, int length) {
        this.a = BigInteger.valueOf(a);
        this.b = BigInteger.valueOf(b);
        this.c0 = BigInteger.valueOf(c0);
        this.x0 = BigInteger.valueOf(x0);
        this.length = length;
    }

    public BigInteger getA() {
        return a;
    }

    public BigInteger getB() {
        return b;
    }

    public BigInteger getC0() {
        return c0;
    }

    public BigInteger getX0() {
        return x0;
    }

    public int getLength() {
        return length;
    }
}
