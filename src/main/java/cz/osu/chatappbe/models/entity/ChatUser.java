package cz.osu.chatappbe.models.entity;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Builder
@Entity
@Getter
@Setter
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@NoArgsConstructor
@AllArgsConstructor
public class ChatUser implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;

	@ManyToMany
	@JoinTable(
			name = "chat_member",
			joinColumns = @JoinColumn(name = "chat_user_id"),
			inverseJoinColumns = @JoinColumn(name = "chat_room_id")
	)
	List<ChatRoom> joinedChatRooms = new ArrayList<>();

	@OneToMany(mappedBy = "user", cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<Message> messages = new ArrayList<>();

	@Column(nullable = false, unique = true)
	private String username;

	//private Map<ChatRoom, Integer> unreadMessages = new HashMap<>();

	@JsonIgnore
	@Column(nullable = false)
	private String password;

	@Column(nullable = true)
	private String avatarUrl;

	public ChatUser(ChatUser chatUser) {
		this.id = chatUser.getId();
		this.joinedChatRooms = new ArrayList<>(chatUser.getJoinedChatRooms());
		this.messages = new ArrayList<>(chatUser.getMessages());
		this.username = chatUser.getUsername();
		this.password = chatUser.getPassword();
		this.avatarUrl = chatUser.getAvatarUrl();
	}

	public void addChatRoom(ChatRoom chatRoom) {
		this.joinedChatRooms.add(chatRoom);
		chatRoom.getJoinedUsers().add(this);
	}

	public void addChatRooms(List<ChatRoom> chatRooms) {
		this.joinedChatRooms.addAll(chatRooms);
		chatRooms.forEach(chatRoom -> chatRoom.getJoinedUsers().add(this));
	}

	public void removeChatRoom(ChatRoom chatRoom) {
		this.joinedChatRooms.remove(chatRoom);
		chatRoom.getJoinedUsers().remove(this);
	}

	public void addMessage(Message message) {
		if(message.getUser() != null) {
			message.getUser().getMessages().remove(message);
		}
		if(message.getRoom() != null) {
			message.getRoom().getMessages().remove(message);
		}
		this.messages.add(message);
		message.setUser(this);
	}



	public String toString() {
		return "ChatUser{" +
		       "id=" + id +
		       ", username='" + username + '\'' +
		       ", password='" + password + '\'' +
				", avatarUrl='" + avatarUrl + '\'' +
		       '}';
	}


}
