package com.ogiqvo.trainwav2velocity;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main {
    static final Logger log = LoggerFactory.getLogger(Main.class);

    static final private int BUFFER_SIZE = 2048;

    static public void main(String[] argv) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        TarsosDSPAudioFormat audioFormat = new TarsosDSPAudioFormat(
                /* sample rate */ 44100,
                /* HERE sample size in bits */ 32,
                /* number of channels */ 1,
                /* signed/unsigned data */ true,
                /* big-endian byte order */ false
        );

        String filename = argv[0];
        InputStream wavStream = new FileInputStream(filename);
        UniversalAudioInputStream audioStream = new UniversalAudioInputStream(wavStream, audioFormat);
        AudioDispatcher adp = new AudioDispatcher(audioStream, BUFFER_SIZE, 0);
        AudioPlayer audioPlayer = new AudioPlayer(audioFormat, BUFFER_SIZE);
        adp.addAudioProcessor(audioPlayer);

        Thread t = new Thread(adp);
        t.start();
    }
}
