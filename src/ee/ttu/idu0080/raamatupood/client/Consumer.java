package ee.ttu.idu0080.raamatupood.client;

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

/**
 * JMS sõnumite tarbija. Ühendub broker-i urlile
 * 
 * @author Allar Tammik
 * @date 08.03.2010
 */
public class Consumer {
	private static final Logger log = Logger.getLogger(Consumer.class);
	private String SUBJECT = "tellimuse.edastamine";
	private String ANSWER = "tellimuse.vastus";
	private String user = ActiveMQConnection.DEFAULT_USER;
	private String password = ActiveMQConnection.DEFAULT_PASSWORD;
	private String url = EmbeddedBroker.URL;

	private MessageProducer producer;
	private Session session;
	
	public static void main(String[] args) {
		Consumer consumerTool = new Consumer();
		consumerTool.run();
	}

	public void run() {
		Connection connection = null;
		try {
			log.info("Connecting to URL: " + url);
			log.info("Consuming queue : " + SUBJECT);

			// 1. Loome ühenduse
			ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(
					user, password, url);
			connection = connectionFactory.createConnection();

			// Kui ühendus kaob, lõpetatakse Consumeri töö veateatega.
			connection.setExceptionListener(new ExceptionListenerImpl());

			// Käivitame ühenduse
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

			// 3. Teadete vastuvõtja
			producer = session.createProducer(destination);
			MessageConsumer consumer = session.createConsumer(answerDestination);

			// Kui teade vastu võetakse käivitatakse onMessage()
			consumer.setMessageListener(new MessageListenerImpl());
			

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

					Tellimus tellimus = (Tellimus) objectMessage.getObject();
					//log.info("Received message: " + tellimus.toString());

					long tooteid = 0;
					double hind = 0;
					for(TellimuseRida rida : tellimus.getTellimusRead()) {
						log.info("Received book: " + rida.getToode().getNimetus());
						log.info("id: " + rida.getToode().getKood());
						log.info("price: " + rida.getToode().getHind());
						log.info("quantity: " + rida.getKogus());
						double tooteidReas = rida.getKogus();
						tooteid += tooteidReas;
						hind = hind + rida.getToode().getHind()*tooteidReas;
					}
					
					sendAnswer("Tootete arv kokku: " + tooteid + ", toodete hind kokku: " + hind);

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
	
	
	
	private void sendAnswer(String message) {
		try {
			TextMessage answer = session.createTextMessage(message);
			log.debug("Sending message: " + answer.getText());
			producer.send(answer);
		} catch (JMSException e) {
			log.warn("Caught: " + e);
			e.printStackTrace();
		}
	}
	
	

}