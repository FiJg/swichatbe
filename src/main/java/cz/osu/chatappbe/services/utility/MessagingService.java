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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class MessagingService {
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

	public void send(String exchange, Message message) {
		rabbitTemplate.convertAndSend(exchange, messageService.prepareForRabbit(message));
	}

	public List<Message> receive(String queueName) {
		List<Message> receivedMessages = new ArrayList<>();

		while (Objects.requireNonNull(admin.getQueueInfo(queueName)).getMessageCount() != 0) {
			receivedMessages.add(messageService.receiveFromRabbit(rabbitTemplate.receiveAndConvert(queueName)));
		}

		return receivedMessages;
	}
}
