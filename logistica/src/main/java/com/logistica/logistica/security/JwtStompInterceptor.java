package com.logistica.logistica.security;

import com.logistica.logistica.service.JwtService;
import com.logistica.logistica.service.UsuarioDetailsService;
import com.logistica.logistica.repository.PedidoRepository;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

@Component
public class JwtStompInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UsuarioDetailsService usuarioDetailsService;
    private final PedidoRepository pedidoRepository;

    public JwtStompInterceptor(
        JwtService jwtService,
        UsuarioDetailsService usuarioDetailsService,
        PedidoRepository pedidoRepository
    ) {
        this.jwtService = jwtService;
        this.usuarioDetailsService = usuarioDetailsService;
        this.pedidoRepository = pedidoRepository;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {

        StompHeaderAccessor accessor =
        StompHeaderAccessor.getAccessor(
                message,
                StompHeaderAccessor.class
        );

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authorizationHeader =
                    accessor.getFirstNativeHeader("Authorization");

            if (authorizationHeader == null
                    || !authorizationHeader.startsWith("Bearer ")) {
                throw new AccessDeniedException(
                        "Se requiere un token JWT para conectarse"
                );
            }

            String token = authorizationHeader.substring(7);

            try {
                String email = jwtService.extraerEmail(token);

                if (email == null || !jwtService.esTokenValido(token)) {
                    throw new AccessDeniedException("Token JWT inválido");
                }

                UserDetails usuarioDetails =
                        usuarioDetailsService.loadUserByUsername(email);

                if (!usuarioDetails.isEnabled()) {
                    throw new AccessDeniedException(
                            "El usuario está desactivado"
                    );
                }

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(
                                usuarioDetails,
                                null,
                                usuarioDetails.getAuthorities()
                        );

                accessor.setUser(authentication);

            } catch (AccessDeniedException exception) {
                throw exception;
            } catch (Exception exception) {
                throw new AccessDeniedException(
                        "No se pudo autenticar la conexión WebSocket",
                        exception
                );
            }
        }

         if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            String destination = accessor.getDestination();

            if (destination == null ||
                    !destination.matches("^/topic/pedidos/\\d+/ubicacion$")) {
                throw new AccessDeniedException(
                        "Destino de seguimiento no permitido"
                );
            }

            Authentication authentication =
                    (Authentication) accessor.getUser();

            if (authentication == null) {
                throw new AccessDeniedException(
                        "Se requiere autenticación"
                );
            }

            boolean esAdmin = authentication.getAuthorities()
                    .stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            boolean esCliente = authentication.getAuthorities()
                    .stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_CLIENTE"));

            if (!esAdmin) {

                if (!esCliente) {
                    throw new AccessDeniedException(
                            "No tenés permiso para seguir este pedido"
                    );
                }

                String[] partes = destination.split("/");
                Long pedidoId = Long.valueOf(partes[3]);

                boolean perteneceAlCliente =
                        pedidoRepository.existsByIdAndClienteUsuarioEmail(
                                pedidoId,
                                authentication.getName()
                        );

                if (!perteneceAlCliente) {
                    throw new AccessDeniedException(
                            "No podés consultar la ubicación de este pedido"
                    );
                }
            }
        }
        if (StompCommand.SEND.equals(accessor.getCommand())) {
            throw new AccessDeniedException(
                    "Los clientes no pueden enviar mensajes al servidor por WebSocket"
            );
        }
        return message;
    }
}