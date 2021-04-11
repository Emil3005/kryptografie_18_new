/*
 * Copyright (c) 2021.
 * Author 6143217
 * All rights reserved
 */

package db.models.message;

import db.models.user.User;

public class MessagePostBox {
    private final User userSender;
    private final User userReceiver;
    private final String message;
    private final String timestamp;

    public MessagePostBox(User userSender, User userReceiver, String message, String timestamp){
        this.userSender = userSender;
        this.userReceiver = userReceiver;
        this.message = message;
        this.timestamp = timestamp;
    }

    public User getUserSender() {
        return userSender;
    }

    public User getUserReceiver() {
        return userReceiver;
    }

    public String getMessage() {
        return message;
    }
}
