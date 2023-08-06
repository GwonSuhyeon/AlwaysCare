package server.AlwaysCare.domain.diary.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PrintDiaryReq {

    String time;

    @Builder
    public PrintDiaryReq(String time) {
        this.time = time;
    }
}
