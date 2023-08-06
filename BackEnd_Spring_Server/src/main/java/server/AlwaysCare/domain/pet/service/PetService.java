package server.AlwaysCare.domain.pet.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import server.AlwaysCare.domain.pet.dto.request.EditPetReq;
import server.AlwaysCare.domain.pet.dto.request.SavePetReq;
import server.AlwaysCare.domain.pet.dto.response.GetPetsInterface;
import server.AlwaysCare.domain.pet.entity.PetAccount;
import server.AlwaysCare.domain.pet.repository.PetRepository;
import server.AlwaysCare.domain.user.repository.UserRepository;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PetService {

    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    // 반려동물 정보 저장
    @Transactional
    public Long save(SavePetReq request, MultipartFile multipartFile, Long userId) throws IOException {

        PetAccount petAccount = new PetAccount(userRepository.findById(userId).get(),
                request.getName(),
                s3Service.uploadFile(multipartFile),
                request.getAge(),
                request.getType(),
                request.getSpecies(),
                "A");

        Long id = petRepository.save(petAccount).getId();
        return id;
    }

    // 반려동물 정보 출력
    @Transactional
    public List<GetPetsInterface> getPetsList(Long userId){

        List<GetPetsInterface> getPetsResList = petRepository.findAllMyPetsByUserId(userId);
        return getPetsResList;
    }

    // 반려동물 모든 정보 수정
    @Transactional
    public void updateAllPet(EditPetReq request, MultipartFile multipartFile, Long petId) throws IOException {

        Long userId = petRepository.findById(petId).get().getUser().getId();

        PetAccount petAccount = petRepository.findByIdAndStatus(petId, "A").get();
        petAccount.editPet(userRepository.findById(userId).get(),
                request.getName(),
                s3Service.uploadFile(multipartFile),
                request.getAge(),
                request.getType(),
                request.getSpecies(),
                "A");
    }

    // 반려동물 정보 수정
    @Transactional
    public void updatePet(EditPetReq request, Long petId) throws IOException {

        Long userId = petRepository.findById(petId).get().getUser().getId();

        PetAccount petAccount = petRepository.findByIdAndStatus(petId, "A").get();
        petAccount.editPet(userRepository.findById(userId).get(),
                request.getName(),
                petAccount.getImageURL(),
                request.getAge(),
                request.getType(),
                request.getSpecies(),
                "A");
    }

    // 반려동물 정보 삭제
    @Transactional
    public void deletePet(Long petId) throws IOException {
        PetAccount petAccount = petRepository.findByIdAndStatus(petId, "A").get();
        String imageURL = petAccount.getImageURL();
        s3Service.deleteFile(imageURL);
        petAccount.deletePet("D");
    }


}
