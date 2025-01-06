package cz.osu.chatappbe.services.utility;

import cz.osu.chatappbe.models.ChatForm;
import cz.osu.chatappbe.models.entity.ChatUser;
import cz.osu.chatappbe.models.SignupForm;
import cz.osu.chatappbe.services.models.ChatRoomService;
import cz.osu.chatappbe.services.models.UserNotFoundException;
import cz.osu.chatappbe.services.models.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class RegistrationService {
	@Autowired
	private final UserService userService;
	@Autowired
	private final ChatRoomService chatRoomService;
	@Autowired
	private final RabbitMQService rabbitMQService;

	public ResponseEntity<String> register(SignupForm signupForm) {
		if (userService.exists(signupForm.getUsername())) {
			return new ResponseEntity<>("Username already exists!", HttpStatus.BAD_REQUEST);
		} else {
			try {
				System.out.println("new user");
				ChatUser user = userService.create(signupForm);

				System.out.println("new user done");

				System.out.println("Creating queues");
				rabbitMQService.createQueue("queue-" + user.getUsername());
				System.out.println("Creating queues done");

				/*for (ChatUser chatUser : userService.list()) {
					if (chatUser.getId().equals(user.getId())) {
						continue;
					}

					ChatRoom chatRoom = chatRoomService.create(user, chatUser);

					userService.addRoom(user, chatRoom);
					userService.addRoom(chatUser, chatRoom);
				}*/

				createExtraRooms(user);

				System.out.println("Creating new extra rooms done ");

				return new ResponseEntity<>("Successfully registered!", HttpStatus.OK);

			} catch (Exception | UserNotFoundException e) {
				e.printStackTrace();
				return new ResponseEntity<>(e.getMessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}


	private void createExtraRooms(ChatUser user) throws UserNotFoundException {

		System.out.println("Creating new extra rooms");
		List<String> onlyThisUser = new ArrayList<>();
		onlyThisUser.add(user.getUsername());
		chatRoomService.create(new ChatForm(user.getUsername()+"1",true,onlyThisUser, user.getId()));
		chatRoomService.create(new ChatForm(user.getUsername()+"2",true,onlyThisUser, user.getId()));
		System.out.println("DONE Creating new extra rooms");
	}
}
