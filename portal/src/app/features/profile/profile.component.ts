import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../../core/auth/auth.service';
import { UserProfile } from './user-profile.model';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { finalize } from 'rxjs/operators';


@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule, MatSnackBarModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.scss']
})
export class ProfileComponent implements OnInit {

  profile?: UserProfile;
  editableProfile?: UserProfile;
  loading = true;
  error = false;
  editing = false;
  saving = false;

   constructor(
    private http: HttpClient,
    private auth: AuthService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) {}

ngOnInit(): void {
  this.auth.getMyInfo().subscribe({
    next: data => {
      this.profile = data;
      this.editableProfile = { ...data };
      this.loading = false;
      this.cdr.detectChanges();
    },
    error: () => {
      this.error = true;
      this.loading = false;
      this.cdr.detectChanges();
    }
  });
}

  startEdit(): void {
    if (!this.profile) {
      return;
    }
    this.editableProfile = { ...this.profile };
    this.editing = true;
  }

  cancelEdit(): void {
    this.editableProfile = this.profile ? { ...this.profile } : undefined;
    this.editing = false;
  }

  save(): void {
    if (!this.profile || !this.editableProfile || this.saving) {
      return;
    }

    const role = this.auth.role;
    const endpoint = role === 'PAZIENTE'
      ? `/profiling/users/external/${this.profile.userId}`
      : `/profiling/users/internal/${this.profile.userId}`;

    const payload = {
      nome: this.editableProfile.nome,
      cognome: this.editableProfile.cognome,
      email: this.editableProfile.email,
      citta: this.editableProfile.citta,
      indirizzo: this.editableProfile.indirizzo,
      codiceFiscale: this.editableProfile.codiceFiscale,
      telefono: this.editableProfile.telefono ?? ''
    };

    this.saving = true;
    this.http.put<UserProfile>(endpoint, payload)
      .pipe(finalize(() => {
        this.zone.run(() => {
          this.saving = false;
          this.cdr.detectChanges();
        });
      }))
      .subscribe({
        next: updated => {
          this.zone.run(() => {
            this.profile = updated;
            this.editableProfile = { ...updated };
            this.editing = false;
            this.auth.getMyInfo().subscribe({ next: () => {}, error: () => {} });
            this.snackBar.open('Dati aggiornati.', 'OK', {
              duration: 2500,
              panelClass: ['toast', 'toast-center', 'toast-success']
            });
            this.cdr.detectChanges();
          });
        },
        error: () => {
          this.zone.run(() => {
            this.snackBar.open('Errore durante il salvataggio.', 'OK', {
              duration: 3000,
              panelClass: ['toast', 'toast-center', 'toast-error']
            });
            this.cdr.detectChanges();
          });
        }
      });
  }
}
