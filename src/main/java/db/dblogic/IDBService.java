/*
 * Copyright (c) 2021.
 * Author 6143217
 * All rights reserved
 */

package db.dblogic;

import db.models.channel.Channel;
import db.models.message.Message;
import db.models.user.User;
import db.models.message.MessagePostBox;

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

    void insertParticipant(User user);

    void insertChannel(Channel channel);

    void insertChannel(String name, String participantA, String participantB);

    void insertPostboxMessage(MessagePostBox messagePostBox);

    void insertPostboxMessage(String participantSender, String participantReceiver, String message);


    // Getter

    List<Channel> getChannels();

    Channel getChannel(String participantA, String participantB);

    String getParticipantType(String participantName);

<<<<<<< HEAD
    Participant getParticipant(String participantName);
=======
    User getOneParticipant(String participantName);
>>>>>>> a57cd88f43bddda1db163989e3aac6588639ac30

    // Check for existence

    boolean channelExists(String channelName);

    boolean participantExists(String name);
}
