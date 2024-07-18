package org.petspa.petcaresystem.doctor.service;

import java.util.Collection;
import java.util.List;

import org.petspa.petcaresystem.authenuser.model.payload.AuthenUser;
import org.petspa.petcaresystem.doctor.model.Doctor;
import org.petspa.petcaresystem.doctor.model.DoctorResponseDTO;
import org.springframework.stereotype.Service;

import javax.print.Doc;

@Service
public interface DoctorService {

    public List<DoctorResponseDTO> findAllDoctor();

    public AuthenUser findDoctorById(Long doctorId);

    public Doctor saveDoctor(Doctor doctor);

    public Doctor updateDoctor(Doctor doctor);

    public Doctor deleteDoctor(Long doctorId);

    public List<Doctor> findAll();
}
