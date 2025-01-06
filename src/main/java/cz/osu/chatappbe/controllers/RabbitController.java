package cz.osu.chatappbe.controllers;

import cz.osu.chatappbe.models.entity.Message;
import cz.osu.chatappbe.services.utility.MessagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class RabbitController {
	@Autowired
	private MessagingService messagingService;

	@GetMapping(value = "/api/queue")
	public List<Message> getMessages(@RequestParam String username) {
		return this.messagingService.receive("queue-" + username);
	}
}
