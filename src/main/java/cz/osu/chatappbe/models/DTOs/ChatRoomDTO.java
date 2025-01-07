package cz.osu.chatappbe.models.DTOs;

// ChatRoomDTO.java
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDTO {
    private Integer id;
    private String name;
    private Boolean isPublic;
    private Boolean isGroup;
    private Integer ownerId; // Optional to show owner
    private List<Integer> joinedUserIds; // Optional to show just user IDs
    private List<MessageDTO> messages; // Optional for full messages
}
