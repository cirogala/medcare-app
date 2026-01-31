import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../auth/auth.service';

export const medcareHttpInterceptor: HttpInterceptorFn = (req, next) => {

  const auth = inject(AuthService);
  let headers = req.headers;

  // DEBUG: log tutte le richieste intercettate
  console.log('[MEDCARE INTERCEPTOR]', req.url, headers);

  // JWT (se non già messo dal jwtInterceptor)
  const token = auth.getToken();
  if (token && !headers.has('Authorization')) {
    headers = headers.set('Authorization', `Bearer ${token}`);
  }

  // === HEADER PER MICROSERVIZIO ===
  if (req.url.includes('/profiling')) {
    headers = headers
      .set('X-Medcare-Accept', 'application/medcare+v1.json')
      .set('X-Medcare-KeyLogic', 'PROFILING');
  }

  if (req.url.includes('/visits')) {
    headers = headers
      .set('X-Medcare-Accept', 'application/medcare+v1.json')
      .set('X-Medcare-KeyLogic', 'PRENOTATION');
  }

  if (req.url.includes('/billing')) {
    headers = headers
      .set('X-Medcare-Accept', 'application/medcare+v1.json')
      .set('X-Medcare-KeyLogic', 'BILLING');
  }

  if (req.url.startsWith('/visits')) {
    console.log('[INTERCEPTOR PRENOTATION]', req.url);
  }

  return next(req.clone({ headers }));
};
