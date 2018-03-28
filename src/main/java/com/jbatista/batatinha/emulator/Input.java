package com.jbatista.batatinha.emulator;

import java.util.Arrays;

public class Input {

    private final char[] keyState = new char[16];

    public enum Key {
        n0, n1, n2, n3, n4, n5, n6, n7, n8, k9, A, B, C, D, E, F
    }

    public Input() {
        //zero fill
        Arrays.fill(keyState, (char) 0x0);
    }

    public void register(Key key) {

    }

    public char[] getKeyState() {
        return keyState;
    }

}
