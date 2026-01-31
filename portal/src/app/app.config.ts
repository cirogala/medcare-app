import { ApplicationConfig, LOCALE_ID } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideAnimations } from '@angular/platform-browser/animations';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { jwtInterceptor } from './core/auth/jwt.interceptor';
import { medcareHttpInterceptor } from './core/http/medcare-http.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    { provide: LOCALE_ID, useValue: 'it-IT' },
    provideRouter(routes),
    provideAnimations(),
    provideHttpClient(
      withInterceptors([
        medcareHttpInterceptor,
        jwtInterceptor
      ])
    )
  ]
};
