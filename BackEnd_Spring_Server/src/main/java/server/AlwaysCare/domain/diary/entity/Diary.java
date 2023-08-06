package server.AlwaysCare.domain.diary.entity;

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

public class Diary extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private PetAccount pet;
    private String sentence;
    private String status;

    @Builder
    public Diary(PetAccount pet, String sentence, String status) {
        this.pet = pet;
        this.sentence = sentence;
        this.status = status;
    }

    public void editDiary(String sentence){
        this.sentence = sentence;
    }
}
