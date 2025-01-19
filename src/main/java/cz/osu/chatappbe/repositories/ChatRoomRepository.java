package cz.osu.chatappbe.repositories;

import cz.osu.chatappbe.models.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {

	/**
	 * Finds all chatrooms joined by the user
	 */
	List<ChatRoom> findByJoinedUsers_Username(String username);

	Optional<ChatRoom> findChatRoomByIsPublicIsTrue();

	List<ChatRoom> findByIsPublicIsTrue();

	// This query forces the joinedUsers to be eagerly fetched
	@Query("SELECT cr FROM ChatRoom cr " +
			"LEFT JOIN FETCH cr.joinedUsers " +
			"WHERE cr.id = :id")
	Optional<ChatRoom> findByIdWithJoinedUsers(@Param("id") Integer id);

	Page<ChatRoom> findByIsPublicIsTrue(Pageable pageable);

	@Query("SELECT cr FROM ChatRoom cr WHERE cr.name LIKE %:name%")
	List<ChatRoom> searchChatRoomsByPartialName(@Param("name") String name);

	@Query("DELETE FROM ChatRoom cr WHERE cr.joinedUsers IS EMPTY")
	@Modifying
	void deleteEmptyChatRooms();

}
