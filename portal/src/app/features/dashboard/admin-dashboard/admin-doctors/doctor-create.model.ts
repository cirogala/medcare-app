export interface CreateDoctorRequest {
  nome: string;
  cognome: string;
  email: string;

  citta: string;
  indirizzo: string;
  codiceFiscale: string;
  telefono: string;
  typeDoctor: string;

  isInternal: boolean;
  isMed: boolean;
  flagDeleted: number;
}
