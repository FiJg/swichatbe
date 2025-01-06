package cz.osu.chatappbe.models;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ChatForm {
	private String name;
	private Boolean isGroup;
	private List<String> joinedUserNames;
	private Integer createdBy;
}
