package cz.osu.chatappbe.repositories;

import cz.osu.chatappbe.models.entity.ChatUser;
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
public interface UserRepository extends JpaRepository<ChatUser, Integer> {
	Optional<ChatUser> findUserByUsernameIgnoreCase(String username);

	boolean existsByUsernameIgnoreCase(String username);


	@Query("SELECT u FROM ChatUser u WHERE u.username LIKE %:username%")
	List<ChatUser> findUsersByPartialUsername(@Param("username") String username);

	@Query("SELECT u FROM ChatUser u WHERE u.joinedChatRooms IS EMPTY")
	List<ChatUser> findUsersWithoutChatRooms();

	Page<ChatUser> findAll(Pageable pageable);
}
