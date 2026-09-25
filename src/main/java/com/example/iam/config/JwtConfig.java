package com.example.iam.config;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

@Configuration
public class JwtConfig {

    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }

    @Bean
    public JwtDecoder jwtDecoder(RSAKey rsaKey) {
        try {
            return NimbusJwtDecoder.withPublicKey(rsaKey.toRSAPublicKey()).build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize JWT decoder", e);
        }
    }

    @Bean
    public RSAKey rsaKey() {
        Path privateKeyPath = Path.of(System.getProperty("user.dir"), "keys", "iam-private.pem");
        Path publicKeyPath = Path.of(System.getProperty("user.dir"), "keys", "iam-public.pem");

        try {
            Files.createDirectories(privateKeyPath.getParent());

            if (Files.exists(privateKeyPath) && Files.exists(publicKeyPath)) {
                RSAPrivateKey privateKey = loadPrivateKey(privateKeyPath);
                RSAPublicKey publicKey = loadPublicKey(publicKeyPath);
                return new RSAKey.Builder(publicKey)
                        .privateKey(privateKey)
                        .keyID("iam-rsa-key")
                        .build();
            }

            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair keyPair = generator.generateKeyPair();

            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            String publicPem = "-----BEGIN PUBLIC KEY-----\n"
                    + Base64.getMimeEncoder(64, System.lineSeparator().getBytes()).encodeToString(publicKey.getEncoded())
                    + "\n-----END PUBLIC KEY-----\n";
            String privatePem = "-----BEGIN PRIVATE KEY-----\n"
                    + Base64.getMimeEncoder(64, System.lineSeparator().getBytes()).encodeToString(privateKey.getEncoded())
                    + "\n-----END PRIVATE KEY-----\n";

            Files.writeString(privateKeyPath, privatePem);
            Files.writeString(publicKeyPath, publicPem);

            return new RSAKey.Builder(publicKey)
                    .privateKey(privateKey)
                    .keyID("iam-rsa-key")
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to initialize RSA keys", e);
        }
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource(RSAKey rsaKey) {
        return new ImmutableJWKSet<>(new JWKSet(rsaKey));
    }

    private RSAPrivateKey loadPrivateKey(Path path) throws Exception {
        String pem = Files.readString(path)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] data = Base64.getDecoder().decode(pem);
        return (RSAPrivateKey) java.security.KeyFactory.getInstance("RSA")
                .generatePrivate(new java.security.spec.PKCS8EncodedKeySpec(data));
    }

    private RSAPublicKey loadPublicKey(Path path) throws Exception {
        String pem = Files.readString(path)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] data = Base64.getDecoder().decode(pem);
        return (RSAPublicKey) java.security.KeyFactory.getInstance("RSA")
                .generatePublic(new java.security.spec.X509EncodedKeySpec(data));
    }
}
