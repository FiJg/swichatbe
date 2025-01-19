package cz.osu.chatappbe.models.entity;

import com.fasterxml.jackson.annotation.*;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class ChatRoom implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@ManyToMany(mappedBy = "joinedChatRooms", fetch = FetchType.EAGER)
	@JsonIgnore
	private List<ChatUser> joinedUsers = new ArrayList<>();

	@OneToMany(mappedBy = "room", cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Message> messages = new ArrayList<>();

	@Column(nullable = false, unique = true)
	private String name;

	@Column(nullable = false)
	private Boolean isPublic;

	@Column(nullable = false)
	private Boolean isGroup;

//	@Column(nullable = true)
//	public Integer owner;

	@ManyToOne(cascade = CascadeType.PERSIST)
	@JoinColumn(name = "owner_id", nullable = true)
	private ChatUser owner;

	public ChatRoom(ChatRoom chatRoom) {
		this.id = chatRoom.getId();
		this.joinedUsers = new ArrayList<>(chatRoom.getJoinedUsers());
		this.messages = new ArrayList<>(chatRoom.getMessages());
		this.name = chatRoom.getName();
		this.isPublic = chatRoom.getIsPublic();
		this.isGroup = chatRoom.getIsGroup();

		this.owner = chatRoom.getOwner();
	}


	public Integer getOwnerInt() {
		return owner.getId();
	}


	public ChatUser getOwner() {
		return owner;
	}

	public void setOwner(ChatUser owner) {
		this.owner = owner;
	}

	public void addMessage(Message message) {
		if(message.getUser() != null) {
			message.getUser().getMessages().remove(message);
		}
		if(message.getRoom() != null) {
			message.getRoom().getMessages().remove(message);
		}
		this.messages.add(message);
		message.setRoom(this);
	}


	public String toString() {
		return "ChatRoom{" +
		       "id=" + id +
		       ", name='" + name + '\'' +
		       ", isPublic=" + isPublic +
		       ", isGroup=" + isGroup +
		       '}';
	}
}
