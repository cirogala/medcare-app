import { Component, OnInit, ChangeDetectorRef, ElementRef, ViewChild, NgZone } from '@angular/core';
import { finalize, timeout } from 'rxjs/operators';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

import { User } from './user.model';
import { UserService } from './user.service';
import { CreateInternalUserRequest } from './internal-users-create.model';
import { UpdateInternalUserRequest } from './internal-user-update.model';
import { DocRepoService, DocumentMetadata } from '../../../referti/doc-repo.service';
import { DoctorService, DoctorProfile } from '../../../doctors/doctor.service';
import { PrenotationService, VisitType } from '../../../prenotations/prenotation-list/prenotation.service';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule, FormsModule, MatSnackBarModule],
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.scss']
})
export class AdminUsersComponent implements OnInit {
  @ViewChild('fascicoloSection') fascicoloSection?: ElementRef<HTMLElement>;

  users: User[] = [];
  filteredUsers: User[] = [];

  searchTerm = '';
  selectedTab: 'internal' | 'external' = 'external';
  selectedInternalUser: User | null = null;
  editingInternalUser: UpdateInternalUserRequest | null = null;
  savingInternalUser = false;
  editingInternalProfile = false;
  editingExternalUser: UpdateInternalUserRequest | null = null;
  savingExternalUser = false;

  currentPage = 1;
  pageSize = 5;

  loading = false;
  errorMsg = '';
  fascicoloLoading = false;
  fascicoloError = '';
  selectedUser: User | null = null;
  fascicoloGroups: Array<{
    doctorId: number;
    reports: DocumentMetadata[];
  }> = [];
  private doctorMap = new Map<number, DoctorProfile>();
  private visitTypeMap = new Map<number, VisitType>();

  constructor(
    private userService: UserService,
    private cdr: ChangeDetectorRef,
    private zone: NgZone,
    private snackBar: MatSnackBar,
    private docRepo: DocRepoService,
    private doctorService: DoctorService,
    private prenotationService: PrenotationService
  ) {}

  ngOnInit(): void {
    this.loadUsers();
    this.loadDoctors();
    this.loadVisitTypes();
  }

  creatingInternalUser = false;
  internalUserError = '';

  newInternalUser: CreateInternalUserRequest = {
    nome: '',
    cognome: '',
    email: '',
    isInternal: true,
    isMed: false,
    flagDeleted: 0,
    citta: '',
    codiceFiscale: '',
    indirizzo: '',
    telefono: ''
  };

  createInternalUser(): void {
    this.creatingInternalUser = true;
    this.internalUserError = '';

    this.userService.createInternalUser(this.newInternalUser)
      .subscribe({
        next: () => {
          this.creatingInternalUser = false;

          // reset form
        this.newInternalUser = {
          nome: '',
          cognome: '',
          email: '',
          isInternal: true,
          isMed: false,
          flagDeleted: 0,
          citta: '',
          codiceFiscale: '',
          indirizzo: '',
          telefono: ''
        };

          this.snackBar.open(
            'Utenza interna creata. Credenziali inviate via email.',
            'OK',
            {
              duration: 4000,
              horizontalPosition: 'center',
              verticalPosition: 'top',
              panelClass: ['toast', 'toast-center', 'toast-success']
            }
          );

          // aggiorna lista utenti
          this.loadUsers();
        },
        error: err => {
          console.error(err);
          this.creatingInternalUser = false;
          this.internalUserError = 'Errore durante la creazione dell’utenza interna';
        }
      });
  }


  // =========================
  // LOAD USERS
  // =========================
  loadUsers(): void {
    this.loading = true;
    this.errorMsg = '';

    const source$ = this.selectedTab === 'internal'
      ? this.userService.getInternalUsers()
      : this.userService.getUsers();

    source$.subscribe({
      next: users => {
        this.zone.run(() => {
          this.users = users;
          this.applyFilter();
          this.loading = false;
          this.cdr.detectChanges();
        });
      },
      error: err => {
        console.error(err);
        this.zone.run(() => {
          this.users = [];
          this.filteredUsers = [];
          this.loading = false;
          this.errorMsg = 'Errore nel caricamento degli utenti';
          this.cdr.detectChanges();
        });
      }
    });
  }

  setTab(tab: 'internal' | 'external'): void {
    if (this.selectedTab === tab) {
      return;
    }
    this.selectedTab = tab;
    this.selectedUser = null;
    this.fascicoloGroups = [];
    this.selectedInternalUser = null;
    this.editingInternalUser = null;
    this.searchTerm = '';
    this.loadUsers();
  }

  // =========================
  // FILTER
  // =========================
  applyFilter(): void {
    const term = this.searchTerm.trim().toLowerCase();

    this.filteredUsers = this.users.filter(u =>
      u.nome.toLowerCase().includes(term) ||
      u.cognome.toLowerCase().includes(term)
    );

    this.currentPage = 1;
  }

  // =========================
  // PAGINATION
  // =========================
  get paginatedUsers(): User[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredUsers.slice(start, start + this.pageSize);
  }

  get totalPages(): number {
    return Math.ceil(this.filteredUsers.length / this.pageSize);
  }

  get pageOptions(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  // =========================
  // ACTIONS
  // =========================
  openFascicolo(user: User): void {
    if (this.selectedUser?.userId === user.userId) {
      this.selectedUser = null;
      this.fascicoloGroups = [];
      this.editingExternalUser = null;
      return;
    }

    this.selectedUser = user;
    this.editingExternalUser = null;
    this.fascicoloLoading = true;
    this.fascicoloError = '';
    this.fascicoloGroups = [];
    this.cdr.detectChanges();
    setTimeout(() => {
      this.fascicoloSection?.nativeElement.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });

    this.docRepo.search({ patientId: user.userId }).subscribe({
      next: docs => {
        const grouped = new Map<number, DocumentMetadata[]>();
        docs.forEach(doc => {
          const list = grouped.get(doc.doctorId) ?? [];
          list.push(doc);
          grouped.set(doc.doctorId, list);
        });

        this.fascicoloGroups = Array.from(grouped.entries()).map(([doctorId, reports]) => ({
          doctorId,
          reports: reports.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
        }));

        this.fascicoloLoading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.fascicoloLoading = false;
        this.fascicoloError = 'Errore nel recupero del fascicolo.';
        this.cdr.detectChanges();
      }
    });
  }

  enableExternalEdit(): void {
    if (!this.selectedUser) {
      return;
    }
    this.editingExternalUser = {
      nome: this.selectedUser.nome,
      cognome: this.selectedUser.cognome,
      email: this.selectedUser.email,
      citta: this.selectedUser.citta,
      indirizzo: this.selectedUser.indirizzo,
      codiceFiscale: this.selectedUser.codiceFiscale,
      telefono: this.selectedUser.telefono ?? ''
    };
  }

  cancelExternalEdit(): void {
    this.editingExternalUser = null;
  }

  saveExternalProfile(): void {
    if (!this.selectedUser || !this.editingExternalUser || this.savingExternalUser) {
      return;
    }

    this.savingExternalUser = true;
    this.userService.updateExternalUser(this.selectedUser.userId, this.editingExternalUser)
      .pipe(
        timeout(10000),
        finalize(() => {
          this.savingExternalUser = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: updated => {
          this.selectedUser = updated;
          this.users = this.users.map(u => u.userId === updated.userId ? updated : u);
          this.applyFilter();
          this.editingExternalUser = null;
          this.snackBar.open('Profilo aggiornato.', 'OK', {
            duration: 3000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['toast', 'toast-center', 'toast-success']
          });
        },
        error: () => {
          this.snackBar.open('Errore durante il salvataggio.', 'Chiudi', {
            duration: 3500,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['toast', 'toast-center', 'toast-error']
          });
        }
      });
  }

  openInternalProfile(user: User): void {
    if (this.selectedInternalUser?.userId === user.userId) {
      this.selectedInternalUser = null;
      this.editingInternalUser = null;
      this.editingInternalProfile = false;
      return;
    }

    this.selectedInternalUser = user;
    this.editingInternalProfile = false;
    this.editingInternalUser = {
      nome: user.nome,
      cognome: user.cognome,
      email: user.email,
      citta: user.citta,
      indirizzo: user.indirizzo,
      codiceFiscale: user.codiceFiscale,
      telefono: user.telefono ?? ''
    };
  }

  enableInternalEdit(): void {
    if (!this.selectedInternalUser || !this.editingInternalUser) {
      return;
    }
    this.editingInternalProfile = true;
  }

  saveInternalProfile(): void {
    if (!this.selectedInternalUser || !this.editingInternalUser || this.savingInternalUser) {
      return;
    }

    this.savingInternalUser = true;
    this.userService.updateInternalUser(this.selectedInternalUser.userId, this.editingInternalUser)
      .pipe(
        timeout(10000),
        finalize(() => {
          this.savingInternalUser = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: updated => {
          this.selectedInternalUser = updated;
          this.users = this.users.map(u => u.userId === updated.userId ? updated : u);
          this.applyFilter();
          this.editingInternalProfile = false;
          this.snackBar.open('Profilo aggiornato.', 'OK', {
            duration: 3000,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['toast', 'toast-center', 'toast-success']
          });
        },
        error: () => {
          this.snackBar.open('Errore durante il salvataggio.', 'Chiudi', {
            duration: 3500,
            horizontalPosition: 'center',
            verticalPosition: 'top',
            panelClass: ['toast', 'toast-center', 'toast-error']
          });
        }
      });
  }

  cancelInternalProfile(): void {
    this.editingInternalProfile = false;
    if (this.selectedInternalUser) {
      this.editingInternalUser = {
        nome: this.selectedInternalUser.nome,
        cognome: this.selectedInternalUser.cognome,
        email: this.selectedInternalUser.email,
        citta: this.selectedInternalUser.citta,
        indirizzo: this.selectedInternalUser.indirizzo,
        codiceFiscale: this.selectedInternalUser.codiceFiscale,
        telefono: this.selectedInternalUser.telefono ?? ''
      };
    }
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

  downloadReport(report: DocumentMetadata): void {
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

  loadDoctors(): void {
    this.doctorService.getDoctors(true).subscribe({
      next: doctors => {
        this.doctorMap = new Map(doctors.map(d => [d.userId, d]));
      }
    });
  }

  loadVisitTypes(): void {
    this.prenotationService.getVisitTypes().subscribe({
      next: visits => {
        this.visitTypeMap = new Map(
          visits.filter(v => !v.flagDeleted).map(v => [v.visitId, v])
        );
      }
    });
  }
}
