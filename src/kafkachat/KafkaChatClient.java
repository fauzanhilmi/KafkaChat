/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package kafkachat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
import kafka.consumer.Consumer;
import kafka.consumer.ConsumerConfig;
import kafka.javaapi.consumer.ConsumerConnector;
import kafka.javaapi.producer.Producer;
import kafka.producer.ProducerConfig;
import kafka.javaapi.message.ByteBufferMessageSet;
import kafka.message.Message;
import kafka.producer.KeyedMessage;
import kafka.producer.SyncProducerConfig;
/**
 *
 * @author fauzanhilmi
 */
public class KafkaChatClient {

    /**
     * @param args the command line arguments
     */
//    private final static String QUEUE_NAME = "hello";
//    private final static String NOTIFICATIONS_EX_NAME = "log";
    private static User user;
    private static ProducerConfig pc;
    
    private static Hashtable<String, ChannelListener> source = new Hashtable<String,ChannelListener>(); 
    private static HashMap<String, ChannelListener> ChannelMap = new HashMap(source);
    private static final List<String> defaultUsernames = new ArrayList<>(
            Arrays.asList("Kucing", "Sapi", "Rusa", "Kambing", "Platipus", "Kucing", "Naga", "Panda")
    );
    
    public KafkaChatClient() {}
    
    public KafkaChatClient(String Username) {
        user = new User();
        user.setName(Username);
        Properties ProducerProperties = new Properties();
        ProducerProperties.put("metadata.broker.list","localhost:9092");
        ProducerProperties.put("serializer.class","kafka.serializer.StringEncoder");
        pc = new ProducerConfig(ProducerProperties);
//        
//        Properties ConsumerProperties = new Properties();
//        ConsumerProperties.put("zookeeper.connect","localhost:2181");
//        ConsumerProperties.put("group.id",Username);
//        ConsumerConfig consumerConfig = new ConsumerConfig(ConsumerProperties);
//        cc = Consumer.createJavaConsumerConnector(consumerConfig);
    }

    public void join(String username,String channelName) {
        ChannelMap.put(channelName,new ChannelListener(username,channelName));
        ChannelMap.get(channelName).start();
        String message = username + " has joined channel " + channelName;
        System.out.println(message);
    }
    
    public void leave(String channelName) {
        String message = "";
        if(ChannelMap.containsKey(channelName)) {
            ChannelMap.get(channelName).shutdown();
            ChannelMap.remove(channelName);
            message = user.getName() + " left channel " + channelName;
        }
        else {
            message = "You're not in channel "+channelName;
        }
        System.out.println(message);
    }
    
    public void send(String message, String channelName) {
        if(ChannelMap.containsKey(channelName)) {
            String msg = "[" + channelName + "] " + "(" + user.getName() + ") " + message;
            kafka.javaapi.producer.Producer<String,String> producer = new kafka.javaapi.producer.Producer<String, String>(pc);
            SimpleDateFormat sdf = new SimpleDateFormat();
            KeyedMessage<String, String> km = new KeyedMessage<String, String>(channelName,msg);
            producer.send(km);
            producer.close();
        }
        else {
            System.out.println("You're not in channel "+channelName);
        }
    }
    
    public void broadcast(String username, String message) {
        kafka.javaapi.producer.Producer<String,String> producer = new kafka.javaapi.producer.Producer<String, String>(pc);
        for(String channelName : ChannelMap.keySet()) {
            String msg = "[" + channelName + "] " + "(" + username + ") " + message;
            SimpleDateFormat sdf = new SimpleDateFormat();
            KeyedMessage<String, String> km = new KeyedMessage<String, String>(channelName,msg);
            producer.send(km);            
        }
        producer.close();
    }
            
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        KafkaChatClient kc = new KafkaChatClient();

        System.out.println("Welcome to KafkaChat!");
        System.out.println("Use command '/NICK <Username>' to begin. Empty <username> if you want random nickname");
        System.out.println("Use command '/EXIT' to exit the program");
        String command = sc.nextLine();
        while(command.length() < 5 || !command.substring(0, 5).equals("/NICK")) {
            if(command.equals("/EXIT")) {
                System.out.println("Thank you for using KafkaChat!");
                return;
            }
            else {
                System.out.println("Command is not recognized");
            }
            command = sc.nextLine();
        }
        
        String name = "";
        if (command.length() <= 6) { //default username
            int rndIdx = new Random().nextInt((defaultUsernames.size() - 0));
            name = defaultUsernames.get(rndIdx);
        } else if (command.charAt(5) == ' ' && command.length() >= 7) {
            name = command.substring(6);
            name = name.trim(); //remove trailing whitespace
        }

        kc = new KafkaChatClient(name);
        System.out.println("You are logged in as "+name);
        System.out.println("Avaliable commands: ");
        System.out.println(" 1. /NICK <Username>            : Change username ");
        System.out.println(" 2. /JOIN <Channel Name>        : Join a channel");
        System.out.println(" 3. @<Channel Name> <Message>   : Send message to a channel");
        System.out.println(" 4. /LEAVE <Channel Name>       : Leave channel");
        System.out.println(" 5. <Random text>               : Send message to all channel");
        System.out.println(" 6. /EXIT                       : Exit program");

        command = sc.nextLine();
        while (!command.equals("/EXIT")) {
            if (command.length() >= 5 && command.substring(0, 5).equals("/NICK")) {
                name = "";
                if (command.length() <= 6) { //default username
                    int rndIdx = new Random().nextInt((defaultUsernames.size() - 0));
                    name = defaultUsernames.get(rndIdx);
                } else if (command.charAt(5) == ' ' && command.length() >= 7) {
                    name = command.substring(6);
                    name = name.trim(); //remove trailing whitespace
                }
                kc = new KafkaChatClient(name);
                System.out.println("You are logged in as "+name);
            } else if (command.length() >= 5 && command.substring(0, 5).equals("/JOIN")) {
                String channelName = "";
                if (command.length() <= 6) { //default username
                    channelName = "channelname";
                } else {
                    channelName = command.substring(command.indexOf(" ")+1);
                }
                kc.join(user.getName(),channelName);
            } else if (command.length() >= 6 && command.substring(0, 6).equals("/LEAVE")) {
                if (command.charAt(6) == ' ' && command.length() >= 8) {
                    String channelName = command.substring(command.indexOf(" ")+1);
                    kc.leave(channelName);
                }
            } else if (command.length() >= 4 && command.charAt(0) == ('@')) {
                if (command.contains(" ")) {
                    String channelname = command.substring(1, command.indexOf(' '));
                    String message = command.substring(command.indexOf(' ') + 1, command.length());
                    kc.send(message, channelname);
                } else {
                    System.out.println("You didn't type the message");
                }
            }
            else {
                kc.broadcast(user.getName(),command);
            }
            command = sc.nextLine();
        }
        
        for(ChannelListener channel : ChannelMap.values()) {
            channel.shutdown();
        }
        
        System.out.println("Thank you for using KafkaChat!");
    }
    
}
