/*
 * Copyright (c) 2021.
 * Author 6143217
 * All rights reserved
 */

package db.models;

import config.AlgorithmUsed;

public class BusMessage {

    private String message;
    private Participant sender;
    private Participant recipient;
    private AlgorithmUsed algorithm;
    private String keyFile;

    public BusMessage(String message, Participant sender, Participant recipient, AlgorithmUsed algorithm, String keyFile){
        this.message = message;
        this.sender = sender;
        this.recipient = recipient;
        this.algorithm = algorithm;
        this.keyFile = keyFile;
    }

    public String getMessage() {
        return message;
    }

    public Participant getSender() {
        return sender;
    }

    public Participant getRecipient() {
        return recipient;
    }

    public AlgorithmUsed getAlgorithm() {
        return algorithm;
    }

    public String getKeyFile() {
        return keyFile;
    }
}
