package com.molina.gainstrack.api.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.molina.gainstrack.api.config.GoogleTokenVerifier;
import com.molina.gainstrack.api.config.JwtService;
import com.molina.gainstrack.api.dto.auth.AuthResponse;
import com.molina.gainstrack.api.dto.auth.GoogleAuthRequest;
import com.molina.gainstrack.api.dto.auth.LoginRequest;
import com.molina.gainstrack.api.dto.auth.RegisterRequest;
import com.molina.gainstrack.api.exception.ForbiddenException;
import com.molina.gainstrack.api.exception.NotFoundException;
import com.molina.gainstrack.api.model.User;
import com.molina.gainstrack.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio que maneja la lógica de negocio de autenticación.
 * Coordina el registro de nuevos usuarios y la validación
 * de credenciales en el proceso de login.
 * Soporta autenticación por email/contraseña y por Google OAuth.
 */
@Service
public class AuthService {

    private static final Logger LOG = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final GoogleTokenVerifier googleTokenVerifier;

    /**
     * @param userRepository        repositorio de usuarios
     * @param passwordEncoder       encoder BCrypt para hashear contraseñas
     * @param jwtService            servicio para generar tokens JWT
     * @param authenticationManager gestor de autenticación de Spring Security
     * @param googleTokenVerifier   servicio para verificar tokens de Google OAuth
     */
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       GoogleTokenVerifier googleTokenVerifier) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.googleTokenVerifier = googleTokenVerifier;
    }

    /**
     * Registra un nuevo usuario con email y contraseña.
     * Hashea la contraseña, crea el usuario y retorna un JWT listo para usar.
     * El usuario comienza sin rutinas — debe crear una para iniciar una sesión.
     *
     * @param request datos de registro con name, email y contraseña en texto plano
     * @return AuthResponse con el JWT generado
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String hashedPassword = passwordEncoder.encode(request.password());
        Long userId = userRepository.save(request.name(),
                                          request.email(),
                                          hashedPassword);
        String token = jwtService.generateToken(request.email());
        LOG.info("Nuevo usuario registrado — userId: {}", userId);
        return new AuthResponse(token);
    }

    /**
     * Autentica un usuario con email y contraseña.
     * Verifica que el usuario no sea de Google antes de delegar
     * la validación de credenciales a Spring Security.
     *
     * @param request datos de login con email y contraseña en texto plano
     * @return AuthResponse con el JWT generado
     * @throws NotFoundException  si el usuario no existe
     * @throws ForbiddenException si el usuario está registrado con Google OAuth
     * @throws org.springframework.security.core.AuthenticationException
     *         si las credenciales son incorrectas
     */
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                                  .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (user.getPassword() == null) {
            throw new ForbiddenException("Esta cuenta usa Google para iniciar sesión");
        }

        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(),
                                                                                   request.password()
        ));

        String token = jwtService.generateToken(request.email());
        LOG.info("Login exitoso — userId obtenido por email");
        return new AuthResponse(token);
    }

    /**
     * Autentica o registra un usuario mediante Google OAuth.
     * Verifica el token con Google y maneja tres casos:
     * - Usuario nuevo → crea cuenta y genera JWT
     * - Usuario existente con Google → genera JWT directamente
     * - Usuario existente con contraseña → lanza ForbiddenException
     *
     * @param request DTO con el token de identidad de Google
     * @return AuthResponse con el JWT generado
     * @throws ForbiddenException si el email ya está registrado con contraseña
     * @throws ForbiddenException si el token de Google es inválido
     */
    @Transactional
    public AuthResponse googleLogin(GoogleAuthRequest request) {
        // 1. Se valida token de google para saber si es original
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(request.googleToken());

        // 2. Se busca usuario por email en la base de datos
        Optional<User> existingUser = userRepository.findByEmail(payload.getEmail());

        // Si es que usuario no existe se crea en la base de datos
        if  (existingUser.isEmpty()) {
            Long userId = userRepository.saveGoogleUser((String) payload.get("name"),
                                                        payload.getEmail(),
                                                        payload.getSubject());
            LOG.info("Nuevo usuario registrado — userId: {}", userId);
        } else {
            User user = existingUser.get();
            if (user.getGoogleId() == null) {
                throw new ForbiddenException("Usuario registrado mediante formulario GainsTrack");
            }
            LOG.info("Login con Google exitoso — userId: {}", user.getId());
        }

        String tokenJwt = jwtService.generateToken(payload.getEmail());
        return new AuthResponse(tokenJwt);
    }
}