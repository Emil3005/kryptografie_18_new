package db.dblogic.enums;

import config.Config;
import db.dblogic.IDBService;
import db.models.Channel;
import db.models.Message;
import db.models.Participant;
import db.models.PostboxMessage;

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

        senderID = getParticipantID(sender);
        receiverID = getParticipantID(receiver);
        algorithmID = getAlgorithmID(algorithm);

        long timeStamp = Instant.now().getEpochSecond();


        try {
            db.update(String.format("INSERT INTO messages" +
                    "(PARTICIPANT_FROM_ID, PARTICIPANT_TO_ID, PLAIN_MESSAGE, ALGORITHM_ID, ENCRYPTED_MESSAGE, KEYFILE, TIMESTAMP)" +
                    "VALUES ("+ senderID +","+ receiverID +",'"+ message +"',"+ algorithmID +",'"+ encryptedMessage +"','"+ keyFile +"',"+ timeStamp +")"));
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.toString());
        }
    }

    @Override
    public void insertMessage(Message message) {
        insertMessage(message.getParticipantSender().getName(),
                message.getParticipantReceiver().getName(),
                message.getAlgorithm(),
                message.getKeyFile(),
                message.getPlainMessage(),
                message.getEncryptedMessage());
    }

    @Override
    public void insertAlgorithm(String algorithm) {
        if (getAlgorithmID(algorithm) < 0) {
            try {
                db.update(String.format("INSERT INTO algorithms (name) VALUES ('"+ algorithm +"')"));
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
                db.update(String.format("INSERT INTO types (name) VALUES ('"+ type +"')"));
            } catch (SQLException exception) {
                Config.instance.textArea.info(exception.toString());
            }
        }
    }

    @Override
    public void insertParticipant(String name, String type) {
        name = name.toLowerCase();
        int id = getTypeID(type);

        try {
            db.update(String.format("INSERT INTO participants (name,type_id) VALUES ('"+ name +"', "+id+")"));
            db.createTablePostbox(name);
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.toString());
        }
    }

    @Override
    public void insertParticipant(Participant participant) {
        insertParticipant(participant.getName(), participant.getType());
    }

    @Override
    public void insertChannel(String name, String participantA, String participantB) {
       name = name.toLowerCase();
        int idA = getParticipantID(participantA);
        int idB = getParticipantID(participantB);

        try {
            db.update(String.format(
                            "INSERT INTO channel" +
                            "(name,participant_01,participant_02)" +
                            "VALUES ('"+ name +"', "+ idA +", "+ idB +")"));
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.toString());
        }
    }

    @Override
    public void insertChannel(Channel channel) {
        insertChannel(channel.getName(),
                channel.getParticipantA().getName(),
                channel.getParticipantB().getName());
    }

    @Override
    public List<Channel> getChannels() {
        List<Channel> channelList = new ArrayList<>();

        try {
            ResultSet resultSet = db.executeQuery("SELECT * from channel");
            while (resultSet.next()) {
                int participantID1 = resultSet.getInt("participant_01");
                int participantID2 = resultSet.getInt("participant_02");
                Participant participant1 = getParticipant(participantID1);
                Participant participant2 = getParticipant(participantID2);
                channelList.add(new Channel(resultSet.getString("name"), participant1, participant2));
            }
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.getMessage());
        }
        return channelList;
    }


    @Override
    public Channel getChannel(String participant1, String participant2) {
        int participantID1 = getParticipantID(participant1);
        int participantID2 = getParticipantID(participant2);

        String sql = MessageFormat.format("SELECT name from channel where (participant_01=''{0}'' AND participant_02=''{1}'') or (participant_01=''{1}'' AND participant_02=''{0}'')", participantID1, participantID2);
        String channel;

        try {
            ResultSet resultSet = db.executeQuery(sql);
            if (!resultSet.next()) {
                throw new SQLException("Channel between: "+ participant1 +", "+ participant2 +"not existing");
            }
            channel = resultSet.getString("name");
            return new Channel(channel, getParticipant(participant1), getParticipant(participant2));
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.getMessage());
            return null;
        }
    }

    public Channel getChannel(String channel) {
        int participantID1;
        int participantID2;

        try {
            ResultSet resultSet = db.executeQuery(String.format("SELECT name, participant_01, participant_02 FROM channel WHERE name='"+ channel +"'"));
            if (!resultSet.next()) {
                throw new SQLException("No channel found with name: " + channel);
            }
            participantID1 = resultSet.getInt("participant_01");
            participantID2 = resultSet.getInt("participant_02");
            return new Channel(channel, getParticipant(participantID1), getParticipant(participantID2));
        } catch (SQLException sqlException) {
            Config.instance.textArea.info(sqlException.getMessage());
            return null;
        }
    }

    public boolean deleteChannel(String channel) {
        var sql = String.format("DELETE FROM channel WHERE name='"+ channel +"'");

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
    public void insertPostboxMessage(String sender, String receiver, String message) {
        if (!participantExists(sender) || !participantExists(receiver)) {
            Config.instance.textArea.info("Participant "+ sender +" not found");
            return;
        }

        int id = getParticipantID(sender);
        long timeStamp = Instant.now().getEpochSecond();
        try {
            db.update(String.format(
                            "INSERT INTO postbox_"+ receiver +"" +
                            " (participant_from_id, message, timestamp)" +
                                    " VALUES ("+ id +", '"+ message +"', "+ timeStamp +")"));
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.toString());
        }
    }

    @Override
    public void insertPostboxMessage(PostboxMessage postboxMessage) {
        insertPostboxMessage(postboxMessage.getParticipantReceiver().getName(),
                postboxMessage.getParticipantSender().getName(),
                postboxMessage.getMessage());
    }


    @Override
    public String getParticipantType(String participant) {
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
    public Participant getParticipant(String participant) {
        participant = participant.toLowerCase();
        if (participantExists(participant)) {
            return new Participant(participant, getParticipantType(participant));
        }
        return null;
    }

    @Override
    public boolean channelExists(String channel) {
        try {
            ResultSet resultSet = db.executeQuery("SELECT name from channel where LOWER(name)='"+ channel.toLowerCase() +"'");
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
    public boolean participantExists(String name) {
        int id = getParticipantID(name.toLowerCase());
        return id != -1;
    }


    private int getTypeID(String name) {
        name = name.toLowerCase();
        try {
            ResultSet resultSet = db.executeQuery("SELECT ID from TYPES where name='"+ name + "'");
            if (!resultSet.next()) {
                throw new SQLException("Type "+ name + " not existing");
            }
            return resultSet.getInt("ID");
        } catch (SQLException sqlException) {
            Config.instance.textArea.info(sqlException.getMessage());
            return -1;
        }
    }

    private int getParticipantID(String name) {
        try {
            ResultSet resultSet = db.executeQuery("SELECT ID from PARTICIPANTS where name='"+ name +"'");
            if (!resultSet.next()) {
                throw new SQLException("Participant "+ name +" not existing");
            }
            return resultSet.getInt("ID");
        } catch (SQLException sqlException) {
            Config.instance.textArea.info(sqlException.getMessage());
        }
        return -1;
    }

    private String getParticipantName(int participantID) {
        try {
            ResultSet resultSet = db.executeQuery("SELECT name from participants where ID="+ participantID);
            if (!resultSet.next()) {
                throw new SQLException("No participant found for id "+ participantID);
            }
            return resultSet.getString("name");
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.getMessage());
            return null;
        }
    }

    private String getTypeName(int typeID) {
        try {
            ResultSet resultSet = db.executeQuery(String.format("SELECT name from TYPES where ID="+ typeID +""));
            if (!resultSet.next()) {
                throw new SQLException("No type found for ID "+ typeID);
            }
            return resultSet.getString("name");
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.getMessage());
            return null;
        }
    }

    private int getAlgorithmID(String algorithm) {
        try {
            ResultSet resultSet = db.executeQuery(String.format("SELECT ID from ALGORITHMS where LOWER(name)=LOWER('"+ algorithm +"')"));
            if (!resultSet.next()) {
                throw new SQLException(("Algorithm "+ algorithm +" not existing"));
            }
            return resultSet.getInt("ID");
        } catch (SQLException sqlException) {
            Config.instance.textArea.info(sqlException.getMessage());
            return -1;
        }
    }

    private Participant getParticipant(int participantID) {
        String name = getParticipantName(participantID);
        return new Participant(name, Objects.requireNonNull(getParticipantType(name)));
    }

    public void init() {
        System.out.println("inserting initial values");

        insertType("normal");
        insertType("intruder");

        insertAlgorithm("shift");
        insertAlgorithm("rsa");

        insertParticipant("branch_hkg","normal");
        insertParticipant("branch_wuh","normal");
        insertParticipant("branch_cpt","normal");
        insertParticipant("branch_syd","normal");
        insertParticipant("branch_sfo","normal");
        insertParticipant("msa","intruder");

        insertChannel("hkg_wuh","branch_hkg","branch_wuh");
        insertChannel("hkg_cpt","branch_hkg","branch_cpt");
        insertChannel("cpt_syd","branch_cpt","branch_syd");
        insertChannel("syd_sfo","branch_syd","branch_sfo");
    }
}