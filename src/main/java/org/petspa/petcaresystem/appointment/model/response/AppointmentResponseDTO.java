package org.petspa.petcaresystem.appointment.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AppointmentResponseDTO {
    private AppointmentResponseInfor responseInfor;
    private AppointmentResponseData data;
}
