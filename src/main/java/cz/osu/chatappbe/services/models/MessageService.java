package cz.osu.chatappbe.services.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.osu.chatappbe.models.entity.ChatRoom;
import cz.osu.chatappbe.models.entity.ChatUser;
import cz.osu.chatappbe.models.entity.Message;
import cz.osu.chatappbe.repositories.MessageRepository;
import cz.osu.chatappbe.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;

@Service
public class MessageService {

	// Jackson's ObjectMapper for JSON serialization and deserialization
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private MessageRepository messageRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ChatRoomService chatRoomService;

	public Message create(ChatUser user, ChatRoom chatRoom, String content, String date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(Long.parseLong(date));

		return this.create(user, chatRoom, content, calendar);
	}

	public Message create(ChatUser user, ChatRoom chatRoom, String content) {
		return this.create(user, chatRoom, content, Calendar.getInstance());
	}

	private Message create(ChatUser user, ChatRoom chatRoom, String content, Calendar date) {

		// Create and save message
		Message message = new Message();
		message.setContent(content);
		message.setSendTime(date.getTime());
		message.setRoom(chatRoom);
		message.setUser(user);
		message = messageRepository.save(message);

		// Add the message to the chatUser's and chatRoom's message collections
		user.getMessages().add(message);
		chatRoom.getMessages().add(message);

		// Update the chatUser and chatRoom in the database
		userRepository.save(user);
		chatRoomService.update(chatRoom);

		// Create minimal ChatRoom and ChatUser objects to avoid cyclic dependencies and data overflow
		ChatRoom chatRoom1 = ChatRoom.builder()
				.id(chatRoom.getId())
				.isGroup(chatRoom.getIsGroup())
				.isPublic(chatRoom.getIsPublic())
				.name(chatRoom.getName())
				.joinedUsers(new ArrayList<>())
				.owner(chatRoom.getOwner())
				.build();

		ChatUser chatUser = ChatUser.builder()
				.id(user.getId())
				.username(user.getUsername())
				.messages(new ArrayList<>())
				.joinedChatRooms(new ArrayList<>())
				.build();

		// Return the new message with the minimal ChatRoom and ChatUser objects
		return Message.builder()
				.id(message.getId())
				.content(message.getContent())
				.sendTime(message.getSendTime())
				.user(chatUser)
				.room(chatRoom1)
				.build();
	}

	// Serialize message to JSON using Jackson
	public String prepareForRabbit(Message message) {
		// Remove cyclic references from the message and related objects
		message.getRoom().setMessages(new ArrayList<>());
		message.getRoom().setJoinedUsers(new ArrayList<>());
		message.getUser().setMessages(new ArrayList<>());
		message.getUser().setJoinedChatRooms(new ArrayList<>());

		try {
			return objectMapper.writeValueAsString(message);  // Serialize to JSON using Jackson
		} catch (JsonProcessingException e) {
			// Handle exception, e.g., log it or rethrow as a runtime exception
			throw new RuntimeException("Error serializing message for Rabbit", e);
		}
	}

	// Deserialize JSON to Message object using Jackson
	public Message receiveFromRabbit(Object message) {
		try {
			return objectMapper.readValue((String) message, Message.class);  // Deserialize JSON to Message
		} catch (JsonProcessingException e) {
			// Handle exception, e.g., log it or rethrow as a runtime exception
			throw new RuntimeException("Error deserializing message from Rabbit", e);
		}
	}
}
