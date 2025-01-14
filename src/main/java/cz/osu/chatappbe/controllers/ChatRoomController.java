package cz.osu.chatappbe.controllers;

import cz.osu.chatappbe.models.AddChatForm;
import cz.osu.chatappbe.models.ChatForm;
import cz.osu.chatappbe.models.entity.ChatRoom;
import cz.osu.chatappbe.models.entity.ChatUser;
import cz.osu.chatappbe.repositories.UserRepository;
import cz.osu.chatappbe.services.models.ChatRoomService;
import cz.osu.chatappbe.services.models.UserNotFoundException;
import cz.osu.chatappbe.services.models.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/api/chatroom")
public class ChatRoomController {

	@Autowired
	private ChatRoomService chatRoomService;
	@Autowired
	private UserService userService;

	@Autowired
	private UserRepository userRepository;

//	@Autowired
/*
	public ChatRoomController(ChatRoomService chatRoomService, UserService userService) {
		this.chatRoomService = chatRoomService;
		this.userService = userService;
	}
*/
	/**
	 * Endpoint to create a new ChatRoom
	 * @param chatForm
	 * @return
	 */
	@PostMapping("/create")
	public ResponseEntity<ChatRoom> createChatRoom(@RequestBody ChatForm chatForm) throws UserNotFoundException {
		return new ResponseEntity<>(this.chatRoomService.prepareRoomForFrontEnd(this.chatRoomService.create(chatForm)), HttpStatus.CREATED);
	}

	/**
	 * Endpoint to get all ChatRooms of a ChatUser
	 * @param username
	 * @return
	 */
	@GetMapping("/list")
	public ResponseEntity<Object> getChatRooms(@RequestBody String username) {
		return new ResponseEntity<>(this.chatRoomService.getUserRooms(username), HttpStatus.OK);
	}

	/**
	 * Endpoint to get a ChatRoom by its ID
	 * @param id
	 * @return
	 */
	@GetMapping("/{id}")
	public ResponseEntity<Object> getChatRoom(@PathVariable Integer id) {
		Optional<ChatRoom> chatRoom = this.chatRoomService.getChatRoomForFrontEnd(id);

		return chatRoom.<ResponseEntity<Object>>map(room -> new ResponseEntity<>(room, HttpStatus.OK))
		               .orElseGet(() -> new ResponseEntity<>("Chatroom " + id + " does not exist.", HttpStatus.BAD_REQUEST));
	}

	/**
	 * Endpoint to get the public ChatRoom
	 * @return
	 */
	@GetMapping("/public")
	public ResponseEntity<Object> getPublicChatRoom() {
		ChatRoom room = this.chatRoomService.getPublicRoomForFrontEnd().get();
		room.setJoinedUsers(new ArrayList<>());
		room.getMessages().forEach(message -> {
			message.getRoom().setMessages(new ArrayList<>());
			message.getUser().setMessages(new ArrayList<>());
			message.getUser().setJoinedChatRooms(new ArrayList<>());
		});

		return new ResponseEntity<>(room, HttpStatus.OK);
	}

	/**
	 * Endpoint to assign a ChatUser to a ChatRoom
	 * @param id of ChatUser
	 * @param addChatForm
	 * @return
	 */
	@PostMapping("/assign/{id}")
	public ResponseEntity<Object> assignUserToChatRoom(@PathVariable Integer id, @RequestBody AddChatForm addChatForm) {

		// Checking whether the ChatRoom exists
		Optional<ChatRoom> chatRoom = this.chatRoomService.get(id);

		System.out.println("chatroom" + chatRoom.toString() );
		if (chatRoom.isEmpty()) {
			return new ResponseEntity<>("Chatroom " + id + " does not exist.", HttpStatus.BAD_REQUEST);
		}

		// Check if ChatUser exists (user being added)
		Optional<ChatUser> chatUser = userService.get(addChatForm.getUserName());

		System.out.println("string username:" + addChatForm.getUserName());
		System.out.println("chatUserFromName"+ chatUser.toString());

		if (chatUser.isEmpty()) {
			return new ResponseEntity<>("User " + addChatForm.getUserName() + " does not exist.", HttpStatus.BAD_REQUEST);
		}
/*
// CHECK

		if (!addChatForm.getId().equals(chatRoom.getOwner())) {
			return new ResponseEntity<>("User " + addChatForm.getUserName() + " doesn't own the room, cannot add user.", HttpStatus.BAD_REQUEST);
		}
*/

		ChatUser chatroomOwner = chatRoomService.getOwner(chatRoom.get());

		if (chatroomOwner == null) {
			return new ResponseEntity<>("Chatroom owner not found. Unable to add users.", HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// Check if the requesting user is the owner
		Optional<ChatUser> requestingUser = userService.get(addChatForm.getReqUserId());
		if (requestingUser.isEmpty() || !chatroomOwner.equals(requestingUser.get())) {
			return new ResponseEntity<>("User needs to be the owner of the room to add other users.", HttpStatus.BAD_REQUEST);
		}


		// Add the ChatUser to the ChatRoom
		System.out.println("before adding rooms, chatuser is " + addChatForm.getUserName());
		this.userService.addRoom(chatUser.get(), chatRoom.get());
		System.out.println("added to room, returning");
		return new ResponseEntity<>(this.chatRoomService.prepareRoomForFrontEnd(chatRoom.get()), HttpStatus.OK);
	}

	/**
	 * Endpoint to remove a ChatUser from a ChatRoom
	 * @param id
	 * @param addChatForm
	 * @return
	 */
	@PostMapping("/delete/{id}")
	public ResponseEntity<Object> removeUserFromChatRoom(@PathVariable Integer id, @RequestBody AddChatForm addChatForm) {

		// Check if ChatRoom exists
		Optional<ChatRoom> chatRoom = this.chatRoomService.get(id);
		System.out.println("chatroom" + chatRoom.toString() );
		if (chatRoom.isEmpty()) {
			return new ResponseEntity<>("Chatroom " + id + " does not exist.", HttpStatus.BAD_REQUEST);
		}

		// Check if ChatUser exists
		Optional<ChatUser> chatUser = userService.get(addChatForm.getUserName());
		System.out.println("string username:" + addChatForm.getUserName());

		System.out.println("chatUserFromName"+ chatUser.toString());
		if (chatUser.isEmpty()) {
			return new ResponseEntity<>("User " + addChatForm.getUserName() + " does not exist.", HttpStatus.BAD_REQUEST);
		}

		// Check if the user is the owner of the chat room.
		//int chatroomOwner = chatRoomService.getOwner(chatRoom.get());
		ChatUser chatroomOwner = chatRoomService.getOwner(chatRoom.get());
		if (!chatroomOwner.equals(chatUser.get())) {
			return new ResponseEntity<>("User needs to be the owner of the room to remove other users.", HttpStatus.BAD_REQUEST);
		}
		//if (chatroomOwner!= addChatForm.getReqUserId()  ) {
		//	return new ResponseEntity<>("User needs to be the owner of the room to remove other users.", HttpStatus.BAD_REQUEST);
	 //	}



		System.out.println("before deleting from room, chatuser is " + addChatForm.getUserName());
		this.userService.removeRoom(chatUser.get(), chatRoom.get());

		System.out.println("deleted from room, returning");
		return new ResponseEntity<>(this.chatRoomService.prepareRoomForFrontEnd(chatRoom.get()), HttpStatus.OK);
	}
}
