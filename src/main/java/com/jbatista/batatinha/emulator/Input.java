package com.jbatista.batatinha.emulator;

import java.util.HashMap;
import java.util.Map;

public class Input {

    private boolean pressRegistred;
    private char lastKey;
    private final Map<Character, Boolean> keyMap = new HashMap<>();

    public Input() {
        for (int i = 0; i < 16; i++) {
            keyMap.put((char) i, false);
        }
    }

    public void toggleKey(Character key) {
        // put returns the previous value
        if (!keyMap.put(key, !keyMap.get(key))) {
            pressRegistred = true;
            lastKey = key;
        }
    }

    public boolean isPressed(Character key) {
        return keyMap.get(key);
    }

    public boolean pressRegistred() {
        return pressRegistred;
    }

    public char getLastKey() {
        return lastKey;
    }

    public void resetPressResgister() {
        pressRegistred = false;
    }

}
