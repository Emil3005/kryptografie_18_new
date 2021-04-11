/*
 * Copyright (c) 2021.
 * Author 6143217
 * All rights reserved
 */

package content;

import cryption.loader.PortLoader;
import cryption.verify.VerifyJar;
import cryption.commandPattern.RSACommand;
import cryption.commandPattern.ShiftCommand;
import config.AlgorithmEnum;
import config.Config;
import db.dblogic.enums.DBService;
import db.models.message.MessageEventBus;
import db.models.channel.Channel;
import db.models.message.Message;
import db.models.user.User;

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

    public String encrypt(String message, AlgorithmEnum algorithm, String strKeyFile) {
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

    public String decrypt(String message, AlgorithmEnum algorithmEnum, String strKeyFile) {
        String result = null;
        keyFile = new File(Config.instance.keyFiles + strKeyFile);
        if (!keyFile.exists()) {
            Config.instance.textArea.info("KeyFile " + strKeyFile + " does not exist");
        } else {
            Config.instance.loggingHandler.createLogfile(algorithmEnum.toString(), "decrypt");
            checkAlgorithm(algorithmEnum);
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
        if (DBService.instance.userExists(name)){
            Config.instance.textArea.info(String.format("participant " + name + " already exists, using existing postbox_"+ name));
        }

        User user = new User(name, type);
        Config.instance.textArea.info(String.format("participant " + name + " with Type " + type + " registered and postbox_"+ name + " created"));
        DBService.instance.insertUser(user);
    }

    public  void createChannel(String channelName, String part1name, String part2name){
        if (DBService.instance.channelExists(channelName))
            Config.instance.textArea.info(String.format("channel "+ channelName +" already exists"));

        if (DBService.instance.getChannel(part1name, part2name) != null)
            Config.instance.textArea.info(String.format("communication channel between "+part1name+" and "+part2name+" already exists"));

        if (part1name.equals(part2name))
            Config.instance.textArea.info(String.format(part1name + " and "+part2name+" are identical - cannot create channel on itself"));

        User instanceFirstUser = DBService.instance.getUser(part1name);
        User instanceSecondUser = DBService.instance.getUser(part2name);

        Channel channel = new Channel(channelName, instanceFirstUser, instanceSecondUser);
        Config.instance.textArea.info(String.format("channel " + channelName + " from "+ part1name + " to "+part2name+" successfully created"));

        DBService.instance.insertChannel(channel);
    }
    public void showChannel(){
        List<Channel> channelList = DBService.instance.getChannels();

        //StringBuilder returnString = new StringBuilder();

        channelList.stream().map(channel -> String.format(channel.getName() + " | " + channel.getUserA().getName() + " and " + channel.getUserB().getName())).forEach(Config.instance.textArea::info);
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

        if (!DBService.instance.deleteChannel(channelName)) {
            Config.instance.textArea.info("Something went wrong");
        }
        else{
            Config.instance.textArea.info("channel "+channelName+" deleted");
        }
    }



    public void intrudeChannel(String channelName, String participant){

        User intruder = DBService.instance.getUser(participant);

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



    public void sendMessage(String message, String sender, String recipient, AlgorithmEnum algorithmEnum, String strKeyFile){
        User senderPart = DBService.instance.getUser(sender);
        User receiverPart = DBService.instance.getUser(recipient);
        Channel channel = DBService.instance.getChannel(sender, recipient);
        Date currentDate = new Date();
        long timestampLong = currentDate.getTime()/1000;
        String timestamp = String.valueOf(timestampLong);

        if (channel == null){
            Config.instance.textArea.info(String.format("no valid channel from "+sender+" to "+recipient));
        }

        String encrypted = encrypt(message, algorithmEnum, strKeyFile);

        Message dbMessage = new Message(senderPart, receiverPart, algorithmEnum.toString().toLowerCase(Locale.ROOT), strKeyFile, timestamp, message, encrypted);
        channel.send(new MessageEventBus(encrypted, senderPart, receiverPart, algorithmEnum, strKeyFile));

        DBService.instance.insertMessage(dbMessage);
        Config.instance.textArea.info(recipient + " received new Message");
    }


    public void checkAlgorithm(AlgorithmEnum algorithm){
        if (algorithm.equals(AlgorithmEnum.RSA)) {
            strJarFileName = "rsa" + ".jar";
        } else {
            strJarFileName = "shift" + ".jar";
        }

        if (algorithm.equals(AlgorithmEnum.RSA)) {
            strClassName = "RSA";
        } else {
            strClassName = "Shift";
        }
    }
}
