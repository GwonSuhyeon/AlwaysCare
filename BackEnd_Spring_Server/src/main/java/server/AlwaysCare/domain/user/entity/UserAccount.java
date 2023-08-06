package server.AlwaysCare.domain.user.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import server.AlwaysCare.domain.pet.entity.PetAccount;
import server.AlwaysCare.global.entity.BaseEntity;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.List;

import static lombok.AccessLevel.PROTECTED;

@DynamicInsert
@Entity
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = PROTECTED)
@EqualsAndHashCode(callSuper = true)
@ToString
public class UserAccount extends BaseEntity {

    private String name;
    private String email;
    private String password;
    private String status;

    @OneToMany(mappedBy = "user")
    @Builder.Default
    @ToString.Exclude
    private List<PetAccount> petAccounts = new ArrayList<>();

    @Builder
    public UserAccount(String name, String email, String password, String status) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.status = status;
    }
}
