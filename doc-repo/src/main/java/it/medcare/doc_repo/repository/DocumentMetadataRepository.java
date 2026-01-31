package it.medcare.doc_repo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import it.medcare.doc_repo.entity.DocumentMetadata;

public interface DocumentMetadataRepository extends MongoRepository<DocumentMetadata, String> {

    List<DocumentMetadata> findByPatientId(Long patientId);

    List<DocumentMetadata> findByDoctorId(Long doctorId);

    List<DocumentMetadata> findByPrenotationId(Long prenotationId);
}
