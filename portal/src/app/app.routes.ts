import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { LoginComponent } from './features/login/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { AuthGuard } from './core/auth/auth.guard';
import { ProfileComponent } from './features/profile/profile.component';
import { adminBookingsResolver } from './features/dashboard/admin-dashboard/admin-bookings/admin-bookings.resolver';


export const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  {path: 'registrazione', loadComponent: () =>
    import('./features/registration/registration.component')
      .then(m => m.RegistrationComponent)},
  {
    path: 'doctors',
    loadComponent: () =>
      import('./features/doctors/doctor-list.component')
        .then(m => m.DoctorListComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'prenotations',
    loadComponent: () =>
      import('./features/prenotations/prenotation-list/prenotation-list.component')
        .then(m => m.PrenotationListComponent),
    canActivate: [AuthGuard]
  },
  {
    path: 'referti',
    loadComponent: () =>
      import('./features/referti/referti-list.component')
        .then(m => m.RefertiListComponent),
    canActivate: [AuthGuard]
  },

  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [AuthGuard],
    children: [
      {
        path: 'admin',
        loadComponent: () =>
          import('./features/dashboard/admin-dashboard/admin-dashboard.component')
            .then(m => m.AdminDashboardComponent),
        children: [
          {
            path: '',
            pathMatch: 'full',
            loadComponent: () =>
              import('./features/dashboard/admin-dashboard/admin-home.component')
                .then(m => m.AdminHomeComponent)
          },
          {
            path: 'bookings',
            loadComponent: () =>
              import('./features/dashboard/admin-dashboard/admin-bookings/admin-bookings.component')
                .then(m => m.AdminBookingsComponent),
                  resolve: {
                    visits: adminBookingsResolver
                  }
          },
          {
            path: 'medici',
            loadComponent: () =>
              import('./features/dashboard/admin-dashboard/admin-doctors/admin-doctors.component')
                .then(m => m.AdminDoctorsComponent),
            runGuardsAndResolvers: 'always'
          },
          {
            path: 'utenti',
            loadComponent: () =>
              import('./features/dashboard/admin-dashboard/admin-users/admin-users.component')
                .then(m => m.AdminUsersComponent)
          },
          {
            path: 'billing',
            loadComponent: () =>
              import('./features/dashboard/admin-dashboard/admin-billing/admin-billing.component')
                .then(m => m.AdminBillingComponent)
          }
        ]
      },
      {
        path: 'medico',
        loadComponent: () =>
          import('./features/dashboard/medico-dashboard/med-dashboard.component')
            .then(m => m.MedDashboardComponent)
      },
      {
        path: 'user',
        loadComponent: () =>
          import('./features/dashboard/paziente-dashboard/paziente-dashboard')
            .then(m => m.PazienteDashboard)
      }
    ]
  },

  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: '**', redirectTo: '' }
];
