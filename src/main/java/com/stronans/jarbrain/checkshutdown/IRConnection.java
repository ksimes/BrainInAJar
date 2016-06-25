package com.stronans.jarbrain.checkshutdown;

import org.apache.log4j.Logger;
import org.lirc.LIRCException;
import org.lirc.Receiver;
import org.lirc.ReceiverFactory;

/**
 * Created by S.King on 27/05/2016.
 */
public class IRConnection {
    /**
     * The <code>Logger</code> to be used.
     */
    private static Logger log = Logger.getLogger(IRConnection.class);
    private Receiver rec;

    public IRConnection() throws LIRCException {
        rec = ReceiverFactory.createReceiver();
    }

    public String keyPress() throws LIRCException  {
        String result = rec.readCode();

        return result;
    }
}
