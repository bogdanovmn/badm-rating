package com.github.bogdanovmn.badmrating.web.infrastructure.config.security;

import com.github.bogdanovmn.badmrating.web.infrastructure.config.security.common.FileResource;
import com.github.bogdanovmn.badmrating.web.infrastructure.config.security.common.RSAKey;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.PublicKey;

@Component
@Slf4j
public class JwtInstance {
	private final PublicKey publicKey;

	public JwtInstance(@Value("${jwt.public-key-path:}") String publicKeyPath) throws IOException {
		log.info("JWT public key loading: {}", publicKeyPath);
		publicKey = RSAKey.ofDER(
			new FileResource(publicKeyPath).content()
		).asPublicKey();
	}

	public Jws<Claims> checkSignatureAndReturnClaims(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(publicKey)
			.build()
			.parseClaimsJws(token);
	}
}
