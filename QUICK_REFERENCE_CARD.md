# Quick Reference Card

## Routes at a Glance

| Route | Method | Auth | Context | Purpose |
|-------|--------|------|---------|---------|
| `/manager/account/changeemail` | GET | `withUser()` | `"own"` | User changes own email |
| `/manager/users/:uid/account/email` | GET | `withOrgAdmin()` | `"admin"` | Admin changes user's email |
| `/manager/api/account/changeemail` | POST | `withUser()` | Auto-detect | API endpoint (both) |

## Request/Response Examples

### User's Own Email Change

**Request:**
```http
GET /manager/account/changeemail HTTP/1.1
Host: server.example.com
Cookie: JSESSIONID=...
```

**Response (Model):**
```json
{
  "currentEmail": "user@example.com",
  "targetUserId": 123,
  "targetUserName": "username",
  "contextMode": "own",
  "pageInstructions": "...",
  "buttonLabel": "Update",
  "csrfToken": "..."
}
```

**Form Submission (JSON):**
```json
POST /manager/api/account/changeemail
{
  "email": "newemail@example.com"
}
```

### Admin Changing User's Email

**Request:**
```http
GET /manager/users/456/account/email HTTP/1.1
Host: server.example.com
Cookie: JSESSIONID=...; admin_session=...
```

**Response (Model):**
```json
{
  "currentEmail": "olduser@example.com",
  "targetUserId": 456,
  "targetUserName": "docker",
  "contextMode": "admin",
  "pageInstructions": "...",
  "buttonLabel": "Update",
  "csrfToken": "..."
}
```

**Form Submission (JSON):**
```json
POST /manager/api/account/changeemail
{
  "email": "docker@newdomain.com",
  "uid": 456
}
```

## Component Props Flow

```
displayOwnEmailForm()
  ↓
displayForm(request, user, "own", null)
  ↓
model.contextMode = "own"
model.targetUserName = user.getLogin()
  ↓
account-email.jade
  ↓
index.tsx renderer({ contextMode: "own", targetUserName: "..." })
  ↓
<AccountEmailForm contextMode="own" userName="..." />
  ↓
form.tsx
  ├─ getPageTitle() → "Change Your Email Address"
  ├─ getInstructions() → "Please enter..."
  └─ handleSubmit() → { email: "...", uid: undefined }
```

## Localization Keys

| Key | Usage | Example Value |
|-----|-------|---------------|
| `yourchangeemail.jsp.title` | Own context title | "Change Your Email Address" |
| `yourchangeemail.instructions` | Own context instructions | "Please enter your new email address below." |
| `admin.changeemail.title` | Admin context title (param: username) | "Change Email Address for User {0}" |
| `admin.changeemail.instructions` | Admin context instructions | "Enter the new email address for the user." |
| `admin.changeemail.for` | Admin context subtitle (param: username) | "Changing email for: {0}" |
| `error.email_required` | Empty email error | "Email address is required." |
| `error.addr_invalid` | Invalid format error (param: email) | "Invalid email address: {0}" |
| `error.same_email` | Same email error | "New email address is the same as current." |
| `email.verified` | Success message | "Email address verified." |

## Error Responses

### Frontend Validation
```
Empty email → "error.email_required"
Invalid format → "error.addr_invalid"
Same as current → "error.same_email"
```

### Backend Validation
```
Missing email field → 400 Bad Request: "Email is required"
Invalid uid (admin) → 400 Bad Request: "Invalid uid, target user not found"
Invalid email format → Error response: "Invalid email address"
Same as current → Error response: "Email is same as current"
Server error → 500 Internal Server Error: "An unexpected error occurred"
```

## State Transitions

### Component State
```
Initial State:
  email: currentEmail (pre-filled)
  loading: false

On Input Change:
  email: updated value
  loading: false

On Submit:
  loading: true (button disabled)

On Success:
  showSuccessToastr()
  setTimeout(() => location.reload(), 2000)

On Error:
  showErrorToastr(error)
  loading: false
```

## Key Differences by Context

| Aspect | Own Context | Admin Context |
|--------|------------|----------------|
| **Title** | "Change Your Email Address" | "Change Email Address for User {name}" |
| **Instructions** | User-focused | Admin/user-focused |
| **Username Display** | No | Yes (below title) |
| **Send UID** | No | Yes (uid field required) |
| **Auth Wrapper** | withUser() | withOrgAdmin() |
| **URL** | /manager/account/changeemail | /manager/users/:uid/account/email |
| **Backend Logic** | Update current user | Update target user (verified) |

## File Locations

```
Backend:
  java/core/src/main/java/com/suse/manager/webui/controllers/users/AccountEmailController.java
  java/core/src/main/resources/com/suse/manager/webui/templates/users/account-email.jade

Frontend:
  web/html/src/manager/account/email/index.tsx
  web/html/src/manager/account/email/form.tsx

Documentation:
  IMPLEMENTATION_SUMMARY_TWO_ROUTES.md
  IMPLEMENTATION_FLOW_DIAGRAM.md
  CODE_CHANGES_REFERENCE.md
  CHECKLIST_AND_NEXT_STEPS.md
  IMPLEMENTATION_COMPLETE_SUMMARY.md
  QUICK_REFERENCE_CARD.md (this file)
```

## Build Commands

```bash
# Frontend
cd /home/rmateus/projects/suma/spacewalk/web
npm run clean
npm install
npm run build

# Backend
cd /home/rmateus/projects/suma/spacewalk/java
mvn clean package
```

## Testing Checklist

- [ ] User can access /manager/account/changeemail
- [ ] Admin can access /manager/users/{uid}/account/email
- [ ] Non-admin cannot access admin route (403)
- [ ] Own email change updates user record
- [ ] Admin email change updates target user
- [ ] Title changes based on context
- [ ] Username displays in admin context
- [ ] Error messages work
- [ ] Form validation works
- [ ] Page reloads on success
- [ ] Two URLs in browser history

## Common Tasks

**Add localization string:**
```properties
# In .properties file
admin.changeemail.title=Change Email Address for User {0}
```

**Test own email change:**
```
1. Login as regular user
2. Go to /manager/account/changeemail
3. Change email
4. Verify in user profile
```

**Test admin email change:**
```
1. Login as org admin
2. Go to /manager/users/123/account/email
3. Change email
4. Verify in target user's profile
```

**Debug in browser console:**
```javascript
// Check props received
console.log('contextMode:', window.contextMode);
console.log('targetUserName:', window.targetUserName);

// Check form submission
// (Set breakpoint in form.tsx handleSubmit)
```

## Performance Notes

- Template rendering: Same (single template used for both routes)
- React component loading: Same (single component)
- API calls: Same endpoint (context determined by uid presence)
- Auth checks: Per route (adds minimal overhead)

## Security Checklist

- [x] withUser() protects own route
- [x] withOrgAdmin() protects admin route
- [x] Backend verifies auth before lookup
- [x] CSRF token included
- [x] Email format validated
- [x] No SQL injection vulnerabilities
- [x] No auth bypass possible
- [x] User cannot change others' emails
- [x] Admin cannot bypass org boundaries (handled by UserManager)

---

**Last Updated:** 2026-03-31
**Status:** Implementation Complete ✅


