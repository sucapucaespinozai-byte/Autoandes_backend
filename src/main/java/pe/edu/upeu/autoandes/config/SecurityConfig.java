package pe.edu.upeu.autoandes.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import pe.edu.upeu.autoandes.security.*;

@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  java.time.Clock clock() {
    return java.time.Clock.system(java.time.ZoneId.of("America/Lima"));
  }

  @Bean
  FilterRegistrationBean<JwtAuthenticationFilter> evitarDobleFiltro(
      JwtAuthenticationFilter filter) {
    var registration = new FilterRegistrationBean<>(filter);
    registration.setEnabled(false);
    return registration;
  }

  @Bean
  SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationFilter jwt,
      RestAuthenticationEntryPoint entry,
      RestAccessDeniedHandler denied)
      throws Exception {
    return http.csrf(c -> c.disable())
        .cors(Customizer.withDefaults())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(e -> e.authenticationEntryPoint(entry).accessDeniedHandler(denied))
        .authorizeHttpRequests(a ->
            a.requestMatchers(HttpMethod.OPTIONS, "/**")
                .permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login")
                .permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                .permitAll()
                .requestMatchers("/api/v1/auth/me")
                .authenticated()
                .requestMatchers(HttpMethod.GET, "/api/v1/clientes")
                .hasAuthority("VEHICULO_LEER")
                .requestMatchers(HttpMethod.GET, "/api/v1/servicios/**")
                .hasAuthority("SERVICIO_LEER")
                .requestMatchers("/api/v1/servicios/**")
                .hasAuthority("SERVICIO_ESCRIBIR")
                .requestMatchers(HttpMethod.GET, "/api/v1/vehiculos/**")
                .hasAuthority("VEHICULO_LEER")
                .requestMatchers("/api/v1/vehiculos/**")
                .hasAuthority("VEHICULO_ESCRIBIR")
                .requestMatchers(HttpMethod.POST, "/api/v1/ordenes")
                .hasAuthority("ORDEN_REGISTRAR")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/ordenes/*/anular")
                .hasAuthority("ORDEN_ANULAR")
                .requestMatchers(HttpMethod.GET, "/api/v1/ordenes/**")
                .hasAuthority("ORDEN_LEER")
                .requestMatchers("/api/v1/reportes/**")
                .hasAuthority("REPORTE_VER")
                .anyRequest()
                .authenticated())
        .addFilterBefore(jwt, UsernamePasswordAuthenticationFilter.class)
        .build();
  }
}
