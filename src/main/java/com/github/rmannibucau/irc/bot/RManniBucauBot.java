package com.github.rmannibucau.irc.bot;

import org.jibble.pircbot.PircBot;

import java.io.IOException;
import java.util.Properties;

public class RManniBucauBot extends PircBot {

    public static final String IRC_NAME = "irc.name";
    public static final String IRC_HOST = "irc.host";
    public static final String IRC_PORT = "irc.port";
    public static final String IRC_PASSWORD = "irc.password";
    public static final String IRC_CHANNEL = "irc.channel";

    private static final String HTTP_PROXY_PORT = "http.proxyPort";
    private static final String HTTP_PROXY_HOST = "http.proxyHost";

    private static final String DELIMITER = ",";

    private static final String DEFAULT_NAME = "rmannibucau-bot";
    private static final String DEFAULT_HOST = "irc.freenode.net";
    private static final int DEFAULT_PORT = 6667;
    private static final String DEFAULT_CHANNEL = "#openejb";
    private static final String COMMANDS_PROPERTIES = "/commands.properties";
    private static final String LIST_COMMANDS = "commands";

    private Properties commands;

    public RManniBucauBot(final String name) {
        setName(name);
        setVersion("1.0");
        readCommands();
    }

    private void readCommands() {
        commands = new Properties();
        try {
            commands.load(getClass().getResourceAsStream(COMMANDS_PROPERTIES));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onMessage(String channel, String sender, String login, String hostname, String rawMessage) {
        if (rawMessage == null || !rawMessage.startsWith(getLogin())) {
            return;
        }

        final String message = subMessage(subMessage(rawMessage, ':'), ',').trim();
        if (commands.containsKey(message)) {
            sendMessage(channel, commands.getProperty(message));
        } else if (LIST_COMMANDS.equals(message)) {
            sendMessage(channel, "Commands are " + commands.stringPropertyNames());
        }
    }

    private String subMessage(final String rawMessage, final char c) {
        final int idx = rawMessage.indexOf(c);
        if (idx > 0 && idx < rawMessage.length()) {
            return rawMessage.substring(idx + 1);
        }
        return rawMessage;
    }

    public static void main(final String[] args) throws Exception {
        final String name = System.getProperty(IRC_NAME, DEFAULT_NAME);
        final String host = System.getProperty(IRC_HOST, DEFAULT_HOST);
        final int port = Integer.getInteger(IRC_PORT, DEFAULT_PORT);
        final String password = System.getProperty(IRC_PASSWORD);
        final String[] channels = System.getProperty(IRC_CHANNEL, DEFAULT_CHANNEL).split(DELIMITER);

        final RManniBucauBot bot = new RManniBucauBot(name);
        bot.setVerbose(true);
        bot.setEncoding("UTF-8");
        bot.setLogin(name);
        bot.setProxy(System.getProperty(HTTP_PROXY_HOST), Integer.getInteger(HTTP_PROXY_PORT, 3128));
        bot.connect(host, port, password);
        for (String channel : channels) {
            if (!channel.isEmpty()) {
                bot.joinChannel(channel);
            }
        }

        /*
        bot.disconnect();
        bot.dispose();
        */
    }
}
