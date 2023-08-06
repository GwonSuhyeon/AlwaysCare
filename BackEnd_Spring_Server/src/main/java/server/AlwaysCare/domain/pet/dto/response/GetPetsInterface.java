package server.AlwaysCare.domain.pet.dto.response;

public interface GetPetsInterface {
    Long getPetId();
    String getPetName();
    String getPetImageURL();
    int getPetAge();
    int getPetType();
    String getPetSpecies();
}
