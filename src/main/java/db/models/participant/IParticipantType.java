package db.models.participant;

import db.models.BusMessage;

public interface IParticipantType {
    public void receiveMessage(BusMessage message);
    public String toString();
}
