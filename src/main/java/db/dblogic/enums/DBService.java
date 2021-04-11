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
    public void insertType(String type) {
        type = type.toLowerCase();
        if (getTypeID(type) < 0) {
            try {
                db.update(String.format("INSERT INTO types (name) VALUES ('%s')", type));
            } catch (SQLException exception) {
                Config.instance.textArea.info(exception.toString());
            }
        }

    }

    @Override
    public void insertAlgorithm(String algorithm) {
        if (getAlgorithmID(algorithm) < 0) {
            try {
                db.update(String.format("INSERT INTO algorithms (name) VALUES ('%s')", algorithm));
            } catch (SQLException exception) {
                Config.instance.textArea.info(exception.toString());
            }
        }

    }

    @Override
    public void insertMessage(String participantSender, String participantReceiver, String algorithm, String keyFile, String plainMessage, String encryptedMessage) {
        int participantSenderID;
        int participantReceiverID;
        int algorithmID;

        participantSenderID = getParticipantID(participantSender);
        participantReceiverID = getParticipantID(participantReceiver);
        algorithmID = getAlgorithmID(algorithm);

        long timeStamp = Instant.now().getEpochSecond();


        try {
            db.update(String.format("INSERT INTO messages" +
                            "(PARTICIPANT_FROM_ID, PARTICIPANT_TO_ID, PLAIN_MESSAGE, ALGORITHM_ID, ENCRYPTED_MESSAGE, KEYFILE, TIMESTAMP)" +
                            "VALUES (%d,%d,'%s',%d,'%s','%s',%d)",
                    participantSenderID,
                    participantReceiverID,
                    plainMessage,
                    algorithmID,
                    encryptedMessage,
                    keyFile,
                    timeStamp)
            );
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
    public void insertParticipant(String name, String type) {
        String lowercaseName = name.toLowerCase();
        int typeID = getTypeID(type);

        try {
            db.update(String.format("INSERT INTO participants (name,type_id) VALUES ('%s', %d)", lowercaseName, typeID));
            db.createTablePostbox(lowercaseName);
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
       String lowercaseName = name.toLowerCase();
        int participantA_ID = getParticipantID(participantA);
        int participantB_ID = getParticipantID(participantB);

        try {
            db.update(String.format(
                            "INSERT INTO channel" +
                            "(name,participant_01,participant_02)" +
                            "VALUES ('%s', %d, %d)",
                            lowercaseName,
                            participantA_ID,
                            participantB_ID));
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
    public void insertPostboxMessage(String participantSender, String participantReceiver, String message) {
        if (!participantExists(participantSender) || !participantExists(participantReceiver)) {
            Config.instance.textArea.info("Participant " + participantSender + " not found");
            return;
        }

        int participantFromID = getParticipantID(participantSender);
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
    public void insertPostboxMessage(PostboxMessage postboxMessage) {
        insertPostboxMessage(postboxMessage.getParticipantReceiver().getName(),
                postboxMessage.getParticipantSender().getName(),
                postboxMessage.getMessage());
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
                int participant1ID = resultSet.getInt("participant_01");
                int participant2ID = resultSet.getInt("participant_02");
                Participant participantA = getParticipant(participant1ID);
                Participant participantB = getParticipant(participant2ID);
                channelList.add(new Channel(resultSet.getString("name"), participantA, participantB));
            }
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.getMessage());
        }

        return channelList;
    }


    @Override
    public Channel getChannel(String participantA, String participantB) {
        int partAID = getParticipantID(participantA);
        int partBID = getParticipantID(participantB);

        String sql = MessageFormat.format("SELECT name from channel where (participant_01=''{0}'' AND participant_02=''{1}'') or (participant_01=''{1}'' AND participant_02=''{0}'')", partAID, partBID);
        String channelName;

        try {
            ResultSet resultSet = db.executeQuery(sql);
            if (!resultSet.next()) {
                throw new SQLException("Channel between: " + participantA + ", " + participantB + "not existing");
            }
            channelName = resultSet.getString("name");
            return new Channel(channelName, getOneParticipant(participantA), getOneParticipant(participantB));
        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.getMessage());
            return null;
        }
    }

    public Channel getChannel(String channelName) {
        int part1Id;
        int part2Id;

        try {
            ResultSet resultSet = db.executeQuery(String.format("SELECT name, participant_01, participant_02 FROM channel WHERE name='%s'", channelName));
            if (!resultSet.next()) {
                throw new SQLException("No channel found with name: " + channelName);
            }
            part1Id = resultSet.getInt("participant_01");
            part2Id = resultSet.getInt("participant_02");
            return new Channel(channelName, getParticipant(part1Id), getParticipant(part2Id));
        } catch (SQLException sqlException) {
            Config.instance.textArea.info(sqlException.getMessage());
            return null;
        }
    }

    @Override
    public String getOneParticipantType(String participantName) {
        if (participantName == null)
            return null;

        participantName = participantName.toLowerCase();
        int typeID;

        try {
            ResultSet resultSet = db.executeQuery("SELECT TYPE_ID from PARTICIPANTS where name='" + participantName + "'");
            if (!resultSet.next()) {
                throw new SQLException(participantName + " not existing");
            }
            typeID = resultSet.getInt("TYPE_ID");
            return getTypeName(typeID);

        } catch (SQLException exception) {
            Config.instance.textArea.info(exception.getMessage());
            return null;
        }
    }

    @Override
    public Participant getOneParticipant(String participantName) {
        participantName = participantName.toLowerCase();
        if (participantExists(participantName)) {
            return new Participant(participantName, getOneParticipantType(participantName));
        }
        return null;
    }

    @Override
    public boolean channelExists(String channelName) {
        try {
            ResultSet resultSet = db.executeQuery("SELECT name from channel where LOWER(name)='" + channelName.toLowerCase() + "'");
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
        int participantID = getParticipantID(name.toLowerCase());
        return participantID != -1;
    }


    private int getTypeID(String name) {
        name = name.toLowerCase();
        try {
            ResultSet resultSet = db.executeQuery("SELECT ID from TYPES where name='" + name + "'");
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

    private String getParticipantName(int participantID) {
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
            ResultSet resultSet = db.executeQuery(String.format("SELECT name from TYPES where ID=%d", typeID));
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
            ResultSet resultSet = db.executeQuery(String.format("SELECT ID from ALGORITHMS where LOWER(name)=LOWER('%s')", algorithm));
            if (!resultSet.next()) {
                throw new SQLException(("Algorithm " + algorithm + " not existing"));
            }
            return resultSet.getInt("ID");
        } catch (SQLException sqlException) {
            Config.instance.textArea.info(sqlException.getMessage());
            return -1;
        }
    }

    private Participant getParticipant(int partID) {
        String name = getParticipantName(partID);
        return new Participant(name, Objects.requireNonNull(getOneParticipantType(name)));
    }

    public void createInitialValues() {
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