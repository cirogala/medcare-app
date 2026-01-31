import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Doctor } from './doctor.model';
import { CreateDoctorRequest } from './doctor-create.model';

@Injectable({ providedIn: 'root' })
export class DoctorService {

  private readonly baseUrl = 'http://localhost:8080/profiling';

  constructor(private http: HttpClient) {}

  getDoctors(isMed: boolean): Observable<Doctor[]> {

    const headers = new HttpHeaders({
      'X-Medcare-Accept': 'PROFILING_V1',
      'X-Medcare-KeyLogic': 'ADMIN'
    });

    const params = new HttpParams().set('isMed', isMed);

    return this.http.post<Doctor[]>(
      `${this.baseUrl}/doctors`,
      null,
      { headers, params }
    );
  }

  createDoctor(request: CreateDoctorRequest): Observable<void> {

    const headers = new HttpHeaders({
      'X-Medcare-Accept': 'PROFILING_V1',
      'X-Medcare-KeyLogic': 'ADMIN'
    });

    return this.http.post<void>(
      `${this.baseUrl}/createDoctorUser`,
      request,
      { headers }
    );
  }
}
