package pe.edu.upeu.autoandes.config;

import java.time.*;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final JwtEncoder encoder;
  private final JwtDecoder decoder;
  private final long expiracion;
  private final Clock clock;

  public JwtService(
      @Value("${app.jwt.secret}") String secret,
      @Value("${app.jwt.expiration-seconds}") long expiracion,
      Clock clock) {
    byte[] bytes = Base64.getDecoder().decode(secret);
    if (bytes.length < 32)
      throw new IllegalArgumentException("JWT_SECRET debe tener al menos 256 bits");
    var key = new SecretKeySpec(bytes, "HmacSHA256");
    encoder =
        new NimbusJwtEncoder(
            new com.nimbusds.jose.jwk.source.ImmutableSecret<
                com.nimbusds.jose.proc.SecurityContext>(key));
    var dec = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    dec.setJwtValidator(
        new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(Duration.ZERO), new JwtIssuerValidator("autoandes")));
    decoder = dec;
    this.expiracion = expiracion;
    this.clock = clock;
  }

  public String crear(String username, Set<String> roles, Set<String> permisos) {
    var now = clock.instant();
    var claims =
        JwtClaimsSet.builder()
            .issuer("autoandes")
            .subject(username)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(expiracion))
            .claim("roles", roles)
            .claim("permisos", permisos)
            .build();
    return encoder
        .encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims))
        .getTokenValue();
  }

  public String validar(String token) {
    return decoder.decode(token).getSubject();
  }

  public long expiracion() {
    return expiracion;
  }
}
