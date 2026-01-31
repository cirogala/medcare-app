import { inject } from '@angular/core';
import { ResolveFn } from '@angular/router';
import { HttpClient } from '@angular/common/http';

export interface Visit {
  visitId: number;
  code: string;
  description: string;
  durationMin: number;
  price: number;
  flagDeleted: boolean;
}

export const adminBookingsResolver: ResolveFn<Visit[]> = () => {
  const http = inject(HttpClient);
  return http.get<Visit[]>('http://localhost:8081/visits/all');
};
