package org.cis1200.othello;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class SoundEffect {
    public static final String CLICK_PATH = "files/click.wav";
    public static final String WIN_PATH = "files/win.wav";

    public SoundEffect() {

    }

    public void playClick() {
        playSound(CLICK_PATH);
    }

    public void playWin() {
        playSound(WIN_PATH);
    }

    private void playSound(final String path) {
        new Thread(() -> {
            synchronized (this) {
                try {
                    Clip clip = AudioSystem.getClip();
                    clip.open(AudioSystem.getAudioInputStream(new File(path)));
                    clip.start();

                } catch (LineUnavailableException e) {
                    e.printStackTrace();
                    System.out.println("Error playing sound 1");
                } catch (UnsupportedAudioFileException e) {
                    e.printStackTrace();
                    System.out.println("Error playing sound 2");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Error playing sound 3");
                }
            }
        }).start();
    }
}
