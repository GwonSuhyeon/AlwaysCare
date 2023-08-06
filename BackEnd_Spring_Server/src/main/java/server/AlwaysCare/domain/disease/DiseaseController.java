package server.AlwaysCare.domain.disease;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import server.AlwaysCare.domain.disease.dto.request.EditDiseaseReq;
import server.AlwaysCare.domain.disease.dto.request.SaveDiseaseReq;
import server.AlwaysCare.domain.disease.dto.response.GetDiseaseRes;
import server.AlwaysCare.domain.disease.service.DiseaseService;
import server.AlwaysCare.domain.pet.entity.PetAccount;
import server.AlwaysCare.domain.pet.repository.PetRepository;
import server.AlwaysCare.global.config.Response.BaseException;
import server.AlwaysCare.global.config.Response.BaseResponse;

import java.util.Optional;

import static server.AlwaysCare.global.config.Response.BaseResponseStatus.*;
import static server.AlwaysCare.global.config.Response.BaseResponseStatus.DATABASE_ERROR;

@RestController
@RequiredArgsConstructor
@RequestMapping("/disease")
public class DiseaseController {

    private final PetRepository petRepository;
    private final DiseaseService diseaseService;

    // 반려동물 질병 저장
    @ResponseBody
    @PostMapping("/save/{petId}")
    public BaseResponse<Long> save(@PathVariable Long petId, @RequestBody SaveDiseaseReq request) throws BaseException {

        Authentication loggedInUser = SecurityContextHolder.getContext().getAuthentication();
        String User = loggedInUser.getName();

        long id = Long.parseLong(User);
        Long userId;

        try {
            Optional<PetAccount> petAccount = petRepository.findByIdAndStatus(petId, "A");
            if(petAccount.isPresent()){
                userId = petAccount.get().getUser().getId();
            }
            else{
                return new BaseResponse<>(NO_EXISTS_PETS);
            }

            if(!userId.equals(id)){
                return new BaseResponse<>(INVALID_JWT);
            }

            Long diseaseId = diseaseService.save(petId, request);
            return new BaseResponse<>(diseaseId);
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>(DATABASE_ERROR);
        }
    }

    // 반려동물 질병 수정
    @ResponseBody
    @PatchMapping ("/edit/{petId}")
    public BaseResponse<Long> edit(@PathVariable Long petId, @RequestBody EditDiseaseReq request) throws BaseException {

        Authentication loggedInUser = SecurityContextHolder.getContext().getAuthentication();
        String User = loggedInUser.getName();

        long id = Long.parseLong(User);
        Long userId;

        try {
            Optional<PetAccount> petAccount = petRepository.findByIdAndStatus(petId, "A");
            if(petAccount.isPresent()){
                userId = petAccount.get().getUser().getId();
            }
            else{
                return new BaseResponse<>(NO_EXISTS_PETS);
            }

            if(!userId.equals(id)){
                return new BaseResponse<>(INVALID_JWT);
            }

            diseaseService.edit(petId, request);
            return new BaseResponse<>(SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>(DATABASE_ERROR);
        }
    }

    // 반려동물 질병 출력
    @ResponseBody
    @GetMapping("/print/{petId}")
    public BaseResponse<GetDiseaseRes> print(@PathVariable Long petId, @RequestParam String date) throws BaseException {

        Authentication loggedInUser = SecurityContextHolder.getContext().getAuthentication();
        String User = loggedInUser.getName();

        long id = Long.parseLong(User);
        Long userId;

        try {
            Optional<PetAccount> petAccount = petRepository.findByIdAndStatus(petId, "A");
            if(petAccount.isPresent()){
                userId = petAccount.get().getUser().getId();
            }
            else{
                return new BaseResponse<>(NO_EXISTS_PETS);
            }

            if(!userId.equals(id)){
                return new BaseResponse<>(INVALID_JWT);
            }

            GetDiseaseRes getDiseaseRes = diseaseService.print(petId, date);
            return new BaseResponse<>(getDiseaseRes);

        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>(DATABASE_ERROR);
        }
    }

}
