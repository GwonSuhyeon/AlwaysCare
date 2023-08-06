package server.AlwaysCare.domain.pet.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SavePetReq {

    String name;
    int age;
    int type;
    String species;

}
