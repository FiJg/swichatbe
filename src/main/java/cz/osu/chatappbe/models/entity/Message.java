package cz.osu.chatappbe.models.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import javax.persistence.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Date;


@Builder
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
