package com.frederikhandberg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.frederikhandberg.adapter.UserDetailsImpl;
import com.frederikhandberg.config.StompPrincipal;
import com.frederikhandberg.model.User;
import com.frederikhandberg.repository.UserRepository;
import com.frederikhandberg.service.JwtService;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                if (jwtService.validateToken(token)) {
                    String username = jwtService.getUsernameFromToken(token);
                    User user = userRepository.findByUsernameOrEmail(username, username)
                            .orElseThrow(() -> new RuntimeException("User not found: " + username));

                    UserDetailsImpl userDetails = new UserDetailsImpl(user);

                    UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userDetails,
                            null, userDetails.getAuthorities());

                    // accessor.setUser(auth);
                    // accessor.getSessionAttributes().put("user", user);

                    accessor.setUser(new StompPrincipal(user.getId().toString()));

                    accessor.getSessionAttributes().put("userId", user.getId());
                    accessor.getSessionAttributes().put("username", user.getUsername());
                    accessor.getSessionAttributes().put("user", user);

                    System.out.println(
                            "WebSocket authenticated: User ID " + user.getId() + ", Username: " + user.getUsername());
                }
            }
        } else {
            // if (accessor.getUser() != null) {
            // SecurityContextHolder.getContext().setAuthentication((Authentication)
            // accessor.getUser());
            // } else {
            // User user = (User) accessor.getSessionAttributes().get("user");
            // if (user != null) {
            // UserDetailsImpl userDetails = new UserDetailsImpl(user);
            // UsernamePasswordAuthenticationToken auth = new
            // UsernamePasswordAuthenticationToken(
            // userDetails, null, userDetails.getAuthorities());
            // SecurityContextHolder.getContext().setAuthentication(auth);
            // }

            // Approach #2:

            Long userId = (Long) accessor.getSessionAttributes().get("userId");
            String username = (String) accessor.getSessionAttributes().get("username");

            if (userId != null && username != null) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("User not found: " + userId));

                UserDetailsImpl userDetails = new UserDetailsImpl(user);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(auth);

                accessor.getSessionAttributes().put("user", user);
            }
            // }
        }

        return message;
    }
}
