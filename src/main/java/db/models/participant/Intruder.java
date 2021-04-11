package db.models.participant;

import config.AlgorithmUsed;
import config.Config;
import cryption.Content;
import db.dblogic.enums.DBService;
import db.models.BusMessage;
import db.models.PostboxMessage;

import java.util.Date;

public class Intruder implements IParticipantType {
    public Intruder (){

    }

    @Override
    public void receiveMessage(BusMessage message) {
        Content utils = new Content();

        String timestamp = String.valueOf(new Date().getTime()/1000);
        DBService.instance.insertPostboxMessage(new PostboxMessage(message.getSender(), message.getRecipient(), "unknown", timestamp));
        try{
            String cracked = message.getAlgorithm().equals(AlgorithmUsed.RSA) ? utils.crackEncryptedMessageUsingRSA(message.getMessage(), message.getKeyFile()) : utils.crackEncryptedMessageUsingShift(message.getMessage());
            Config.instance.textArea.info(String.format("cracked message: %s", cracked));
        } catch (Exception e){
            Config.instance.textArea.info(String.format("cracking encrypted message \"%s\"", message.getMessage()));
        }

    }

}
