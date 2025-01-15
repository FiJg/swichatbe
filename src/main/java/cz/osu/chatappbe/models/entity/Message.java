package cz.osu.chatappbe.models.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import javax.persistence.*;
import java.time.ZonedDateTime;


@Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({ "id", "user", "room", "content", "sendTime", "username" })
public class Message implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "chat_room_id", nullable = false)
	private ChatRoom room;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "chat_user_id", nullable = false)
	private ChatUser user;

	@Column(nullable = false)
	private String content;

	@Column(nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date sendTime;

	@Transient
	private String username;

	@Column(nullable = false)
	private Instant addedToQueueTimestamp;
	@PrePersist
	public void prePersist() {
		if (addedToQueueTimestamp == null) {
			addedToQueueTimestamp = Instant.now();
		}
	}
	@Column(nullable = true)
	private Instant retrievedFromQueueTimestamp;

	private String fileUrl;
	private String fileName; // Optional: Original file name
	private String fileType;

	public String getUsername() {
		if (user != null) {
			return user.getUsername();
		}
		return this.username;
	}

	public void setUsername(String username) {
		this.username = username;
	}


	public String toString() {
		return "Message{" +
		       "id=" + id +
				", room=" + (room != null ? room.getId() : "null") +
				", username=" + (user != null ? user.getUsername() : "null") +
		       ", content='" + content + '\'' +
		       ", sendTime=" + sendTime +
				", addedToQueueTimestamp=" + addedToQueueTimestamp +
				", retrievedFromQueueTimestamp=" + retrievedFromQueueTimestamp +
		       '}';
	}
}
