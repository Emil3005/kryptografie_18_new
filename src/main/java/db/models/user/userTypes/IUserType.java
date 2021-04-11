package db.models.user.userTypes;

import db.models.message.MessageEventBus;

public interface IUserType {
    void receiveMessage(MessageEventBus message);
    String toString();
}
