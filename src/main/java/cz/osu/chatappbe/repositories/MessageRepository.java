package cz.osu.chatappbe.repositories;

import cz.osu.chatappbe.models.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {


    List<Message> findByRoom_Id(Integer roomId);

    List<Message> findByUser_Id(Integer userId);

    @Query("SELECT m FROM Message m WHERE m.sendTime BETWEEN :startDate AND :endDate")
    List<Message> findMessagesByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Modifying
    @Query("DELETE FROM Message m WHERE m.sendTime < :cutoffDate")
    void deleteMessagesOlderThan(@Param("cutoffDate") Date cutoffDate);

}
