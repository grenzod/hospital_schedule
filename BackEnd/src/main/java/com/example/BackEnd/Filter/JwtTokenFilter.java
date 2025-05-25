package com.example.BackEnd.Filter;

import com.example.BackEnd.Entity.User;
import com.example.BackEnd.Repositories.mysql.TokenRepository;
import com.example.BackEnd.Utils.JWTTokenUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    @Value("${api.prefix}")
    private String apiPrefix;
    private final TokenRepository tokenRepository;
    private final UserDetailsService userDetailsService;
    private final JWTTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            if (isBypassToken(request)) {
                filterChain.doFilter(request, response);
                return;
            }
            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }
            final String token = authHeader.substring(7);
            if (tokenRepository.findByToken(token) == null) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                        "Unauthorized, You have accessed over 3 devices");
                return;
            }

            final String subject = jwtTokenUtil.extractSubject(token);
            if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User userDetails = (User) userDetailsService.loadUserByUsername(subject);
                if (jwtTokenUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }

    private boolean isBypassToken(@NonNull HttpServletRequest request) {
        if (request.getServletPath().contains(String.format("%s/reviews/analyze", apiPrefix)) 
            && request.getMethod().contains("GET")) {
            return false;
        }
        final List<Pair<String, String>> bypassTokens = Arrays.asList(
                Pair.of(String.format("%s/roles", apiPrefix), "GET"),
                Pair.of(String.format("%s/departments", apiPrefix), "GET"),
                Pair.of(String.format("%s/doctors", apiPrefix), "GET"),
                Pair.of(String.format("%s/articles", apiPrefix), "GET"),
                Pair.of(String.format("%s/reviews", apiPrefix), "GET"),
                // Pair.of(String.format("%s/reviews", apiPrefix), "GET"),
                Pair.of(String.format("%s/images", apiPrefix), "GET"),
                Pair.of(String.format("%s/users/register", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/login", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/verify", apiPrefix), "POST"),
                Pair.of(String.format("%s/users/retrieve", apiPrefix), "POST"));

        for (var it : bypassTokens) {
            if (request.getServletPath().contains(it.getFirst()) && request.getMethod().contains(it.getSecond())) {
                return true;
            }
        }
        return false;
    }
}
