import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpParams } from '@angular/common/http';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { forkJoin } from 'rxjs';
import { finalize } from 'rxjs/operators';

interface OverviewDTO {
  from: string;
  to: string;
  totalVisits: number;
  paidCount: number;
  pendingCount: number;
  refundedCount: number;
  totalExpectedAmount: number;
  totalPaidAmount: number;
  averageTicket: number;
}

interface RevenuePointDTO {
  label: string;
  totalAmount: number;
}

interface RevenueBySpecializationDTO {
  specialization: string;
  totalAmount: number;
}

interface RevenueByDoctorDTO {
  doctorId: number;
  doctorName: string | null;
  totalAmount: number;
}

interface PaymentSummaryDTO {
  paidCount: number;
  pendingCount: number;
  refundedCount: number;
  totalPaidAmount: number;
  totalPendingAmount: number;
}

type PaymentStatus = 'PENDING' | 'PAID' | 'REFUNDED';

interface PaymentDTO {
  paymentId?: number;
  prenotationId: number;
  amount: number;
  status: PaymentStatus;
  paidAt?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}

@Component({
  selector: 'app-admin-billing',
  standalone: true,
  imports: [CommonModule, FormsModule, MatSnackBarModule],
  templateUrl: './admin-billing.component.html',
  styleUrls: ['./admin-billing.component.scss']
})
export class AdminBillingComponent implements OnInit {

  loading = false;
  error = '';

  overview: OverviewDTO | null = null;
  paymentSummary: PaymentSummaryDTO | null = null;
  revenuePoints: RevenuePointDTO[] = [];
  revenueBySpecialization: RevenueBySpecializationDTO[] = [];
  revenueByDoctor: RevenueByDoctorDTO[] = [];
  payments: PaymentDTO[] = [];

  fromDate = '';
  toDate = '';
  revenueGroup: 'day' | 'week' | 'month' = 'month';
  paymentStatusFilter: PaymentStatus | 'ALL' = 'PENDING';
  updatingPaymentId: number | null = null;
  actionMenuOpenId: number | null = null;

  constructor(
    private http: HttpClient,
    private snackBar: MatSnackBar,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) {}

  ngOnInit(): void {
    const today = new Date();
    const startOfMonth = new Date(today.getFullYear(), today.getMonth(), 1);
    const endOfMonth = new Date(today.getFullYear(), today.getMonth() + 1, 0);
    this.fromDate = this.formatDate(startOfMonth);
    this.toDate = this.formatDate(endOfMonth);
    this.loadData();
  }

  loadData(): void {
    if (!this.fromDate || !this.toDate) {
      return;
    }

    this.loading = true;
    this.error = '';

    const params = new HttpParams()
      .set('from', this.fromDate)
      .set('to', this.toDate);

    const revenueParams = params.set('group', this.revenueGroup);

    const paymentsParams = this.paymentStatusFilter === 'ALL'
      ? params
      : params.set('status', this.paymentStatusFilter);

    forkJoin({
      overview: this.http.get<OverviewDTO>('/billing/overview', { params }),
      paymentSummary: this.http.get<PaymentSummaryDTO>('/billing/payments/summary', { params }),
      revenuePoints: this.http.get<RevenuePointDTO[]>('/billing/revenue', { params: revenueParams }),
      revenueBySpecialization: this.http.get<RevenueBySpecializationDTO[]>('/billing/revenue/by-specialization', { params }),
      revenueByDoctor: this.http.get<RevenueByDoctorDTO[]>('/billing/revenue/by-doctor', { params }),
      payments: this.http.get<PaymentDTO[]>('/billing/payments', { params: paymentsParams })
    })
      .pipe(finalize(() => {
        this.loading = false;
        this.cdr.detectChanges();
      }))
      .subscribe({
        next: data => {
          this.zone.run(() => {
            this.overview = data.overview;
            this.paymentSummary = data.paymentSummary;
            this.revenuePoints = data.revenuePoints;
            this.revenueBySpecialization = data.revenueBySpecialization;
            this.revenueByDoctor = data.revenueByDoctor;
            this.payments = data.payments;
            this.cdr.detectChanges();
          });
        },
        error: () => {
          this.zone.run(() => {
            this.error = 'Errore nel caricamento dei dati di billing.';
            this.cdr.detectChanges();
          });
        }
      });
  }

  markPaid(payment: PaymentDTO): void {
    if (!payment || this.updatingPaymentId !== null) {
      return;
    }

    this.updatingPaymentId = payment.prenotationId;

    this.http.patch<PaymentDTO>(`/billing/payments/${payment.prenotationId}/status`, {
      status: 'PAID'
    })
      .pipe(finalize(() => (this.updatingPaymentId = null)))
      .subscribe({
        next: () => {
          this.snackBar.open('Pagamento aggiornato.', 'OK', {
            duration: 2500,
            panelClass: ['toast', 'toast-center', 'toast-success']
          });
          this.loadData();
        },
        error: () => {
          this.snackBar.open('Impossibile aggiornare il pagamento.', 'OK', {
            duration: 3000,
            panelClass: ['toast', 'toast-center', 'toast-error']
          });
        }
      });
  }

  trackByLabel(_: number, item: RevenuePointDTO): string {
    return item.label;
  }

  trackBySpec(_: number, item: RevenueBySpecializationDTO): string {
    return item.specialization;
  }

  trackByDoctor(_: number, item: RevenueByDoctorDTO): number {
    return item.doctorId;
  }

  setPaymentFilter(status: PaymentStatus): void {
    this.paymentStatusFilter = status;
    this.loadData();
  }

  toggleActionMenu(paymentId: number): void {
    this.actionMenuOpenId = this.actionMenuOpenId === paymentId ? null : paymentId;
  }

  markRefunded(payment: PaymentDTO): void {
    if (!payment || this.updatingPaymentId !== null) {
      return;
    }

    this.updatingPaymentId = payment.prenotationId;

    this.http.patch<PaymentDTO>(`/billing/payments/${payment.prenotationId}/status`, {
      status: 'REFUNDED'
    })
      .pipe(finalize(() => (this.updatingPaymentId = null)))
      .subscribe({
        next: () => {
          this.actionMenuOpenId = null;
          this.snackBar.open('Rimborso emesso.', 'OK', {
            duration: 2500,
            panelClass: ['toast', 'toast-center', 'toast-success']
          });
          this.loadData();
        },
        error: () => {
          this.snackBar.open('Impossibile emettere il rimborso.', 'OK', {
            duration: 3000,
            panelClass: ['toast', 'toast-center', 'toast-error']
          });
        }
      });
  }

  getMaxRevenuePoints(): number {
    return Math.max(0, ...this.revenuePoints.map(item => item.totalAmount || 0));
  }

  getMaxBySpecialization(): number {
    return Math.max(0, ...this.revenueBySpecialization.map(item => item.totalAmount || 0));
  }

  getMaxByDoctor(): number {
    return Math.max(0, ...this.revenueByDoctor.map(item => item.totalAmount || 0));
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

  private formatDate(date: Date): string {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }
}
