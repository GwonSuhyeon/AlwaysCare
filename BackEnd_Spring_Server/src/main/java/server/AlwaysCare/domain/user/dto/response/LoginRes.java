package server.AlwaysCare.domain.user.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor

public class LoginRes {
    private String jwt;
    private Long userId;

    public LoginRes(String jwt, Long userId) {
        this.jwt = jwt;
        this.userId = userId;
    }
}
