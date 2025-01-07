package cz.osu.chatappbe.controllers;

import cz.osu.chatappbe.models.entity.Message;
import cz.osu.chatappbe.services.utility.MessagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@CrossOrigin
public class RabbitController {

	private static final Logger logger = LoggerFactory.getLogger(RabbitController.class);


	@Autowired
	private MessagingService messagingService;

	@GetMapping(value = "/api/queue")
	public List<Message> getMessages(@RequestParam String username) {
		logger.info("Received request to fetch messages for user: {}", username);
		List<Message> messages = this.messagingService.receive("queue-" + username);
		logger.info("Fetched {} messages for user: {}", messages.size(), username);

		return messages;
	}
}
