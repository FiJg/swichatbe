package cz.osu.chatappbe.controllers;

import cz.osu.chatappbe.models.SignupForm;
import cz.osu.chatappbe.services.utility.AuthService;
import cz.osu.chatappbe.services.utility.RegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class MainController {
	
	@Autowired
	private RegistrationService registrationService;
	@Autowired
	private AuthService authService;
	@Value("${app.name}")
	private String appName;
	
	@GetMapping(value = "/")
	public String getAppName() {
		return appName;
	}
	
	@PostMapping(value = "/api/signup")
	public ResponseEntity<String> register(@RequestBody SignupForm signupForm) {
		System.out.println("Signup");
		return registrationService.register(signupForm);

	}
	
	@PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> authenticate(@RequestBody SignupForm loginForm) {
		return authService.authenticate(loginForm);
	}
	
}
