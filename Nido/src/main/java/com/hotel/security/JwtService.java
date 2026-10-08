package com.hotel.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/** Genera y valida los tokens JWT que se usan para autenticar cada peticion (US01). */
@Service
public class JwtService {

    private final SecretKey clave;
    private final long minutosExpiracion;

    public JwtService(@Value("${nido.jwt.secret}") String secreto,
                      @Value("${nido.jwt.expiracion-minutos:120}") long minutosExpiracion) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.minutosExpiracion = minutosExpiracion;
    }

    public String generarToken(UserDetails usuario) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + minutosExpiracion * 60_000);
        List<String> roles = usuario.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("roles", roles)
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(clave)
                .compact();
    }

    /** Devuelve el login guardado en el token. Lanza una excepcion si el token es invalido o expiro. */
    public String obtenerLogin(String token) {
        return leer(token).getSubject();
    }

    public long getMinutosExpiracion() {
        return minutosExpiracion;
    }

    private Claims leer(String token) {
        return Jwts.parser().verifyWith(clave).build().parseSignedClaims(token).getPayload();
    }
}
