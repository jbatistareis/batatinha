package com.jbatista.batatinha.emulator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

public class Buzzer {

    private final double pi2 = 2 * Math.PI;

    private Clip clip = null;
    private final AudioFormat audioFormat = new AudioFormat(22050, 16, 2, true, true);
    private String note;

    public Buzzer(String note) {
        setNote(note);
    }

    public void beep() {
        clip.setFramePosition(0);
        clip.start();
    }

    private byte[] sineWave(int frequency, int amplitude) {
        final byte[] output = new byte[3000];
        final double f = (double) frequency / 22050;

        for (int i = 0; i < output.length; i++) {
            output[i] = (byte) (((i > 2949) ? --amplitude : amplitude) * Math.sin(pi2 * f * i));
        }

        return output;
    }

    public void setNote(String note) {
        this.note = note;

        try {
            if (clip != null) {
                clip.close();
            }

            clip = AudioSystem.getClip();

            switch (this.note) {
                case "A":
                    clip.open(audioFormat, sineWave(440, 50), 0, 3000);
                    break;
                case "B":
                    clip.open(audioFormat, sineWave(493, 50), 0, 3000);
                    break;
                case "C":
                    clip.open(audioFormat, sineWave(523, 50), 0, 3000);
                    break;
                case "D":
                    clip.open(audioFormat, sineWave(587, 50), 0, 3000);
                    break;
                case "E":
                    clip.open(audioFormat, sineWave(659, 50), 0, 3000);
                    break;
                case "F":
                    clip.open(audioFormat, sineWave(698, 50), 0, 3000);
                    break;
                case "G":
                    clip.open(audioFormat, sineWave(783, 50), 0, 3000);
                    break;
                default:
                    new RuntimeException("Sound note '" + note + "' not recognized.");
            }
        } catch (LineUnavailableException ex) {
            throw new RuntimeException("Couldn't open sound device.");
        }
    }

    public String getNote() {
        return this.note;
    }

}
