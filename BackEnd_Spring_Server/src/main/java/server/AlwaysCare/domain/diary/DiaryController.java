package server.AlwaysCare.domain.diary;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import server.AlwaysCare.domain.diary.dto.request.EditDiaryReq;
import server.AlwaysCare.domain.diary.dto.request.SaveDiaryReq;
import server.AlwaysCare.domain.diary.service.DiaryService;
import server.AlwaysCare.domain.pet.entity.PetAccount;
import server.AlwaysCare.domain.pet.repository.PetRepository;
import server.AlwaysCare.global.config.Response.BaseException;
import server.AlwaysCare.global.config.Response.BaseResponse;

import java.util.List;
import java.util.Optional;

import static server.AlwaysCare.global.config.Response.BaseResponseStatus.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/diarys")
public class DiaryController {
    private final PetRepository petRepository;
    private final DiaryService diaryService;

    // 반려동물 일기 저장
    @ResponseBody
    @PostMapping("/save/{petId}")
    public BaseResponse<Long> save(@PathVariable Long petId, @RequestBody SaveDiaryReq request) throws BaseException {

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

            Long diaryId = diaryService.save(petId, request);
            return new BaseResponse<>(diaryId);
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>(DATABASE_ERROR);
        }
    }

    // 반려동물 일기 수정
    @ResponseBody
    @PatchMapping ("/edit/{petId}")
    public BaseResponse<Long> edit(@PathVariable Long petId, @RequestBody EditDiaryReq request) throws BaseException {

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

            diaryService.edit(petId, request);
            return new BaseResponse<>(SUCCESS);
        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>(DATABASE_ERROR);
        }
    }

    // 반려동물 일기 출력
    @ResponseBody
    @GetMapping("/print/{petId}")
    public BaseResponse<String> print(@PathVariable Long petId, @RequestParam String date) throws BaseException {

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

            String sentence = diaryService.print(petId, date);
            return new BaseResponse<>(sentence);

        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>(DATABASE_ERROR);
        }
    }

    // 반려동물 일기 작성한 날짜 출력
    @ResponseBody
    @GetMapping("/list/{petId}")
    public BaseResponse<List<String>> list(@PathVariable Long petId) throws BaseException {

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

            List<String> list = diaryService.list(petId);
            return new BaseResponse<>(list);

        } catch (Exception e) {
            e.printStackTrace();
            return new BaseResponse<>(DATABASE_ERROR);
        }
    }
}
