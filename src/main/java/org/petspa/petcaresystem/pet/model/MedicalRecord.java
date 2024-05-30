package org.petspa.petcaresystem.pet.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.petspa.petcaresystem.authenuser.model.AuthenUser;

@Entity
@Getter
@Setter
@Table(name = "MedicalRecord")
public class  MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String record_id;

    @OneToOne(mappedBy = "pet_id")
    private AuthenUser pet_id;
}
