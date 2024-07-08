package org.petspa.petcaresystem.authenuser.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.petspa.petcaresystem.authenuser.model.payload.AuthenUser;
import org.petspa.petcaresystem.authenuser.model.response.JwtResponseDTO;
import org.petspa.petcaresystem.authenuser.model.response.ResponseAPI;
import org.petspa.petcaresystem.authenuser.service.AuthenUserService;
import org.petspa.petcaresystem.enums.Gender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/petspa/user")
@CrossOrigin
@Tag(name = "User", description = "User Management API")
@ApiResponses(value = {
    @ApiResponse (responseCode = "200", content = { @Content(schema = @Schema(implementation = AuthenUser.class), mediaType = "application/json") }),
    @ApiResponse (responseCode = "404", content = { @Content(schema = @Schema()) }),
    @ApiResponse (responseCode = "500", content = { @Content(schema = @Schema()) }) })
public class AuthenUserController {

    @Autowired
    AuthenUserService authenUserService;
    @Autowired
    private HttpServletRequest request;


    @GetMapping("/login")
    public JwtResponseDTO login(@RequestParam(value = "email") String email,
                                @RequestParam(value = "password") String password){
        JwtResponseDTO jwtResponseDTO = authenUserService.login(email, password);
        return jwtResponseDTO;
    }

    @PostMapping("/register")
    public ResponseAPI register(@RequestParam(value = "address") String address,
                                @RequestParam(value = "email") String email,
                                @RequestParam(value = "full_name") String full_name,
                                @RequestParam(value = "gender") Gender gender,
                                @RequestParam(value = "password") String password,
                                @RequestParam(value = "phone") Long phone){
        AuthenUser authenUser = new AuthenUser();
        authenUser.setAddress(address.trim());
        authenUser.setEmail(email.trim());
        authenUser.setFull_name(full_name.trim());
        authenUser.setGender(gender);
        authenUser.setPassword(password.trim());
        authenUser.setPhone(phone);
        ResponseAPI responseAPI = authenUserService.register(authenUser);
        return responseAPI;
    }

    @GetMapping("/getAllUser")
    public List<AuthenUser> getAllAccount(){
        return authenUserService.getAllUser();
    }

    @GetMapping("/getSession")
    public String getSession(HttpServletRequest request) {
        HttpSession session = request.getSession();
        String jwtToken = (String) session.getAttribute("jwtToken");

        if (jwtToken != null) {
            if (!jwtToken.isEmpty()) {
                return "JWT Token: " + jwtToken;
            } else {
                return "JWT Token do not exist or it empty";
            }
        } else {
            return "Session don't have JWT Token";
        }
    }
}
