package server.AlwaysCare.domain.pet.entity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import server.AlwaysCare.domain.user.entity.UserAccount;
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

public class PetAccount extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    private String name;
    private String imageURL;
    private int age;
    private int type;
    private String species;
    private String status;

    @Builder
    public PetAccount(UserAccount user, String name, String imageURL, int age, int type, String species, String status) {
        this.user = user;
        this.name = name;
        this.imageURL = imageURL;
        this.age = age;
        this.type = type;
        this.species = species;
        this.status = status;
    }

    // 정보 수정
    public void editPet(UserAccount user, String name, String imageURL, int age, int type, String species, String status) {
        this.user = user;
        this.name = name;
        this.imageURL = imageURL;
        this.age = age;
        this.type = type;
        this.species = species;
        this.status = status;
    }

    // 정보 삭제
    public void deletePet(String status)
    {
        this.status = status;
    }
}

