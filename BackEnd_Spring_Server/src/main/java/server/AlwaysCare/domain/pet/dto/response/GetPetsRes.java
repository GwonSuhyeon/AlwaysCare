package server.AlwaysCare.domain.pet.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetPetsRes {
    private Long petId;
    private String name;
    private String imageURL;
    private int age;
    private int type;
    private String species;

    @Builder
    public GetPetsRes(Long petId, String name, String imageURL, int age, int type, String species) {
        this.petId = petId;
        this.name = name;
        this.imageURL = imageURL;
        this.age = age;
        this.type = type;
        this.species = species;
    }
}
