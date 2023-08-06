package server.AlwaysCare.domain.disease.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SaveDiseaseReq {
    int disease;
    int percent;
}
