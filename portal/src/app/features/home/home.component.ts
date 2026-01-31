import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { AuthService } from '../../core/auth/auth.service';
import { RoleType } from '../../core/auth/jwt-payload.model';
import {
  trigger,
  transition,
  style,
  animate,
  query,
  stagger
} from '@angular/animations';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatIconModule
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
  animations: [

    trigger('fadeSlide', [
      transition(':enter', [
        style({ opacity: 0, transform: 'translateY(32px)' }),
        animate(
          '700ms ease-out',
          style({ opacity: 1, transform: 'translateY(0)' })
        )
      ])
    ]),
    trigger('listFade', [
      transition(':enter', [
        query(
          '.service-card',
          [
            style({ opacity: 0, transform: 'translateY(24px)' }),
            stagger(120, [
              animate(
                '500ms ease-out',
                style({ opacity: 1, transform: 'translateY(0)' })
              )
            ])
          ],
          { optional: true }
        )
      ])
    ])
  ]
})
export class HomeComponent implements OnInit {

  isLogged = false;
  role: RoleType | null = null;
  username: string | null = null;
  fullName = '';

  constructor(
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.isLogged = this.authService.isLoggedIn;
    this.role = this.authService.role;
    this.username = this.authService.username;
    this.fullName = this.authService.fullName || this.username || '';

    if (this.isLogged) {
      this.authService.getMyInfo().subscribe({
        next: () => {
          this.fullName = this.authService.fullName || this.username || '';
          this.cdr.detectChanges();
        },
        error: () => {
          this.cdr.detectChanges();
        }
      });
    }
  }
}
