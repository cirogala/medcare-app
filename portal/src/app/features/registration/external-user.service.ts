import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ExternalUserService {

  private API_URL = 'http://localhost:8080/profiling';

  constructor(private http: HttpClient) {}

  createExternalUser(body: any): Observable<void> {
    const headers = new HttpHeaders({
      'X-Medcare-Accept': 'application/medcare.v1+json',
      'X-Medcare-KeyLogic': '123456',
      'X-Medcare-TransactionID': crypto.randomUUID(),
      'Content-Type': 'application/json'
        });

    return this.http.post<void>(
      `${this.API_URL}/createExternalUser`,
      body,
      { headers }
    );
  }
}
