package it.medcare.prenotation.mapper;

import org.springframework.stereotype.Component;

import it.medcare.prenotation.dto.PrenotationDTO;
import it.medcare.prenotation.entity.Prenotation;


@Component
public class PrenotationMapper {

	public PrenotationDTO toDto(Prenotation entity) {
		
		PrenotationDTO dto = new PrenotationDTO();
		dto.setPrenotationId(entity.getPrenotationId());
		dto.setUserId(entity.getUserId());
		dto.setDoctorId(entity.getDoctorId());
		dto.setVisitTypeId(entity.getVisitTypeId());
		dto.setDate(entity.getDate());
		dto.setSlotTime(entity.getSlotTime());
		dto.setStatus(entity.getStatus());
		
		return dto;
	}
}
