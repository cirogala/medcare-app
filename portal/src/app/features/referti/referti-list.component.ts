import { Component, OnInit, OnDestroy, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../core/auth/auth.service';
import { DocRepoService, DocumentMetadata } from './doc-repo.service';
import { DoctorService, DoctorProfile } from '../doctors/doctor.service';
import { PrenotationService, VisitType } from '../prenotations/prenotation-list/prenotation.service';

@Component({
  selector: 'app-referti-list',
  standalone: true,
  imports: [CommonModule, DatePipe, MatButtonModule, MatSnackBarModule],
  templateUrl: './referti-list.component.html',
  styleUrls: ['./referti-list.component.scss'],
})
export class RefertiListComponent implements OnInit, OnDestroy {
  referti: DocumentMetadata[] = [];
  loading = false;
  error = '';
  doctorMap = new Map<number, DoctorProfile>();
  visitTypeMap = new Map<number, VisitType>();
  previewMap = new Map<string, SafeUrl>();
  private rawPreviewMap = new Map<string, string>();

  constructor(
    private docRepo: DocRepoService,
    private auth: AuthService,
    private doctorService: DoctorService,
    private prenotationService: PrenotationService,
    private snackBar: MatSnackBar,
    private sanitizer: DomSanitizer,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) {}

  ngOnInit(): void {
    this.loadReferti();
    this.loadDoctors();
    this.loadVisitTypes();
  }

  loadReferti(): void {
    const userId = this.auth.getDecodedToken()?.userId;
    if (!userId) {
      this.error = 'Utente non disponibile.';
      return;
    }

    this.loading = true;
    this.error = '';
    this.docRepo.search({ patientId: userId }).subscribe({
      next: docs => {
        this.zone.run(() => {
          this.referti = docs;
          this.loading = false;
          this.loadPreviews();
          this.cdr.detectChanges();
        });
      },
      error: () => {
        this.zone.run(() => {
          this.loading = false;
          this.error = 'Errore nel recupero dei referti.';
          this.cdr.detectChanges();
        });
      }
    });
  }

  private loadPreviews(): void {
    this.clearPreviews();
    this.referti
      .filter(doc => doc.contentType?.startsWith('image/'))
      .forEach(doc => {
        this.docRepo.download(doc.id).subscribe({
          next: blob => {
            this.zone.run(() => {
              const url = URL.createObjectURL(blob);
              this.rawPreviewMap.set(doc.id, url);
              this.previewMap.set(doc.id, this.sanitizer.bypassSecurityTrustUrl(url));
              this.cdr.detectChanges();
            });
          }
        });
      });
  }

  previewUrl(id: string): SafeUrl | null {
    return this.previewMap.get(id) ?? null;
  }

  private clearPreviews(): void {
    this.rawPreviewMap.forEach((url) => {
      if (url.startsWith('blob:')) {
        URL.revokeObjectURL(url);
      }
    });
    this.previewMap.clear();
    this.rawPreviewMap.clear();
  }

  ngOnDestroy(): void {
    this.clearPreviews();
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

  loadVisitTypes(): void {
    this.prenotationService.getVisitTypes().subscribe({
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

  doctorName(doctorId: number): string {
    const doctor = this.doctorMap.get(doctorId);
    if (!doctor) return `Medico #${doctorId}`;
    const fullName = `${doctor.nome ?? ''} ${doctor.cognome ?? ''}`.trim();
    return fullName || doctor.username || `Medico #${doctorId}`;
  }

  visitLabel(visitTypeId: number): string {
    const visit = this.visitTypeMap.get(visitTypeId);
    return visit?.description ?? `Tipo visita #${visitTypeId}`;
  }

  download(referto: DocumentMetadata): void {
    this.docRepo.download(referto.id).subscribe({
      next: blob => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = referto.filename || 'referto';
        link.click();
        window.URL.revokeObjectURL(url);
        this.snackBar.open('Download avviato.', 'OK', {
          duration: 2500,
          horizontalPosition: 'center',
          verticalPosition: 'top',
          panelClass: ['toast', 'toast-center', 'toast-success']
        });
      },
      error: () => {
        this.snackBar.open('Errore durante il download.', 'Chiudi', {
          duration: 3500,
          horizontalPosition: 'center',
          verticalPosition: 'top',
          panelClass: ['toast', 'toast-center', 'toast-error']
        });
      }
    });
  }
}
