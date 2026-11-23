// se implementa el endpoint de inicio de sesion de la plataforma logistica
package com.logistica.logistica.controller;

import com.logistica.logistica.dto.LoginRequest;
import com.logistica.logistica.dto.LoginResponse;
import com.logistica.logistica.service.JwtService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager; // coordinar la autenticación.
    private final JwtService jwtService; // genera y administra los tokens.

    public AuthController( 
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login( // recibe solicitudes HTTP POST.
            @Valid @RequestBody LoginRequest request) { // indica que los datos llegan en el cuerpo de la solicitud, generalmente como JSON.

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken( // se constrye un objeto UsernamePass... con email y contraseña
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtService.generarToken(request.getEmail()); // Una vez autenticado el usuario, se genera un token JWT asociado a su email.
        LoginResponse response = new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpiration()
        );

        return ResponseEntity.ok(response);
    }
}