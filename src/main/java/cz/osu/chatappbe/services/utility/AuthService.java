package cz.osu.chatappbe.services.utility;

import cz.osu.chatappbe.models.entity.ChatUser;
import cz.osu.chatappbe.models.Json.UserToken;
import cz.osu.chatappbe.models.SignupForm;
import cz.osu.chatappbe.services.models.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthService {
	@Autowired
	UserService userService;

	public ResponseEntity<Object> authenticate(SignupForm loginForm) {
		Optional<ChatUser> optionalUser = userService.login(loginForm);

		if (optionalUser.isPresent()) {
			ChatUser user = optionalUser.get();
			return new ResponseEntity<>(new UserToken(user.getId(), user.getUsername()), HttpStatus.OK);
		}

		return new ResponseEntity<>("Incorrect credentials.", HttpStatus.BAD_REQUEST);
	}


	public boolean isTokenValid(String username) {
		// Retrieve the user based on the username
		Optional<ChatUser> user = userService.get(username);

		// Check if the user exists
		return user.isPresent();
	}
}
