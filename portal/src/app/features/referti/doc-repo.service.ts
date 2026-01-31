import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DocumentMetadata {
  id: string;
  prenotationId: number;
  patientId: number;
  doctorId: number;
  visitTypeId: number;
  filename: string;
  contentType: string;
  size: number;
  createdAt: string;
}

export interface UploadResponse {
  id: string;
}

@Injectable({ providedIn: 'root' })
export class DocRepoService {
  private readonly BASE_URL = '/doc-repo';

  constructor(private http: HttpClient) {}

  upload(
    file: File,
    payload: {
      prenotationId: number;
      patientId: number;
      doctorId: number;
      visitTypeId: number;
    }
  ): Observable<UploadResponse> {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('prenotationId', String(payload.prenotationId));
    formData.append('patientId', String(payload.patientId));
    formData.append('doctorId', String(payload.doctorId));
    formData.append('visitTypeId', String(payload.visitTypeId));

    return this.http.post<UploadResponse>(
      `${this.BASE_URL}/upload`,
      formData,
      { headers: this.buildHeaders(false) }
    );
  }

  search(params: {
    patientId?: number;
    doctorId?: number;
    prenotationId?: number;
  }): Observable<DocumentMetadata[]> {
    let httpParams = new HttpParams();
    if (params.patientId != null) httpParams = httpParams.set('patientId', String(params.patientId));
    if (params.doctorId != null) httpParams = httpParams.set('doctorId', String(params.doctorId));
    if (params.prenotationId != null) httpParams = httpParams.set('prenotationId', String(params.prenotationId));

    return this.http.get<DocumentMetadata[]>(
      `${this.BASE_URL}/search`,
      { headers: this.buildHeaders(), params: httpParams }
    );
  }

  download(id: string): Observable<Blob> {
    return this.http.get(
      `${this.BASE_URL}/${id}/download`,
      {
        headers: this.buildHeaders(),
        responseType: 'blob'
      }
    );
  }

  private buildHeaders(json = true): HttpHeaders {
    const headers: Record<string, string> = {
      'X-Medcare-Accept': 'application/medcare.docrepo+v1.json',
      'X-Medcare-KeyLogic': 'DOCREPO',
      'X-Medcare-TransactionID': crypto.randomUUID()
    };
    if (json) {
      headers['Content-Type'] = 'application/json';
    }
    return new HttpHeaders(headers);
  }
}
