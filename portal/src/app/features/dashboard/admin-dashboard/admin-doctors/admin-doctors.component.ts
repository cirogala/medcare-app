import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { Doctor } from './doctor.model';
import { CreateDoctorRequest } from './doctor-create.model';
import { ChangeDetectorRef } from '@angular/core';

import { DoctorService } from './doctor.service';
import { Router, NavigationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-admin-doctors',
  standalone: true,
  imports: [CommonModule, FormsModule, MatSnackBarModule],
  templateUrl: './admin-doctors.component.html',
  styleUrls: ['./admin-doctors.component.scss']
})
export class AdminDoctorsComponent implements OnInit {

  doctors: Doctor[] = [];
  readonly specializations = [
    { code: 'CARD', label: 'Cardiologia' },
    { code: 'ORTO', label: 'Ortopedia' },
    { code: 'DERM', label: 'Dermatologia' },
    { code: 'OCUL', label: 'Oculistica' },
    { code: 'GINE', label: 'Ginecologia' },
    { code: 'ODON', label: 'Odontoiatria' },
    { code: 'RAD', label: 'Radiologia' },
    { code: 'ECO', label: 'Ecografia' }
  ];

  newDoctor: CreateDoctorRequest = {
    nome: '',
    cognome: '',
    email: '',
    citta: '',
    indirizzo: '',
    codiceFiscale: '',
    telefono: '',
    typeDoctor: '',
    isInternal: false,
    isMed: true,
    flagDeleted: 0
  };

  creating = false;
  errorMsg = '';

  constructor(
    private doctorService: DoctorService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
  this.loadDoctors();

  this.router.events
    .pipe(filter(event => event instanceof NavigationEnd))
    .subscribe(() => {
      this.loadDoctors();
    });
  }

  loadDoctors(): void {
    this.doctorService.getDoctors(true).subscribe({
      next: doctors => {
        console.log('DOCTORS ARRIVATI', doctors);
        this.doctors = doctors;
        this.cdr.detectChanges();
      },
      error: err => {
        console.error(err);
        this.errorMsg = 'Errore nel caricamento dei medici';
        this.doctors = [];
      }
    });
  }
  createDoctor(): void {
    this.creating = true;
    this.errorMsg = '';

    this.doctorService.createDoctor(this.newDoctor)
      .pipe(finalize(() => this.creating = false))
      .subscribe({
        next: () => {
          this.loadDoctors();
          this.resetForm();

          this.snackBar.open(
            'Medico creato. Credenziali inviate via email.',
            'OK',
            {
              duration: 4000,
              horizontalPosition: 'center',
              verticalPosition: 'top',
              panelClass: ['toast', 'toast-center', 'toast-success']
            }
          );
        },
        error: err => {
          console.error(err);
          this.errorMsg = 'Errore durante la creazione del medico';
        }
      });
  }

  private resetForm(): void {
    this.newDoctor = {
      nome: '',
      cognome: '',
      email: '',
      citta: '',
      indirizzo: '',
      codiceFiscale: '',
      telefono: '',
      typeDoctor: '',
      isInternal: false,
      isMed: true,
      flagDeleted: 0
    };
  }

  specializationLabel(code?: string): string {
    if (!code) return '—';
    return this.specializations.find(spec => spec.code === code)?.label ?? code;
  }
}
