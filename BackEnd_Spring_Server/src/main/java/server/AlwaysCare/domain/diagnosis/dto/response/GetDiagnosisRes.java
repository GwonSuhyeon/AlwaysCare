package server.AlwaysCare.domain.diagnosis.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GetDiagnosisRes {

    private Long petId;
    private int disease;
    private int percent;

    @Builder
    public GetDiagnosisRes(Long petId, int disease, int percent) {
        this.petId = petId;
        this.disease = disease;
        this.percent = percent;
    }
}
