package cz.osu.chatappbe.models.Json;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class UserToken {
	private Integer id;
	private String username;
}
