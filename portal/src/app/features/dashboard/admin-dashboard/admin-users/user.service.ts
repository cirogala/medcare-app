import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

import { User } from './user.model';
import { CreateInternalUserRequest } from './internal-users-create.model';

@Injectable({ providedIn: 'root' })
export class UserService {

  private readonly baseUrl = 'http://localhost:8080/profiling';

  constructor(private http: HttpClient) {}

  getUsers(): Observable<User[]> {

    const headers = new HttpHeaders({
      'X-Medcare-Accept': 'PROFILING_V1',
      'X-Medcare-KeyLogic': 'ADMIN'
    });

    return this.http.get<User[]>(
      `${this.baseUrl}/users`,
      { headers }
    );
  }

  getInternalUsers(): Observable<User[]> {
    const headers = new HttpHeaders({
      'X-Medcare-Accept': 'PROFILING_V1',
      'X-Medcare-KeyLogic': 'ADMIN'
    });

    return this.http.get<User[]>(
      `${this.baseUrl}/users/internal`,
      { headers }
    );
  }

  createInternalUser(
  request: CreateInternalUserRequest
): Observable<void> {

  const headers = new HttpHeaders({
    'X-Medcare-Accept': 'application/medcare+v1.json',
    'X-Medcare-KeyLogic': '123456'
  });

  return this.http.post<void>(
    `${this.baseUrl}/createInternalUser`,
    request,
    { headers }
  );
}

  updateInternalUser(userId: number, request: {
    nome: string;
    cognome: string;
    email: string;
    citta: string;
    indirizzo: string;
    codiceFiscale: string;
    telefono: string;
  }): Observable<User> {
    const headers = new HttpHeaders({
      'X-Medcare-Accept': 'PROFILING_V1',
      'X-Medcare-KeyLogic': 'ADMIN'
    });

    return this.http.put<User>(
      `${this.baseUrl}/users/internal/${userId}`,
      request,
      { headers }
    );
  }

  updateExternalUser(userId: number, request: {
    nome: string;
    cognome: string;
    email: string;
    citta: string;
    indirizzo: string;
    codiceFiscale: string;
    telefono: string;
  }): Observable<User> {
    const headers = new HttpHeaders({
      'X-Medcare-Accept': 'PROFILING_V1',
      'X-Medcare-KeyLogic': 'ADMIN'
    });

    return this.http.put<User>(
      `${this.baseUrl}/users/external/${userId}`,
      request,
      { headers }
    );
  }

}
