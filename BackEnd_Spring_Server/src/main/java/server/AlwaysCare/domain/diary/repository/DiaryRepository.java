package server.AlwaysCare.domain.diary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
import server.AlwaysCare.domain.diary.entity.Diary;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@EnableJpaRepositories
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    @Query(value = "select d \n" +
            "from Diary as d \n" +
            "where function('date_format', d.updatedAt, '%Y-%m-%d') = function('date_format', :time, '%Y-%m-%d') and d.pet.id = :petId and d.status = 'A' ")
    Optional<Diary> findByPetIdAndTime(Long petId, String time);

    @Query(value = "select d.updatedAt \n" +
            "from Diary as d \n" +
            "where d.pet.id = :petId and d.status = 'A' ")
    List<Timestamp> findDayByPetId(Long petId);
}
