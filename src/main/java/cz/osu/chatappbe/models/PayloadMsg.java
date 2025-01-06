package cz.osu.chatappbe.models;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class PayloadMsg {
	private Integer senderId;
	private Integer chatId;
	private String content;
	private Instant dateInstant;
	private String date;

	/**
	 * File support
	 */
	private String fileType;
	private String fileUrl;
	private String fileName;
	private Long fileSize;

}
