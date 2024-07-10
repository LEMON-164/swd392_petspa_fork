package org.petspa.petcaresystem.authenuser.service.implement;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.persistence.Query;
import org.petspa.petcaresystem.authenuser.model.payload.AuthenUser;
import org.petspa.petcaresystem.authenuser.model.payload.CustomAuthenUserForRegister;
import org.petspa.petcaresystem.authenuser.model.payload.CustomAuthenUserForUpdateProfile;
import org.petspa.petcaresystem.authenuser.model.response.*;
import org.petspa.petcaresystem.authenuser.repository.AuthenUserRepository;
import org.petspa.petcaresystem.authenuser.service.AuthenUserService;
import org.petspa.petcaresystem.config.JwtUtil;
import org.petspa.petcaresystem.config.MyUserDetails;
import org.petspa.petcaresystem.enums.Gender;
import org.petspa.petcaresystem.enums.Status;
import org.petspa.petcaresystem.role.model.Role;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AuthenUserServiceImpl implements AuthenUserService {

    private static final String format_pattern = "yyyy-MM-dd HH:mm";
    private static final String logging_message = "An error occurred:";
    private static final Logger logger = LoggerFactory.getLogger(AuthenUserService.class);

    @Autowired
    AuthenUserRepository authenUserRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // @Autowired
    // PasswordEncoder passwordEncoder;
    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    private HttpServletRequest request;

    @Override
    public AuthenUser createUser(AuthenUser authenUser) {
        return authenUserRepository.save(authenUser);
    }

    @Override
    public JwtResponseDTO login(String email, String password) {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format_pattern);
        String timeStamp = localDateTime.format(formatter);
        String message = "Login success";
        int statusCode = HttpStatus.OK.value();
        HttpStatus statusValue = HttpStatus.OK;
        Optional<AuthenUser> authenUser;
        String jwtToken = "";

        try {
            authenUser = Optional.ofNullable(authenUserRepository.findByEmailAndPassword(email, password));
            if(authenUser.isPresent()){
                jwtToken = jwtUtil.generateToken(authenUser.get().getEmail(),
                        authenUser.get().getRole().getRoleName(),
                        authenUser.get().getUserName(),
                        authenUser.get().getUserId());
                HttpSession session = request.getSession();
                session.setAttribute("jwtToken", jwtToken);
            }else{
                message = "Invalid email/password";
                statusCode = HttpStatus.UNAUTHORIZED.value();
                statusValue = HttpStatus.UNAUTHORIZED;
                return new JwtResponseDTO(jwtToken, message, timeStamp, statusCode, statusValue);
            }
        } catch (AuthenticationException e) {
            message = "Invalid email/password";
            statusCode = HttpStatus.UNAUTHORIZED.value();
            statusValue = HttpStatus.UNAUTHORIZED;
        } catch (Exception e) {
            logger.error("Error occurred during login", e);
            message = "Something went wrong, server error!";
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
            statusValue = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new JwtResponseDTO(jwtToken, message, timeStamp, statusCode, statusValue);
    }


    @Override
    public RegisterResponseDTO register(AuthenUser authenUser) {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format_pattern);
        String timeStamp = localDateTime.format(formatter);
        String message = "Create new account successfully";
        int statusCode = HttpStatus.OK.value();
        HttpStatus statusValue = HttpStatus.OK;

        // encode password
        // String encodedPassword = passwordEncoder.encode(authenUser.getPassword());
        // authenUser.setPassword(encodedPassword);

        // set create date
        authenUser.setCreate_date(localDateTime);

        // set status value
        authenUser.setStatus(Status.valueOf("ACTIVE"));

        // set roleID = customer role
        Role role = new Role();
        role.setRoleId(3L);
        authenUser.setRole(role);

        // check user name used?
        if (authenUserRepository.findByUserName(authenUser.getUserName()) != null) {
            message = "This user name has already used! Please try another";
            statusCode = HttpStatus.CONFLICT.value();
            statusValue = HttpStatus.CONFLICT;
            return new RegisterResponseDTO(message, timeStamp, statusCode, statusValue, null);
        }

        // check email exist?
        if (authenUserRepository.findByEmail(authenUser.getEmail()) != null) {
            message = "This email has already existed! Please try another";
            statusCode = HttpStatus.CONFLICT.value();
            statusValue = HttpStatus.CONFLICT;
            return new RegisterResponseDTO(message, timeStamp, statusCode, statusValue, null);
        }

        // check phone number exist?
        if (authenUserRepository.findByPhone(authenUser.getPhone()) != null) {
            message = "This phone number has already existed! Please try another";
            statusCode = HttpStatus.CONFLICT.value();
            statusValue = HttpStatus.CONFLICT;
            return new RegisterResponseDTO(message, timeStamp, statusCode, statusValue, null);
        }

        CustomAuthenUserForRegister customAuthenUserForRegister = new CustomAuthenUserForRegister();
        try {
            authenUserRepository.save(authenUser);
            // handle data response
            customAuthenUserForRegister.setUserId(authenUser.getUserId());
            customAuthenUserForRegister.setUserName(authenUser.getUserName());
            customAuthenUserForRegister.setEmail(authenUser.getEmail());
            customAuthenUserForRegister.setFullName(authenUser.getFullName());
            customAuthenUserForRegister.setGender(String.valueOf(authenUser.getGender()));
            customAuthenUserForRegister.setAddress(authenUser.getAddress());
            customAuthenUserForRegister.setPhone(authenUser.getPhone());
            customAuthenUserForRegister.setCreate_date(timeStamp);
            customAuthenUserForRegister.setStatus("ACTIVE");
            customAuthenUserForRegister.setRole("CUSTOMER");
        } catch (Exception e) {
            logger.error(this.logging_message, e);
            message = "Something went wrong, server error!";
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
            statusValue = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new RegisterResponseDTO(message, timeStamp, statusCode, statusValue, customAuthenUserForRegister);
    }

    @Override
    public UpdateProfileResponseDTO updateProfile(AuthenUser authenUser) {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format_pattern);
        String timeStamp = localDateTime.format(formatter);
        String message = "Update profile successfully";
        int statusCode = HttpStatus.OK.value();
        HttpStatus statusValue = HttpStatus.OK;

        HttpSession session = request.getSession();
        String token = (String) session.getAttribute("jwtToken");
        Long userId = jwtUtil.extractUserId(token);

        // check user name used?
        AuthenUser existingUserName = authenUserRepository.findByUserName(authenUser.getFullName());
        if (existingUserName != null && !existingUserName.getUserId().equals(userId)) {
            message = "This user name has already used! Please try another";
            statusCode = HttpStatus.CONFLICT.value();
            statusValue = HttpStatus.CONFLICT;
            return new UpdateProfileResponseDTO(message, timeStamp, statusCode, statusValue, null);
        }

        // check email exist?
        AuthenUser existingEmail = authenUserRepository.findByEmail(authenUser.getEmail());
        if (existingEmail != null && !existingEmail.getUserId().equals(userId)) {
            message = "This email has already existed! Please try another";
            statusCode = HttpStatus.CONFLICT.value();
            statusValue = HttpStatus.CONFLICT;
            return new UpdateProfileResponseDTO(message, timeStamp, statusCode, statusValue, null);
        }

        // check phone number exist?
        AuthenUser existingPhone = authenUserRepository.findByPhone(authenUser.getPhone());
        if (existingPhone != null && !existingPhone.getUserId().equals(userId)) {
            message = "This phone number has already existed! Please try another";
            statusCode = HttpStatus.CONFLICT.value();
            statusValue = HttpStatus.CONFLICT;
            return new UpdateProfileResponseDTO(message, timeStamp, statusCode, statusValue, null);
        }

        CustomAuthenUserForUpdateProfile customAuthenUserForUpdateProfile = new CustomAuthenUserForUpdateProfile();
        try {
            authenUserRepository.save(authenUser);
            // handle data response
            customAuthenUserForUpdateProfile.setUserId(authenUser.getUserId());
            customAuthenUserForUpdateProfile.setUserName(authenUser.getUserName());
            customAuthenUserForUpdateProfile.setEmail(authenUser.getEmail());
            customAuthenUserForUpdateProfile.setFullName(authenUser.getFullName());
            customAuthenUserForUpdateProfile.setGender(String.valueOf(authenUser.getGender()));
            customAuthenUserForUpdateProfile.setAddress(authenUser.getAddress());
            customAuthenUserForUpdateProfile.setPhone(authenUser.getPhone());
        } catch (Exception e) {
            logger.error(this.logging_message, e);
            message = "Something went wrong, server error!";
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
            statusValue = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new UpdateProfileResponseDTO(message, timeStamp, statusCode, statusValue, customAuthenUserForUpdateProfile);
    }

    @Override
    public UpdatePassowordResponseDTO updatePassword(String current_password, String new_password, String confirm_password){
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format_pattern);
        String timeStamp = localDateTime.format(formatter);
        String message = "Update new password successfully";
        int statusCode = HttpStatus.OK.value();
        HttpStatus statusValue = HttpStatus.OK;

        // get token from session and extract userId
        HttpSession session = request.getSession();
        String token = (String) session.getAttribute("jwtToken");
        Long userId = jwtUtil.extractUserId(token);

        // check current password valid?
        AuthenUser authenUser = authenUserRepository.findByUserId(userId);
        String userPasswordStoredInDatabase = authenUser.getPassword();
        // if(!checkPassword(current_password, userPasswordStoredInDatabase)){
        //     message = "Incorrect current password! Use 'Forget Password' if you don't remember your password";
        //     return new UpdatePassowordResponseDTO(message, timeStamp, statusCode, statusValue);
        // }

        // check confirm password match?
        // check confirm password match?
        if(!new_password.equals(confirm_password)){
            message = "Not match new password and confirm password! Please try again";
            return new UpdatePassowordResponseDTO(message, timeStamp, statusCode, statusValue);
        }

        // encode new password
        // String encodedNewPassword = passwordEncoder.encode(authenUser.getPassword());
        // authenUser.setPassword(encodedNewPassword);

        try{
            authenUserRepository.save(authenUser);
        }catch (Exception e){
            logger.error(this.logging_message, e);
            message = "Something went wrong, server error!";
            statusCode = HttpStatus.INTERNAL_SERVER_ERROR.value();
            statusValue = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new UpdatePassowordResponseDTO(message, timeStamp, statusCode, statusValue);
    }

    // public boolean checkPassword(String rawPassword, String encodedPassword) {
    //     return passwordEncoder.matches(rawPassword, encodedPassword);
    // }


    @Override
    public UserDetails loadUserByEmail(String email) {
        AuthenUser authenUser = authenUserRepository.findByEmail(email);
        return new MyUserDetails(authenUser);
    }


    @Override
    public List<AuthenUser> getAllUser(){
        List<AuthenUser> authenUserList = authenUserRepository.findAll();
        return authenUserList;
    }

    @Override
    public ResponseAPI getUserById(Long id) {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format_pattern);
        String timeStamp = localDateTime.format(formatter);
        String message = "Get user successfully";
        int statusCode = HttpStatus.OK.value();
        HttpStatus statusValue = HttpStatus.OK;
        Optional<AuthenUser> authenUser = Optional.ofNullable(new AuthenUser());
        try {
            authenUser = authenUserRepository.findById(id);
        }catch (Exception e){
            logger.error(this.logging_message, e);
            message = "Something went wrong, server error!";
            statusValue = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseAPI(timeStamp, message, statusCode, statusValue, authenUser);
    }

    @Override
    public ResponseAPI getUsersByRole(Role role) {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format_pattern);
        String timeStamp = localDateTime.format(formatter);
        String message = "Get user successfully";
        int statusCode = HttpStatus.OK.value();
        HttpStatus statusValue = HttpStatus.OK;
        List<AuthenUser> authenUserList = new ArrayList<>();
        try {
            authenUserList = authenUserRepository.findByRole(role); // code cua An,
            // authenUserList = (List<AuthenUser>) authenUserRepository.findByRole(role);
        }catch (Exception e){
            logger.error(this.logging_message, e);
            message = "Something went wrong, server error!";
            statusValue = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseAPI(timeStamp, message, statusCode, statusValue, authenUserList);
    }

   @Override
   public ResponseAPI getUsersByCreateDateRange(LocalDateTime start_date, LocalDateTime end_date) {
       LocalDateTime localDateTime = LocalDateTime.now();
       DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format_pattern);
       String timeStamp = localDateTime.format(formatter);
       String message = "Get user successfully";
       int statusCode = HttpStatus.OK.value();
       HttpStatus statusValue = HttpStatus.OK;
       List<AuthenUser> authenUserList = new ArrayList<>();
       try {
           authenUserList = authenUserRepository.findAllUsersWithCreateDateRange(start_date, end_date);
       }catch (Exception e){
           logger.error(this.logging_message, e);
           message = "Something went wrong, server error!";
           statusValue = HttpStatus.INTERNAL_SERVER_ERROR;
       }
       return new ResponseAPI(timeStamp, message, statusCode, statusValue, authenUserList);
   }

   @Override
   public ResponseAPI searchByUserNameTEST(String searchTerm, Gender gender, Status status, String orderBy, String order) {
        LocalDateTime localDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format_pattern);
        String timeStamp = localDateTime.format(formatter);
        String message = "Get user successfully";
        int statusCode = HttpStatus.OK.value();
        HttpStatus statusValue = HttpStatus.OK;
        List<AuthenUser> authenUserList = new ArrayList<>();
        try {
            String sql = "SELECT * FROM pet_spa.authen_user WHERE (full_name LIKE :searchTerm OR email LIKE :searchTerm OR phone LIKE :searchTerm) AND (:gender IS NULL OR gender = :gender) AND (:status IS NULL OR status = :status) ORDER BY " + orderBy + " " + order;
            Query query = entityManager.createNativeQuery(sql, AuthenUser.class);
            query.setParameter("searchTerm", "%" + searchTerm + "%");
            query.setParameter("gender", null);
            query.setParameter("status", null);
            if (gender != null) {
                query.setParameter("gender", gender.toString().toUpperCase());
            }
            if (status != null) {
                query.setParameter("status", status.toString().toUpperCase());
            }
            authenUserList = query.getResultList();
        }catch (Exception e){
            logger.error(this.logging_message, e);
            message = "Something went wrong, server error!";
            statusValue = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseAPI(timeStamp, message, statusCode, statusValue, authenUserList);
   }

   public ResponseAPI findAllUsersWithAgeRange(Integer startAge, Integer endAge) {
    LocalDateTime localDateTime = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format_pattern);
    String timeStamp = localDateTime.format(formatter);
    String message = "Get user successfully";
    int statusCode = HttpStatus.OK.value();
    HttpStatus statusValue = HttpStatus.OK;
    List<AuthenUser> authenUserList = new ArrayList<>();
    try {
        authenUserList = authenUserRepository.findAllUsersWithAgeRange(startAge, endAge);
    }catch (Exception e){
        logger.error(this.logging_message, e);
        message = "Something went wrong, server error!";
        statusValue = HttpStatus.INTERNAL_SERVER_ERROR;
    }
    return new ResponseAPI(timeStamp, message, statusCode, statusValue, authenUserList);
   }
}
