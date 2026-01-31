import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserProfileSummary {
  userId: number;
  username: string;
  nome: string;
  cognome: string;
}

@Injectable({ providedIn: 'root' })
export class UserDirectoryService {
  private readonly baseUrl = 'http://localhost:8080/profiling';

  constructor(private http: HttpClient) {}

  getPatients(): Observable<UserProfileSummary[]> {
    const headers = new HttpHeaders({
      'X-Medcare-Accept': 'application/medcare.v1+json',
      'X-Medcare-KeyLogic': '1234567',
      'X-Medcare-TransactionID': crypto.randomUUID(),
    });

    return this.http.get<UserProfileSummary[]>(
      `${this.baseUrl}/users`,
      { headers }
    );
  }
}
