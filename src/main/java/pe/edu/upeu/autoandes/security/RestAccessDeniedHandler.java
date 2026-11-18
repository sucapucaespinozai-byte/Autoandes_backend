package pe.edu.upeu.autoandes.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {
  private final RestSecurityError error;

  public void handle(
      jakarta.servlet.http.HttpServletRequest r,
      jakarta.servlet.http.HttpServletResponse s,
      org.springframework.security.access.AccessDeniedException e)
      throws java.io.IOException {
    error.escribir(
        r, s, org.springframework.http.HttpStatus.FORBIDDEN, "No tiene el permiso requerido");
  }
}
