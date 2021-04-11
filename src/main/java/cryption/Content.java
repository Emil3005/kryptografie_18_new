/*
 * Copyright (c) 2021.
 * Author 6143217
 * All rights reserved
 */

package cryption;

import cryption.commandPattern.RSACommand;
import cryption.commandPattern.ShiftCommand;
import config.AlgorithmUsed;
import config.Config;
import db.dblogic.enums.DBService;
import db.models.BusMessage;
import db.models.Channel;
import db.models.Message;
import db.models.Participant;

import java.io.File;
import java.io.InvalidObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class Content {
    File keyFile;
    String strJarFileName;
    String strClassName;

    public String encrypt(String message, AlgorithmUsed algorithm, String strKeyFile) {
        String result = null;
        keyFile = new File(Config.instance.keyFiles + strKeyFile);

        if (!keyFile.exists()) {
            Config.instance.textArea.info("Keyfile " + strKeyFile + " not existing");
        } else {
            Config.instance.loggingHandler.createLogfile(algorithm.toString(), "encrypt");
            checkAlgorithm(algorithm);
            if (VerifyJar.verified(strJarFileName)) {
                Config.instance.textArea.info("jar could not be verified - not loading corrupted jar");
            } else {
                try {
                    Object port = PortLoader.getPort(Config.instance.jarPath + strJarFileName, strClassName);
                    Method method = port.getClass().getDeclaredMethod("encrypt", File.class, String.class, Logger.class);
                    Object answer = method.invoke(port, keyFile, message, Config.instance.loggingHandler.getLogger());
                    if (answer == null) {
                        Config.instance.textArea.info("Problems encrypting message, keyFile might be invalid");
                    } else {
                        result = answer.toString();
                    }
                } catch (InvalidObjectException e) {
                    Config.instance.textArea.info("Invalid KeyFile Provided");
                } catch (IllegalAccessException | SecurityException | IllegalArgumentException | NoSuchMethodException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }

        return result;
    }

    public String decrypt(String message, AlgorithmUsed algorithmUsed, String strKeyFile) {
        String result = null;
        keyFile = new File(Config.instance.keyFiles + strKeyFile);
        if (!keyFile.exists()) {
            Config.instance.textArea.info("KeyFile " + strKeyFile + " does not exist");
        } else {
            Config.instance.loggingHandler.createLogfile(algorithmUsed.toString(), "decrypt");
            checkAlgorithm(algorithmUsed);
            if (VerifyJar.verified(strJarFileName)) {
                Config.instance.textArea.info("jar could not be verified - not loading corrupted jar");
            } else {
                try {
                    Object port = PortLoader.getPort(Config.instance.jarPath + strJarFileName, strClassName);
                    Method method = port.getClass().getDeclaredMethod("decrypt", File.class, String.class, Logger.class);
                    Object answer = method.invoke(port, keyFile, message, Config.instance.loggingHandler.getLogger());
                    if (answer == null) {
                        Config.instance.textArea.info("Problems decrypting message, keyFile might be invalid");
                    } else {
                        result = answer.toString();
                    }
                } catch (InvalidObjectException e) {
                    Config.instance.textArea.info("KeyFile is invalid");
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public String crackEncryptedMessageUsingRSA(String message, String keyFile){

        String cracked;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new RSACommand(keyFile, message));
        try {
            cracked = future.get(30, TimeUnit.SECONDS);
            return cracked;
        } catch (Exception e) {
            Config.instance.textArea.info(String.format("cracking encrypted message \""+message+"\" failed"));
        }
        return null;
    }

    public String crackEncryptedMessageUsingShift(String message){
        String cracked;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<String> future = executor.submit(new ShiftCommand(message));
        try {
            cracked = future.get(30, TimeUnit.SECONDS);
            return cracked;
        } catch (Exception e) {
            Config.instance.textArea.info(String.format("cracking encrypted message \"" + message + "\" failed"));
        }
        return null;
    }
    public void registerParticipant(String name, String type){
        if (DBService.instance.participantExists(name)){
            Config.instance.textArea.info(String.format("participant " + name + " already exists, using existing postbox_"+ name));
        }

        Participant participant = new Participant(name, type);
        Config.instance.textArea.info(String.format("participant " + name + " with Type " + type + " registered and postbox_"+ name + " created"));
        DBService.instance.insertParticipant(participant);
    }

    public  void createChannel(String channelName, String part1name, String part2name){
        if (DBService.instance.channelExists(channelName))
            Config.instance.textArea.info(String.format("channel "+ channelName +" already exists"));

        if (DBService.instance.getChannel(part1name, part2name) != null)
            Config.instance.textArea.info(String.format("communication channel between "+part1name+" and "+part2name+" already exists"));

        if (part1name.equals(part2name))
            Config.instance.textArea.info(String.format(part1name + " and "+part2name+" are identical - cannot create channel on itself"));

        Participant instanceFirstParticipant = DBService.instance.getOneParticipant(part1name);
        Participant instanceSecondParticipant = DBService.instance.getOneParticipant(part2name);

        Channel channel = new Channel(channelName, instanceFirstParticipant, instanceSecondParticipant);
        Config.instance.textArea.info(String.format("channel " + channelName + " from "+ part1name + " to "+part2name+" successfully created"));

        DBService.instance.insertChannel(channel);
    }
    public void showChannel(){
        List<Channel> channelList = DBService.instance.getChannels();

        //StringBuilder returnString = new StringBuilder();

        channelList.stream().map(channel -> String.format(channel.getName() + " | " + channel.getParticipantA().getName() + " and " + channel.getParticipantB().getName())).forEach(Config.instance.textArea::info);
        if (!channelList.isEmpty()) {
            return;
        }
        Config.instance.textArea.info("channel list empty!");
    }

    public void dropChannel(String channelName){
        Channel channel = DBService.instance.getChannel(channelName);

        if (channel == null) {
            Config.instance.textArea.info(String.format("unknown channel "+channelName));
        }

        if (!DBService.instance.removeChannel(channelName)) {
            Config.instance.textArea.info("Something went wrong");
        }
        else{
            Config.instance.textArea.info("channel "+channelName+" deleted");
        }
    }



    public void intrudeChannel(String channelName, String participant){
        Participant intruder = DBService.instance.getOneParticipant(participant);

        if (intruder == null){
            Config.instance.textArea.info(String.format("intruder "+participant+" could not be found"));
        }

        Channel channel = DBService.instance.getChannel(channelName);

        if (channel == null){
            Config.instance.textArea.info(String.format("channel %s could not be found", channelName));
        }

        channel.intrude(intruder);
        Config.instance.textArea.info(String.format("intruder " + intruder.getName() + " cracked Message from participant "+participant+ " | " + channelName ));
    }



    public void sendMessage(String message, String sender, String recipient, AlgorithmUsed algorithmUsed, String strKeyFile){
        Participant senderPart = DBService.instance.getOneParticipant(sender);
        Participant receiverPart = DBService.instance.getOneParticipant(recipient);
        Channel channel = DBService.instance.getChannel(sender, recipient);
        Date currentDate = new Date();
        long timestampLong = currentDate.getTime()/1000;
        String timestamp = String.valueOf(timestampLong);

        if (channel == null){
            Config.instance.textArea.info(String.format("no valid channel from "+sender+" to "+recipient));
        }

        String encrypted = encrypt(message, algorithmUsed, strKeyFile);

        Message dbMessage = new Message(senderPart, receiverPart, algorithmUsed.toString().toLowerCase(Locale.ROOT), strKeyFile, timestamp, message, encrypted);
        channel.send(new BusMessage(encrypted, senderPart, receiverPart, algorithmUsed, strKeyFile));

        DBService.instance.insertMessage(dbMessage);
        Config.instance.textArea.info(recipient + " received new Message");
    }


    public void checkAlgorithm(AlgorithmUsed algorithm){
        if (algorithm.equals(AlgorithmUsed.RSA)) {
            strJarFileName = "rsa" + ".jar";
        } else {
            strJarFileName = "shift" + ".jar";
        }

        if (algorithm.equals(AlgorithmUsed.RSA)) {
            strClassName = "RSA";
        } else {
            strClassName = "Shift";
        }
    }
}
