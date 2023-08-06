package server.AlwaysCare.domain.diagnosis.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SaveDiagnosisReq {
    int disease;
    int percent;
}
