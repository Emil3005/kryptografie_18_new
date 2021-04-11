package db.models.message;

import config.AlgorithmEnum;
import db.models.user.User;

public class MessageEventBus {

    private String message;
    private User sender;
    private User recipient;
    private AlgorithmEnum algorithm;
    private String keyFile;

    public MessageEventBus(String message, User sender, User recipient, AlgorithmEnum algorithm, String keyFile) {
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

    public AlgorithmEnum getAlgorithm() {
        return algorithm;
    }

    public String getKeyFile() {
        return keyFile;
    }
}
