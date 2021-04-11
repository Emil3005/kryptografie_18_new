/*
 * Copyright (c) 2021.
 * Author 6143217
 * All rights reserved
 */

package db.models;

public class Message {
    private final Participant participantSender;
    private final Participant participantReceiver;
    private final String algorithm;
    private final String keyFile;
    private final String timestamp;
    private final String plainMessage;
    private final String encryptedMessage;

    public Message(Participant participantSender, Participant participantReceiver, String algorithm, String keyFile, String timestamp, String plainMessage, String encryptedMessage){
        this.participantSender = participantSender;
        this.participantReceiver = participantReceiver;
        this.algorithm = algorithm;
        this.keyFile = keyFile;
        this.timestamp = timestamp;
        this.plainMessage = plainMessage;
        this.encryptedMessage = encryptedMessage;
    }

    public Participant getParticipantSender(){
        return this.participantSender;
    }

    public Participant getParticipantReceiver(){
        return this.participantReceiver;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public String getKeyFile() {
        return this.keyFile;
    }

    public String getPlainMessage() {
        return this.plainMessage;
    }

    public String getEncryptedMessage() {
        return this.encryptedMessage;
    }
}
