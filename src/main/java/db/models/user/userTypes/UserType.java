package db.models.user.userTypes;

import content.Content;
import config.Config;
import db.dblogic.enums.DBService;
import db.models.user.User;
import db.models.message.MessageEventBus;
import db.models.message.MessagePostBox;

import java.util.Date;

public class UserType implements IUserType {
    private User user;
    public UserType(User user){
        this.user = user;
    }

    @Override
    public void receiveMessage(MessageEventBus message) {
        if (message.getSender().getName().equals(this.user.getName())) return;
        Content utils = new Content();
        try {
            String decrypted;
            String timestamp;

            decrypted = utils.decrypt(message.getMessage(), message.getAlgorithm(), message.getKeyFile());
            timestamp = String.valueOf(new Date().getTime() / 1000);

            DBService.instance.insertPostboxMessage(new MessagePostBox(message.getSender(), message.getRecipient(), decrypted, timestamp));
            Config.instance.textArea.info(String.format( user.getName() + " s received new message"));
        } catch (Exception e) {
            Config.instance.textArea.info("Decryption timed out!");
        }

    }

    @Override
    public String toString(){
        return "PARTICIPANT";
    }
}
