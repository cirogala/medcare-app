package it.medcare.profiling.service.impl;

import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;

import it.medcare.profiling.constants.Constants;
import it.medcare.profiling.enums.RoleType;
import it.medcare.profiling.repository.UserRepo;
import it.medcare.profiling.service.CredentialService;

@Service
public class CredentialServiceImpl implements CredentialService{
	
	private final UserRepo userRepo;
	
    public CredentialServiceImpl(UserRepo userRepo) {
        this.userRepo = userRepo;
    }
	
	@Override
	public String generateUsername(RoleType roleType) {

	    String prefix;
	    
	    //check sul ruolo
	    switch (roleType) {
	        case ADMIN:
	            prefix = Constants.INTERNO;
	            break;
	        case MEDICO:
	            prefix = Constants.MEDICO;
	            break;
	        case PAZIENTE:
	            prefix = Constants.PAZIENTE;
	            break;
	        default:
	            throw new IllegalArgumentException(Constants.RUOLO_NON_SUPPORTATO);
	    }

	    String username;
	    boolean exists;
	    
	    //genero le 6 cifre in maniera randomica
	    //fin quando non è una user univoca
	    do {
	        String randomPart = String.format("%06d", new Random().nextInt(1000000));
	        username = prefix + randomPart;
	        //controllo a DB se esiste
	        exists = userRepo.existsByUsername(username);
	    } while (exists);

	    return username;
	}
	
	public String generateRawPassword() {
		
	    return RandomStringUtils.random(10, true, true);
	}

}
