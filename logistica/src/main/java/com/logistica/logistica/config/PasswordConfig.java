// Mecanismo de cifrado de contraseñas mediante Spring Security y BCrypt.
package com.logistica.logistica.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration; // indica que la clase tiene configuraciones de Spring.
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; 
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration // Importado mediante annotation.Configuration. Indica a Spring que debe procesar la clase como fuente de configuracion.
public class PasswordConfig {

    @Bean // Indica que el metodo genera un objeto que Spring administrará.
    public PasswordEncoder passwordEncoder() { // PasswordEncoder es la interfaz que define las operaciones de codificacion y verificacion.authController
                                                //  de contraseñas. El usuario recibe su contraseña, el UsuarioService recibe los datos, utiliza PasswordEncoder para codificar la contraseña y guarda el resultado en PostgreSQL.
        return new BCryptPasswordEncoder(); // Implementacion del algoritmo BCrypt para proteger contraseñas.
    }
}