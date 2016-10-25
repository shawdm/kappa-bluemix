package com.ibm.hursley.kappa.bluemix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.messagehub.samples.env.MessageHubCredentials;
import com.messagehub.samples.env.MessageHubEnvironment;

public class Bluemix {
	
	private static final Logger logger = Logger.getLogger(Bluemix.class);
    //private static final String userDir = System.getProperty("user.dir");
    

    //private static final String resourceDir = userDir + File.separator + "apps" + File.separator + "kappa-bluemix.war" + File.separator + "resources";
    // local
    //private static final String resourceDir = userDir + File.separator + "apps" + File.separator + "kappa-bluemix.war" + File.separator + "resources";
    
    private static String bootstrapServers = null;
    private static boolean initialised = false;
    
    private static Properties producerProperties = null;
    private static Properties consumerProperties = null;
    
    
    private static void init(){
    	if(Bluemix.initialised){
    		return;
    	}
    	
    	// Retrieve kafka-Host, rest-Host and API key from Message Hub
        // VCAP_SERVICES.
        // Set JAAS configuration property.
        if (System.getProperty("java.security.auth.login.config") == null) {
            System.setProperty("java.security.auth.login.config", "");
        }
        
    	// Arguments parsed via VCAP_SERVICES environment variable.
        // Retrieve VCAP json through Bluemix system environment variable
        // "VCAP_SERVICES"
        String vcapServices = System.getenv("VCAP_SERVICES");
        ObjectMapper mapper = new ObjectMapper();

        logger.log(Level.WARN, "VCAP_SERVICES: \n" + vcapServices);

        if (vcapServices != null) {
            try {
                // Parse VCAP_SERVICES into Jackson JsonNode, then map the
                // 'messagehub' entry
                // to an instance of MessageHubEnvironment.
                JsonNode vcapServicesJson = mapper.readValue(vcapServices, JsonNode.class);
                ObjectMapper envMapper = new ObjectMapper();
                String vcapKey = null;
                Iterator<String> it = vcapServicesJson.fieldNames();

                // Find the Message Hub service bound to this application.
                while (it.hasNext() && vcapKey == null) {
                    String potentialKey = it.next();

                    if (potentialKey.startsWith("messagehub")) {
                        logger.log(Level.INFO, "Using the '" + potentialKey + "' key from VCAP_SERVICES.");
                        vcapKey = potentialKey;
                    }
                }

                if (vcapKey == null) {
                    logger.log(Level.ERROR,"Error while parsing VCAP_SERVICES: A Message Hub service instance is not bound to this application.");
                    return;
                }

                MessageHubEnvironment messageHubEnvironment = envMapper.readValue(vcapServicesJson.get(vcapKey).get(0).toString(),MessageHubEnvironment.class);
                MessageHubCredentials credentials = messageHubEnvironment.getCredentials();

                replaceUsernameAndPassword(credentials.getUser(), credentials.getPassword());

                System.out.println("A");
                
                if(credentials.getKafkaBrokersSasl() != null && credentials.getKafkaBrokersSasl().length > 0){
                	System.out.println("B");
                	bootstrapServers = "";
            		for (int i=0; i<credentials.getKafkaBrokersSasl().length; i++){
            			if(i<1){
            				bootstrapServers = credentials.getKafkaBrokersSasl()[i];
            			}
            			else{
            				bootstrapServers = bootstrapServers + "," + credentials.getKafkaBrokersSasl()[i];
            			}
            		}
                }
                
                System.out.println("C:" + bootstrapServers);
                
                
                
            } catch (final Exception e) {
                e.printStackTrace();
                return;
            }
        } else {
            logger.log(Level.ERROR, "VCAP_SERVICES environment variable is null.");
            return;
        }
        
        Bluemix.initialised = true;
    }
    
    
    /**
     * Adding in credentials for MessageHub auth
     */
    private static void replaceUsernameAndPassword(String username, String password) {
        try {
            File xmlFile = new File(System.getProperty("server.config.dir") + File.separator + "server.xml");
            
            System.out.println("editing server.xml:" + xmlFile.toString());
            
            BufferedReader br = new BufferedReader(new FileReader(xmlFile));
            String newline = System.getProperty("line.separator");
            StringBuffer sb = new StringBuffer("");
            String line = null;

            // read in a line at at time
            while ((line = br.readLine()) != null) {
                if (line.indexOf("#USERNAME") != -1) {
                    logger.log(Level.WARN, "Replacing placeholder username and password");
                    line = line.replaceAll("#USERNAME", username);
                    line = line.replaceAll("#PASSWORD", password);
                }
                sb.append(line).append(newline); // append line to new variable
            }
            br.close();

            // write out file again
            logger.log(Level.WARN, "Writing server.xml back to disk");
            BufferedWriter bw = new BufferedWriter(new FileWriter(xmlFile));
            bw.write(sb.toString());
            bw.close();
        } catch (IOException e) {
            logger.log(Level.ERROR, "Couldnt edit server.xml: " + e.getMessage());
        }
    }
    
    
	/**
     * Retrieve client configuration information, using a properties file, for connecting to secure Kafka.
     * 
     * @param broker
     *            {String} A string representing a list of brokers the producer can contact.
     * @param apiKey
     *            {String} The API key of the Bluemix Message Hub service.
     * @param isProducer
     *            {Boolean} Flag used to determine whether or not the configuration is for a producer.
     * @return {Properties} A properties object which stores the client configuration info.
     */
    private static Properties getClientConfiguration(boolean isProducer) {
    	init();
    	
        Properties props = new Properties();
        InputStream propsStream;
        //String fileName = resourceDir + File.separator;

        if (isProducer) {
            //fileName += "producer.properties";
            propsStream = Bluemix.class.getResourceAsStream("/resources/producer.properties");
        } 
        else {
           // fileName += "consumer.properties";
            propsStream = Bluemix.class.getResourceAsStream("/resources/consumer.properties");
        }

        try {
            logger.log(Level.WARN, "Reading properties file from: " + propsStream.toString());
            //propsStream = new FileInputStream(fileName);
            props.load(propsStream);
            propsStream.close();
        } 
        catch (IOException e) {
            logger.log(Level.ERROR, e);
            return props;
        }

        props.put("bootstrap.servers", bootstrapServers);

        if (!props.containsKey("ssl.truststore.location") || props.getProperty("ssl.truststore.location").length() == 0) {
            props.put("ssl.truststore.location", "/home/vcap/app/.java/jre/lib/security/cacerts");
        }

        logger.log(Level.WARN, "Using properties: " + props);

        return props;
    }
    
    
    public static Properties getProducerConfiguration(){
    	if(producerProperties == null){
    		producerProperties = getClientConfiguration(true);
    	}
    	return producerProperties;
    }
    
    
    public static Properties getConsumerConfiguration(){
    	if(consumerProperties == null){
    		consumerProperties = getClientConfiguration(false);
    	}
    	return consumerProperties;
    }
    
	
}