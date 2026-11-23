// define quién puede aceder, que operaciones puede realizar y como se autentican los usuarios.
package com.logistica.logistica.config;

import com.logistica.logistica.service.UsuarioDetailsService; // busca lod atos del usuario en el sistema para autenticarlo.
import com.logistica.logistica.security.JwtAuthenticationFilter; // intercepta las solicitudes HTTP y comprueba los tokens JWT.

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final UsuarioDetailsService usuarioDetailsService; // obtiene los datos del usuario.
    private final PasswordEncoder passwordEncoder; // comprueba las contraseñas mediante BCrypt.
    private final JwtAuthenticationFilter jwtAuthenticationFilter; // valida los tokens JWT de las solicitudes.

    public SecurityConfig( // Spring inyecta automáticamente estos componentes mediante inyección de dependencias por constructor.
            UsuarioDetailsService usuarioDetailsService,
            PasswordEncoder passwordEncoder,
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.usuarioDetailsService = usuarioDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(List.of("http://localhost:5173"));

        configuration.setAllowedMethods(
            List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(
            List.of("Authorization", "Content-Type")
        );

        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(usuarioDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }
    // configura el proveedor encargado de autenticar usuarios utilizando sus credenciales.

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) // reglas generales de seguridad de la API --> que sol estan permitidas y cuales necesitan aut o determinados roles.
            throws Exception {

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            .csrf(csrf -> csrf.disable())

            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // aplicacion configurada como STATELESS --> cada solicitud protegida debe presentar su propio token JWT.

            .exceptionHandling(exception -> exception // si no esta autenticado, entonces el servidor devuelve 401 unauthorized.
                .authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)
                )
            )

            .authenticationProvider(authenticationProvider())

            .authorizeHttpRequests(auth -> auth // se establecen las reglas de autorizacion para los endpoints.
                // los endpoints publicos permiten que cualquier persona pueda resgistrarse y acceder al login.
                // Registro público
                // Registro público: solamente CLIENTE
                .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()

                // Alta administrativa de repartidores: se controlará en el servicio
                .requestMatchers("/api/auth/**").permitAll()

                // endpoints exclusivos para ADMIN.
                // Gestión de usuarios exclusiva para ADMIN
                // El usuario autenticado puede consultar sus propios datos.
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/usuarios/me"
                ).authenticated()

                // Gestión de usuarios exclusiva para ADMIN.
                .requestMatchers("/api/usuarios/**").hasRole("ADMIN")
                // Gestión de clientes exclusiva para ADMIN
                .requestMatchers("/api/clientes/**").hasRole("ADMIN")
                // Gestión de repartidores exclusiva para ADMIN
                .requestMatchers("/api/repartidores/**").hasRole("ADMIN")

                // endpoints exclusivos para REPARTIDORES.
                // Registrar ubicación propia: exclusivo para REPARTIDOR
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/ubicaciones/mi-ubicacion"
                ).hasRole("REPARTIDOR")
                // Historial propio de rutas: exclusivo para REPARTIDOR
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/rutas/mi-historial"
                ).hasRole("REPARTIDOR")
                // Consulta de rutas asignadas: exclusivo para REPARTIDOR
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/rutas/mis-rutas"
                ).hasRole("REPARTIDOR")
                .requestMatchers("/api/ubicaciones/**").hasRole("ADMIN")
                .requestMatchers(
                    HttpMethod.PATCH,
                    "/api/rutas/*/iniciar",
                    "/api/rutas/*/completar",
                    "/api/rutas/*/pedidos/*/entregar"
                ).hasRole("REPARTIDOR")
                // Consulta de productos: ADMIN y CLIENTE
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/productos",
                    "/api/productos/**"
                ).hasAnyRole("ADMIN", "CLIENTE")

                // Gestión de productos: exclusiva para ADMIN
                .requestMatchers("/api/productos/**").hasRole("ADMIN")
                .requestMatchers("/api/inventario/**").hasRole("ADMIN")
                .requestMatchers("/api/vehiculos/**").hasRole("ADMIN")
                // Historial general de rutas: exclusivo para ADMIN
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/rutas/historial"
                ).hasRole("ADMIN")
                // Gestión de rutas: exclusiva para ADMIN
                .requestMatchers("/api/rutas/**").hasRole("ADMIN")

                // endpoints exclusivos para clientes.
                // Consulta de pedidos propios para CLIENTE
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/pedidos/mis-pedidos/estado"
                ).hasRole("CLIENTE")
                // Consulta del estado de un pedido propio: exclusivo para CLIENTE
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/pedidos/*/estado"
                ).hasRole("CLIENTE")
                // Historial propio de entregas: exclusivo para CLIENTE
                .requestMatchers(
                        HttpMethod.GET,
                        "/api/pedidos/mi-historial"
                ).hasRole("CLIENTE")

                // Creación de pedidos: exclusiva para CLIENTE
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/pedidos"
                ).hasRole("CLIENTE")

                // Consulta de detalles de un pedido propio: exclusivo para CLIENTE
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/pedidos/*/detalles"
                ).hasRole("CLIENTE")
                // Consulta de la ruta asociada a un pedido propio: exclusivo para CLIENTE
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/pedidos/*/ruta"
                ).hasRole("CLIENTE")
                // Gestión de pedidos: exclusiva para ADMIN
                .requestMatchers("/api/pedidos/**").hasRole("ADMIN")       
                // Los demás endpoints requieren autenticación
                .requestMatchers("/ws").permitAll()
                .anyRequest().authenticated() // cualquier endpoint que no haya sido contemplado requiere que el usuario esté autenticado.
            )

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}