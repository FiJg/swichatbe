package cz.osu.chatappbe.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AddChatForm {

    private Integer reqUserId;
    private Integer roomId;
    private String userName;

}
