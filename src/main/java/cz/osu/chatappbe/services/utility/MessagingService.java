package cz.osu.chatappbe.services.utility;

import cz.osu.chatappbe.models.entity.ChatRoom;
import cz.osu.chatappbe.models.entity.ChatUser;
import cz.osu.chatappbe.models.entity.Message;
import cz.osu.chatappbe.models.PayloadMsg;
import cz.osu.chatappbe.services.models.ChatRoomService;
import cz.osu.chatappbe.services.models.MessageService;
import cz.osu.chatappbe.services.models.UserService;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.*;

import static cz.osu.chatappbe.config.RabbitMQConfig.queueName;

@Service
public class MessagingService {
	private static final Logger logger = LoggerFactory.getLogger(MessagingService.class);

	@Autowired
	private AmqpTemplate rabbitTemplate;
	@Autowired
	private AmqpAdmin admin;
	@Autowired
	private MessageService messageService;
	@Autowired
	private UserService userService;
	@Autowired
	private ChatRoomService chatRoomService;

	public PayloadMsg receivePublicMessage(PayloadMsg msg) {
		return this.receiveMessage(msg);
	}

	public PayloadMsg receiveGroupMessage(PayloadMsg msg) {
		return this.receiveMessage(msg);
	}

	private PayloadMsg receiveMessage(PayloadMsg msg) {
		Optional<ChatUser> optionalUser = userService.get(msg.getSenderId());
		Optional<ChatRoom> optionalRoom = chatRoomService.get(msg.getChatId());

		if (optionalUser.isEmpty() || optionalRoom.isEmpty()) {
			return null;
		}

		ChatUser user = optionalUser.get();
		ChatRoom room = optionalRoom.get();

		Message message = messageService.create(user, room, msg.getContent(), msg.getDate());

		room.getJoinedUsers().forEach(u -> {
			this.send("queue-" + u.getUsername(), message);
		});

		return msg;
	}

	public PayloadMsg receivePrivateMessage(PayloadMsg msg) {
		return this.receiveMessage(msg);
	}

	public void send(String queueName, Message message) {
		Map<String, Object> preparedMessage = messageService.prepareForRabbit(message);

		logger.info("Sending prepared message to queue {}: {}", queueName, preparedMessage);

		// This should work now, because the Map contains only serializable data.
		rabbitTemplate.convertAndSend(queueName, preparedMessage);
	}


	/**
	 * Recieves queues
	 * @param queueName
	 * @return
	 */
	public List<Message> receive(String queueName) {
		List<Message> receivedMessages = new ArrayList<>();

		logger.info("Checking messages in queue: {}", queueName);

		while (Objects.requireNonNull(admin.getQueueInfo(queueName)).getMessageCount() != 0) {
			Object rawMessage = rabbitTemplate.receiveAndConvert(queueName);

			// Log the raw message received from RabbitMQ
			logger.debug("Raw message received from RabbitMQ: {}", rawMessage);

			if (rawMessage != null) {
				try {
					// Deserialize and process the message
					Message processedMessage = messageService.receiveFromRabbit(rawMessage);

					logger.debug("Deserialized message: {}", processedMessage);

					receivedMessages.add(processedMessage);
				} catch (Exception e) {
					logger.error("Error processing message from RabbitMQ", e);
				}
			}
		}

		// Log the final list of processed messages
		logger.info("Total messages received from queue {}: {}", queueName, receivedMessages.size());

		return receivedMessages;
	}
}