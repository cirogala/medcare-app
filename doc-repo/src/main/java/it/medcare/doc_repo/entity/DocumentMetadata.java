package it.medcare.doc_repo.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "doc_report")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMetadata {

    @Id
    private String id;

    private Long prenotationId;
    private Long patientId;
    private Long doctorId;
    private Long visitTypeId;

    private String fileId;
    private String filename;
    private String contentType;
    private long size;
    private LocalDateTime createdAt;
}
