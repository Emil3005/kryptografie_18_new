package cryption.parser;

import cryption.Content;
import config.AlgorithmUsed;
import config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    public static void evaluateCommand(String command){
        String[] extracted;
        String[] patterns;
        extracted= null;
        patterns = new String[]{

                "(encrypt message) \"(.+)\" using (rsa|shift) and keyfile ([A-Za-z0-9\\. ]*)",
                "(decrypt message) \"(.+)\" using (rsa|shift) and keyfile ([A-Za-z0-9\\. ]*)",
                "(crack encrypted message) \"(.+)\" using (shift)",
                "(crack encrypted message) \"(.+)\" using (rsa) and keyfile ([A-Za-z0-9\\. ]*)",
                "(register participant) ([A-Za-z0-9_ ]*) with type (normal|intruder)",
                "(create channel) ([A-Za-z0-9_ ]*) from ([A-Za-z0-9_ ]*) to ([A-Za-z0-9_ ]*)",
                "(show channel)",
                "(drop channel) ([A-Za-z0-9_ ]*)",
                "(intrude channel) ([A-Za-z0-9_ ]*) by ([A-Za-z0-9_ ]*)",
                "(send message) \"(.+)\" from ([A-Za-z0-9_ ]*) to ([A-Za-z0-9_ ]*) using ([A-Za-z0-9_ ]*) and keyfile ([A-Za-z0-9_\\. ]*)"

        };
        int i = 0;
        while (i < patterns.length) {
            String pattern = patterns[i];
            extracted = getRegexGroups(pattern, command);
            if (extracted != null) break;
            i++;
        }
        if (extracted != null) processCommand(extracted);
        else {
            Config.instance.textArea.info(String.format("Command \"%s\" could not be processed", command));
            return;
        }

    }

    private static String[] getRegexGroups(String regex, String command) {
        String[] result = null;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(command);
        if (matcher.find()) {
            List<String> groups = new ArrayList<>();
            for (int i = 1; i < matcher.groupCount() + 1; i++) {
                groups.add(matcher.group(i));
            }
            result = groups.toArray(new String[0]);
        }
        return result;
    }

    private static void processCommand(String[] extracted){
        Content utils = new Content();
        switch (extracted[0]) {
            case "encrypt message" -> {
                String encrypted = utils.encrypt(extracted[1], extracted[2].equals("rsa") ? AlgorithmUsed.RSA : AlgorithmUsed.SHIFT, extracted[3]);
                if (encrypted != null) Config.instance.textArea.info(encrypted);
            }
            case "decrypt message" -> {
                String decrypted = utils.decrypt(extracted[1], extracted[2].equals("rsa") ? AlgorithmUsed.RSA : AlgorithmUsed.SHIFT, extracted[3]);
                if (decrypted != null) Config.instance.textArea.info(decrypted);
            }
            case "crack encrypted message" -> {
                String cracked;
                if ("shift".equals(extracted[2])) {
                    cracked = utils.crackEncryptedMessageUsingShift(extracted[1]);
                } else {
                    cracked = utils.crackEncryptedMessageUsingRSA(extracted[1], extracted[3]);
                }
                if (cracked != null) Config.instance.textArea.info(cracked);
            }
            case "register participant" -> utils.registerParticipant(extracted[1], extracted[2]);
            case "create channel" -> utils.createChannel(extracted[1], extracted[2], extracted[3]);
            case "show channel" -> utils.showChannel();
            case "drop channel" -> utils.dropChannel(extracted[1]);
            case "intrude channel" -> utils.intrudeChannel(extracted[1], extracted[2]);
            case "send message" -> utils.sendMessage(extracted[1], extracted[2], extracted[3], extracted[4].equals("rsa") ? AlgorithmUsed.RSA : AlgorithmUsed.SHIFT, extracted[5]);
        }
    }
}
