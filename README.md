# Company Resource Access System
A Keycloak + Spring Boot Security Learning Project

A Spring Boot security project featuring JWT authentication, role &amp; permission-based access control, and fine-grained endpoint protection using Spring Security’s pre/post authorization and filtering.

Built for learning and demonstrating:

- **Keycloak Identity & Access Management**
- **Spring Boot Security (Spring Security 7)**
- **OAuth2 Resource Server (JWT)**
- **RBAC (Role-Based Access Control)**
- **PBAC (Permission-Based Access Control)**
- **ABAC (Attribute-Based Access Control)**
- **Ownership-based security**
- **PreFilter / PostFilter**
- **PostAuthorize**
- **Role Hierarchy**
- **Current User Context extraction**

## Tech Stack

### Backend
- **Spring Boot 3.5**
- **Spring Security 7** (method-level security)
- **OAuth2 Resource Server**
- **Lombok**
- **Java 21**

### Identity Provider
- **Keycloak (v23+)**
- **Realm roles + client roles**
- **Custom token mappers** (department, permissions)
- **Refresh token flow**
- **Token introspection for logout**

## Project Objectives

This project demonstrates how real-world applications secure access to resources based on:

- **User's RBAC roles**
- **Domain-level permissions** (`PROJECT_READ`, `PROJECT_WRITE`)
- **User attributes** like department
- **Resource ownership**
- **Hierarchical roles** (Admin > Manager > User)

Perfect for interview showcase or security-focused learning.

---

## System Overview

Users log into **Keycloak** → receive **JWT** → call **Spring Boot API**.  
Spring validates the JWT, extracts roles/claims, and applies:

- **Method-level authorization**
- **Custom filters**
- **ABAC rules** in service layer
- **Ownership checks**

Example user attributes passed in token:

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

## Security Features Implemented

### 1️⃣ RBAC – Role-Based Access Control
Example:
```java
@PreAuthorize("hasAnyRole('APP_ADMIN','APP_MANAGER','APP_USER')")
```

### 2️⃣ PBAC – Permission-Based Access Control
(Using Keycloak roles for simplicity)
```java
@PreAuthorize("hasRole('PROJECT_WRITE')")
```

### 3️⃣ ABAC – Attribute-Based Access Control
Department-based filtering:
```java
if (!user.getDepartment().equals(project.getDepartment()))
    throw new AccessDeniedException("Department mismatch");
```

### 4️⃣ Ownership-Based Access
Users can view only projects they own unless admin.

### 5️⃣ Role Hierarchy
Configured:
```
APP_ADMIN   > APP_MANAGER > APP_USER
APP_MANAGER > APP_USER
```

### 6️⃣ PreFilter & PostFilter
Filter requests and responses by data rules.

### 7️⃣ PostAuthorize
Authorization executed after method returns:
```java
@PostAuthorize("returnObject.ownerUsername == authentication.token.claims['preferred_username'] 
                or hasRole('APP_ADMIN')")
```

### 8️⃣ Custom User Extraction
A `CurrentUserService` extracts:
- username  
- roles  
- department claim  
- permissions  
- userId

            |
---

## 🗂️ API Endpoints

All endpoints require a valid **Keycloak access token**.  
Access is enforced through a combination of **RBAC**, **permissions**, **ABAC**, **ownership rules**, and Spring Security’s **pre/post authorization**.

### 📁 Project Endpoints

| Method | Endpoint                         | Description                             | Security                         |
|--------|-----------------------------------|-----------------------------------------|----------------------------------|
| GET    | `/api/projects/{id}`              | Get project by ID                       | RBAC + Ownership                 |
| GET    | `/api/projects`                   | Get all accessible projects             | RBAC + Ownership                 |
| POST   | `/api/projects`                   | Create a new project                    | Permission (`PROJECT_WRITE`) + ABAC |
| GET    | `/api/projects/department`        | Get projects by user's department       | ABAC                             |
| GET    | `/api/projects/filtered`          | Post-filter based on claims             | PostFilter                       |
| POST   | `/api/projects/bulk-details`      | Fetch projects by IDs                   | PreFilter                        |
| GET    | `/api/projects/secure/{id}`       | Post-authorize owner check              | PostAuthorize                    |
| GET    | `/api/projects/role-hierarchy`    | Test role inheritance                   | Role hierarchy                   |

---

## 🧪 Testing Using Postman

Set environment variables:

{{baseUrl}} = http://localhost:8080  
{{projects}} = /api/projects

### Examples

#### Get with RBAC
GET {{baseUrl}}{{projects}}/1

#### Department-based filter
GET {{baseUrl}}{{projects}}/department

#### Bulk project details
POST {{baseUrl}}{{projects}}/bulk-details  
Body: [101,102,103]

#### PostAuthorize example
GET {{baseUrl}}{{projects}}/secure/101

---

## 🧪 Refresh Token & Logout (Keycloak)

### Get new access token:
POST /realms/company-internal/protocol/openid-connect/token  
grant_type=refresh_token

### Logout (invalidate refresh token):
POST /realms/company-internal/protocol/openid-connect/logout  
refresh_token=<token>

> Access tokens cannot be invalidated mid-life (JWT is stateless).

---

## 💾 In-Memory Database

A simple in-memory repository simulating persistence:

- Auto-generated IDs  
- CRUD operations  
- Bulk fetch  
- Pre-seeded sample projects

## 🎯 What This Project Demonstrates

This is a security-heavy, enterprise-style implementation covering nearly every concept you'd be asked about in backend/security interviews:

✔ **Keycloak integration**  
✔ **JWT validation & role extraction**  
✔ **RBAC, PBAC, ABAC**  
✔ **Attribute-based access decisions**  
✔ **Hierarchical role structure**  
✔ **PreFilter / PostFilter usage**  
✔ **PostAuthorize for response-time checks**  
✔ **UserContext pattern for extracting current user details**  
✔ **Real-world API security design with ownership, permissions, and domain rules**
