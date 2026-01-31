export interface CreateInternalUserRequest {
  nome: string;
  cognome: string;
  email: string;
  isInternal: boolean;
  isMed: boolean;
  flagDeleted: number;
  citta: string;
  codiceFiscale: string;
  indirizzo: string;
  telefono: string;
}
