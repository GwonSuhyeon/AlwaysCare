package server.AlwaysCare.domain.diagnosis.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import server.AlwaysCare.domain.diagnosis.dto.request.EditDiagnosisReq;
import server.AlwaysCare.domain.diagnosis.dto.request.SaveDiagnosisReq;
import server.AlwaysCare.domain.diagnosis.dto.response.GetDiagnosisRes;
import server.AlwaysCare.domain.diagnosis.entity.Diagnosis;
import server.AlwaysCare.domain.diagnosis.repository.DiagnosisRepository;
import server.AlwaysCare.domain.pet.repository.PetRepository;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class DiagnosisService {

    private final PetRepository petRepository;
    private final DiagnosisRepository diagnosisRepository;

    // 질병 저장
    @Transactional
    public Long save(Long petId, SaveDiagnosisReq request) throws IOException {

        int diagnosisCheck = request.getDisease();

        Diagnosis diagnosis;
        if(diagnosisCheck == 1000){
            diagnosis = new Diagnosis(petRepository.findById(petId).get(),
                    request.getDisease(),
                    100,
                    "A");
        } else{

            diagnosis = new Diagnosis(petRepository.findById(petId).get(),
                    request.getDisease(),
                    request.getPercent(),
                    "A");
        }
        Long id = diagnosisRepository.save(diagnosis).getId();

        return id;
    }

    // 질병 수정
    @Transactional
    public void edit(Long petId, EditDiagnosisReq request) throws IOException {

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        String time = new SimpleDateFormat("yyyy/MM/dd").format(timestamp);
        Diagnosis diagnosis = diagnosisRepository.findByPetIdAndTime(petId, time).get();
        diagnosis.editDiagnosis(request.getDisease(), request.getPercent());
    }

    // 질병 출력
    @Transactional
    public GetDiagnosisRes print(Long petId, String date) throws IOException {

        Diagnosis diagnosis  = diagnosisRepository.findByPetIdAndTime(petId, date).get();
        GetDiagnosisRes diagnosisRes = new GetDiagnosisRes(petId, diagnosis.getDisease(), diagnosis.getPercent());

        return diagnosisRes;
    }
}
