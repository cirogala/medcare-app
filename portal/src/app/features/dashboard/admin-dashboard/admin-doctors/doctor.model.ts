export interface Doctor {
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
  typeDoctor: string;
  enabled: boolean;
}
