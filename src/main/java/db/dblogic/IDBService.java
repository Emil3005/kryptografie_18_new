

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

    void insertUser(String name, String type);

    void insertUser(User user);

    void insertChannel(Channel channel);

    void insertChannel(String name, String participantA, String participantB);

    void insertPostboxMessage(MessagePostBox messagePostBox);

    void insertPostboxMessage(String participantSender, String participantReceiver, String message);


    // Getter

    List<Channel> getChannels();

    Channel getChannel(String participantA, String participantB);

    String getUserType(String participantName);

    User getUser(String participantName);

    // Check for existence

    boolean channelExists(String channelName);

    boolean userExists(String name);
}
