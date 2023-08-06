package server.AlwaysCare.domain.diagnosis.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
import server.AlwaysCare.domain.diagnosis.entity.Diagnosis;

import java.util.Optional;

@Repository
@EnableJpaRepositories
public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {

    @Query(value = "select d \n" +
            "from Diagnosis as d \n" +
            "where function('date_format', d.updatedAt, '%Y-%m-%d') = function('date_format', :time, '%Y-%m-%d') and d.pet.id = :petId and d.status = 'A' ")
    Optional<Diagnosis> findByPetIdAndTime(Long petId, String time);
}
