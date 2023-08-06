package server.AlwaysCare.domain.disease.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PrintDiseaseReq {

    String time;

    @Builder
    public PrintDiseaseReq(String time) {
        this.time = time;
    }
}

