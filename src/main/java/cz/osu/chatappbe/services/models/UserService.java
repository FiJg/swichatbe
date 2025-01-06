package cz.osu.chatappbe.services.models;

import cz.osu.chatappbe.models.entity.ChatRoom;
import cz.osu.chatappbe.models.entity.ChatUser;
import cz.osu.chatappbe.models.SignupForm;
import cz.osu.chatappbe.repositories.UserRepository;
import cz.osu.chatappbe.services.utility.HashService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
	@Autowired
	private UserRepository repository;
	@Autowired
	private ChatRoomService chatRoomService;
	@Autowired
	private HashService hashService;

	public ChatUser create(SignupForm signupForm) {
		System.out.println("UserService: creating new user");
		ChatUser user = new ChatUser();

		System.out.println("UserService: invoking chatroom services");

		System.out.println("UserService: optional rooms");
		Optional<ChatRoom> optionalRoom = chatRoomService.getPublicRoom();
		System.out.println("UserService: new room declared");
		ChatRoom room;
		System.out.println("UserService: get optional rooms");
		if (optionalRoom.isPresent()) {
			room = optionalRoom.get();
		} else {
			System.out.println("UserService: room to create");
			room = chatRoomService.create();
			System.out.println("UserService: room to create");
		}
        System.out.println("UserServices: Setting username and password");
		user.setUsername(signupForm.getUsername());
		user.setPassword(this.hashService.hash(signupForm.getPassword()));
		System.out.println("UserService: adding chatroom");
		user.addChatRoom(room);
		System.out.println("UserService: Save");
		return this.repository.save(user);
	}

	public void addRoom(ChatUser user, ChatRoom room) {
		user.addChatRoom(room);

		this.repository.save(user);
	}

	public boolean exists(String username) {
		return this.repository.existsByUsernameIgnoreCase(username);
	}

	public List<ChatUser> list() {
		List<ChatUser> users = this.repository.findAll();

		/*users.forEach(chatUser -> chatUser.setJoinedRooms(null));
		users.forEach(chatUser -> chatUser.setMessages(null));*/

		return users;
	}

	public Optional<ChatUser> login(SignupForm loginForm) {
		Optional<ChatUser> user = this.get(loginForm.getUsername());

		if (user.isEmpty() || !this.hashService.verify(user.get().getPassword(), loginForm.getPassword())) {
			return Optional.empty();
		}

		return user;
	}

	public Optional<ChatUser> get(String username) {
		return repository.findUserByUsernameIgnoreCase(username);
	}

	public Optional<ChatUser> get(Integer id) {
		return repository.findById(id);
	}

	public void removeRoom(ChatUser chatUser, ChatRoom chatRoom) {

		chatUser.removeChatRoom(chatRoom);
		this.repository.save(chatUser);
	}


}
