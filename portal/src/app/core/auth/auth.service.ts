import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, Subject, tap } from 'rxjs';
import { finalize } from 'rxjs/operators';
import { jwtDecode } from 'jwt-decode';

import { JwtPayload, RoleType } from './jwt-payload.model';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface UserProfile {
  userId: number;
  username: string;
  email: string;
  nome: string;
  cognome: string;
  citta: string;
  indirizzo: string;
  codiceFiscale: string;
  role: string;
}

export interface PasswordResetRequest {
  identifier: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {

  private readonly TOKEN_KEY = 'portal_token';
  private readonly API_URL = 'http://localhost:8080/profiling';
  private user: UserProfile | null = null;
  private loadingUser = false;
  private readonly userChanged = new Subject<void>();
  readonly userChanged$ = this.userChanged.asObservable();


  constructor(private http: HttpClient) {}

  login(request: LoginRequest): Observable<LoginResponse> {
    const headers = new HttpHeaders({
      'X-Medcare-Accept': 'application/medcare.v1+json',
      'Content-Type': 'application/json',
      'X-Medcare-KeyLogic': '1234567',
      'X-Medcare-TransactionID': crypto.randomUUID(),
    });

    return this.http
      .post<LoginResponse>(`${this.API_URL}/login`, request, { headers })
      .pipe(
        tap(res => {
          // pulizia preventiva (evita token vecchi)
          localStorage.removeItem(this.TOKEN_KEY);

          this.setToken(res.accessToken);
          this.ensureUserLoaded();
          this.userChanged.next();

          console.log('[JWT payload]', this.getDecodedToken());
          console.log('[JWT role]', this.role);
        })
      );
  }

  resetPassword(request: PasswordResetRequest): Observable<void> {
    const headers = new HttpHeaders({
      'X-Medcare-Accept': 'application/medcare.v1+json',
      'Content-Type': 'application/json',
      'X-Medcare-KeyLogic': '1234567',
      'X-Medcare-TransactionID': crypto.randomUUID(),
    });

    return this.http.post<void>(
      `${this.API_URL}/users/reset-password`,
      request,
      { headers }
    );
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    this.user = null;
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  get isLoggedIn(): boolean {
    return !!this.getToken();
  }

  getDecodedToken(): JwtPayload | null {
    const token = this.getToken();
    if (!token) return null;

    try {
      return jwtDecode<JwtPayload>(token);
    } catch {
      return null;
    }
  }


  get role(): RoleType | null {
    const payload: any = this.getDecodedToken();
    if (!payload) return null;

    // claim più comuni
    const rawRole =
      payload.role ??
      payload.roles?.[0] ??
      payload.authorities?.[0] ??
      payload.realm_access?.roles?.[0];

    if (!rawRole || typeof rawRole !== 'string') return null;

    const normalized = rawRole.replace(/^ROLE_/, '').toUpperCase();

    if (normalized === 'ADMIN' || normalized === 'AMMINISTRATORE') return 'ADMIN';
    if (normalized === 'MEDICO' || normalized === 'DOCTOR') return 'MEDICO';
    if (normalized === 'PAZIENTE' || normalized === 'PATIENT') return 'PAZIENTE';

    return null;
  }

  get username(): string | null {
    return this.getDecodedToken()?.sub ?? null;
  }

  isTokenExpired(): boolean {
    const payload = this.getDecodedToken();
    if (!payload?.exp) return true;

    const now = Math.floor(Date.now() / 1000);
    return payload.exp < now;
  }

  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

 getMyInfo(): Observable<UserProfile> {
  return this.http
    .post<UserProfile>(
      `${this.API_URL}/retrieveMyInfo`,
      {},
      {
        headers: {
          'X-Medcare-Accept': 'application/medcare.v1+json',
          'X-Medcare-KeyLogic': '1234567',
          'X-Medcare-TransactionID': crypto.randomUUID(),
        }
      }
    )
    .pipe(
      tap(profile => {
        this.user = profile;
        this.userChanged.next();
        console.log('[USER LOADED]', profile);
      })
    );
}

  ensureUserLoaded(): void {
    if (!this.isLoggedIn || this.user || this.loadingUser) {
      return;
    }

    this.loadingUser = true;
    this.getMyInfo()
      .pipe(finalize(() => {
        this.loadingUser = false;
      }))
      .subscribe({
        next: () => {},
        error: () => {}
      });
  }

get nome(): string | null {
  return this.user?.nome ?? this.getDecodedToken()?.nome ?? null;
}

get cognome(): string | null {
  return this.user?.cognome ?? this.getDecodedToken()?.cognome ?? null;
}

get fullName(): string {
  const nome = this.user?.nome ?? this.getDecodedToken()?.nome ?? '';
  const cognome = this.user?.cognome ?? this.getDecodedToken()?.cognome ?? '';
  return `${nome} ${cognome}`.trim();
}

}
