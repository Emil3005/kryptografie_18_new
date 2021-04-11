package db.models.participant;

import cryption.Content;
import config.Config;
import db.dblogic.enums.DBService;
import db.models.BusMessage;
import db.models.PostboxMessage;

import java.util.Date;

public class Participant implements IParticipantType {
    private db.models.Participant participant;
    public Participant (db.models.Participant participant){
        this.participant = participant;
    }

    @Override
    public void receiveMessage(BusMessage message) {
        if (message.getSender().getName().equals(this.participant.getName())) return;

        Content utils = new Content();

        try {
            String decrypted = utils.decrypt(message.getMessage(), message.getAlgorithm(), message.getKeyFile());
            String timestamp = String.valueOf(new Date().getTime()/1000);
            DBService.instance.insertPostboxMessage(new PostboxMessage(message.getSender(), message.getRecipient(), decrypted, timestamp));
            Config.instance.textArea.info(String.format("%s received new message", participant.getName()));
        } catch (Exception e) {
            Config.instance.textArea.info("Decryption timed out!");
        }

    }

    @Override
    public String toString(){
        return "PARTICIPANT";
    }
}
