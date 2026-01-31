import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';

import { TopBarComponent } from './shared/top-bar/top-bar';
import { AuthService } from './core/auth/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,
    TopBarComponent
  ],
  template: `
    <app-top-bar *ngIf="authService.isLoggedIn"></app-top-bar>
    <router-outlet></router-outlet>
  `
})
export class App implements OnInit {
  constructor(public authService: AuthService) {}

  ngOnInit(): void {
    this.authService.ensureUserLoaded();
  }
}
