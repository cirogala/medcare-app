export type RoleType = 'ADMIN' | 'MEDICO' | 'PAZIENTE';

export interface JwtPayload {
  sub: string; 
  userId: number;
  role: RoleType;
  iat: number;
  exp: number;
  nome?: string;
  cognome?: string;
}
