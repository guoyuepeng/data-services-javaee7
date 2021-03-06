package de.qaware.oss.cloud.source.csv;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.jms.*;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonWriter;
import java.io.StringWriter;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
public class VehicleLocationTopic {
    private static final Logger LOGGER = Logger.getLogger(VehicleLocationTopic.class.getName());

    @Resource(lookup = "jms/activeMqConnectionFactory")
    private ConnectionFactory connectionFactory;

    @Resource(lookup = "jms/VehicleLocation")
    private Topic topic;

    public void publish(JsonObject vehicleLocation) {
        try (Connection connection = connectionFactory.createConnection()) {
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(topic);
            producer.setTimeToLive(TimeUnit.SECONDS.toMillis(30));

            StringWriter payload = new StringWriter();
            JsonWriter jsonWriter = Json.createWriter(payload);
            jsonWriter.writeObject(vehicleLocation);

            TextMessage textMessage = session.createTextMessage(payload.toString());
            textMessage.setJMSType("VehicleLocation");
            textMessage.setStringProperty("contentType", "application/vnd.location.v1+json");

            producer.send(textMessage);
        } catch (JMSException e) {
            LOGGER.log(Level.WARNING, "Could not send JMS message.", e);
        }
    }
}
