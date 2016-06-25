package com.stronans.jarbrain.displayoutput;

import com.google.common.base.Optional;
import com.stronans.jarbrain.BrainInaJarStartup;
import com.stronans.jarbrain.tempcheck.SerialComms;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import static com.stronans.jarbrain.displayoutput.DisplayComment.Status.NORMAL;
import static com.stronans.jarbrain.displayoutput.DisplayComment.Status.OVERHEATING;
import static com.stronans.jarbrain.displayoutput.DisplayComment.Status.SHUTINGDOWN;

/**
 * Reads a comment for the file and displays it on the LCD screen.
 * Created by S.King on 03/05/2015.
 */
public class DisplayComment implements Runnable {
    /**
     * The <code>Logger</code> to be used.
     */
    private static Logger log = Logger.getLogger(DisplayComment.class);

    enum Status {
        NORMAL,
        OVERHEATING,
        SHUTINGDOWN
    }

    private static final long ONEMINUTE = 60000;
//    private static final long TWOMINUTES = ONEMINUTE * 2;
//    private static final long THREEMINUTES = ONEMINUTE * 3;
//    private static final long FOURMINUTES = ONEMINUTE * 4;

    private static final long PAUSE = ONEMINUTE;
    private final static String DEFAULT_FILE = "comments.txt";

    private Controller controller;
    private String filename;
    private List<String> cache = new ArrayList<>();
    private static DisplayComment display;
    private int startPoint;
    private static Status status = NORMAL;
    private SerialComms comms;

    public DisplayComment(String newFilename) {
        try {
            filename = newFilename;
            // Default LCD controller assumes display is 4 x 20
            controller = new LCDController();
//          controller = new TextController();
        } catch (InterruptedException ie) {
            log.error("Thread interrupted when connecting to LCD screen " + ie.getMessage());
        } catch (IOException ioe) {
            log.error("IO Exception when connecting to LCD screen " + ioe.getMessage());
        }
    }

    public void shutdown() {
        controller.clear();
        controller.home();
        controller.shutdown();
    }

    public DisplayComment(SerialComms comms) {
        this(DEFAULT_FILE);
        this.comms = comms;
        cacheFile();
        Random randomGenerator = new Random();
        startPoint = randomGenerator.nextInt(cache.size() - 1);

        show("Hold on. I'm warmingup. Soon I will     dispense some usefuladvice.");
        display = this;
    }

    private void show() {
        Optional<String> text = getComment();

        if (text.isPresent()) {
            controller.clear();
            controller.home();
            controller.write(text.get());
        }
    }

    private void show(String text) {
        if (text != null) {
            controller.clear();
            controller.home();
            controller.write(text);
        }
    }

    private Optional<String> getComment() {
        Optional<String> result = Optional.absent();
        //note a single Random object is reused here

        String s = cache.get(startPoint++);

        if (s != null) {
            result = Optional.of(s);
        }

        if (startPoint > cache.size() - 1) {
            startPoint = 0;
        }

        return result;
    }

    private void cacheFile() {
        Optional<BufferedInputStream> file = getFile(filename);

        if (file.isPresent()) {
            Scanner sc = new Scanner(new InputStreamReader(file.get()));
            sc.useDelimiter("\\\\s*%%%\\\\s*");

            while (sc.hasNext()) {
                String s = sc.nextLine();
                cache.add(s.substring(3));
            }
        }
    }

    private Optional<BufferedInputStream> getFile(String configFileName) {
        InputStream inputStream = null;

        try {
            // **NOTE** Has to be 'this.getClass' or you will get all sorts of problems finding file.
            inputStream = this.getClass().getResourceAsStream("/" + configFileName);

            if (inputStream == null) {
                File file = new File("./" + configFileName);
                inputStream = new FileInputStream(file);
            }

        } catch (IOException ioException) {
            log.error("Unable to find or open comments file", ioException);
        }

        if (inputStream != null)
            return Optional.of(new BufferedInputStream(inputStream));
        else
            return Optional.absent();
    }

    public void overHeating(boolean state) {
        if (state) {
            status = OVERHEATING;
        } else {
            status = NORMAL;
        }
    }

    public void shuttingDown() {
        status = SHUTINGDOWN;
    }

    @Override
    public void run() {
        try {
            while (!BrainInaJarStartup.finished()) {
                Thread.sleep(PAUSE);
                switch (status) {
                    case NORMAL:
                        display.show();
                        break;

                    case OVERHEATING:
                        display.show("I'm too hot!!!      Please lift off my  lid so I can cool   off.");
                        break;

                    case SHUTINGDOWN:
                        display.show("I'm going to sleep  now.");
                        break;
                }
            }
        } catch (InterruptedException ie) {
            log.error("Thread interrupted " + ie.getMessage());
        }
    }
}
