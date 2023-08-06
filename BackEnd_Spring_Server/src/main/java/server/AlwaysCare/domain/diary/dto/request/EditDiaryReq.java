package server.AlwaysCare.domain.diary.dto.request;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EditDiaryReq {

    String sentence;

    @Builder
    public EditDiaryReq(String sentence) {
        this.sentence = sentence;
    }
}
