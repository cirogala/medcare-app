package it.medcare.profiling.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import it.medcare.profiling.common.rest.CustomExceptions;
import it.medcare.profiling.common.rest.Headers;
import it.medcare.profiling.common.rest.MediaType;
import it.medcare.profiling.config.MedcareSecurityConfig;
import it.medcare.profiling.constants.Constants;
import it.medcare.profiling.controller.dtos.LoginRequest;
import it.medcare.profiling.controller.dtos.LoginResponse;
import it.medcare.profiling.entity.User;
import it.medcare.profiling.repository.UserRepo;
import it.medcare.profiling.service.JwtService;

@RestController
@RequestMapping("/profiling")
@MedcareSecurityConfig(enableAnonymous = true)
public class LoginController {
	
	private final UserRepo userRepo;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	
    public LoginController(UserRepo userRepo, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    
    @Value("${jwt.expiration}")
    private long expiration;
	
    @PostMapping (path = "/login", produces = MediaType.PROFILING_V1)
	public LoginResponse login(
			@RequestHeader(name= Headers.ACCEPT, required = true) String accept,
            @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
            @RequestHeader (name = Headers.TRANSACTION_ID, required = false) String transId,
			@RequestBody LoginRequest request) {

		// cerco la user
	    User user = userRepo.findByUsername(request.getUsername()).orElseThrow(() -> new CustomExceptions(Constants.INVALID_CREDENTIAL));
	    
	    // faccio un check della password
	    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
	    	
	        throw new CustomExceptions(Constants.INVALID_CREDENTIAL);
	    }
	    
	    //genero il token
	    String token = jwtService.generateToken(user);

	    return new LoginResponse(token, "Bearer", expiration);
	}
}
