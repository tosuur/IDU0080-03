package ee.ttu.idu0080.raamatupood.client;

import java.util.Date;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import ee.ttu.idu0080.raamatupood.server.EmbeddedBroker;
import ee.ttu.idu0080.raamatupood.types.Tellimus;
import ee.ttu.idu0080.raamatupood.types.TellimuseRida;
import ee.ttu.idu0080.raamatupood.types.Toode;

/**
 * JMS sõnumite tootja. Ühendub brokeri url-ile
 * 
 * @author Allar Tammik
 * @date 08.03.2010
 */
public class Producer {
	private static final Logger log = Logger.getLogger(Producer.class);
	public static final String SUBJECT = "tellimuse.edastamine"; // järjekorra nimi
	public static final String ANSWER = "tellimuse.vastus";

	private String user = ActiveMQConnection.DEFAULT_USER;// brokeri jaoks vaja
	private String password = ActiveMQConnection.DEFAULT_PASSWORD;

	long sleepTime = 1000; // 1000ms
	
	private long timeToLive = 1000000;
	private String url = EmbeddedBroker.URL;
	
	private MessageProducer producer;
	private Session session;

	public static void main(String[] args) {
		Producer producerTool = new Producer();
		producerTool.run();
	}

	public void run() {
		Connection connection = null;
		try {
			log.info("Connecting to URL: " + url);
			log.debug("Sleeping between publish " + sleepTime + " ms");
			if (timeToLive != 0) {
				log.debug("Messages time to live " + timeToLive + " ms");
			}
			log.info("Sending message: Tellimus");
			log.info("Connecting to URL: " + url);

			// 1. Loome ühenduse
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					user, password, url);
			connection = connectionFactory.createConnection();
			// Käivitame yhenduse
			connection.start();

			// 2. Loome sessiooni
			/*
			 * createSession võtab 2 argumenti: 1. kas saame kasutada
			 * transaktsioone 2. automaatne kinnitamine
			 */
			session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);

			// Loome teadete sihtkoha (järjekorra). Parameetriks järjekorra nimi
			Destination destination = session.createQueue(SUBJECT);
			Destination answerDestination = session.createQueue(ANSWER);

			// 3. Loome teadete saatja
			MessageConsumer consumer = session.createConsumer(destination);
			producer = session.createProducer(answerDestination);

			consumer.setMessageListener(new MessageListenerImpl());
			
			producer.setTimeToLive(timeToLive);
			
			sendTellimus(session, producer);

		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * Käivitatakse, kui tuleb sõnum
	 */
	class MessageListenerImpl implements javax.jms.MessageListener {

		public void onMessage(Message message) {
			try {
				if (message instanceof TextMessage) {
					TextMessage txtMsg = (TextMessage) message;
					String msg = txtMsg.getText();
					log.info("Received: " + msg);
				} else if (message instanceof ObjectMessage) {
					ObjectMessage objectMessage = (ObjectMessage) message;

					String msg = objectMessage.getObject().toString();
					log.info("Received: " + msg);
					

				} else {
					log.info("Received: " + message);
				}

			} catch (JMSException e) {
				log.warn("Caught: " + e);
				e.printStackTrace();
			}
		}
	}

	/**
	 * Käivitatakse, kui tuleb viga.
	 */
	class ExceptionListenerImpl implements javax.jms.ExceptionListener {

		public synchronized void onException(JMSException ex) {
			log.error("JMS Exception occured. Shutting down client.");
			ex.printStackTrace();
		}
	}
	
	protected void sendTellimus(Session session, MessageProducer producer)
			throws Exception {
		
		ObjectMessage objectMessage = session.createObjectMessage();
		objectMessage.setObject(createTellimus()); // peab olema Serializable
		producer.send(objectMessage);

		TextMessage message = session
				.createTextMessage(createMessageText(0));
		log.debug("Sending message: " + message.getText());
		producer.send(message);
		
	}
	
	private String createMessageText(int index) {
		return "Message: " + index + " sent at: " + (new Date()).toString();
	}
	
	private Tellimus createTellimus() {
		Tellimus tellimus = new Tellimus();
		tellimus.addTellimus(new TellimuseRida(new Toode(1, "Hyppa yles", 15.59), new Long(4)));
		tellimus.addTellimus(new TellimuseRida(new Toode(2, "Kalkun aasal", 9.99), new Long(2)));

		return tellimus;
	}
	

}


