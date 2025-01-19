package cz.osu.chatappbe.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class PayloadMsg {
	private Integer senderId;
	private Integer chatId;
	private MessageContent content;
	private String date;

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	@ToString
	public static class MessageContent {
		private String content;
		private String fileType;
		private String fileUrl;
		private String fileName;
		private Long fileSize;
	}

}
