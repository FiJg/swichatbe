package cz.osu.chatappbe.controllers;

import cz.osu.chatappbe.models.entity.ChatRoom;
import cz.osu.chatappbe.models.entity.ChatUser;
import cz.osu.chatappbe.services.models.ChatRoomService;
import cz.osu.chatappbe.services.models.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class DbController {
	//@Autowired
	private ChatRoomService chatRoomService;
	//@Autowired
	private UserService userService;


	public DbController(ChatRoomService chatRoomService, UserService userService) {
		this.chatRoomService = chatRoomService;
		this.userService = userService;
	}

	/**
	 * Get list of all users
	 * @return
	 */
	@GetMapping("/users")
	public ResponseEntity<List<ChatUser>> getUsers() {

		return new ResponseEntity<>(userService.list(), HttpStatus.OK);
	}

	/**
	 * Get all chatrooms of a user
	 * @param username
	 * @return
	 */
	@GetMapping("/chatrooms")
	public ResponseEntity<List<ChatRoom>> getChatRooms(@RequestParam String username) {
		return new ResponseEntity<>(chatRoomService.getUserRooms(username), HttpStatus.OK);
	}

}
