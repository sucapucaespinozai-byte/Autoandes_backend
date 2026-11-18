package pe.edu.upeu.autoandes.config;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.upeu.autoandes.security.*;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtService jwt;
  private final UsuarioDetailsService usuarios;
  private final RestSecurityError error;

  protected void doFilterInternal(
      jakarta.servlet.http.HttpServletRequest r,
      jakarta.servlet.http.HttpServletResponse s,
      jakarta.servlet.FilterChain chain)
      throws jakarta.servlet.ServletException, java.io.IOException {
    String auth = r.getHeader("Authorization");
    if (auth != null && auth.startsWith("Bearer ")) {
      try {
        var user = usuarios.loadUserByUsername(jwt.validar(auth.substring(7)));
        if (!user.isEnabled())
          throw new org.springframework.security.authentication.DisabledException(
              "Cuenta inactiva");
        SecurityContextHolder.getContext()
            .setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                    user, null, user.getAuthorities()));
      } catch (org.springframework.security.oauth2.jwt.JwtException
          | org.springframework.security.core.AuthenticationException
          | IllegalArgumentException e) {
        SecurityContextHolder.clearContext();
        error.escribir(
            r,
            s,
            org.springframework.http.HttpStatus.UNAUTHORIZED,
            "Se requiere un token válido y vigente");
        return;
      }
    }
    chain.doFilter(r, s);
  }
}
