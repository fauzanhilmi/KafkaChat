/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package kafkachat;

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
    private static ConsumerConnector cc;
    
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
        ProducerConfig producerConfig = new ProducerConfig(ProducerProperties);
        
        Properties ConsumerProperties = new Properties();
        ConsumerProperties.put("zookeeper.connect","localhost:2181");
        ConsumerProperties.put("group.id",Username);
        ConsumerConfig consumerConfig = new ConsumerConfig(ConsumerProperties);
        cc = Consumer.createJavaConsumerConnector(consumerConfig);
    }

            
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        KafkaChatClient kc = new KafkaChatClient();

        String command = sc.nextLine();
        while (!command.equals("/EXIT")) {
            if (command.length() >= 5 && command.substring(0, 5).equals("/NICK")) {
                String name = "";
                if (command.length() <= 6) { //default username
                    int rndIdx = new Random().nextInt((defaultUsernames.size() - 0));
                    name = defaultUsernames.get(rndIdx);
                } else if (command.charAt(5) == ' ' && command.length() >= 7) {
                    name = command.substring(6);
                    name = name.trim(); //remove trailing whitespace
                }
                String message = name + " has joined";
                kc = new KafkaChatClient(name);
                System.out.println(message);
            } else if (command.length() >= 5 && command.substring(0, 5).equals("/JOIN")) {
                String channelName = "";
                if (command.length() <= 6) { //default username
                    channelName = "channelname";
                } else {
                    channelName = command.substring(command.indexOf(" ")+1);
                }
                ChannelMap.put(channelName,new ChannelListener(user.getName(),channelName));
                String message = user.getName() + " has joined channel " + channelName;
                System.out.println(message);
            }
            command = sc.nextLine();
        }
        
        for(ChannelListener channel : ChannelMap.values()) {
            channel.shutdown();
        }
        
//        Properties props = new Properties();
//        props.put("zk.connect","127.0.0.1:2181");
//        props.put("serializer.class","kafka.serializer.StringEncoder");
//        ProducerConfig config = new ProducerConfig(props);
//        Producer<String, String> producer = new Producer<String, String>(config);
//        
//        ProducerData<String, String> data = new ProducerData<String, String>("test-topic", "test-message");
//        producer.send(data);	
//                

    }
    
}
