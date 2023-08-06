package server.AlwaysCare.domain.diagnosis.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PrintDiagnosisReq {

    String time;

    @Builder
    public PrintDiagnosisReq(String time) {
        this.time = time;
    }
}
