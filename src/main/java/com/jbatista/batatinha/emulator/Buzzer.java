package com.jbatista.batatinha.emulator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

public class Buzzer {

    private final Clip clip;

    public Buzzer(String note) {
        try {
            this.clip = AudioSystem.getClip();

            switch (note) {
                case "A":
                    clip.open(new AudioFormat(22500, 8, 1, true, false), sineWave(440, 2.0, 1000), 0, 1024);
                    break;
                case "B":
                    clip.open(new AudioFormat(22500, 8, 1, true, false), sineWave(493, 2.0, 1000), 0, 1024);
                    break;
                case "C":
                    clip.open(new AudioFormat(22500, 8, 1, true, false), sineWave(523, 2.0, 1000), 0, 1024);
                    break;
                case "D":
                    clip.open(new AudioFormat(22500, 8, 1, true, false), sineWave(587, 2.0, 1000), 0, 1024);
                    break;
                case "E":
                    clip.open(new AudioFormat(22500, 8, 1, true, false), sineWave(659, 2.0, 1000), 0, 1024);
                    break;
                case "F":
                    clip.open(new AudioFormat(22500, 8, 1, true, false), sineWave(698, 2.0, 1000), 0, 1024);
                    break;
                case "G":
                    clip.open(new AudioFormat(22500, 8, 1, true, false), sineWave(783, 2.0, 1000), 0, 1024);
                    break;
                default:
                    new RuntimeException("Sound note '" + note + "' not recognized.");
            }
        } catch (LineUnavailableException ex) {
            throw new RuntimeException("Couldn't open sound device.");
        }
    }

    public void beep() {
        clip.setFramePosition(0);
        clip.start();
    }

    private byte[] sineWave(int frequency, double amplitude, int durationMs) {
        final byte[] output = new byte[((durationMs * 44100) / 1000)];

        for (int i = 0; i < output.length; i++) {
            output[i] = (byte) (Math.sin((amplitude * Math.PI * i) / (44100 / frequency)) * 100f);
        }

        return output;
    }

}
