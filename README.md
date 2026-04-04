# Spring Boot Security Learning Platform

This project is a modular, extensible playground for mastering Spring Security concepts with Spring Boot 3.2.x and Java 21+. It is designed as a learning system demonstrating clean architecture and multiple interchangeable security strategies.

## Features & Architecture

The application is structured into the following domains:

*   **Core:** Shared entities (`User`, `Role`), DTOs (`LoginRequest`, `AuthResponse`), and core configuration properties.
*   **Auth-Modules:** Plug-and-play authentication strategies.
    *   *Implemented:* JWT (Stateless).
    *   *Extensible for:* Session-auth, OAuth2, LDAP, MFA.
*   **Authorization:** Contains role-based access control (RBAC) and attribute-based access control (ABAC).
    *   RBAC uses `@PreAuthorize("hasRole('ADMIN')")`.
    *   ABAC uses a custom `PermissionEvaluator` bean (`@PreAuthorize("@abac.canAccess(#id, authentication)")`).
*   **API Security:** Features such as Rate Limiting using Bucket4j, injected seamlessly into the `SecurityFilterChain`.
*   **Data Security:** Utility classes to handle BCrypt hashing and AES encryption simulation for data at rest.

## Configuration

Switch security features dynamically by modifying `src/main/resources/application.yml`:

```yaml
security:
  auth: jwt # Future options: session
  authorization: rbac # Or abac
  jwt:
    secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
    expiration-ms: 3600000
  rate-limit:
    enabled: true
    capacity: 10
```

## Exploring & Debugging

Two default users are created on startup:
1. `user` / `password` (Role: USER)
2. `admin` / `password` (Role: ADMIN)

### Example Flow (Postman / cURL)

1.  **Login to get a token:**
    ```bash
    curl -X POST http://localhost:8080/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"admin", "password":"password"}'
    ```
    *(Extract the `token` from the response).*

2.  **Access an RBAC endpoint:**
    ```bash
    curl -H "Authorization: Bearer <TOKEN>" http://localhost:8080/api/demo/admin
    ```

3.  **Access an ABAC endpoint:**
    ```bash
    curl -H "Authorization: Bearer <TOKEN>" http://localhost:8080/api/demo/resource/5
    ```

4.  **Check Security Context (Debug):**
    ```bash
    curl -H "Authorization: Bearer <TOKEN>" http://localhost:8080/debug/security-context
    ```

## Extending the System

To add a new authentication strategy (e.g., Session-based):
1.  Create a new package `com.example.security.auth.session`.
2.  Implement your `SessionSecurityConfig` annotated with `@ConditionalOnProperty(prefix = "security", name = "auth", havingValue = "session")`.
3.  Configure `HttpSecurity` differently (e.g., enable CSRF, `SessionCreationPolicy.IF_REQUIRED`).
4.  Change `application.yml` -> `security.auth: session` to switch seamlessly.

## Common Mistakes to Avoid
*   **Monolithic Configs:** Do not put all rules in one massive `SecurityConfig`. Use filter chaining and modular classes.
*   **Hardcoding Roles:** Using Enums is good, but relying purely on static roles instead of Permissions or Attributes (ABAC) makes scaling hard.
*   **Exposing Entities:** Always use DTOs for login requests and responses. Never serialize your database `User` object directly to the client.
*   **Token Secrets:** Never hardcode secrets in code; always use environment variables in production.

## Microservices Security (Bonus)

If converting this to a microservices architecture:
*   Extract the Token Generation logic to a centralized **Authorization Server** (like Keycloak or Spring Authorization Server).
*   Convert this Spring Boot app into an **OAuth2 Resource Server** by adding `spring-boot-starter-oauth2-resource-server` and configuring it to validate incoming JWTs against the Authorization Server's JWK Set URI.
*   Rely on an API Gateway for rate-limiting instead of handling it at the application level.
