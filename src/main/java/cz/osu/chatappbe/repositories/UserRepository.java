package cz.osu.chatappbe.repositories;

import cz.osu.chatappbe.models.entity.ChatUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<ChatUser, Integer> {
	Optional<ChatUser> findUserByUsernameIgnoreCase(String username);

	boolean existsByUsernameIgnoreCase(String username);
}
