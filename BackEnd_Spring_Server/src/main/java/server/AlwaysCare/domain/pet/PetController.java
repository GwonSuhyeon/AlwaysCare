package server.AlwaysCare.domain.pet;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import server.AlwaysCare.domain.pet.dto.request.EditPetReq;
import server.AlwaysCare.domain.pet.dto.request.SavePetReq;
import server.AlwaysCare.domain.pet.dto.response.GetPetsInterface;
import server.AlwaysCare.domain.pet.entity.PetAccount;
import server.AlwaysCare.domain.pet.repository.PetRepository;
import server.AlwaysCare.domain.pet.service.PetService;
import server.AlwaysCare.global.config.Response.BaseException;
import server.AlwaysCare.global.config.Response.BaseResponse;

import java.util.List;
import java.util.Optional;

import static server.AlwaysCare.global.config.Response.BaseResponseStatus.*;
import static server.AlwaysCare.global.utils.FileCheck.checkImage;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pets")
public class PetController {

    private final PetRepository petRepository;
    private final PetService petService;

    // 반려동물 정보 저장
    @ResponseBody
    @PostMapping("/save")
    public BaseResponse<Long> save(@RequestPart(value = "SaveReq") SavePetReq request, @RequestPart(value = "file", required = false) MultipartFile multipartFile) throws BaseException {

        Authentication loggedInUser = SecurityContextHolder.getContext().getAuthentication();
        String User = loggedInUser.getName();

        long id = Long.parseLong(User);

        try {
            if(multipartFile != null){
                if(!checkImage(multipartFile)){
                    return new BaseResponse<>(INVALID_IMAGE_FILE);
                }
                Long petId = petService.save(request, multipartFile, id);
                return new BaseResponse<Long>(petId);
            }
            else {
                Long petId = petService.save(request, multipartFile, id);
                return new BaseResponse<Long>(petId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>(DATABASE_ERROR);
        }
    }

    // 반려동물들 정보 출력
    @ResponseBody
    @GetMapping("/list/{userId}")
    public BaseResponse<List<GetPetsInterface>> list(@PathVariable Long userId) throws BaseException {

        Authentication loggedInUser = SecurityContextHolder.getContext().getAuthentication();
        String User = loggedInUser.getName();

        long id = Long.parseLong(User);

        try {
            if(!userId.equals(id)){
                return new BaseResponse<>(INVALID_JWT);
            }

            List<GetPetsInterface> petsResList = petService.getPetsList(userId);
            if(petsResList.isEmpty()){
                return new BaseResponse<>(NO_EXISTS_PETS);
            }
            return new BaseResponse<>(petsResList);

        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>(DATABASE_ERROR);
        }
    }

    // 반려동물 모든 정보 수정
    @ResponseBody
    @PatchMapping ("/editAll/{petId}")
    public BaseResponse<String> editAll(@PathVariable Long petId, @RequestPart(value = "EditPetReq") EditPetReq request, @RequestPart(value = "file", required = false) MultipartFile multipartFile) throws BaseException {

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

            petService.updateAllPet(request, multipartFile, petId);
            return new BaseResponse<>(SUCCESS);

        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>(DATABASE_ERROR);
        }
    }

    // 반려동물 정보 수정
    @ResponseBody
    @PatchMapping ("/edit/{petId}")
    public BaseResponse<String> edit(@PathVariable Long petId, @RequestBody EditPetReq request) throws BaseException {

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

            petService.updatePet(request, petId);
            return new BaseResponse<>(SUCCESS);

        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>(DATABASE_ERROR);
        }
    }

    // 반려동물 정보 삭제
    @ResponseBody
    @PatchMapping("delete/{petId}")
    public BaseResponse<String> delete(@PathVariable Long petId){
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

            petService.deletePet(petId);
            return new BaseResponse<>(SUCCESS);

        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>(DATABASE_ERROR);
        }

    }
}
