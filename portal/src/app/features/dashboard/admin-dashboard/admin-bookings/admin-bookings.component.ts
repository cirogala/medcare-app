import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { HttpClient, HttpParams } from '@angular/common/http';
import { forkJoin } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { DoctorService, DoctorProfile } from '../../../doctors/doctor.service';
import { UserDirectoryService, UserProfileSummary } from '../../../../core/users/user-directory.service';

export interface Visit {
  visitId: number;
  code: string;
  description: string;
  durationMin: number;
  price: number;
  specialization?: string;
  flagDeleted: boolean;
}

interface Prenotation {
  prenotationId: number;
  userId: number;
  doctorId: number;
  visitTypeId: number;
  slotId: number;
  date: string;
  slotTime: string;
  status: string;
}

type PaymentStatus = 'PENDING' | 'PAID' | 'REFUNDED';

interface PaymentDTO {
  prenotationId: number;
  amount: number;
  status: PaymentStatus;
}

@Component({
  selector: 'app-admin-bookings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-bookings.component.html',
  styleUrls: ['./admin-bookings.component.scss']
})
export class AdminBookingsComponent implements OnInit {

  visits: Visit[] = [];
  visitMap = new Map<number, Visit>();

  prenotations: Prenotation[] = [];
  paymentMap = new Map<number, PaymentDTO>();
  loadingPrenotations = false;
  errorPrenotations = false;
  selectedTab: 'active' | 'history' | 'pending' | 'paid' = 'active';
  currentPage = 1;
  readonly pageSize = 5;

  doctorMap = new Map<number, DoctorProfile>();
  patientMap = new Map<number, UserProfileSummary>();

  fromDate = '';
  toDate = '';
  searchId = '';

  constructor(
    private route: ActivatedRoute,
    private http: HttpClient,
    private doctorService: DoctorService,
    private userDirectory: UserDirectoryService,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) {
    console.log('[AdminBookingsComponent] COSTRUTTORE');
  }

  ngOnInit(): void {
    console.log('[AdminBookingsComponent] NGONINIT');
    this.visits = this.route.snapshot.data['visits'];
    this.visitMap = new Map(this.visits.map(v => [v.visitId, v]));

    const today = new Date();
    const startOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    const endOfMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0);
    this.fromDate = this.formatDate(startOfMonth);
    this.toDate = this.formatDate(endOfMonth);

    this.loadDoctors();
    this.loadPatients();
    this.loadPrenotations();

    console.log('[AdminBookingsComponent] VISITS', this.visits);
  }

  loadPrenotations(): void {
    this.loadingPrenotations = true;
    this.errorPrenotations = false;

    const params = new HttpParams()
      .set('from', this.fromDate)
      .set('to', this.toDate);

    forkJoin({
      prenotations: this.http.get<Prenotation[]>('/visits/admin/prenotations', { params }),
      payments: this.http.get<PaymentDTO[]>('/billing/payments', { params })
    })
      .pipe(finalize(() => {
        this.zone.run(() => {
          this.loadingPrenotations = false;
          this.cdr.detectChanges();
        });
      }))
      .subscribe({
        next: data => {
          this.zone.run(() => {
            this.prenotations = data.prenotations ?? [];
            this.paymentMap = new Map(
              (data.payments ?? []).map(p => [p.prenotationId, p])
            );
            this.cdr.detectChanges();
          });
        },
        error: () => {
          this.zone.run(() => {
            this.errorPrenotations = true;
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
      next: (users: UserProfileSummary[]) => {
        this.zone.run(() => {
          this.patientMap = new Map(users.map((u: UserProfileSummary) => [u.userId, u]));
          this.cdr.detectChanges();
        });
      }
    });
  }

  setTab(tab: 'active' | 'history' | 'pending' | 'paid'): void {
    this.selectedTab = tab;
    this.currentPage = 1;
  }

  get filteredPrenotations(): Prenotation[] {
    let base = this.prenotations;
    if (this.selectedTab === 'active') {
      base = base.filter(p => p.status === 'ACTIVE');
    } else if (this.selectedTab === 'history') {
      base = base.filter(p => p.status === 'COMPLETED' || p.status === 'CANCELLED');
    } else if (this.selectedTab === 'paid') {
      base = base.filter(p => this.paymentStatus(p.prenotationId) === 'PAID');
    } else {
      base = base.filter(p => this.paymentStatus(p.prenotationId) === 'PENDING');
    }

    const id = Number(this.searchId);
    if (this.searchId && !Number.isNaN(id)) {
      base = base.filter(p => p.prenotationId === id);
    }
    return base;
  }

  get totalPages(): number {
    return Math.max(1, Math.ceil(this.filteredPrenotations.length / this.pageSize));
  }

  get paginatedPrenotations(): Prenotation[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredPrenotations.slice(start, start + this.pageSize);
  }

  get visiblePages(): number[] {
    return Array.from({ length: this.totalPages }, (_, idx) => idx + 1);
  }

  goToPage(page: number): void {
    if (page < 1 || page > this.totalPages) {
      return;
    }
    this.currentPage = page;
  }

  appointmentDate(p: Prenotation): Date | null {
    if (!p.date || !p.slotTime) return null;
    return new Date(`${p.date}T${p.slotTime}`);
  }

  paymentStatus(prenotationId: number): PaymentStatus {
    return this.paymentMap.get(prenotationId)?.status ?? 'PENDING';
  }

  formatPrenotationStatus(status: string): string {
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

  formatPaymentStatus(status: PaymentStatus): string {
    switch (status) {
      case 'PAID':
        return 'Pagato';
      case 'REFUNDED':
        return 'Rimborsato';
      default:
        return 'In attesa';
    }
  }

  doctorName(doctorId: number): string {
    const doctor = this.doctorMap.get(doctorId);
    if (!doctor) return `Medico #${doctorId}`;
    const fullName = `${doctor.nome ?? ''} ${doctor.cognome ?? ''}`.trim();
    return fullName || doctor.username || `Medico #${doctorId}`;
  }

  patientName(userId: number): string {
    const patient = this.patientMap.get(userId);
    if (!patient) return `Paziente #${userId}`;
    const fullName = `${patient.nome ?? ''} ${patient.cognome ?? ''}`.trim();
    return fullName || patient.username || `Paziente #${userId}`;
  }

  visitLabel(visitTypeId: number): string {
    const visit = this.visitMap.get(visitTypeId);
    return visit?.description ?? `Tipo visita #${visitTypeId}`;
  }

  visitPrice(visitTypeId: number): string {
    const visit = this.visitMap.get(visitTypeId);
    if (!visit) return 'Prezzo non disponibile';
    return `€ ${visit.price.toFixed(2)}`;
  }

  visitDuration(visitTypeId: number): string {
    const visit = this.visitMap.get(visitTypeId);
    if (!visit) return 'Durata non disponibile';
    return `${visit.durationMin} min`;
  }

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
