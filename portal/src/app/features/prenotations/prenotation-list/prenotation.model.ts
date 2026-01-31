export interface Prenotation {
  prenotationId: number;
  userId?: number;
  doctorId: number;
  visitTypeId: number;
  date: string;      // ISO date
  slotTime: string;  // HH:mm:ss o HH:mm
  status: string;
}
