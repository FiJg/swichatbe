package cz.osu.chatappbe.controllers;

import cz.osu.chatappbe.exceptions.RoomNotFoundException;
import cz.osu.chatappbe.exceptions.UnauthorizedException;
import cz.osu.chatappbe.models.PayloadMsg;
import cz.osu.chatappbe.services.utility.MessagingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;


import javax.transaction.Transactional;

@Controller
@Transactional
public class ChatController {
	@Autowired
	private MessagingService messagingService;


	/*public ChatController(MessagingService messagingService) {
		this.messagingService = messagingService;
	}
*/
	/**
	 * Checking for public messages
	 * @return
	 */
	@MessageMapping("/checkPublic")
	public boolean checkPublicMessages() {
		return true;
	}

	/**
	 * Recieve public messages
	 * @param msg
	 * @return
	 */
	@MessageMapping("/message")
	@SendTo("/chatroom/public")
	public PayloadMsg receivePublicMessage(@Payload PayloadMsg msg) throws RoomNotFoundException, UnauthorizedException {
		return messagingService.receivePublicMessage(msg);
	}

	/**
	 * Method for receiving group message
	 * @param msg
	 * @return
	 */
	@MessageMapping("/group-message")
	@SendTo("/chatroom/public")
	public PayloadMsg receiveGroupMessage(@Payload PayloadMsg msg) throws RoomNotFoundException, UnauthorizedException {
		return messagingService.receiveGroupMessage(msg);
	}

	/**
	 * Method for receiving private message
	 * @param msg
	 * @return
	 */
	@MessageMapping("/private-message")
	@SendTo("/chatroom/public")
	public PayloadMsg receivePrivateMessage(@Payload PayloadMsg msg) throws RoomNotFoundException, UnauthorizedException {
		return messagingService.receivePrivateMessage(msg);
	}



}
