package server.AlwaysCare.domain.diagnosis.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import server.AlwaysCare.domain.pet.entity.PetAccount;
import server.AlwaysCare.global.entity.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Data
@SuperBuilder
@NoArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
public class Diagnosis extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private PetAccount pet;
    private int disease;
    private int percent;
    private String status;

    @Builder
    public Diagnosis(PetAccount pet, int disease, int percent, String status) {
        this.pet = pet;
        this.disease = disease;
        this.percent = percent;
        this.status = status;
    }

    public void editDiagnosis(int disease, int percent){
        this.disease = disease;
        this.percent = percent;
    }
}
