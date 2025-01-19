package cz.osu.chatappbe.services.models;

import cz.osu.chatappbe.models.ChatForm;
import cz.osu.chatappbe.models.entity.ChatRoom;
import cz.osu.chatappbe.models.entity.ChatUser;
import cz.osu.chatappbe.repositories.ChatRoomRepository;
import cz.osu.chatappbe.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatRoomService {
	@Autowired
	private ChatRoomRepository repository;
	@Autowired
	private UserRepository userRepository;

	public Optional<ChatRoom> findByIdWithUsers(Integer id) {
		return repository.findByIdWithJoinedUsers(id);
	}

	public Optional<ChatRoom> getRoomByName(String name) {
		return repository.findByName(name);
	}

	public ChatRoom create(ChatUser user, ChatUser chatUser) {
		ChatRoom chatRoom = new ChatRoom();

		chatRoom.setName(user.getUsername() + ", " + chatUser.getUsername());
		chatRoom.setIsPublic(false);
		chatRoom.setIsGroup(false);
		chatRoom.getJoinedUsers().add(user);
		chatRoom.getJoinedUsers().add(chatUser);

		//chatRoom.setOwner(user.getId());
		chatRoom.setOwner(user);

		return repository.save(chatRoom);
	}

	/**
	 * Method to create a public room
	 * @return
	 */
	public ChatRoom create() {
		System.out.println("Chatrooms service:new chatroom");
		ChatRoom chatRoom = new ChatRoom();

		System.out.println("Chatrooms service: name public");
		chatRoom.setName("Public");
		chatRoom.setIsPublic(true);
		chatRoom.setIsGroup(false);

		System.out.println("Chatrooms service: save");
		return repository.save(chatRoom);
	}

	@Transactional
	public ChatRoom create(ChatForm chatForm) throws UserNotFoundException {
		System.out.println("Chatrooms service:new chatroom");
		ChatRoom chatRoom = new ChatRoom();

		System.out.println("Chatrooms service: set");
		chatRoom.setName(chatForm.getName());
		chatRoom.setIsGroup(chatForm.getIsGroup());
		chatRoom.setIsPublic(false);

		System.out.println("Chatrooms service: sender");
		// Fetch the sender (owner) of the chat room
		Optional<ChatUser> optionalSender = userRepository.findById(chatForm.getCreatedBy());
		if (optionalSender.isEmpty()) {
			throw new UserNotFoundException("User with ID: " + chatForm.getCreatedBy() + " not found");
		}

		System.out.println("Chatrooms service: sender get");
		ChatUser sender = optionalSender.get();  // Make sure sender is managed

		// Add the sender to the chat room and other users
		System.out.println("Chatrooms service: hashset");
		Set<ChatUser> users = new HashSet<>();
		users.add(sender);
		chatForm.getJoinedUserNames().forEach(username ->
				userRepository.findUserByUsernameIgnoreCase(username).ifPresent(users::add)
		);
		chatRoom.getJoinedUsers().addAll(users);

		// Set the owner
		System.out.println("Chatroomservice owner:");
		chatRoom.setOwner(sender);  // Set the sender as the owner

		// Save the chat room and ensure the owner is persisted properly
		chatRoom = repository.save(chatRoom);

		// Update each user with the new chat room
		ChatRoom tmpChatRoom1 = chatRoom;
		users.forEach(user -> {
			user.getJoinedChatRooms().add(tmpChatRoom1);
			tmpChatRoom1.getJoinedUsers().add(user);
			userRepository.save(user);  // Make sure the user is persisted
		});

		return chatRoom;
	}


	public Optional<ChatRoom> getPublicRoomForFrontEnd() {
		return this.getPublicRoom().map(this::prepareRoomForFrontEnd).or(() -> Optional.of(this.create()));
	}

	public Optional<ChatRoom> getPublicRoom() {
		return repository.findChatRoomByIsPublicIsTrue();
	}

	public List<ChatRoom> getUserRooms(String username) {
		List<ChatRoom> rooms = this.repository.findByJoinedUsers_Username(username);

		for (ChatRoom chatRoom : rooms) {
			this.prepareRoomForFrontEnd(chatRoom);
		}

		return rooms;
	}

	public ChatRoom update(ChatRoom chatRoom) {
		return repository.save(chatRoom);
	}

	//public int getOwner(ChatRoom chatRoom) {
	//	return chatRoom.getOwner();
	//}
	public ChatUser getOwner(ChatRoom chatRoom) {
		return chatRoom.getOwner();
	}

	public Optional<ChatRoom> get(Integer id) {
		return this.repository.findById(id);
	}

	public Optional<ChatRoom> getChatRoomForFrontEnd(Integer id) {
		return prepareRoomForFrontEnd(this.get(id));
	}

	private Optional<ChatRoom> prepareRoomForFrontEnd(Optional<ChatRoom> optionalRoom) {
		return optionalRoom.map(this::prepareRoomForFrontEnd);
	}

	public ChatRoom prepareRoomForFrontEnd(ChatRoom room) {
		room.getJoinedUsers().forEach(user -> {
			user.setJoinedChatRooms(new ArrayList<>());
			user.setMessages(new ArrayList<>());
			user.setPassword(null);
		});
		room.getMessages().forEach(message -> {
			message.setRoom(null);
			message.getUser().setMessages(new ArrayList<>());
			message.getUser().setJoinedChatRooms(new ArrayList<>());
		});
		List<Integer> userIds = room.getJoinedUsers().stream()
				.map(ChatUser::getId) // Assuming ChatUser has a method getId()
				.collect(Collectors.toList());

		List<ChatUser> freshJoined = userRepository.findAllById(userIds);
		System.out.println(freshJoined.stream().toArray().toString());
		room.setJoinedUsers(freshJoined);

		return room;
	}



}
