/*
 * Copyright (c) 2021.
 * Author 6143217
 * All rights reserved
 */

package db.dblogic;

import db.models.Channel;
import db.models.Message;
import db.models.Participant;
import db.models.PostboxMessage;

import java.util.List;

public interface IDBService {

    void setupConnection();

    void shutdown();

    // Inserts

    void insertType(String type);

    void insertAlgorithm(String algorithm);

    void insertMessage(String participantSender, String participantReceiver, String algorithm, String keyFile, String plainMessage, String encryptedMessage);

    void insertMessage(Message message);

    void insertParticipant(String name, String type);

    void insertParticipant(Participant participant);

    void insertChannel(Channel channel);

    void insertChannel(String name, String participantA, String participantB);

    void insertPostboxMessage(PostboxMessage postboxMessage);

    void insertPostboxMessage(String participantSender, String participantReceiver, String message);


    // Getter

    List<Channel> getChannels();

    Channel getChannel(String participantA, String participantB);

    String getParticipantType(String participantName);

    Participant getParticipant(String participantName);

    // Check for existence

    boolean channelExists(String channelName);

    boolean participantExists(String name);
}
