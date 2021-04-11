package db.models.user.userTypes;

import config.AlgorithmUsed;
import config.Config;
import cryption.Content;
import db.dblogic.enums.DBService;
import db.models.message.MessageEventBus;
import db.models.message.MessagePostBox;

import java.util.Date;

public class Intruder implements IUserType {

    @Override
    public void receiveMessage(MessageEventBus message) {
        Content utils = new Content();

        String timestamp = String.valueOf(new Date().getTime()/1000);

        DBService.instance.insertPostboxMessage(new MessagePostBox(message.getSender(), message.getRecipient(), "unknown", timestamp));

        try{
            String cracked;
            if (message.getAlgorithm().equals(AlgorithmUsed.RSA)) {
                cracked = utils.crackEncryptedMessageUsingRSA(message.getMessage(), message.getKeyFile());
            }
            else {
                cracked = utils.crackEncryptedMessageUsingShift(message.getMessage());
            }
            Config.instance.textArea.info(String.format("cracked message: "+cracked));
        } catch (Exception e){
            Config.instance.textArea.info(String.format("cracking encrypted message \""+message.getMessage()+"\""));
        }

    }

}
