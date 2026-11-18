package pe.edu.upeu.autoandes.security;

import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import pe.edu.upeu.autoandes.dto.ErrorResponseDTO;

@Component
@RequiredArgsConstructor
public class RestSecurityError {
  private final tools.jackson.databind.ObjectMapper mapper;

  public void escribir(
      HttpServletRequest request, HttpServletResponse response, HttpStatus status, String mensaje)
      throws java.io.IOException {
    response.setStatus(status.value());
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    mapper.writeValue(
        response.getWriter(),
        new ErrorResponseDTO(
            java.time.LocalDateTime.now(java.time.ZoneId.of("America/Lima")),
            status.value(),
            status.getReasonPhrase(),
            mensaje,
            request.getRequestURI(),
            null));
  }
}
