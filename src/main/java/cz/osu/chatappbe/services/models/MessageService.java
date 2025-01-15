package cz.osu.chatappbe.services.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.osu.chatappbe.controllers.RabbitController;
import cz.osu.chatappbe.models.PayloadMsg;
import cz.osu.chatappbe.models.entity.ChatRoom;
import cz.osu.chatappbe.models.entity.ChatUser;
import cz.osu.chatappbe.models.entity.Message;
import cz.osu.chatappbe.repositories.MessageRepository;
import cz.osu.chatappbe.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class MessageService {

	private static final Logger logger = LoggerFactory.getLogger(MessageService.class);

	// Jackson's ObjectMapper for JSON serialization and deserialization
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private MessageRepository messageRepository;

	public MessageService(MessageRepository messageRepository) {
		this.messageRepository = messageRepository;
	}

	public Message updateMessageWithFile(Integer messageId, String fileUrl, String fileName, String fileType) {
		Optional<Message> optionalMessage = messageRepository.findById(messageId);
		if (optionalMessage.isEmpty()) {
			throw new RuntimeException("Message not found");
		}
		Message message = optionalMessage.get();
		message.setFileUrl(fileUrl);
		message.setFileName(fileName);
		message.setFileType(fileType);
		return messageRepository.save(message);
	}

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ChatRoomService chatRoomService;


	/**
	 *  For messages that have file content
	 * @param user
	 * @param chatRoom
	 * @param content
	 * @param date
	 * @return
	 */
	public Message create(ChatUser user, ChatRoom chatRoom, PayloadMsg.MessageContent content, String date) {
		//Calendar calendar = Calendar.getInstance();
	//	calendar.setTimeInMillis(Long.parseLong(date));
		Date parsedDate = parseDateFromString(date);
		System.out.println("create method 1");
		return this.create(user, chatRoom, content, parsedDate);
	}

	/**
	 * For text-only messages
	 * @param user
	 * @param chatRoom
	 * @param content
	 * @return
	 */
	public Message create(ChatUser user, ChatRoom chatRoom, String content) {
		System.out.println("create method 2");
		// If no file info is available, pass a "blank" MessageContent or handle differently
		PayloadMsg.MessageContent msgContent = new PayloadMsg.MessageContent();
		msgContent.setContent(content);
		// Or just build a text-only version
		return this.create(user, chatRoom, msgContent, new Date());
	}

	/**
	 *    The private method that does the actual creation & saving.
	 *       It takes the full MessageContent so we can set fileUrl, fileName, fileType.
	 * @param user
	 * @param chatRoom
	 * @param content
	 * @param date
	 * @return
	 */


	private Message create(ChatUser user, ChatRoom chatRoom, PayloadMsg.MessageContent content, Date date) {
		System.out.println("create method 3");

		// Create and save message
		Message message = new Message();

		message.setSendTime(date);
		message.setRoom(chatRoom);
		message.setUser(user);

		message.setAddedToQueueTimestamp(Instant.now());

		// Set text content
		message.setContent(content.getContent());

		// Set file fields
		message.setFileUrl(content.getFileUrl());
		message.setFileName(content.getFileName());
		message.setFileType(content.getFileType());

		//Save to DB
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
		logger.debug("Creating message for user: {} in chatRoom: {}", user.getUsername(), chatRoom.getName());

		// Return the new message with the minimal ChatRoom and ChatUser objects
		// Optionally return a "trimmed" Message to avoid circular references
		Message returnedMessage = Message.builder()
				.id(message.getId())
				.content(message.getContent())
				.sendTime(message.getSendTime())
				.addedToQueueTimestamp(message.getAddedToQueueTimestamp())
				.retrievedFromQueueTimestamp(message.getRetrievedFromQueueTimestamp())
				.fileUrl(message.getFileUrl())
				.fileName(message.getFileName())
				.fileType(message.getFileType())
				.room(ChatRoom.builder()
						.id(chatRoom.getId())
						.isGroup(chatRoom.getIsGroup())
						.isPublic(chatRoom.getIsPublic())
						.name(chatRoom.getName())
						.joinedUsers(new ArrayList<>()) // empty
						.owner(chatRoom.getOwner())
						.build()
				)
				.user(ChatUser.builder()
						.id(user.getId())
						.username(user.getUsername())
						.messages(new ArrayList<>()) // empty
						.joinedChatRooms(new ArrayList<>()) // empty
						.build()
				)
				.username(user.getUsername())
				.build();
		return returnedMessage;
	}

	// Serialize message to JSON using Jackson
	public String prepareForRabbit2(Message message) {
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
	public String prepareForRabbit(Message message) {
		// Remove cyclic references
		message.getRoom().setMessages(new ArrayList<>());
		message.getRoom().setJoinedUsers(new ArrayList<>());
		message.getUser().setMessages(new ArrayList<>());
		message.getUser().setJoinedChatRooms(new ArrayList<>());

		try {
			return objectMapper.writeValueAsString(message);

		} catch (JsonProcessingException e) {
			throw new RuntimeException("Error serializing message for Rabbit", e);
		}
	}

	// Alternative: Prepare a message for RabbitMQ as a plain map
	public Map<String, Object> prepareForRabbitAsMap(Message message) {
		Map<String, Object> msgMap = new HashMap<>();
		msgMap.put("id", message.getId());
		msgMap.put("content", message.getContent());
		msgMap.put("sendTime", message.getSendTime().getTime());

		if (message.getUser() != null) {
			Map<String, Object> userMap = new HashMap<>();
			userMap.put("id", message.getUser().getId());
			System.out.println("!!!!! PrepareforRabbitAsaMap username "+ message.getUser().getUsername());
			userMap.put("username", message.getUser().getUsername());
			msgMap.put("user", userMap);
		} else {
			msgMap.put("user", null);
		}

		if (message.getRoom() != null) {
			msgMap.put("roomId", message.getRoom().getId());
		} else {
			msgMap.put("roomId", null);
		}

		msgMap.put("fileUrl", message.getFileUrl() != null ? message.getFileUrl() : "No File");
		msgMap.put("fileName", message.getFileName() != null ? message.getFileName() : "No Name");
		msgMap.put("fileType", message.getFileType() != null ? message.getFileType() : "No Type");
		msgMap.put("username", message.getUsername() != null ? message.getUsername() : "No Username");

		return msgMap;
	}



	// Deserialize JSON to Message object using Jackson
// Deserialize a message received from RabbitMQ
	public Message receiveFromRabbitOld(Object rawMessage) {
		try {
			if (rawMessage instanceof String) {
				return objectMapper.readValue((String) rawMessage, Message.class);
			} else if (rawMessage instanceof Map) {

				// Convert Map to Message manually
				Map<String, Object> messageData = (Map<String, Object>) rawMessage;
				Message deserializedMessage = new Message();
				deserializedMessage.setId((Integer) messageData.get("id"));
				deserializedMessage.setContent((String) messageData.get("content"));

				// Resolve user
				Map<String, Object> userMap = (Map<String, Object>) messageData.get("user");
				if (userMap != null) {
					ChatUser user = new ChatUser();
					user.setId((Integer) userMap.get("id"));
					user.setUsername((String) userMap.get("username"));
					deserializedMessage.setUser(user);
				}

				// Resolve chatroom
				Integer roomId = (Integer) messageData.get("roomId");
				if (roomId != null) {
					ChatRoom room = new ChatRoom();
					room.setId(roomId);
					deserializedMessage.setRoom(room);
				}

				//  Re-set the sendTime
				Long sendTime = (Long) messageData.get("sendTime");
				if (sendTime != null) {
					deserializedMessage.setSendTime(new Date(sendTime));
				}
				if (deserializedMessage.getAddedToQueueTimestamp() == null) {
					deserializedMessage.setAddedToQueueTimestamp(Instant.now());
				}
				deserializedMessage.setRetrievedFromQueueTimestamp(Instant.now());

				// Re-set the file fields
				String fileUrl = (String) messageData.get("fileUrl");
				if (fileUrl != null && !fileUrl.equals("No File")) {
					deserializedMessage.setFileUrl(fileUrl);
				}

				String fileName = (String) messageData.get("fileName");
				if (fileName != null && !fileName.equals("No Name")) {
					deserializedMessage.setFileName(fileName);
				}

				String fileType = (String) messageData.get("fileType");
				if (fileType != null && !fileType.equals("No Type")) {
					deserializedMessage.setFileType(fileType);
				}


				return deserializedMessage;
			} else {
				throw new IllegalArgumentException("Unsupported message format: " + rawMessage.getClass());
			}
		} catch (Exception e) {
			throw new RuntimeException("Error deserializing message from Rabbit", e);
		}
	}
	private Date parseDateFromString(String date) {
		try {
			long timestamp = Long.parseLong(date);
			return new Date(timestamp);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid date format: " + date, e);
		}
	}

	public Message receiveFromRabbit(Object message) {
		try {
			// Cast the received message to a Map
			@SuppressWarnings("unchecked")
			Map<String, Object> msgMap = (Map<String, Object>) message;

			// Extract fields from the map
			Message deserializedMessage = new Message();
			deserializedMessage.setId((Integer) msgMap.get("id"));
			deserializedMessage.setContent((String) msgMap.get("content"));

			// Set the user
			Map<String, Object> userMap = (Map<String, Object>) msgMap.get("user");
			if (userMap != null) {
				ChatUser user = new ChatUser();
				user.setId((Integer) userMap.get("id"));
				user.setUsername((String) userMap.get("username"));
				deserializedMessage.setUser(user);
			}

			// Set the room ID
			Integer roomId = (Integer) msgMap.get("roomId");
			if (roomId != null) {
				ChatRoom room = new ChatRoom();
				room.setId(roomId);
				deserializedMessage.setRoom(room);
			}

			// Set the send time
			Long sendTime = (Long) msgMap.get("sendTime");
			if (sendTime != null) {
				deserializedMessage.setSendTime(new Date(sendTime));
			}

			if (deserializedMessage.getAddedToQueueTimestamp() == null) {
				deserializedMessage.setAddedToQueueTimestamp(Instant.now());
			}

			logger.debug("AddedToQueueTimestamp: {}", deserializedMessage.getAddedToQueueTimestamp());

			// ALSO do:
			String fileUrl = (String) msgMap.get("fileUrl");
			if (fileUrl != null && !fileUrl.equals("No File")) {
				deserializedMessage.setFileUrl(fileUrl);
			}

			String fileName = (String) msgMap.get("fileName");
			if (fileName != null && !fileName.equals("No Name")) {
				deserializedMessage.setFileName(fileName);
			}

			String fileType = (String) msgMap.get("fileType");
			if (fileType != null && !fileType.equals("No Type")) {
				deserializedMessage.setFileType(fileType);
			}



			// Set the retrievedFromQueueTimestamp
			deserializedMessage.setRetrievedFromQueueTimestamp(Instant.now());

			return deserializedMessage;
		} catch (Exception e) {
			throw new RuntimeException("Error deserializing message from Rabbit", e);
		}




	}

	public Message save(Message message) {
		return messageRepository.save(message);
	}
}
