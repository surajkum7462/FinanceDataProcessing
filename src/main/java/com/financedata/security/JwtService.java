package com.financedata.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtService {

	private final SecretKey secretKey;
	private final long expirationMs;

	public JwtService(
			@Value("${app.jwt.secret}") String secret,
			@Value("${app.jwt.expiration-ms}") long expirationMs) {
		this.secretKey = Keys.hmacShaKeyFor(resolveKeyBytes(secret));
		this.expirationMs = expirationMs;
	}

	private static byte[] resolveKeyBytes(String secret) {
		try {
			if (secret.startsWith("base64:")) {
				return Decoders.BASE64.decode(secret.substring("base64:".length()));
			}
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return digest.digest(secret.getBytes(StandardCharsets.UTF_8));
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 not available", e);
		}
	}

	public String generateToken(UserDetails userDetails) {
		Date now = new Date();
		Date expiry = new Date(now.getTime() + expirationMs);
		return Jwts.builder()
				.subject(userDetails.getUsername())
				.issuedAt(now)
				.expiration(expiry)
				.signWith(secretKey)
				.compact();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		String subject = extractSubject(token);
		return subject.equalsIgnoreCase(userDetails.getUsername()) && !isTokenExpired(token);
	}

	public String extractSubject(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	public <T> T extractClaim(String token, Function<Claims, T> resolver) {
		Claims claims = Jwts.parser()
				.verifyWith(secretKey)
				.build()
				.parseSignedClaims(token)
				.getPayload();
		return resolver.apply(claims);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}
}
