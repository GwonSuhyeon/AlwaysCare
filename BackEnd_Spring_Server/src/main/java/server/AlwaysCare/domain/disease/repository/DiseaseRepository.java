package server.AlwaysCare.domain.disease.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
import server.AlwaysCare.domain.disease.entity.Disease;

import java.util.Optional;

@Repository
@EnableJpaRepositories
public interface DiseaseRepository extends JpaRepository<Disease, Long> {

    @Query(value = "select d \n" +
            "from Disease as d \n" +
            "where function('date_format', d.updatedAt, '%Y-%m-%d') = function('date_format', :time, '%Y-%m-%d') and d.pet.id = :petId and d.status = 'A' ")
    Optional<Disease> findByPetIdAndTime(Long petId, String time);
}
