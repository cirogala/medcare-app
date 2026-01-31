import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Prenotation } from './prenotation.model';

export interface CreatePrenotationRequest {
  doctorId: number;
  visitTypeId: number;
  date: string;
  slotTime: string;
}

export interface VisitSlot {
  slotId: number;
  visitDate: string;
  startTime: string;
  endTime: string;
  available: boolean;
  booked?: boolean;
}

export interface VisitType {
  visitId: number;
  code: string;
  description: string;
  durationMin: number;
  price: number;
  flagDeleted: boolean;
}

@Injectable({ providedIn: 'root' })
export class PrenotationService {

  private readonly VISITS_URL = '/visits';

  constructor(private http: HttpClient) {}

  /** Prenotazioni del paziente loggato */
  getMyPrenotations(): Observable<Prenotation[]> {
    return this.http.get<Prenotation[]>(
      `${this.VISITS_URL}/my`,
      { headers: this.buildHeaders() }
    );
  }

  /** Prenotazioni del medico loggato */
  getMyPrenotationsDoctor(): Observable<Prenotation[]> {
    return this.http.get<Prenotation[]>(
      `${this.VISITS_URL}/doctor/my`,
      { headers: this.buildHeaders() }
    );
  }

  /** Cancella prenotazione */
  cancel(prenotationId: number): Observable<void> {
    return this.http.delete<void>(
      `${this.VISITS_URL}/delete/${prenotationId}`,
      { headers: this.buildHeaders() }
    );
  }

  /** Segna prenotazione completata */
  markCompleted(prenotationId: number): Observable<void> {
    return this.http.post<void>(
      `${this.VISITS_URL}/${prenotationId}/complete`,
      {},
      { headers: this.buildHeaders() }
    );
  }

  /** Crea una nuova prenotazione */
  createPrenotation(request: CreatePrenotationRequest): Observable<Prenotation> {
    return this.http.post<Prenotation>(
      `${this.VISITS_URL}/prenotations`,
      request,
      { headers: this.buildHeaders() }
    );
  }

  /** Slot disponibili per visita e data */
  getAvailableSlots(visitId: number, doctorId: number, date: string): Observable<VisitSlot[]> {
    const params = new HttpParams()
      .set('visitId', String(visitId))
      .set('doctorId', String(doctorId))
      .set('date', date);

    return this.http.get<VisitSlot[]>(
      `${this.VISITS_URL}/available`,
      { headers: this.buildHeaders(), params }
    );
  }

  /** Medici disponibili per visita, data e orario */
  getAvailableDoctors(visitId: number, date: string, slotTime: string): Observable<number[]> {
    const params = new HttpParams()
      .set('visitId', String(visitId))
      .set('date', date)
      .set('slotTime', slotTime);

    return this.http.get<number[]>(
      `${this.VISITS_URL}/available-doctors`,
      { headers: this.buildHeaders(), params }
    );
  }

  /** Slot agenda del medico loggato */
  getDoctorSlots(date: string): Observable<VisitSlot[]> {
    const params = new HttpParams().set('date', date);
    return this.http.get<VisitSlot[]>(
      `${this.VISITS_URL}/slots/doctor`,
      { headers: this.buildHeaders(), params }
    );
  }

  /** Aggiorna disponibilità slot */
  updateSlotAvailability(slotId: number, available: boolean): Observable<void> {
    const params = new HttpParams().set('available', String(available));
    return this.http.patch<void>(
      `${this.VISITS_URL}/slots/${slotId}/availability`,
      {},
      { headers: this.buildHeaders(), params }
    );
  }

  /** Tipologie di visita */
  getVisitTypes(): Observable<VisitType[]> {
    return this.http.get<VisitType[]>(
      `${this.VISITS_URL}/all`,
      { headers: this.buildHeaders() }
    );
  }

  private buildHeaders(): HttpHeaders {
    return new HttpHeaders({
      'X-Medcare-Accept': 'application/medcare.v1+json',
      'X-Medcare-KeyLogic': 'PRENOTATION',
      'X-Medcare-TransactionID': crypto.randomUUID(),
    });
  }
}
