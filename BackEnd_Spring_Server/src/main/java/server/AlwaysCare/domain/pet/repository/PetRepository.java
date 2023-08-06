package server.AlwaysCare.domain.pet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import server.AlwaysCare.domain.pet.dto.response.GetPetsInterface;
import server.AlwaysCare.domain.pet.entity.PetAccount;

import java.util.List;
import java.util.Optional;

@Repository
@EnableJpaRepositories
public interface PetRepository extends JpaRepository<PetAccount, Long> {

    @Query(value = "select p.id as petId, \n" +
            "p.name as petName, \n" +
            "p.imageURL as petImageURL, \n" +
            "p.age as petAge, \n" +
            "p.type as petType, \n" +
            "p.species as petSpecies \n" +
            "from PetAccount p \n" +
            "where p.user.id = :userId and p.status = 'A' ")
    List<GetPetsInterface> findAllMyPetsByUserId(@Param("userId") Long userId);

    Optional<PetAccount> findByIdAndStatus(Long petId, String status);

}
