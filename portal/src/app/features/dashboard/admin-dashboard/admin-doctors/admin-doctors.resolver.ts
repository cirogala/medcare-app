import { ResolveFn } from '@angular/router';
import { inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Doctor } from './doctor.model';

export const adminDoctorsResolver: ResolveFn<Doctor[]> = () => {
  const http = inject(HttpClient);

  return http.post<Doctor[]>(
    'http://localhost:8080/profiling/retrieveDoctors',
    {}
  );
};
