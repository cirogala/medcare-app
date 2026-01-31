package it.medcare.doc_repo.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import it.medcare.doc_repo.common.rest.Headers;
import it.medcare.doc_repo.config.MedcareSecurityConfig;
import it.medcare.doc_repo.dto.DocumentMetadataDTO;
import it.medcare.doc_repo.dto.UploadResponse;
import it.medcare.doc_repo.service.DocumentService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/doc-repo")
@MedcareSecurityConfig(enableAnonymous = true)
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/upload", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadResponse> upload(
        @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
        @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
        @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
        @RequestParam("file") MultipartFile file,
        @RequestParam Long prenotationId,
        @RequestParam Long patientId,
        @RequestParam Long doctorId,
        @RequestParam Long visitTypeId
    ) throws IOException {

        DocumentMetadataDTO saved = documentService.upload(file, prenotationId, patientId, doctorId, visitTypeId);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new UploadResponse(saved.getId()));
    }

    @GetMapping(value = "/{id}", produces = it.medcare.doc_repo.common.rest.MediaType.DOC_REPO_V1)
    public DocumentMetadataDTO getMetadata(
        @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
        @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
        @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
        @PathVariable String id
    ) {
    	
        return documentService.findById(id);
    }

    @GetMapping(value = "/{id}/download")
    public ResponseEntity<InputStreamResource> download(
        @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
        @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
        @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
        @PathVariable String id
    ) throws IOException {
    	
        var resource = documentService.download(id);

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(resource.getContentType()))
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
            .body(new InputStreamResource(resource.getInputStream()));
    }

    @GetMapping(value = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<DocumentMetadataDTO>> search(
        @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
        @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
        @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
        @RequestParam(required = false) Long patientId,
        @RequestParam(required = false) Long doctorId,
        @RequestParam(required = false) Long prenotationId
    ) {
    	
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(documentService.search(patientId, doctorId, prenotationId));
    }
}
