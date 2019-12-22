package com.ogiqvo.trainwav2velocity;

import static com.sipgate.mp3wav.Converter.convertFrom;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.TarsosDSPAudioInputStream;
import be.tarsos.dsp.io.UniversalAudioInputStream;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.io.jvm.AudioPlayer;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.*;
import java.io.*;

public class Main implements OnsetHandler {
    static final Logger log = LoggerFactory.getLogger(Main.class);

    static final private int BUFFER_SIZE = 2048;
    static final private int SAMPLE_RATE = 44100;
    static final private int PERCUSSION_SENSITIVITY = 30;
    static final private int PERCUSSION_THRESHOLD = 2;
    static final private int SAMPLE_SIZE_IN_BITS = 32;

    static TarsosDSPAudioFormat AUDIO_FORMAT = new TarsosDSPAudioFormat(
            /* sample rate */ SAMPLE_RATE,
            /* HERE sample size in bits */ SAMPLE_SIZE_IN_BITS,
            /* number of channels */ 1,
            /* signed/unsigned data */ true,
            /* big-endian byte order */ false
    );

    static public void main(String[] argv) throws FileNotFoundException, LineUnavailableException {
        AudioDispatcher audioDispatcher = null;
        if (argv.length > 0) {
            String filename = argv[0];
            audioDispatcher = getAudioStreamFromFilename(filename);
        } else {
            audioDispatcher = AudioDispatcherFactory.fromDefaultMicrophone(SAMPLE_RATE, BUFFER_SIZE, 0);
        }
        Main m = new Main(audioDispatcher);
    }

    static private AudioDispatcher getAudioStreamFromFilename(String filename) throws FileNotFoundException {
        String filenameExtension = Main.getNameWithoutExtension(new File(filename));
        InputStream rawInputStream = new FileInputStream(filename);
        TarsosDSPAudioInputStream audioStream;
        switch (filenameExtension) {
            case ".mp3":
                final ByteArrayOutputStream output = new ByteArrayOutputStream();
                final AudioFormat baseAudioFormat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, 1, true, false);
                convertFrom(rawInputStream).withTargetFormat(baseAudioFormat).to(output);
                final byte[] wavContent = output.toByteArray();
                final ByteArrayInputStream input = new ByteArrayInputStream(wavContent);
                input.reset();
                audioStream = new UniversalAudioInputStream(input, AUDIO_FORMAT);
                break;
            default:
                audioStream = new UniversalAudioInputStream(rawInputStream, AUDIO_FORMAT);
                break;
        }
        AudioDispatcher adp = new AudioDispatcher(audioStream, BUFFER_SIZE, 0);
        return adp;
    }

    public Main(AudioDispatcher adp) throws LineUnavailableException {
        AudioPlayer audioPlayer = new AudioPlayer(AUDIO_FORMAT, BUFFER_SIZE);
        adp.addAudioProcessor(audioPlayer);
        adp.addAudioProcessor(new PercussionOnsetDetector(SAMPLE_RATE, BUFFER_SIZE, this, PERCUSSION_SENSITIVITY, PERCUSSION_THRESHOLD));

        Thread t = new Thread(adp);
        t.start();
    }

    static private String getNameWithoutExtension(File file) {
        String fileName = file.getName();
        int index = fileName.lastIndexOf('.');
        if (index != -1) {
            return fileName.substring(index);
        }
        return "";
    }

    private double prevTime = -1;

    @Override
    public void handleOnset(double time, double salience) {
        if (prevTime != -1) {
            double timeDelta = time - prevTime;
            double bpm = 60.0 / timeDelta;
            log.info("BPM={} @ t={}", bpm, time);
        } else {
            log.info("Found first hit");
        }
        prevTime = time;
    }
}
