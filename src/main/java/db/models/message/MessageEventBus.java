/*
 * Copyright (c) 2021.
 * Author 6143217
 * All rights reserved
 */

package db.models.message;

import config.AlgorithmUsed;
import db.models.user.User;

public class MessageEventBus {

    private String message;
    private User sender;
    private User recipient;
    private AlgorithmUsed algorithm;
    private String keyFile;

    public MessageEventBus(String message, User sender, User recipient, AlgorithmUsed algorithm, String keyFile){
        this.message = message;
        this.sender = sender;
        this.recipient = recipient;
        this.algorithm = algorithm;
        this.keyFile = keyFile;
    }

    public String getMessage() {
        return message;
    }

    public User getSender() {
        return sender;
    }

    public User getRecipient() {
        return recipient;
    }

    public AlgorithmUsed getAlgorithm() {
        return algorithm;
    }

    public String getKeyFile() {
        return keyFile;
    }
}
