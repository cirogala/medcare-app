import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

import { AuthService } from '../../core/auth/auth.service';
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSnackBarModule
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {

  loading = false;
  error: string | null = null;
  resetLoading = false;
  showReset = false;
  resetForm!: FormGroup;
  form!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.form = this.fb.group({
      username: ['', Validators.required],
      password: ['', Validators.required]
    });
    this.resetForm = this.fb.group({
      identifier: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.loading = true;
    this.error = null;

    const { username, password } = this.form.value;

    this.authService.login({
      username: username!,
      password: password!
    }).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.loading = false;
        this.error = 'Credenziali non valide';
      }
    });
  }

  toggleReset(): void {
    this.showReset = !this.showReset;
    this.error = null;
    this.resetForm.reset();
  }

  submitReset(): void {
    if (this.resetForm.invalid || this.resetLoading) return;

    this.resetLoading = true;
    const { identifier } = this.resetForm.value;

    this.authService.resetPassword({ identifier: identifier! }).subscribe({
      next: () => {
        this.resetLoading = false;
        this.showReset = false;
        this.snackBar.open(
          'Ti abbiamo inviato una nuova password via email.',
          'OK',
          {
            duration: 4000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['toast', 'toast-center', 'toast-success']
          }
        );
      },
      error: () => {
        this.resetLoading = false;
        this.snackBar.open(
          'Impossibile completare la richiesta. Verifica username o email.',
          'Chiudi',
          {
            duration: 4000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['toast', 'toast-center', 'toast-error']
          }
        );
      }
    });
  }
}
