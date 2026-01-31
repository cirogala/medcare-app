package it.medcare.doc_repo.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMetadataDTO {
	
    private String id;
    private Long prenotationId;
    private Long patientId;
    private Long doctorId;
    private Long visitTypeId;
    private String filename;
    private String contentType;
    private long size;
    private LocalDateTime createdAt;
}
