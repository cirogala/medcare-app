package it.medcare.profiling.service;

import java.util.List;

import it.medcare.profiling.controller.dtos.UserProfileDTO;
import it.medcare.profiling.entity.User;
import it.medcare.profiling.controller.dtos.UserUpdateRequest;
import it.medcare.profiling.controller.dtos.PasswordResetRequest;


public interface UserService {

    User createInternalUser(User user);
    User createExternalUser(User user);
    User createDoctorUser(User user);
    UserProfileDTO retrieveMyInfo (String user);
    List<UserProfileDTO> retrieveAllDoctors (Boolean isMed);
    List<UserProfileDTO> retrieveAllExtUsers ();
    List<UserProfileDTO> retrieveAllIntUsers ();
    UserProfileDTO updateInternalUser(Long userId, UserUpdateRequest request);
    UserProfileDTO updateExternalUser(Long userId, UserUpdateRequest request);
    void resetPassword(PasswordResetRequest request);
}
