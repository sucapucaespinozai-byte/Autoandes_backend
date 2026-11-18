package pe.edu.upeu.autoandes.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.service.AuthService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService service;

  @PostMapping("/login")
  @io.swagger.v3.oas.annotations.security.SecurityRequirements
  public LoginResponseDTO login(@jakarta.validation.Valid @RequestBody LoginRequestDTO d) {
    return service.login(d);
  }

  @GetMapping("/me")
  public UsuarioResponseDTO me(java.security.Principal p) {
    return service.actual(p.getName());
  }
}
