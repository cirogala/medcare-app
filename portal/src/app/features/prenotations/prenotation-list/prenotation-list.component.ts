import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PrenotationService, VisitType } from './prenotation.service';
import { Prenotation } from './prenotation.model';
import { DoctorService, DoctorProfile } from '../../doctors/doctor.service';
import { AuthService } from '../../../core/auth/auth.service';
import { UserDirectoryService, UserProfileSummary } from '../../../core/users/user-directory.service';

import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';


@Component({
  standalone: true,
  selector: 'app-prenotation-list',
  imports: [CommonModule, RouterModule, MatCardModule, MatProgressBarModule, MatButtonModule, MatSnackBarModule],
  templateUrl: './prenotation-list.component.html',
  styleUrls: ['./prenotation-list.component.scss'],
})
export class PrenotationListComponent implements OnInit {

  prenotations: Prenotation[] = [];
  loading = false;
  error = false;
  showCompleted = false;

  visitTypeMap = new Map<number, VisitType>();
  doctorMap = new Map<number, DoctorProfile>();
  patientMap = new Map<number, UserProfileSummary>();

  constructor(
    private service: PrenotationService,
    private doctorService: DoctorService,
    private auth: AuthService,
    private userDirectory: UserDirectoryService,
    private cdr: ChangeDetectorRef,
    private zone: NgZone,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.load();
    this.loadVisitTypes();
    this.loadDoctors();
    this.loadPatients();
  }

  load(): void {
    this.loading = true;
    const request$ = this.auth.role === 'MEDICO'
      ? this.service.getMyPrenotationsDoctor()
      : this.service.getMyPrenotations();

    request$.subscribe({
      next: (data) => {
        this.zone.run(() => {
          this.prenotations = data;
          this.loading = false;
          this.cdr.detectChanges();
        });
      },
      error: () => {
        this.zone.run(() => {
          this.error = true;
          this.loading = false;
          this.cdr.detectChanges();
        });
      }
    });
  }

  loadVisitTypes(): void {
    this.service.getVisitTypes().subscribe({
      next: visits => {
        this.zone.run(() => {
          this.visitTypeMap = new Map(
            visits.filter(v => !v.flagDeleted).map(v => [v.visitId, v])
          );
          this.cdr.detectChanges();
        });
      }
    });
  }

  loadDoctors(): void {
    this.doctorService.getDoctors(true).subscribe({
      next: doctors => {
        this.zone.run(() => {
          this.doctorMap = new Map(doctors.map(d => [d.userId, d]));
          this.cdr.detectChanges();
        });
      }
    });
  }

  loadPatients(): void {
    this.userDirectory.getPatients().subscribe({
      next: users => {
        this.zone.run(() => {
          this.patientMap = new Map(users.map(u => [u.userId, u]));
          this.cdr.detectChanges();
        });
      }
    });
  }

  doctorName(doctorId: number): string {
    const doctor = this.doctorMap.get(doctorId);
    if (!doctor) return `Medico #${doctorId}`;
    const fullName = `${doctor.nome ?? ''} ${doctor.cognome ?? ''}`.trim();
    return fullName || doctor.username || `Medico #${doctorId}`;
  }

  doctorSpecialization(doctorId: number): string {
    const doctor = this.doctorMap.get(doctorId);
    return doctor?.typeDoctor ?? 'Specializzazione non disponibile';
  }

  patientName(userId?: number): string {
    if (!userId) return 'Paziente non disponibile';
    const patient = this.patientMap.get(userId);
    if (!patient) return `Paziente #${userId}`;
    const fullName = `${patient.nome ?? ''} ${patient.cognome ?? ''}`.trim();
    return fullName || patient.username || `Paziente #${userId}`;
  }

  visitLabel(visitTypeId: number): string {
    const visit = this.visitTypeMap.get(visitTypeId);
    return visit?.description ?? `Tipo visita #${visitTypeId}`;
  }

  appointmentDate(p: Prenotation): Date | null {
    if (!p.date || !p.slotTime) return null;
    return new Date(`${p.date}T${p.slotTime}`);
  }

  visitPrice(visitTypeId: number): string {
    const visit = this.visitTypeMap.get(visitTypeId);
    if (!visit) return 'Prezzo non disponibile';
    return `€ ${visit.price.toFixed(2)}`;
  }

  visitDuration(visitTypeId: number): string {
    const visit = this.visitTypeMap.get(visitTypeId);
    if (!visit) return 'Durata non disponibile';
    return `${visit.durationMin} min`;
  }

  cancel(id: number): void {
    this.snackBar.open('Vuoi annullare la prenotazione?', 'Conferma', {
      duration: 5000,
      panelClass: ['toast', 'toast-center', 'toast-error'],
      horizontalPosition: 'center',
      verticalPosition: 'top'
    }).onAction().subscribe(() => {
      this.service.cancel(id).subscribe({
        next: () => {
          this.load();
          this.snackBar.open('Prenotazione annullata.', 'OK', {
            duration: 3000,
            panelClass: ['toast', 'toast-center', 'toast-success'],
            horizontalPosition: 'center',
            verticalPosition: 'top'
          });
        },
        error: () => {
          this.snackBar.open('Errore durante l’annullamento.', 'Chiudi', {
            duration: 4000,
            panelClass: ['toast', 'toast-center', 'toast-error'],
            horizontalPosition: 'center',
            verticalPosition: 'top'
          });
        }
      });
    });
  }

  get filteredPrenotations(): Prenotation[] {
    if (this.showCompleted) {
      return this.prenotations.filter(p => p.status === 'COMPLETED');
    }
    return this.prenotations.filter(p => p.status === 'ACTIVE');
  }

  get activePrenotations(): Prenotation[] {
    return this.prenotations.filter(p => p.status !== 'CANCELLED');
  }

  badgeClass(status: string): string {
    return `status ${status.toLowerCase()}`;
  }

  formatStatus(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'Attiva';
      case 'COMPLETED':
        return 'Completata';
      case 'CANCELLED':
        return 'Annullata';
      default:
        return status;
    }
  }
}
