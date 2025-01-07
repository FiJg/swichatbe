package cz.osu.chatappbe.models.entity;

import lombok.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Message implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable = false)
	private Integer id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "chat_room_id", nullable = false)
	@JsonBackReference // Prevent circular reference during serialization
	private ChatRoom room;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "chat_user_id", nullable = false)
	@JsonManagedReference // Ensure the user is serialized with the message
	private ChatUser user;

	@Column(nullable = false)
	private String content;

	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	//private Instant sendTimeInstant;
	private Date sendTime;

	public String toString() {
		return "Message{" +
		       "id=" + id +
				", room=" + (room != null ? room.getId() : "null") +
				", user=" + (user != null ? user.getId() : "null") +
		       ", content='" + content + '\'' +
		       ", sendTime=" + sendTime +
		       '}';
	}
}
