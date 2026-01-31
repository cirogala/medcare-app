package it.medcare.profiling.service;

import it.medcare.profiling.enums.RoleType;

public interface CredentialService {

	String generateUsername(RoleType roleType);
	String generateRawPassword();
}
