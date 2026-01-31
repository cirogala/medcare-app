import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { finalize, timeout } from 'rxjs/operators';
import { PrenotationService, VisitType, VisitSlot } from '../../prenotations/prenotation-list/prenotation.service';
import { Prenotation } from '../../prenotations/prenotation-list/prenotation.model';
import { AuthService } from '../../../core/auth/auth.service';
import { UserDirectoryService, UserProfileSummary } from '../../../core/users/user-directory.service';
import { DocRepoService, DocumentMetadata } from '../../referti/doc-repo.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  standalone: true,
  templateUrl: './med-dashboard.component.html',
  styleUrls: ['./med-dashboard.component.scss'],
  imports: [CommonModule, DatePipe, FormsModule, MatSnackBarModule]
})
export class MedDashboardComponent implements OnInit {

  fullName = '';
  today = new Date();

  todayVisits = 0;
  weekVisits = 0;
  pendingReports = 0;

  appointments: Array<{
    patientName: string;
    exam: string;
    date: Date;
    completed: boolean;
    status: string;
    prenotationId: number;
    patientId?: number;
    doctorId: number;
    visitTypeId: number;
  }> = [];
  showCompletedAppointments = false;
  activeAppointmentsCount = 0;
  completedAppointmentsCount = 0;
  private reportMap = new Map<number, DocumentMetadata>();
  private anamnesiMap = new Map<number, DocumentMetadata>();

  agendaDate = this.todayLocalDate();
  doctorSlots: VisitSlot[] = [];
  agendaLoading = false;
  agendaError = '';
  showAllSlots = false;
  showAvailableOnly = false;
  readonly slotsLimit = 12;

  private visitTypeMap = new Map<number, VisitType>();
  private patientMap = new Map<number, UserProfileSummary>();
  private rawPrenotations: Prenotation[] = [];
  showAnamnesiForm = false;
  submittingAnamnesi = false;
  showAnamnesiViewer = false;
  anamnesiLoading = false;
  anamnesiContent = '';
  anamnesiParsed: {
    motivo?: string;
    terapie?: string;
    allergie?: string;
    note?: string;
    patientName?: string;
    date?: string;
    exam?: string;
  } | null = null;
  selectedAnamnesi: any | null = null;
  anamnesiForm = {
    motivo: '',
    terapie: '',
    allergie: '',
    note: ''
  };

  constructor(
    private prenotationService: PrenotationService,
    private auth: AuthService,
    private userDirectory: UserDirectoryService,
    private docRepo: DocRepoService,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) {}

  private todayLocalDate(): string {
    const now = new Date();
    const offsetMs = now.getTimezoneOffset() * 60000;
    return new Date(now.getTime() - offsetMs).toISOString().slice(0, 10);
  }

  ngOnInit(): void {
    this.fullName = this.auth.fullName || '';
    this.auth.userChanged$.subscribe(() => {
      this.zone.run(() => {
        this.fullName = this.auth.fullName || '';
        this.loadReports();
        this.cdr.detectChanges();
      });
    });
    this.loadVisitTypes();
    this.loadPatients();
    this.loadPrenotations();
    this.loadDoctorSlots();
    this.loadReports();
  }

  private loadVisitTypes(): void {
    this.prenotationService.getVisitTypes().subscribe({
      next: visits => {
        this.zone.run(() => {
          this.visitTypeMap = new Map(
            visits.filter(v => !v.flagDeleted).map(v => [v.visitId, v])
          );
          this.buildAppointments();
          this.cdr.detectChanges();
        });
      }
    });
  }

  private loadPrenotations(): void {
    this.prenotationService.getMyPrenotationsDoctor().subscribe({
      next: data => {
        this.zone.run(() => {
          this.rawPrenotations = data;
          this.buildAppointments();
          this.cdr.detectChanges();
        });
      }
    });
  }

  private loadReports(): void {
    const doctorId = this.auth.getDecodedToken()?.userId;
    if (!doctorId) return;

    this.docRepo.search({ doctorId }).subscribe({
      next: docs => {
        this.zone.run(() => {
          this.reportMap.clear();
          this.anamnesiMap.clear();
          docs.forEach(doc => {
            const isAnamnesi = (doc.filename || '').startsWith('anamnesi-');
            const targetMap = isAnamnesi ? this.anamnesiMap : this.reportMap;
            const existing = targetMap.get(doc.prenotationId);
            if (!existing) {
              targetMap.set(doc.prenotationId, doc);
              return;
            }
            const existingTime = new Date(existing.createdAt).getTime();
            const currentTime = new Date(doc.createdAt).getTime();
            if (currentTime > existingTime) {
              targetMap.set(doc.prenotationId, doc);
            }
          });
          this.cdr.detectChanges();
        });
      }
    });
  }

  loadDoctorSlots(): void {
    this.agendaError = '';
    this.agendaLoading = true;
    this.prenotationService.getDoctorSlots(this.agendaDate).subscribe({
      next: slots => {
        this.zone.run(() => {
          this.doctorSlots = slots;
          this.showAllSlots = false;
          this.agendaLoading = false;
          this.cdr.detectChanges();
        });
      },
      error: () => {
        this.zone.run(() => {
          this.agendaError = 'Errore nel recupero dell’agenda.';
          this.agendaLoading = false;
          this.cdr.detectChanges();
        });
      }
    });
  }

  toggleSlot(slot: VisitSlot): void {
    this.prenotationService.updateSlotAvailability(slot.slotId, !slot.available).subscribe({
      next: () => {
        this.zone.run(() => {
          slot.available = !slot.available;
          this.cdr.detectChanges();
        });
      }
    });
  }

  get filteredSlots(): VisitSlot[] {
    const sorted = [...this.doctorSlots].sort((a, b) => a.startTime.localeCompare(b.startTime));
    return this.showAvailableOnly ? sorted.filter(slot => slot.available) : sorted;
  }

  get visibleSlots(): VisitSlot[] {
    return this.showAllSlots ? this.filteredSlots : this.filteredSlots.slice(0, this.slotsLimit);
  }

  get hiddenSlotCount(): number {
    return Math.max(0, this.filteredSlots.length - this.visibleSlots.length);
  }

  private loadPatients(): void {
    this.userDirectory.getPatients().subscribe({
      next: users => {
        this.zone.run(() => {
          this.patientMap = new Map(users.map(u => [u.userId, u]));
          this.buildAppointments();
          this.cdr.detectChanges();
        });
      }
    });
  }

  private patientName(userId?: number): string {
    if (!userId) return 'Paziente';
    const patient = this.patientMap.get(userId);
    if (!patient) return `Paziente #${userId}`;
    const fullName = `${patient.nome ?? ''} ${patient.cognome ?? ''}`.trim();
    return fullName || patient.username || `Paziente #${userId}`;
  }

  buildAppointments(): void {
    const active = this.rawPrenotations.filter(p => p.status !== 'CANCELLED');
    const mapped = active.map(p => ({
      patientName: this.patientName(p.userId),
      exam: this.visitTypeMap.get(p.visitTypeId)?.description ?? `Visita #${p.visitTypeId}`,
      date: new Date(`${p.date}T${p.slotTime}`),
      completed: p.status === 'COMPLETED',
      status: p.status,
      prenotationId: p.prenotationId,
      patientId: p.userId,
      doctorId: p.doctorId,
      visitTypeId: p.visitTypeId
    }));
    this.activeAppointmentsCount = mapped.filter(a => a.status === 'ACTIVE').length;
    this.completedAppointmentsCount = mapped.filter(a => a.status === 'COMPLETED').length;
    this.appointments = this.showCompletedAppointments
      ? mapped.filter(a => a.status === 'COMPLETED')
      : mapped.filter(a => a.status === 'ACTIVE');

    const todayKey = this.toDateKey(new Date());
    this.todayVisits = active.filter(p => this.toDateKey(p.date) === todayKey).length;

    const weekRange = this.getWeekRange(new Date());
    this.weekVisits = active.filter(p => {
      const d = new Date(p.date);
      return d >= weekRange.start && d <= weekRange.end;
    }).length;

    const now = new Date();
    this.pendingReports = active.filter(p => {
      if (p.status !== 'ACTIVE') return false;
      const visitDateTime = new Date(`${p.date}T${p.slotTime}`);
      return visitDateTime < now;
    }).length;
  }

  private toDateKey(date: Date | string): string {
    const d = typeof date === 'string' ? new Date(date) : date;
    const offsetMs = d.getTimezoneOffset() * 60000;
    return new Date(d.getTime() - offsetMs).toISOString().slice(0, 10);
  }

  private getWeekRange(date: Date): { start: Date; end: Date } {
    const day = date.getDay();
    const diffToMonday = day === 0 ? -6 : 1 - day;
    const start = new Date(date);
    start.setDate(date.getDate() + diffToMonday);
    start.setHours(0, 0, 0, 0);
    const end = new Date(start);
    end.setDate(start.getDate() + 6);
    end.setHours(23, 59, 59, 999);
    return { start, end };
  }

  openUpload(appointment: any) {
    const input = document.createElement('input');
    input.type = 'file';
    input.accept = 'application/pdf,image/*';
    input.onchange = () => {
      const file = input.files?.[0];
      if (!file) return;
      this.docRepo.upload(file, {
        prenotationId: appointment.prenotationId,
        patientId: appointment.patientId,
        doctorId: appointment.doctorId,
        visitTypeId: appointment.visitTypeId
      }).subscribe({
        next: () => {
          this.prenotationService.markCompleted(appointment.prenotationId).subscribe({
            next: () => {
              this.loadPrenotations();
              this.loadReports();
              this.snackBar.open('Referto caricato con successo.', 'OK', {
                duration: 3000,
                horizontalPosition: 'center',
                verticalPosition: 'top',
                panelClass: ['toast', 'toast-center', 'toast-success']
              });
            },
            error: () => {
              this.snackBar.open('Referto caricato, ma stato non aggiornato.', 'Chiudi', {
                duration: 4000,
                horizontalPosition: 'center',
                verticalPosition: 'top',
                panelClass: ['toast', 'toast-center', 'toast-error']
              });
            }
          });
        },
        error: () => {
          this.snackBar.open('Errore durante l’upload del referto.', 'Chiudi', {
            duration: 4000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['toast', 'toast-center', 'toast-error']
          });
        }
      });
    };
    input.click();
  }

  hasReport(prenotationId: number): boolean {
    return this.reportMap.has(prenotationId);
  }

  hasAnamnesi(prenotationId: number): boolean {
    return this.anamnesiMap.has(prenotationId);
  }

  downloadReport(appointment: any): void {
    const report = this.reportMap.get(appointment.prenotationId);
    if (!report) return;
    this.docRepo.download(report.id).subscribe({
      next: blob => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = report.filename || 'referto';
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

  downloadAnamnesi(appointment: any): void {
    const report = this.anamnesiMap.get(appointment.prenotationId);
    if (!report) return;
    this.docRepo.download(report.id).subscribe({
      next: blob => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = report.filename || 'anamnesi';
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

  openAnamnesiViewer(appointment: any): void {
    const report = this.anamnesiMap.get(appointment.prenotationId);
    if (!report) return;

    this.anamnesiLoading = true;
    this.anamnesiContent = '';
    this.anamnesiParsed = null;
    this.selectedAnamnesi = appointment;
    this.showAnamnesiViewer = true;

    this.docRepo.download(report.id).pipe(
      timeout(10000)
    ).subscribe({
      next: (blob: Blob) => {
        const reader = new FileReader();
        reader.onloadend = () => {
          this.zone.run(() => {
            this.anamnesiContent = typeof reader.result === 'string' ? reader.result : '';
            this.anamnesiParsed = this.parseAnamnesi(this.anamnesiContent);
            this.anamnesiLoading = false;
            this.cdr.detectChanges();
          });
        };
        reader.onerror = () => {
          this.zone.run(() => {
            this.anamnesiLoading = false;
            this.snackBar.open('Errore nel caricamento anamnesi.', 'Chiudi', {
              duration: 3500,
              horizontalPosition: 'center',
              verticalPosition: 'top',
              panelClass: ['toast', 'toast-center', 'toast-error']
            });
            this.cdr.detectChanges();
          });
        };
        reader.readAsText(blob);
      },
      error: () => {
        this.zone.run(() => {
          this.anamnesiLoading = false;
          this.snackBar.open('Errore nel caricamento anamnesi.', 'Chiudi', {
            duration: 3500,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['toast', 'toast-center', 'toast-error']
          });
          this.cdr.detectChanges();
        });
      }
    });
  }

  closeAnamnesiViewer(): void {
    this.showAnamnesiViewer = false;
    this.anamnesiContent = '';
    this.anamnesiParsed = null;
    this.selectedAnamnesi = null;
  }

  private parseAnamnesi(raw: string): {
    motivo?: string;
    terapie?: string;
    allergie?: string;
    note?: string;
    patientName?: string;
    date?: string;
    exam?: string;
  } | null {
    try {
      const parsed = JSON.parse(raw);
      return {
        motivo: parsed.motivo,
        terapie: parsed.terapie,
        allergie: parsed.allergie,
        note: parsed.note,
        patientName: parsed.patientName,
        date: parsed.date,
        exam: parsed.exam
      };
    } catch {
      return null;
    }
  }

  openAnamnesi(appointment: any): void {
    this.zone.run(() => {
      this.selectedAnamnesi = appointment;
      this.anamnesiForm = {
        motivo: '',
        terapie: '',
        allergie: '',
        note: ''
      };
      this.showAnamnesiForm = true;
      this.cdr.detectChanges();
    });
  }

  cancelAnamnesi(): void {
    this.zone.run(() => {
      this.showAnamnesiForm = false;
      this.selectedAnamnesi = null;
      this.cdr.detectChanges();
    });
  }

  submitAnamnesi(): void {
    if (!this.selectedAnamnesi || this.submittingAnamnesi) {
      return;
    }

    this.submittingAnamnesi = true;
    const payload = {
      prenotationId: this.selectedAnamnesi.prenotationId,
      patientId: this.selectedAnamnesi.patientId,
      doctorId: this.selectedAnamnesi.doctorId,
      visitTypeId: this.selectedAnamnesi.visitTypeId,
      date: this.selectedAnamnesi.date,
      patientName: this.selectedAnamnesi.patientName,
      motivo: this.anamnesiForm.motivo,
      terapie: this.anamnesiForm.terapie,
      allergie: this.anamnesiForm.allergie,
      note: this.anamnesiForm.note
    };

    const json = JSON.stringify(payload, null, 2);
    const file = new File(
      [new Blob([json], { type: 'application/json' })],
      `anamnesi-${this.selectedAnamnesi.prenotationId}.json`,
      { type: 'application/json' }
    );

    this.docRepo.upload(file, {
      prenotationId: this.selectedAnamnesi.prenotationId,
      patientId: this.selectedAnamnesi.patientId,
      doctorId: this.selectedAnamnesi.doctorId,
      visitTypeId: this.selectedAnamnesi.visitTypeId
    }).pipe(
      finalize(() => {
        this.submittingAnamnesi = false;
        this.cdr.detectChanges();
      })
    ).subscribe({
      next: () => {
        this.zone.run(() => {
          this.showAnamnesiForm = false;
          this.selectedAnamnesi = null;
          this.loadReports();
          this.loadPrenotations();
          this.snackBar.open('Anamnesi salvata con successo.', 'OK', {
            duration: 3000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['toast', 'toast-center', 'toast-success']
          });
          this.cdr.detectChanges();
        });
      },
      error: () => {
        this.zone.run(() => {
          this.snackBar.open('Errore durante il salvataggio anamnesi.', 'Chiudi', {
            duration: 4000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['toast', 'toast-center', 'toast-error']
          });
          this.cdr.detectChanges();
        });
      }
    });
  }
}
