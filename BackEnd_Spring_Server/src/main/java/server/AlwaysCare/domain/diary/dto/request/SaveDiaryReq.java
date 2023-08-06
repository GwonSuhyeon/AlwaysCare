package server.AlwaysCare.domain.diary.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SaveDiaryReq {

    String sentence;

    @Builder
    public SaveDiaryReq(String sentence) {
        this.sentence = sentence;
    }
}
