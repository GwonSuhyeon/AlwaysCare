package server.AlwaysCare.domain.user.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SignUpReq {

    private String name;
    private String email;
    private String password;

    public SignUpReq() {
    }
}
