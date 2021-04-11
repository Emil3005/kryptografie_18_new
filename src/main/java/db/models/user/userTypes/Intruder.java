package db.models.user.userTypes;

import config.AlgorithmEnum;
import config.Config;
import content.Content;
import db.dblogic.enums.DBService;
import db.models.message.MessageEventBus;
import db.models.message.MessagePostBox;

import java.util.Date;

public class Intruder implements IUserType {

    @Override
    public void receiveMessage(MessageEventBus message) {
        Content utils = new Content();

        String timestamp = String.valueOf(new Date().getTime() / 1000);

        DBService.instance.insertPostboxMessage(new MessagePostBox(message.getSender(), message.getRecipient(), "unknown", timestamp));

        try {
            String cracked;
            if (message.getAlgorithm().equals(AlgorithmEnum.RSA)) {
                cracked = utils.crackEncryptedMessageUsingRSA(message.getMessage(), message.getKeyFile());
            } else {
                cracked = utils.crackEncryptedMessageUsingShift(message.getMessage());
            }
            Config.instance.textArea.info(String.format("Cracked message: " + cracked));
        } catch (Exception e) {
            Config.instance.textArea.info(String.format("Cracking encrypted message \"" + message.getMessage() + "\""));
        }

    }

}
