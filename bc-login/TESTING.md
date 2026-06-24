# Login API Testing

## Endpoint

**POST** `/internal/logins/sessions/login`

## Request

**Content-Type:** `application/json`

```json
{
  "username": "admin",
  "password": "Password123!"
}
```

## Success Response (200 OK)

**Response Headers:**
- `Set-Cookie: X-Session-Token=<token>; Path=/; HttpOnly; Max-Age=28800`

**Response Body:**
```json
{
  "personRef": "550e8400-e29b-41d4-a716-446655440000",
  "username": "admin",
  "sessionToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "createdAt": "2026-05-20T14:30:00",
  "expiresAt": "2026-05-21T22:30:00"
}
```

## Error Response (401 Unauthorized)

**When:** Invalid credentials or inactive account

**Response Body:** Empty

## Testing with Postman

Follow these steps in Postman — Postman is the canonical test tool for this endpoint in this project.

1. Create a request
   - Method: `POST`
   - URL: `http://localhost:9455/internal/logins/sessions/login`
   - Headers: `Content-Type: application/json`
   - Body (raw JSON):
     ```json
     {
       "username": "admin",
       "password": "Password123!"
     }
     ```

2. Send the request and verify the response in Postman
   - Status code: `200 OK` on success, `401 Unauthorized` for invalid credentials
   - Response body: JSON `LoginResult` with `personRef`, `username`, `sessionToken`, `createdAt`, `expiresAt`
   - Response headers: the server sets an HTTP-only cookie `Set-Cookie: X-Session-Token=...`. The server also includes an `X-Session-Token: ...` response header for convenience; Postman will show that in the Headers tab.

3. Inspect cookies in Postman
   - After sending the request, click the "Cookies" link (right side of the response panel) to view stored cookies for `localhost` and confirm `X-Session-Token` is present.

4. Using the token in subsequent Postman requests
   - Prefer to let Postman send the cookie automatically (the cookie will be sent if present in Postman's Cookies store).
   - Alternatively, copy the token from the `X-Session-Token` response header or from the response JSON's `sessionToken` field and add a request header `X-Session-Token: <token>` to subsequent requests.

Notes
   - Postman is the only supported testing tool for this endpoint in this repository.

## Notes

- Session duration: **8 hours**
- Session token is automatically set as an HTTP-only cookie
- Username and password credentials must be valid for a registered user

