import { Component, OnInit, ChangeDetectorRef, NgZone } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-top-bar',
  standalone: true,
  imports: [CommonModule, MatIconModule, MatButtonModule],
  templateUrl: './top-bar.html',
  styleUrls: ['./top-bar.scss']
})
export class TopBarComponent implements OnInit {

  menuOpen = false;

  constructor(
    private auth: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef,
    private zone: NgZone
  ) {}

  ngOnInit(): void {
    this.auth.userChanged$.subscribe(() => {
      this.zone.run(() => {
        this.cdr.detectChanges();
      });
    });
  }

  get username(): string | null {
    return this.auth.username;
  }

  get fullName(): string {
    return this.auth.fullName || '';
  }

  get initials(): string {
    const fullName = this.auth.fullName?.trim();
    if (fullName) {
      const parts = fullName.split(/\s+/).filter(Boolean);
      const first = parts[0]?.charAt(0) ?? '';
      const last = parts.length > 1 ? parts[parts.length - 1].charAt(0) : '';
      return (first + last).toUpperCase() || 'U';
    }

    const username = this.auth.username?.trim() ?? '';
    if (username.length >= 2) {
      return username.slice(0, 2).toUpperCase();
    }
    if (username.length === 1) {
      return username.toUpperCase();
    }
    return 'U';
  }

  get role(): string | null {
    return this.auth.role;
  }

  goHome(): void {
  this.router.navigateByUrl('/');
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  goProfile(): void {
    this.menuOpen = false;
    this.router.navigate(['/profile']);
  }

  logout(): void {
    this.menuOpen = false;
    this.auth.logout();
    this.router.navigate(['/']);
  }
}
