package server.AlwaysCare.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.AlwaysCare.domain.user.dto.request.LoginReq;
import server.AlwaysCare.domain.user.dto.request.SignUpReq;
import server.AlwaysCare.domain.user.dto.response.LoginRes;
import server.AlwaysCare.domain.user.entity.UserAccount;
import server.AlwaysCare.domain.user.repository.UserRepository;
import server.AlwaysCare.global.config.Response.BaseException;
import server.AlwaysCare.global.config.security.jwt.JwtTokenProvider;

import java.util.Optional;

import static server.AlwaysCare.global.config.Response.BaseResponseStatus.POST_USERS_NO_EXISTS_USER;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public Long signup(SignUpReq request){
        UserAccount userAccount = new UserAccount(request.getName(), request.getEmail(), request.getPassword(), "A");
        Long id = userRepository.save(userAccount).getId();
        return id;
    }

    @Transactional
    public LoginRes login(LoginReq request) throws BaseException {

        Optional<UserAccount> optionalUserAccount = userRepository.findByEmail(request.getEmail());
        if(optionalUserAccount.isPresent()){
            UserAccount userAccount = optionalUserAccount.get();
            if(request.getPassword().equals(userAccount.getPassword())) {
                return new LoginRes(jwtTokenProvider.createAccessToken(Long.toString(userAccount.getId())), userAccount.getId());
            }
            else {
                throw new BaseException(POST_USERS_NO_EXISTS_USER);
            }
        }
        else{
            throw new BaseException(POST_USERS_NO_EXISTS_USER);
        }
    }


}
