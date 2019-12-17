package com.ogiqvo.trainwav2velocity;

import static com.sipgate.mp3wav.Converter.convertFrom;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.*;

public class Main {
    static final Logger log = LoggerFactory.getLogger(Main.class);

    static final private int BUFFER_SIZE = 2048;

    static public void main(String[] argv) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        String filename = argv[0];
        String filenameExtension = getNameWithoutExtension(new File(filename));
        TarsosDSPAudioFormat audioFormat = new TarsosDSPAudioFormat(
                /* sample rate */ 44100,
                /* HERE sample size in bits */ 32,
                /* number of channels */ 1,
                /* signed/unsigned data */ true,
                /* big-endian byte order */ false
        );
        InputStream rawInputStream = new FileInputStream(filename);
        UniversalAudioInputStream audioStream;
        switch (filenameExtension) {
            case ".mp3":
                final ByteArrayOutputStream output = new ByteArrayOutputStream();
                final AudioFormat baseAudioFormat = new AudioFormat(44100, 32, 1, false, false);
                convertFrom(rawInputStream).withTargetFormat(baseAudioFormat).to(output);
                final byte[] wavContent = output.toByteArray();
                final ByteArrayInputStream input = new ByteArrayInputStream(wavContent);
                input.reset();
                audioStream = new UniversalAudioInputStream(input, audioFormat);

                break;
            default:
                audioStream = new UniversalAudioInputStream(rawInputStream, audioFormat);
                break;
        }

        AudioDispatcher adp = new AudioDispatcher(audioStream, BUFFER_SIZE, 0);
        AudioPlayer audioPlayer = new AudioPlayer(audioFormat, BUFFER_SIZE);
        adp.addAudioProcessor(audioPlayer);

        Thread t = new Thread(adp);
        t.start();
    }

    static public String getNameWithoutExtension(File file) {
        String fileName = file.getName();
        int index = fileName.lastIndexOf('.');
        if (index != -1) {
            return fileName.substring(index);
        }
        return "";
    }
}
