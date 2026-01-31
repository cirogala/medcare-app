package it.medcare.prenotation.constants;

public class Constants {

	//Errors
	
	public static final String EMAIL_ESISTENTE = "Email already exists";
	public static final String RUOLO_NON_TROVATO = "Role not found";
	public static final String RUOLO_NON_SUPPORTATO = "Unsupported role type";
	public static final String INVALID_CREDENTIAL = "Credenziali non valide";
	public static final String MISSING_HEADER_AUTH = "Header 'Authorization' mancante";
	public static final String SLOT_NOT_AVAILABLE = "Slot non disponibile";
	public static final String INVALID_DATE = "Data non valida";
	public static final String PRENOTATION_NF_OR_UNAUTH = "Prenotazione non trovata o utente non autorizzato";
	public static final String PRENOTATION_IN_THE_PAST = "Impossibile cancellare una prenotazione già passata";
	
	//PREFIX
	
	public static final String INTERNO = "INT";
	public static final String PAZIENTE = "PAT";
	public static final String MEDICO = "MED";
}

