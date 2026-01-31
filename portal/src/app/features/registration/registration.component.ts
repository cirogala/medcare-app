import { Component, OnDestroy, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, NgForm } from '@angular/forms';
import { finalize } from 'rxjs/operators';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { ExternalUserService } from './external-user.service';
import { Router } from '@angular/router';

@Component({
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatSnackBarModule
  ],
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.scss']
})
export class RegistrationComponent implements OnDestroy {

  creating = false;
  errorMsg = '';
  success = false;
  redirectSeconds = 0;
  private redirectTimer?: ReturnType<typeof setInterval>;

  newUser = {
    nome: '',
    cognome: '',
    email: '',
    citta: '',
    indirizzo: '',
    codiceFiscale: '',
    telefono: '',
    isInternal: false,
    isMed: false,
    flagDeleted: 0
  };

  constructor(
    private service: ExternalUserService,
    private snackBar: MatSnackBar,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) {}

  register(form: NgForm): void {
    if (this.creating || form.invalid) {
      return;
    }

    this.creating = true;
    this.errorMsg = '';
    this.success = false;

    this.service.createExternalUser(this.newUser)
      .pipe(
        finalize(() => {
          setTimeout(() => {
            this.creating = false;
          });
        })
      )
      .subscribe({
        next: () => {
          this.success = true;
          this.showSuccessToast();
          this.startRedirectCountdown();

          setTimeout(() => {
            form.resetForm();
          });

          setTimeout(() => {
            this.router.navigateByUrl('/');
          }, 5000);
        },
        error: (err) => {
          console.error('Errore registrazione', err);
          this.errorMsg = 'Errore durante la registrazione';
          this.showErrorToast();
        }
      });
  }

  private showSuccessToast(): void {
    this.snackBar.open(
      'Registrazione completata! Controlla la tua email 📧',
      'OK',
      {
        duration: 4000,
        horizontalPosition: 'center',
        verticalPosition: 'top',
        panelClass: ['toast', 'toast-center', 'toast-success']
      }
    );
  }

  private showErrorToast(): void {
    this.snackBar.open(
      'Errore durante la registrazione',
      'Chiudi',
      {
        duration: 5000,
        horizontalPosition: 'center',
        verticalPosition: 'top',
        panelClass: ['toast', 'toast-center', 'toast-error']
      }
    );
  }

  private startRedirectCountdown(): void {
    this.redirectSeconds = 5;
    if (this.redirectTimer) {
      clearInterval(this.redirectTimer);
    }
    this.redirectTimer = setInterval(() => {
      this.zone.run(() => {
        this.redirectSeconds = Math.max(0, this.redirectSeconds - 1);
        this.cdr.detectChanges();
      });
    }, 1000);
  }

  ngOnDestroy(): void {
    if (this.redirectTimer) {
      clearInterval(this.redirectTimer);
    }
  }
}
