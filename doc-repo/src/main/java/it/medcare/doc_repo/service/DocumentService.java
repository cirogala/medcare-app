package it.medcare.doc_repo.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.client.gridfs.model.GridFSFile;

import it.medcare.doc_repo.constants.Constants;
import it.medcare.doc_repo.dto.DocumentMetadataDTO;
import it.medcare.doc_repo.dto.NotificationReportRequest;
import it.medcare.doc_repo.dto.UserProfileDTO;
import it.medcare.doc_repo.entity.DocumentMetadata;
import it.medcare.doc_repo.repository.DocumentMetadataRepository;
import it.medcare.doc_repo.client.NotificationClient;
import it.medcare.doc_repo.client.ProfilingClient;

@Service
public class DocumentService {

    private final GridFsTemplate gridFsTemplate;
    private final DocumentMetadataRepository metadataRepository;
    private final ProfilingClient profilingClient;
    private final NotificationClient notificationClient;

    public DocumentService(GridFsTemplate gridFsTemplate, DocumentMetadataRepository metadataRepository, ProfilingClient profilingClient, NotificationClient notificationClient) {
    	
        this.gridFsTemplate = gridFsTemplate;
        this.metadataRepository = metadataRepository;
        this.profilingClient = profilingClient;
        this.notificationClient = notificationClient;
    }

    public DocumentMetadataDTO upload(MultipartFile file, Long prenotationId, Long patientId, Long doctorId, Long visitTypeId) throws IOException {
    	
        Document metadata = new Document();
        metadata.put("prenotationId", prenotationId);
        metadata.put("patientId", patientId);
        metadata.put("doctorId", doctorId);
        metadata.put("visitTypeId", visitTypeId);

        ObjectId fileId = gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), metadata);

        DocumentMetadata saved = metadataRepository.save(new DocumentMetadata(
            null,
            prenotationId,
            patientId,
            doctorId,
            visitTypeId,
            fileId.toHexString(),
            file.getOriginalFilename(),
            file.getContentType(),
            file.getSize(),
            LocalDateTime.now()
        ));

        notifyReportUploaded(saved);

        return toDto(saved);
    }

    public DocumentMetadataDTO findById(String id) {
    	
        DocumentMetadata metadata = metadataRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(Constants.FILE_NOT_FOUND));
        
        return toDto(metadata);
    }

    public List<DocumentMetadataDTO> search(Long patientId, Long doctorId, Long prenotationId) {
    	
        if (prenotationId != null) {
        	
            return metadataRepository.findByPrenotationId(prenotationId).stream().map(this::toDto).toList();
        }
        
        if (patientId != null) {
        	
            return metadataRepository.findByPatientId(patientId).stream().map(this::toDto).toList();
        }
        
        if (doctorId != null) {
        	
            return metadataRepository.findByDoctorId(doctorId).stream().map(this::toDto).toList();
        }
        
        return metadataRepository.findAll().stream().map(this::toDto).toList();
    }

    public GridFsResource download(String id) {
    	
        DocumentMetadata metadata = metadataRepository.findById(id).orElseThrow(() -> new IllegalArgumentException(Constants.FILE_NOT_FOUND));

        GridFSFile gridFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(new ObjectId(metadata.getFileId()))));

        if (gridFile == null) {
        	
            throw new IllegalArgumentException(Constants.FILE_NOT_FOUND);
        }

        return gridFsTemplate.getResource(gridFile);
    }

    private DocumentMetadataDTO toDto(DocumentMetadata metadata) {
    	
        return new DocumentMetadataDTO(
            metadata.getId(),
            metadata.getPrenotationId(),
            metadata.getPatientId(),
            metadata.getDoctorId(),
            metadata.getVisitTypeId(),
            metadata.getFilename(),
            metadata.getContentType(),
            metadata.getSize(),
            metadata.getCreatedAt()
        );
    }

    private void notifyReportUploaded(DocumentMetadata metadata) {
    	
        try {
            UserProfileDTO patient = profilingClient.fetchExternalUserById(metadata.getPatientId());
            UserProfileDTO doctor = profilingClient.fetchDoctorById(metadata.getDoctorId());

            NotificationReportRequest request = new NotificationReportRequest();
            request.setReportId(metadata.getId());
            request.setPrenotationId(metadata.getPrenotationId());
            request.setVisitType("Visita #" + metadata.getVisitTypeId());
            request.setDate("");
            request.setSlotTime("");

            if (patient != null) {
            	
                request.setPatientEmail(patient.getEmail());
                request.setPatientName((patient.getNome() + " " + patient.getCognome()).trim());
            }
            
            if (doctor != null) {
            	
                request.setDoctorName((doctor.getNome() + " " + doctor.getCognome()).trim());
            }

            notificationClient.notifyReportUploaded(request);
            
        } catch (Exception ex) {
            // ignoro
        }
    }
}
