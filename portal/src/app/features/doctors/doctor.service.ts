import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface DoctorProfile {
  userId: number;
  username: string;
  email: string;
  nome: string;
  cognome: string;
  citta: string;
  indirizzo: string;
  codiceFiscale: string;
  role: string;
  typeDoctor: string;
}

@Injectable({ providedIn: 'root' })
export class DoctorService {
  private readonly API_URL = 'http://localhost:8080/profiling';

  constructor(private http: HttpClient) {}

  getDoctors(isMed = true): Observable<DoctorProfile[]> {
    const headers = new HttpHeaders({
      'X-Medcare-Accept': 'application/medcare.v1+json',
      'X-Medcare-KeyLogic': '1234567',
      'X-Medcare-TransactionID': crypto.randomUUID(),
    });

    return this.http.post<DoctorProfile[]>(
      `${this.API_URL}/doctors`,
      {},
      {
        headers,
        params: {
          isMed: String(isMed)
        }
      }
    );
  }
}
