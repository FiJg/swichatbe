package cz.osu.chatappbe.models.DTOs;

// ChatUserDTO.java
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatUserDTO {
    private Integer id;
    private String username;
    private List<Integer> chatRoomIds; // Optional to show joined room IDs
}
