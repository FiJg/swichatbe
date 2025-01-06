package cz.osu.chatappbe.models.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
public class ChatUser implements Serializable {
	@ManyToMany
	@JoinTable(
			name = "chat_member",
			joinColumns = @JoinColumn(name = "chat_user_id"),
			inverseJoinColumns = @JoinColumn(name = "chat_room_id")
	)
	@JsonBackReference // prevents serialization of the back reference
	List<ChatRoom> joinedChatRooms = new ArrayList<>();

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(nullable = false)
	private Integer id;

	@OneToMany(mappedBy = "user", cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.LAZY)
	private List<Message> messages = new ArrayList<>();

	@Column(nullable = false)
	private String username;

	@Column(nullable = false)
	private String password;

	public ChatUser(ChatUser chatUser) {
		this.id = chatUser.getId();
		this.joinedChatRooms = new ArrayList<>(chatUser.getJoinedChatRooms());
		this.messages = new ArrayList<>(chatUser.getMessages());
		this.username = chatUser.getUsername();
		this.password = chatUser.getPassword();
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
		       '}';
	}


}
