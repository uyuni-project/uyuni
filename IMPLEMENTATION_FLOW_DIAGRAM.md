# Implementation Flow Diagram

## Request Flow: Own Email Change

```
User navigates to: /manager/account/changeemail
         ↓
   [Spark Router]
         ↓
withUser() → [Check user authenticated]
         ↓
withCsrfToken() → [Add CSRF token]
         ↓
displayOwnEmailForm(request, response, user)
         ↓
displayForm(request, user, "own", null)
         ↓
[Java Controller populates model]
  - currentEmail: user.getEmail()
  - targetUserId: user.getId()
  - targetUserName: user.getLogin()
  - contextMode: "own"
  - Other properties...
         ↓
[Renders: account-email.jade]
         ↓
Jade Template: spaImportReactPage('account/account-email')
         ↓
[React Renderer called with props]
  - contextMode: "own"
  - currentEmail: "user@example.com"
  - targetUserId: 123
  - targetUserName: "username"
         ↓
[AccountEmailForm Component]
  - getPageTitle() returns: "Change Your Email Address"
  - getInstructions() returns: "Please enter your new email address below."
  - No username display (contextMode === "own")
         ↓
[User enters email and clicks Update]
         ↓
Form.handleSubmit() → Network.post("/rhn/account/changeemail", {
  email: "newemail@example.com",
  uid: undefined  ← NOT sent in own mode
})
         ↓
[API Handler: submitForm]
  - uid is null → uses current logged-in user
  - Updates: user.setEmail(newEmail)
  - Returns: success response
         ↓
[React Component reloads page after 2s]
```

## Request Flow: Admin Email Change

```
Admin navigates to: /manager/users/456/account/email
         ↓
   [Spark Router]
         ↓
withOrgAdmin() → [Check user is org admin] ✓
         ↓
withCsrfToken() → [Add CSRF token]
         ↓
displayAdminEmailForm(request, response, adminUser)
         ↓
uid = request.params(":uid")  → 456
         ↓
displayForm(request, adminUser, "admin", 456)
         ↓
[Java Controller populates model]
  - targetUser = UserManager.lookupUser(adminUser, 456)
  - currentEmail: targetUser.getEmail()
  - targetUserId: 456
  - targetUserName: "docker"  ← Target user, not admin
  - contextMode: "admin"
  - Other properties...
         ↓
[Renders: account-email.jade]  ← SAME template
         ↓
Jade Template: spaImportReactPage('account/account-email')
         ↓
[React Renderer called with props]
  - contextMode: "admin"
  - currentEmail: "gino@example.com"
  - targetUserId: 456
  - targetUserName: "docker"  ← Different from current user
         ↓
[AccountEmailForm Component]
  - getPageTitle() returns: "Change Email Address for User docker"
  - getInstructions() returns: "Enter the new email address for the user"
  - Username display: "Changing email for: docker"  ✓
         ↓
[Admin enters email and clicks Update]
         ↓
Form.handleSubmit() → Network.post("/rhn/account/changeemail", {
  email: "gino-new@example.com",
  uid: 456  ← SENT in admin mode
})
         ↓
[API Handler: submitForm]
  - uid = 456 → looks up target user
  - targetUser = UserManager.lookupUser(currentAdmin, 456)
  - Updates: targetUser.setEmail(newEmail)
  - Returns: success response
         ↓
[React Component reloads page after 2s]
```

## Component Tree

```
SPA Page Renderer
├── index.tsx
│   ├── MessagesContainer (Toastr notifications)
│   └── AccountEmailForm
│       ├── Form Header
│       │   ├── Page Title (context-aware)
│       │   └── Target User Info (if admin mode)
│       ├── Form Body
│       │   ├── Email Input
│       │   ├── Instructions (context-aware)
│       │   └── Submit Button
│       └── Event Handlers
│           ├── handleEmailChange()
│           └── handleSubmit()
│               └── Network.post() → API
```

## Props Flow

```
Jade Template Props
├─ currentEmail: "user@example.com"
├─ targetUserId: 123
├─ targetUserName: "docker"
├─ contextMode: "own" | "admin"
└─ [other props]
    ↓
index.tsx renderer()
    ↓
<AccountEmailForm
  currentEmail={currentEmail}
  userId={targetUserId}
  userName={targetUserName}
  contextMode={contextMode}
/>
    ↓
form.tsx component
    ├── getPageTitle(): string (uses contextMode)
    ├── getInstructions(): string (uses contextMode)
    └── handleSubmit() (sends uid if contextMode === "admin")
```

## Authentication Flow

```
Route: /manager/account/changeemail
  ├─ withUser() 
  │  └─ Any authenticated user ✓
  └─ Allows own email change only

Route: /manager/users/:uid/account/email
  ├─ withOrgAdmin()
  │  └─ Only organization admins ✓
  └─ Can change any user's email in org

Route: /manager/api/account/changeemail (POST)
  ├─ withUser()
  │  └─ Any authenticated user ✓
  └─ Backend checks:
     ├─ If uid null/missing: update current user
     └─ If uid provided: verify current user is OrgAdmin
                        then update target user
```

## State Management

### AccountEmailForm Component State

```typescript
const [email, setEmail] = useState(props.currentEmail)
  ├─ Initial: pre-filled with current email
  └─ Updated: as user types in input field

const [loading, setLoading] = useState(false)
  ├─ false: Form interactive
  ├─ true: While submitting (button disabled)
  └─ false: After response received
```

## Validation Layers

```
Frontend (React):
├─ HTML5 Email Input Validation (type="email")
├─ Not empty check
├─ Different from current check
└─ Format validation via emailInput.validity

Backend (Java):
├─ Email required check
├─ Email format validation (RFC 5321/5322)
├─ Different from current check
├─ User existence check (uid parameter)
└─ Permission check (admin changing other user)
```

## Error Handling

```
Scenarios:
├─ Empty email → showErrorToastr("error.email_required")
├─ Invalid format → showErrorToastr("error.addr_invalid")
├─ Same as current → showErrorToastr("error.same_email")
├─ User not found → 400 Bad Request with error message
├─ Network error → showErrorToastr("Network error: ...")
└─ Server error (500) → showErrorToastr("An unexpected error occurred")

Success:
└─ showSuccessToastr(response.data || "email.verified")
   └─ Auto-reload after 2 seconds
```

## Configuration Summary

| Configuration | Value |
|---------------|-------|
| User Route | `/manager/account/changeemail` |
| Admin Route | `/manager/users/:uid/account/email` |
| API Endpoint | `/manager/api/account/changeemail` (POST) |
| User Auth | `withUser()` |
| Admin Auth | `withOrgAdmin()` |
| Template | `account-email.jade` (shared) |
| Component | `AccountEmailForm` (shared) |
| Context Flag | `contextMode: "own" \| "admin"` |
| User Extraction | Path param (`:uid`) for admin |


