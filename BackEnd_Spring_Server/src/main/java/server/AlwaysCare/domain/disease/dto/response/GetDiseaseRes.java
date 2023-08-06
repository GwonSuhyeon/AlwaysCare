package server.AlwaysCare.domain.disease.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetDiseaseRes {

    private Long petId;
    private int disease;
    private int percent;

    @Builder
    public GetDiseaseRes(Long petId, int disease, int percent) {
        this.petId = petId;
        this.disease = disease;
        this.percent = percent;
    }
}

