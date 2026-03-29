# Company Resource Access System

A Spring Boot security project featuring JWT authentication, role-based and permission-based access control, and fine-grained endpoint protection using Spring Security's pre/post authorization and filtering — built to demonstrate enterprise-grade security patterns with Keycloak as the identity provider.

---

## Table of Contents

- [Tech Stack](#tech-stack)
- [Project Objectives](#project-objectives)
- [System Overview](#system-overview)
- [Security Features](#security-features)
- [API Endpoints](#api-endpoints)
- [Keycloak Configuration](#keycloak-configuration)
- [In-Memory Database](#in-memory-database)
- [Testing with Postman](#testing-with-postman)
- [Token Management](#token-management)
- [What This Project Demonstrates](#what-this-project-demonstrates)

---

## Tech Stack

**Backend**

| Technology | Purpose |
|---|---|
| Spring Boot 3.5 | Application framework |
| Spring Security 7 | Method-level security |
| OAuth2 Resource Server | JWT validation |
| Java 21 | Runtime |
| Lombok | Boilerplate reduction |

**Identity Provider**

| Technology | Purpose |
|---|---|
| Keycloak (v23+) | Identity & Access Management |
| Realm + Client Roles | RBAC model |
| Custom Token Mappers | Department & permissions in JWT |
| Refresh Token Flow | Session management |
| Token Introspection | Logout support |

---

## Project Objectives

This project demonstrates how real-world applications secure access to resources based on:

- **RBAC roles** — coarse-grained access by user role
- **Domain-level permissions** — fine-grained control via `PROJECT_READ`, `PROJECT_WRITE`
- **User attributes** — department-based filtering (ABAC)
- **Resource ownership** — users can only access resources they own
- **Hierarchical roles** — `APP_ADMIN > APP_MANAGER > APP_USER`

---

## System Overview

Users authenticate against **Keycloak** and receive a **JWT access token**. All API calls include this token, and Spring Boot validates it, extracts roles and claims, then enforces:

- Method-level pre/post authorization
- Custom security filters
- ABAC rules in the service layer
- Ownership checks per resource

**Example JWT claims:**

```json
{
  "preferred_username": "arjun.user",
  "department": "IT",
  "resource_access": {
    "company-resource-api": {
      "roles": ["APP_USER", "PROJECT_READ"]
    }
  }
}
```

---

## Security Features

### 1. Role-Based Access Control (RBAC)

Restricts endpoint access based on the user's assigned role.

```java
@PreAuthorize("hasAnyRole('APP_ADMIN', 'APP_MANAGER', 'APP_USER')")
```

### 2. Permission-Based Access Control (PBAC)

Uses Keycloak client roles as fine-grained permissions.

```java
@PreAuthorize("hasRole('PROJECT_WRITE')")
```

### 3. Attribute-Based Access Control (ABAC)

Access decisions driven by user attributes embedded in the token — specifically `department`.

```java
if (!user.getDepartment().equals(project.getDepartment()))
    throw new AccessDeniedException("Department mismatch");
```

### 4. Ownership-Based Access

Users can view only the projects they own. Admins bypass this restriction.

### 5. Role Hierarchy

Configured in Spring Security so higher roles inherit lower-role permissions automatically:

```
APP_ADMIN   > APP_MANAGER
APP_MANAGER > APP_USER
```

### 6. PreFilter and PostFilter

Filters collection inputs and outputs based on security rules — without requiring manual iteration in service code.

### 7. PostAuthorize

Authorization is evaluated **after** the method executes, allowing response-time ownership checks:

```java
@PostAuthorize(
  "returnObject.ownerUsername == authentication.token.claims['preferred_username'] " +
  "or hasRole('APP_ADMIN')"
)
```

### 8. Current User Context

A `CurrentUserService` extracts and exposes the following from the active JWT:

- Username
- Roles
- Department claim
- Permissions
- User ID

---

## API Endpoints

All endpoints require a valid Keycloak access token. Access is enforced through a combination of RBAC, permissions, ABAC, ownership rules, and Spring Security's pre/post authorization.

### Project Endpoints

| Method | Endpoint | Description | Security Mechanism |
|--------|----------|-------------|-------------------|
| `GET` | `/api/projects/{id}` | Get project by ID | RBAC + Ownership |
| `GET` | `/api/projects` | List all accessible projects | RBAC + Ownership |
| `POST` | `/api/projects` | Create a new project | PBAC (`PROJECT_WRITE`) + ABAC |
| `GET` | `/api/projects/department` | List projects by user's department | ABAC |
| `GET` | `/api/projects/filtered` | Post-filtered project list | `@PostFilter` |
| `POST` | `/api/projects/bulk-details` | Fetch projects by IDs | `@PreFilter` |
| `GET` | `/api/projects/secure/{id}` | Get project with owner check | `@PostAuthorize` |
| `GET` | `/api/projects/role-hierarchy` | Verify role inheritance | Role Hierarchy |

---

## Keycloak Configuration

### Realm

- **Realm Name:** `company-access-realm`

### Client

- **Client ID:** `company-resource-api`
- **Client Type:** `Bearer-only`

> The API only validates tokens — it does not request them. No client secret is needed.

### Client Roles

Create the following roles under the `company-resource-api` client:

| Role | Type |
|------|------|
| `APP_ADMIN` | RBAC |
| `APP_MANAGER` | RBAC |
| `APP_USER` | RBAC |
| `PROJECT_READ` | Permission |
| `PROJECT_WRITE` | Permission |

### Attribute Mapper — Department

Add a **User Attribute Mapper** to propagate the `department` attribute into the JWT:

| Field | Value |
|-------|-------|
| Name | `department` |
| Mapper Type | User Attribute |
| User Attribute | `department` |
| Token Claim Name | `department` |
| Claim JSON Type | `String` |

This enables ABAC (department-based access decisions) inside the API.

### Example Test User

| Field | Value |
|-------|-------|
| Username | `arjun.user` |
| Roles | `APP_USER`, `PROJECT_READ` |
| Attribute | `department = IT` |

---

## In-Memory Database

A simple in-memory repository simulates persistence for demo purposes:

- Auto-generated IDs
- Full CRUD operations
- Bulk fetch support
- Pre-seeded sample projects

No external database setup is required.

---

## Testing with Postman

Set the following environment variables in Postman:

```
{{baseUrl}}   = http://localhost:8080
{{projects}}  = /api/projects
```

**Examples**

Get project by ID (RBAC):
```
GET {{baseUrl}}{{projects}}/1
```

Get projects filtered by department (ABAC):
```
GET {{baseUrl}}{{projects}}/department
```

Bulk fetch by IDs (PreFilter):
```
POST {{baseUrl}}{{projects}}/bulk-details
Body: [101, 102, 103]
```

Ownership-checked fetch (PostAuthorize):
```
GET {{baseUrl}}{{projects}}/secure/101
```

---

## Token Management

### Refresh Access Token

```
POST /realms/company-internal/protocol/openid-connect/token

grant_type=refresh_token
refresh_token=<your_refresh_token>
```

### Logout (Invalidate Refresh Token)

```
POST /realms/company-internal/protocol/openid-connect/logout

refresh_token=<your_refresh_token>
```

> **Note:** JWT access tokens are stateless and cannot be invalidated before they expire. Only refresh tokens can be revoked via the logout endpoint.

---

## What This Project Demonstrates

This is a security-heavy, enterprise-style implementation covering the concepts most commonly asked about in backend and security-focused interviews:

- Keycloak integration as an external identity provider
- JWT validation and structured claim extraction
- RBAC, PBAC, and ABAC — implemented and clearly differentiated
- Attribute-based decisions using token claims
- Hierarchical role structure with automatic permission inheritance
- `@PreFilter` and `@PostFilter` for collection-level filtering
- `@PostAuthorize` for response-time authorization checks
- `CurrentUserContext` pattern for clean, reusable principal extraction
- Real-world API security design combining ownership, permissions, and domain rules
