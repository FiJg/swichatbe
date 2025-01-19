package cz.osu.chatappbe.services.utility;

import cz.osu.chatappbe.exceptions.RoomNotFoundException;
import cz.osu.chatappbe.exceptions.UnauthorizedException;
import cz.osu.chatappbe.models.entity.ChatRoom;
import cz.osu.chatappbe.models.entity.ChatUser;
import cz.osu.chatappbe.models.entity.Message;
import cz.osu.chatappbe.models.PayloadMsg;
import cz.osu.chatappbe.services.models.ChatRoomService;
import cz.osu.chatappbe.services.models.MessageService;
import cz.osu.chatappbe.services.models.UserService;
import org.apache.catalina.User;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.time.ZoneId;
import java.time.ZonedDateTime;


import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

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
	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private AuthService authService;


	public PayloadMsg receivePublicMessage(PayloadMsg msg) throws RoomNotFoundException, UnauthorizedException {
		return this.receiveMessage(msg);
	}

	public PayloadMsg receiveGroupMessage(PayloadMsg msg) throws RoomNotFoundException, UnauthorizedException {
		return this.receiveMessage(msg);
	}

	private PayloadMsg receiveMessage(PayloadMsg msg) throws RoomNotFoundException, UnauthorizedException {


		Optional<ChatUser> optionalUser = userService.get(msg.getSenderId());
		if (optionalUser.isEmpty() || !isUserAuthenticated(optionalUser.get())) {
			logger.warn("Unauthorized message attempt from user ID: {}", msg.getSenderId());
			throw new UnauthorizedException("User is not authenticated");
		}

		Optional<ChatRoom> optionalRoom = chatRoomService.get(msg.getChatId());
		if (optionalRoom.isEmpty()) {
			logger.warn("Chat room not found for chat ID: {}", msg.getChatId());
			throw new RoomNotFoundException("Chat room not found");
		}

		ChatUser user = optionalUser.get();
		ChatRoom room = optionalRoom.get();

		Message message = messageService.create(user, room, msg.getContent(), msg.getDate());

		room.getJoinedUsers().stream().distinct().forEach(u -> {
			String userQueueName = "queue-" + u.getUsername();
			send(userQueueName, message, "chat"); // Specify messageType as "chat"
		});

		return msg;
	}

	public PayloadMsg receivePrivateMessage(PayloadMsg msg) throws RoomNotFoundException, UnauthorizedException {
		return this.receiveMessage(msg);
	}


	public void send(String queueName, Message message, String messageType) {
		message.setAddedToQueueTimestamp(Instant.now());
		//messageService.save(message);

		Map<String, Object> preparedMessage = messageService.prepareForRabbitAsMap(message);
		preparedMessage.put("messageType", messageType);

		logger.info("Sending prepared message to queue {}: {}", queueName, preparedMessage);

		try {
			rabbitTemplate.convertAndSend(queueName, preparedMessage);
			if ("chat".equals(messageType)) {
				notifyChatRoomMembers(message);
			}
		} catch (Exception e) {
			logger.error("Error sending message to queue {}: {}", queueName, e.getMessage(), e);
		}
	}

	private void notifyChatRoomMembers(Message message) {
		// 1) Force re-fetch the real ChatRoom from DB, with joinedUsers
		ChatRoom reloadedRoom = chatRoomService.findByIdWithUsers(message.getRoom().getId())
				.orElseThrow(() -> new RuntimeException("ChatRoom not found"));

		logger.info("81a. Notifying chatroom {} with message {}",
				reloadedRoom.getName(), message.getContent());
		logger.info("81b. ChatRoom: {} (ID: {})",
				reloadedRoom.getName(), reloadedRoom.getId());

		List<ChatUser> joinedUsers = reloadedRoom.getJoinedUsers();
		int userCount = joinedUsers.size();
		logger.info("81f. Number of joined users to notify: {}", userCount);

		if (userCount > 0) {
			String usernames = joinedUsers.stream()
					.map(ChatUser::getUsername)
					.collect(Collectors.joining(", "));
			logger.info("81g. Users to be notified: {}", usernames);
		} else {
			logger.warn("81g. No users are currently joined in the chatroom '{}'.",
					reloadedRoom.getName());
		}

		// 2) Now loop over the re-fetched joinedUsers
		reloadedRoom.getJoinedUsers().forEach(user -> {
			String destination = "/user/" + user.getUsername() + "/notifications";
			logger.info("81d. Notifying user: {}", user.getUsername());
			logger.info("81e. Sending notification to destination: {}", destination);

			messagingTemplate.convertAndSend(destination,
					Map.of(
							"chatRoomId", reloadedRoom.getId(),
							"chatRoomName", reloadedRoom.getName(),
							"newMessage", message.getContent()
					)
			);
		});
	}


	/**
	 *
	 * @param queueName
	 * @return
	 */
	public List<Message> receive(String queueName) {
		List<Message> receivedMessages = new ArrayList<>();

		logger.info("Checking messages in queue: {}", queueName);

		while (Objects.requireNonNull(admin.getQueueInfo(queueName)).getMessageCount() > 0) {
			Object rawMessage = rabbitTemplate.receiveAndConvert(queueName);

			logger.debug("Raw message received from RabbitMQ: {}", rawMessage);

			if (rawMessage != null) {
				try {
					Message processedMessage = messageService.receiveFromRabbit(rawMessage);
					// Set the timestamp when the message is retrieved from the queue
					processedMessage.setRetrievedFromQueueTimestamp(Instant.now());
					messageService.save(processedMessage);

					logger.debug("Deserialized message: {}", processedMessage);
					receivedMessages.add(processedMessage);
				} catch (Exception e) {
					logger.error("Error processing message from RabbitMQ: {}", e.getMessage(), e);
				}
			}
		}

		logger.info("Total messages received from queue {}: {}", queueName, receivedMessages.size());
		return receivedMessages;
	}


	private boolean isUserAuthenticated(ChatUser user) {
		// Check if the user is null
		if (user == null || user.getUsername() == null) {
			return false;
		}

		// Validate using the AuthService
		return authService.isTokenValid(user.getUsername());
	}

}