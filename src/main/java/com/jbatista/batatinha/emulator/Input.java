package com.jbatista.batatinha.emulator;

import java.util.HashSet;
import java.util.Set;

public class Input {

    private boolean pressRegistred;
    private char lastKey;
    private final Set<Character> pressedKeys = new HashSet<>(16);

    public void toggleKey(Character key) {
        if (!pressedKeys.contains(key)) {
            pressedKeys.add(key);
            pressRegistred = true;
            lastKey = key;
        } else {
            pressedKeys.remove(key);
        }
    }

    public boolean isPressed(Character key) {
        return pressedKeys.contains(key);
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
