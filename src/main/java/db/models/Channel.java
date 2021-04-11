/*
 * Copyright (c) 2021.
 * Author 6143217
 * All rights reserved
 */

package db.models;


import com.google.common.eventbus.EventBus;
import config.Config;
import db.dblogic.enums.DBService;

public class Channel {

    private final String name;
    private final Participant participantA;
    private final Participant participantB;
    private final EventBus eventBus = new EventBus();

    public Channel(String name, Participant participantA, Participant participantB){
        this.name = name;
        this.participantA = participantA;
        this.participantB = participantB;
        eventBus.register(participantA);
        eventBus.register(participantB);
        if (Config.instance.intrudedChannels.containsKey(this.name)){
            eventBus.register(DBService.instance.getParticipant(Config.instance.intrudedChannels.get(this.name)));
        }
    }

    public String getName(){
        return this.name;
    }

    public Participant getParticipantA(){
        return this.participantA;
    }

    public Participant getParticipantB(){
        return this.participantB;
    }

    public void send(BusMessage message){
        eventBus.post(message);
    }

    public void intrude(Participant intruder){
        eventBus.register(intruder);
        Config.instance.intrudedChannels.put(this.name, intruder.getName());
    }

}
