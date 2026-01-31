import { Component, OnInit } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [RouterOutlet],
  template: '<router-outlet></router-outlet>',
})
export class DashboardComponent implements OnInit {

  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    const role = this.auth.role;
    console.log('[Dashboard] redirect per ruolo:', role);

    switch (role) {

      case 'ADMIN':
        this.router.navigateByUrl('/dashboard/admin', { replaceUrl: true });
        break;

      case 'MEDICO':
        this.router.navigateByUrl('/dashboard/medico', { replaceUrl: true });
        break;

      case 'PAZIENTE':
        this.router.navigateByUrl('/dashboard/user', { replaceUrl: true });
        break;

      default:
        this.router.navigateByUrl('/login', { replaceUrl: true });
    }
  }
}
