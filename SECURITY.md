# Security Review

## Summary

This project uses Spring Security, Spring Authorization Server, BCrypt password hashing, and Redis-backed throttling to protect the core IAM functions.

## Checks Performed

- Plaintext passwords are not stored; BCrypt is used for hashing.
- The app stores no production secrets in source files.
- JWT signing uses an RSA keypair generated locally at startup.
- Authorization is enforced at the HTTP layer.
- Sensitive admin endpoints are restricted to ROLE_ADMIN.
- Rate limiting protects login and OTP flows using Redis.
- MFA is required for enabled users before final authentication completion.
- Password reset tokens are short-lived and stored with expiry metadata.

## Risks to Address in Production

- Use a reverse proxy to terminate TLS.
- Externalize JWT private keys into a managed secret store.
- Replace mock email/SMS delivery with real providers.
- Enforce stricter production rate limits and separate tenant policies.
- Add audit retention and data masking policies.

## Security Controls Present

- BCrypt password encoding
- RBAC via roles and authorities
- JWT signing via RSA public/private pair
- OAuth 2.0 and OIDC endpoint security
- Redis-backed activity throttling
- MFA support for TOTP and OTP
- Audit logging for important events

## Security Notes

This is a local development-ready IAM server. It should not be used as-is for production without completing the production hardening steps above.
