export interface User {
  userId: number;
  username: string;
  email: string;
  nome: string;
  cognome: string;
  citta: string;
  indirizzo: string;
  codiceFiscale: string;
  telefono?: string;
  role: string;
  enabled: boolean;
}
