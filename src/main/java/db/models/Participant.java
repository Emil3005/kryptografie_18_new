/*
 * Copyright (c) 2021.
 * Author 6143217
 * All rights reserved
 */

package db.models;

import com.google.common.eventbus.Subscribe;
import db.models.participant.IParticipantType;
import db.models.participant.Intruder;

public class Participant {
    private String name;
    private IParticipantType type;

    public Participant(String name, String input){
        this.name = name;
        if (input.equals("intruder")) {
            this.type = new Intruder();
        } else{
            this.type = new db.models.participant.Participant(this);
        }
    }

    @Subscribe
    public void receiveMessage(BusMessage message){
        type.receiveMessage(message);
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type.toString();
    }
}
