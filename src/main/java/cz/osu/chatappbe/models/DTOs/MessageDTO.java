package cz.osu.chatappbe.models.DTOs;

// MessageDTO.java
import lombok.*;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {
    private Integer id;
    private Integer roomId; // Optional to link to the chat room
    private Integer userId; // Optional to link to the user
    private String username; // Optional to show the sender's username
    private String content;
    private Date sendTime;
}
