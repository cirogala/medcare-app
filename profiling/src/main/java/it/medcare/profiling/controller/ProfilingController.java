package it.medcare.profiling.controller;

import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import it.medcare.profiling.common.rest.Headers;
import it.medcare.profiling.common.rest.MediaType;
import it.medcare.profiling.config.MedcareSecurityConfig;
import it.medcare.profiling.controller.dtos.UserProfileDTO;
import it.medcare.profiling.controller.dtos.UserUpdateRequest;
import it.medcare.profiling.controller.dtos.PasswordResetRequest;
import it.medcare.profiling.entity.User;
import it.medcare.profiling.enums.RoleType;
import it.medcare.profiling.service.UserService;

@RestController
@RequestMapping("/profiling")
public class ProfilingController {

	private final UserService userService;
	
    public ProfilingController(UserService userService) {
        this.userService = userService;
    }
	
    //security disattivata per test
	@MedcareSecurityConfig(enableAnonymous = true)
    @PostMapping (path = "/createInternalUser", produces = MediaType.PROFILING_V1)
    public User createInternalUser(
    		@RequestHeader(name= Headers.ACCEPT, required = true) String accept,
            @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
            @RequestHeader (name = Headers.TRANSACTION_ID, required = false) String transId,
    		@RequestBody User user) {
    	
        return userService.createInternalUser(user);
    }
	
    @PostMapping (path = "/createExternalUser", produces = MediaType.PROFILING_V1)
    @MedcareSecurityConfig(enableAnonymous = true)
    public User createExternalUser(
    		@RequestHeader(name= Headers.ACCEPT, required = true) String accept,
            @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
            @RequestHeader (name = Headers.TRANSACTION_ID, required = false) String transId,
    		@RequestBody User user) {
    	
        return userService.createExternalUser(user);
    }
	
	@MedcareSecurityConfig(enableAnonymous = true, allowedRoles = {RoleType.ADMIN})
    @PostMapping (path = "/createDoctorUser", produces = MediaType.PROFILING_V1)
    public User createDoctorUser(
    		@RequestHeader(name= Headers.ACCEPT, required = true) String accept,
            @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
            @RequestHeader (name = Headers.TRANSACTION_ID, required = false) String transId,
    		@RequestBody User user) {
    	
        return userService.createDoctorUser(user);
    }
	
	@PostMapping(path = "/retrieveMyInfo", produces = MediaType.PROFILING_V1)
	public UserProfileDTO retrieveMyInfo(
	        @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
	        @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
	        @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId
	) {

	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    String username = authentication.getName();

	    return userService.retrieveMyInfo(username);
	}
	
	@MedcareSecurityConfig(enableAnonymous = true)
	@PostMapping(path = "/doctors", produces = MediaType.PROFILING_V1)
	public List<UserProfileDTO> doctors(
	        @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
	        @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
	        @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
	        @RequestParam Boolean isMed
	) {


	    return userService.retrieveAllDoctors(isMed);
	}
	
	@GetMapping(path = "/users", produces = MediaType.PROFILING_V1)
	public List<UserProfileDTO> users(
	        @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
	        @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
	        @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId
	) {


	    return userService.retrieveAllExtUsers();
	}

	@GetMapping(path = "/users/internal", produces = MediaType.PROFILING_V1)
	public List<UserProfileDTO> internalUsers(
	        @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
	        @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
	        @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId
	) {
	    return userService.retrieveAllIntUsers();
	}

	@PutMapping(path = "/users/internal/{userId}", produces = MediaType.PROFILING_V1)
	public UserProfileDTO updateInternalUser(
	        @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
	        @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
	        @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
	        @PathVariable Long userId,
	        @RequestBody UserUpdateRequest request
	) {
	    return userService.updateInternalUser(userId, request);
	}

	@PutMapping(path = "/users/external/{userId}", produces = MediaType.PROFILING_V1)
	public UserProfileDTO updateExternalUser(
	        @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
	        @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
	        @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
	        @PathVariable Long userId,
	        @RequestBody UserUpdateRequest request
	) {
	    return userService.updateExternalUser(userId, request);
	}

	@MedcareSecurityConfig(enableAnonymous = true)
	@PostMapping(path = "/users/reset-password", produces = MediaType.PROFILING_V1)
	public void resetPassword(
	        @RequestHeader(name = Headers.ACCEPT, required = true) String accept,
	        @RequestHeader(name = Headers.KEY_LOGIC, required = true) String keyLogic,
	        @RequestHeader(name = Headers.TRANSACTION_ID, required = false) String transId,
	        @RequestBody PasswordResetRequest request
	) {
	    userService.resetPassword(request);
	}
	
}
