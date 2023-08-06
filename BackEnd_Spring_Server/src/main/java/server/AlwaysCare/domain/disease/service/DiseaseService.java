package server.AlwaysCare.domain.disease.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.AlwaysCare.domain.disease.dto.request.EditDiseaseReq;
import server.AlwaysCare.domain.disease.dto.request.SaveDiseaseReq;
import server.AlwaysCare.domain.disease.dto.response.GetDiseaseRes;
import server.AlwaysCare.domain.disease.entity.Disease;
import server.AlwaysCare.domain.disease.repository.DiseaseRepository;
import server.AlwaysCare.domain.pet.repository.PetRepository;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DiseaseService {

    private final PetRepository petRepository;
    private final DiseaseRepository diseaseRepository;

    // 질병 저장
    @Transactional
    public Long save(Long petId, SaveDiseaseReq request) throws IOException {

        int diseaseCheck = request.getDisease();

        Disease disease;
        if(diseaseCheck == 1000){
            disease = new Disease(petRepository.findById(petId).get(),
                    request.getDisease(),
                    100,
                    "A");
        } else{
            disease = new Disease(petRepository.findById(petId).get(),
                    request.getDisease(),
                    request.getPercent(),
                    "A");
        }


        Long id = diseaseRepository.save(disease).getId();

        return id;
    }

    // 질병 수정
    @Transactional
    public void edit(Long petId, EditDiseaseReq request) throws IOException {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String time = new SimpleDateFormat("yyyy/MM/dd").format(timestamp);
        Disease disease = diseaseRepository.findByPetIdAndTime(petId, time).get();
        disease.editDisease(request.getDisease(), request.getPercent());
    }

    // 질병 출력
    @Transactional
    public GetDiseaseRes print(Long petId, String date) throws IOException {

        Disease disease  = diseaseRepository.findByPetIdAndTime(petId, date).get();
        GetDiseaseRes diseaseRes = new GetDiseaseRes(petId, disease.getDisease(), disease.getPercent());

        return diseaseRes;
    }
}
