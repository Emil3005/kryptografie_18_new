package db.dblogic.enums;

import config.Config;
import db.dblogic.IDBService;
import db.models.channel.Channel;
import db.models.message.Message;
import db.models.message.MessagePostBox;
import db.models.user.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public enum DBService implements IDBService {
    instance;

    private final HSQLDB db;

    DBService() {
        db = HSQLDB.instance;
    }

    @Override
    public void setupConnection() {
        db.setupConnection();
    }

    @Override
    public void shutdown() {
        db.shutdown();
    }

    @Override
    public void insertMessage(String sender, String receiver, String algorithm, String keyFile, String message, String encryptedMessage) {
        int senderID;
        int receiverID;
        int algorithmID;

        senderID = getUserID(sender);
        receiverID = getUserID(receiver);
        algorithmID = getAlgorithmID(algorithm);

        long timeStamp = Instant.now().getEpochSecond();


        try {
            db.update(String.format("INSERT INTO messages" +
                    "(PARTICIPANT_FROM_ID, PARTICIPANT_TO_ID, PLAIN_MESSAGE, ALGORITHM_ID, ENCRYPTED_MESSAGE, KEYFILE, TIMESTAMP)" +
                    "VALUES (" + senderID + "," + receiverID + ",'" + message + "'," + algorithmID + ",'" + encryptedMessage + "','" + keyFile + "'," + timeStamp + ")"));
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.toString());
        }
    }

    @Override
    public void insertMessage(Message message) {
        insertMessage(message.getUserSender().getName(),
                message.getUserReceiver().getName(),
                message.getAlgorithm(),
                message.getKeyFile(),
                message.getPlainMessage(),
                message.getEncryptedMessage());
    }

    @Override
    public void insertAlgorithm(String algorithm) {
        if (getAlgorithmID(algorithm) < 0) {
            try {
                db.update(String.format("INSERT INTO algorithms (name) VALUES ('" + algorithm + "')"));
            } catch (SQLException exception) {
                Config.instance.textArea.info(exception.toString());
            }
        }
    }

    @Override
    public void insertType(String type) {
        type = type.toLowerCase();
        if (getTypeID(type) < 0) {
            try {
                db.update(String.format("INSERT INTO types (name) VALUES ('" + type + "')"));
            } catch (SQLException exception) {
                Config.instance.textArea.info(exception.toString());
            }
        }
    }

    @Override
    public void insertUser(String name, String type) {
        name = name.toLowerCase();
        int id = getTypeID(type);

        try {
            db.update(String.format("INSERT INTO participants (name,type_id) VALUES ('" + name + "', " + id + ")"));
            db.createTablePostbox(name);
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.toString());
        }
    }

    @Override
    public void insertUser(User user) {
        insertUser(user.getName(), user.getType());
    }

    @Override
    public void insertChannel(String name, String participantA, String participantB) {
        name = name.toLowerCase();
        int idA = getUserID(participantA);
        int idB = getUserID(participantB);

        try {
            db.update(String.format(
                    "INSERT INTO channel" +
                            "(name,participant_01,participant_02)" +
                            "VALUES ('" + name + "', " + idA + ", " + idB + ")"));
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.toString());
        }
    }

    @Override
    public void insertChannel(Channel channel) {
        insertChannel(channel.getName(),
                channel.getUserA().getName(),
                channel.getUserB().getName());
    }

    @Override
    public void insertPostboxMessage(String participantSender, String participantReceiver, String message) {
        if (!userExists(participantSender) || !userExists(participantReceiver)) {
            Config.instance.textArea.info("Participant " + participantSender + " not found");
            return;
        }

        int participantFromID = getUserID(participantSender);
        long timeStamp = Instant.now().getEpochSecond();
        try {
            db.update(String.format(
                    "INSERT INTO postbox_%s" +
                            " (participant_from_id, message, timestamp)" +
                            " VALUES (%d, '%s', %d)",
                    participantReceiver,
                    participantFromID,
                    message,
                    timeStamp));
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.toString());
        }
    }

    @Override
    public void insertPostboxMessage(MessagePostBox messagePostBox) {
        insertPostboxMessage(messagePostBox.getUserReceiver().getName(),
                messagePostBox.getUserSender().getName(),
                messagePostBox.getMessage());
    }

    public boolean removeChannel(String channelName) {
        var sql = String.format("DELETE FROM channel WHERE name='%s'", channelName);

        int affected;
        try {
            affected = db.update(sql);
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.toString());
            return false;
        }
        return affected != 0;

    }

    @Override
    public List<Channel> getChannels() {
        List<Channel> channelList = new ArrayList<>();

        try {
            ResultSet resultSet = db.executeQuery("SELECT * from channel");
            while (resultSet.next()) {

                int userID1 = resultSet.getInt("participant_01");
                int userID2 = resultSet.getInt("participant_02");
                User user1 = getUser(userID1);
                User user2 = getUser(userID2);
                channelList.add(new Channel(resultSet.getString("name"), user1, user2));

                int participant1ID = resultSet.getInt("participant_01");
                int participant2ID = resultSet.getInt("participant_02");
                User userA = getUser(participant1ID);
                User userB = getUser(participant2ID);
                channelList.add(new Channel(resultSet.getString("name"), userA, userB));

            }
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.getMessage());
        }
        return channelList;
    }


    @Override
    public Channel getChannel(String user1, String user2) {
        int participantID1 = getUserID(user1);
        int participantID2 = getUserID(user2);

        String sql = MessageFormat.format("SELECT name from channel where (participant_01=''{0}'' AND participant_02=''{1}'') or (participant_01=''{1}'' AND participant_02=''{0}'')", participantID1, participantID2);
        String channel;

        try {
            ResultSet resultSet = db.executeQuery(sql);
            if (!resultSet.next()) {
                throw new SQLException("Channel between: " + user1 + ", " + user2 + "not existing");
            }
            channel = resultSet.getString("name");
            return new Channel(channel, getUser(user1), getUser(user2));
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.getMessage());
            return null;
        }
    }

    public Channel getChannel(String channel) {
        int participantID1;
        int participantID2;

        try {
            ResultSet resultSet = db.executeQuery(String.format("SELECT name, participant_01, participant_02 FROM channel WHERE name='" + channel + "'"));
            if (!resultSet.next()) {
                throw new SQLException("No channel found with name: " + channel);
            }
            participantID1 = resultSet.getInt("participant_01");
            participantID2 = resultSet.getInt("participant_02");
            return new Channel(channel, getUser(participantID1), getUser(participantID2));
        } catch (SQLException sqlException) {
            Config.instance.textArea.info(sqlException.getMessage());
            return null;
        }
    }

    public boolean deleteChannel(String channel) {
        var sql = String.format("DELETE FROM channel WHERE name='" + channel + "'");

        int i;
        try {
            i = db.update(sql);
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.toString());
            return false;
        }
        return i != 0;

    }

    @Override
    public String getUserType(String participant) {
        if (participant == null)
            return null;

        participant = participant.toLowerCase();
        int typeID;

        try {
            ResultSet resultSet = db.executeQuery("SELECT TYPE_ID from PARTICIPANTS where name='" + participant + "'");
            if (!resultSet.next()) {
                throw new SQLException(participant + " not existing");
            }
            typeID = resultSet.getInt("TYPE_ID");
            return getTypeName(typeID);

        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.getMessage());
            return null;
        }
    }

    @Override
    public User getUser(String user) {
        user = user.toLowerCase();
        if (userExists(user)) {
            return new User(user, getUserType(user));
        }
        return null;
    }


    @Override
    public boolean channelExists(String channel) {
        try {
            ResultSet resultSet = db.executeQuery("SELECT name from channel where LOWER(name)='" + channel.toLowerCase() + "'");
            if (!resultSet.next()) {
                throw new SQLException();
            }
            return true;
        } catch (SQLException sqlException) {
            Config.instance.textArea.info(sqlException.getMessage());
            return false;
        }
    }

    @Override
    public boolean userExists(String name) {
        int id = getUserID(name.toLowerCase());
        return id != -1;
    }


    private int getTypeID(String name) {
        String nameLowercase = name.toLowerCase();
        try {

            ResultSet resultSet = db.executeQuery("SELECT ID from TYPES where name='" + name + "'");

            if (!resultSet.next()) {
                throw new SQLException("Type " + nameLowercase + " not existing");
            }
            return resultSet.getInt("ID");
        } catch (SQLException sqlException) {
            Config.instance.textArea.info(sqlException.getMessage());
            return -1;
        }
    }

    private int getUserID(String name) {
        try {
            ResultSet resultSet = db.executeQuery("SELECT ID from PARTICIPANTS where name='" + name + "'");
            if (!resultSet.next()) {
                throw new SQLException("Participant " + name + " not existing");
            }
            return resultSet.getInt("ID");
        } catch (SQLException sqlException) {
            Config.instance.textArea.info(sqlException.getMessage());
        }
        return -1;
    }

    private String getUserName(int participantID) {
        try {
            ResultSet resultSet = db.executeQuery("SELECT name from participants where ID=" + participantID);
            if (!resultSet.next()) {
                throw new SQLException("No participant found for id " + participantID);
            }
            return resultSet.getString("name");
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.getMessage());
            return null;
        }
    }

    private String getTypeName(int typeID) {
        try {
            ResultSet resultSet = db.executeQuery(String.format("SELECT name from TYPES where ID=" + typeID + ""));
            if (!resultSet.next()) {
                throw new SQLException("No type found for ID " + typeID);
            }
            return resultSet.getString("name");
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.getMessage());
            return null;
        }
    }

    private int getAlgorithmID(String algorithm) {
        try {
            ResultSet resultSet = db.executeQuery(String.format("SELECT ID from ALGORITHMS where LOWER(name)=LOWER('" + algorithm + "')"));
            if (!resultSet.next()) {
                throw new SQLException(("Algorithm " + algorithm + " not existing"));
            }
            return resultSet.getInt("ID");
        } catch (SQLException sqlException) {
            Config.instance.textArea.info(sqlException.getMessage());
            return -1;
        }
    }


    private User getUser(int partID) {
        String name = getUserName(partID);
        return new User(name, Objects.requireNonNull(getUserType(name)));

    }

    public void init() {
        System.out.println("inserting initial values");

        insertType("normal");
        insertType("intruder");

        insertAlgorithm("shift");
        insertAlgorithm("rsa");

        insertUser("branch_hkg", "normal");
        insertUser("branch_wuh", "normal");
        insertUser("branch_cpt", "normal");
        insertUser("branch_syd", "normal");
        insertUser("branch_sfo", "normal");
        insertUser("msa", "intruder");

        insertChannel("hkg_wuh", "branch_hkg", "branch_wuh");
        insertChannel("hkg_cpt", "branch_hkg", "branch_cpt");
        insertChannel("cpt_syd", "branch_cpt", "branch_syd");
        insertChannel("syd_sfo", "branch_syd", "branch_sfo");
    }
}