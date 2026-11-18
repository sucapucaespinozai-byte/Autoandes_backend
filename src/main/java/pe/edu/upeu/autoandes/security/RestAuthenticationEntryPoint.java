package pe.edu.upeu.autoandes.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final RestSecurityError error;

  public void commence(
      jakarta.servlet.http.HttpServletRequest r,
      jakarta.servlet.http.HttpServletResponse s,
      org.springframework.security.core.AuthenticationException e)
      throws java.io.IOException {
    error.escribir(
        r,
        s,
        org.springframework.http.HttpStatus.UNAUTHORIZED,
        "Se requiere un token válido y vigente");
  }
}
