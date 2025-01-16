package cz.osu.chatappbe.services.utility;

import cz.osu.chatappbe.models.entity.ChatRoom;
import cz.osu.chatappbe.models.entity.ChatUser;
import cz.osu.chatappbe.repositories.ChatRoomRepository;
import cz.osu.chatappbe.repositories.UserRepository;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;

@Service
public class ChatRoomInitializerService {

    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    public ChatRoomInitializerService(ChatRoomRepository chatRoomRepository, UserRepository userRepository) {
        this.chatRoomRepository = chatRoomRepository;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void createPublicRoom() {
        // Check if there is already a public room to avoid duplicates
        Optional<ChatRoom> existingRoom = chatRoomRepository.findChatRoomByIsPublicIsTrue();
        if (existingRoom.isEmpty()) {
            // Create a new public room
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setName("Main Chat");
            chatRoom.setIsPublic(true);
            chatRoom.setIsGroup(false);
            chatRoom.setOwner(null);  // Public rooms may not have an owner

            // Fetch all users and add them to the public room
            List<ChatUser> allUsers = userRepository.findAll();
            chatRoom.getJoinedUsers().addAll(allUsers);  // Add all users to the public room

            // Save the public room
            chatRoomRepository.save(chatRoom);
        }
    }
}
