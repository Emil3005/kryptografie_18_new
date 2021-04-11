package db.models.message;

import db.models.user.User;

public class Message {
    private final User userSender;
    private final User userReceiver;
    private final String algorithm;
    private final String keyFile;
    private final String timestamp;
    private final String plainMessage;
    private final String encryptedMessage;

    public Message(User userSender, User userReceiver, String algorithm, String keyFile, String timestamp, String plainMessage, String encryptedMessage) {
        this.userSender = userSender;
        this.userReceiver = userReceiver;
        this.algorithm = algorithm;
        this.keyFile = keyFile;
        this.timestamp = timestamp;
        this.plainMessage = plainMessage;
        this.encryptedMessage = encryptedMessage;
    }

    public User getUserSender() {
        return this.userSender;
    }

    public User getUserReceiver() {
        return this.userReceiver;
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
