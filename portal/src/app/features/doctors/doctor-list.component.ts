import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DoctorService, DoctorProfile } from './doctor.service';
import { PrenotationService, VisitType } from '../prenotations/prenotation-list/prenotation.service';
import { RouterModule } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { finalize } from 'rxjs/operators';

@Component({
  standalone: true,
  selector: 'app-doctor-list',
  imports: [CommonModule, FormsModule, RouterModule, MatSnackBarModule],
  templateUrl: './doctor-list.component.html',
  styleUrls: ['./doctor-list.component.scss'],
})
export class DoctorListComponent implements OnInit {

  doctors: DoctorProfile[] = [];
  loading = false;
  error = false;
  creating = false;
  createError = '';
  createSuccess = '';
  visitTypeId: number | null = null;
  bookingDate = '';
  bookingTime = '';
  selectedDoctorId: number | null = null;
  selectedDoctorName = '';
  availableDoctorIds = new Set<number>();
  slotsLoading = false;
  slotsError = '';
  visitTypes: VisitType[] = [];
  visitsLoading = false;
  visitsError = '';
  readonly timeOptions = [
    '09:00:00',
    '10:00:00',
    '11:00:00',
    '12:00:00',
    '13:00:00',
    '14:00:00',
    '15:00:00',
    '16:00:00',
    '17:00:00'
  ];

  constructor(
    private doctorService: DoctorService,
    private prenotationService: PrenotationService,
    private cdr: ChangeDetectorRef,
    private zone: NgZone,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.load();
    this.loadVisitTypes();
  }

  load(): void {
    this.loading = true;
    this.error = false;
    this.doctorService.getDoctors(true).subscribe({
      next: doctors => {
        this.zone.run(() => {
          this.doctors = doctors;
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
    this.visitsLoading = true;
    this.visitsError = '';
    this.prenotationService.getVisitTypes().subscribe({
      next: visits => {
        this.zone.run(() => {
          this.visitTypes = visits.filter(v => !v.flagDeleted);
          this.visitsLoading = false;
          this.cdr.detectChanges();
        });
      },
      error: () => {
        this.zone.run(() => {
          this.visitsError = 'Errore nel recupero delle tipologie di visita.';
          this.visitsLoading = false;
          this.cdr.detectChanges();
        });
      }
    });
  }

  onVisitTypeChange(value: number | null): void {
    this.createSuccess = '';
    this.createError = '';
    this.visitTypeId = value;
    if (this.selectedDoctorId && !this.filteredDoctors.some(d => d.userId === this.selectedDoctorId)) {
      this.selectedDoctorId = null;
      this.selectedDoctorName = '';
      this.availableDoctorIds.clear();
      this.bookingTime = '';
    }

    this.loadAvailableDoctors();
  }

  onDateChange(value: string): void {
    this.createSuccess = '';
    this.createError = '';
    this.bookingDate = value;
    this.loadAvailableDoctors();
  }

  onTimeChange(value: string): void {
    this.createSuccess = '';
    this.createError = '';
    this.bookingTime = value;
    this.loadAvailableDoctors();
  }

  loadAvailableDoctors(): void {
    this.slotsError = '';
    this.availableDoctorIds.clear();

    if (!this.visitTypeId || !this.bookingDate || !this.bookingTime) {
      this.zone.run(() => {
        this.slotsLoading = false;
        this.cdr.detectChanges();
      });
      return;
    }

    this.slotsLoading = true;
    this.prenotationService.getAvailableDoctors(
      this.visitTypeId,
      this.bookingDate,
      this.bookingTime
    )
      .pipe(finalize(() => {
        this.zone.run(() => {
          this.slotsLoading = false;
          this.cdr.detectChanges();
        });
      }))
      .subscribe({
        next: doctorIds => {
          this.zone.run(() => {
            this.availableDoctorIds = new Set(doctorIds);
            if (this.selectedDoctorId && !this.availableDoctorIds.has(this.selectedDoctorId)) {
              this.selectedDoctorId = null;
              this.selectedDoctorName = '';
            }
            this.cdr.detectChanges();
          });
        },
        error: () => {
          this.zone.run(() => {
            this.availableDoctorIds.clear();
            this.slotsError = 'Errore nel recupero dei medici disponibili.';
            this.cdr.detectChanges();
          });
        }
      });
  }

  createPrenotation(): void {
    if (!this.visitTypeId || !this.bookingDate || !this.bookingTime || !this.selectedDoctorId) {
      this.createError = 'Compila tipo visita, data e orario.';
      this.createSuccess = '';
      return;
    }

    this.creating = true;
    this.createError = '';
    this.createSuccess = '';

    this.prenotationService.createPrenotation({
      doctorId: this.selectedDoctorId,
      visitTypeId: this.visitTypeId,
      date: this.bookingDate,
      slotTime: this.bookingTime
    })
      .pipe(finalize(() => {
        this.zone.run(() => {
          this.creating = false;
          this.cdr.detectChanges();
        });
      }))
      .subscribe({
        next: () => {
          this.zone.run(() => {
            this.createSuccess = '';
            this.loadAvailableDoctors();
            this.snackBar.open('Prenotazione creata con successo.', 'OK', {
              duration: 3000,
              panelClass: ['toast', 'toast-center', 'toast-success'],
              horizontalPosition: 'center',
              verticalPosition: 'top'
            });
            this.cdr.detectChanges();
          });
        },
        error: () => {
          this.zone.run(() => {
            this.createError = '';
            this.snackBar.open('Errore durante la prenotazione.', 'Chiudi', {
              duration: 4000,
              panelClass: ['toast', 'toast-center', 'toast-error'],
              horizontalPosition: 'center',
              verticalPosition: 'top'
            });
            this.cdr.detectChanges();
          });
        }
      });
  }

  fullName(doctor: DoctorProfile): string {
    return `${doctor.nome ?? ''} ${doctor.cognome ?? ''}`.trim();
  }

  selectDoctor(doctor: DoctorProfile): void {
    this.selectedDoctorId = doctor.userId;
    this.selectedDoctorName = this.fullName(doctor) || doctor.username || `Medico #${doctor.userId}`;
  }

  get selectedVisitType(): VisitType | undefined {
    if (!this.visitTypeId) return undefined;
    return this.visitTypes.find(v => v.visitId === this.visitTypeId);
  }

  get filteredDoctors(): DoctorProfile[] {
    const selected = this.selectedVisitType?.code?.toUpperCase();
    if (!selected) {
      return [];
    }

    const bySpecialization = this.doctors.filter(
      doctor => (doctor.typeDoctor ?? '').toUpperCase() === selected
    );

    if (!this.bookingDate || !this.bookingTime) {
      return [];
    }

    return bySpecialization.filter(doctor => this.availableDoctorIds.has(doctor.userId));
  }

}
