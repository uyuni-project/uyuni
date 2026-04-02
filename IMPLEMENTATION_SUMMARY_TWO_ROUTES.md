# Implementation Summary: Two Routes with Single Template and Context Flags

## Overview
Successfully implemented the improved Approach 2 architecture: **Two distinct routes using a single template with context flags to distinguish between user and admin contexts.**

## Architecture

```
Two Entry Points:
├── /manager/account/changeemail          (User's own email)
│   └── withUser() + contextMode="own"
├── /manager/users/:uid/account/email     (Admin changing user's email)
│   └── withOrgAdmin() + contextMode="admin"
└── Shared Template: account-email.jade
    └── Single React Component (contextMode-aware)
```

## Changes Made

### 1. Java Controller (`AccountEmailController.java`)

**Routes Registered:**
```java
// User's own email change
get("/manager/account/changeemail",
    withCsrfToken(withUser(AccountEmailController::displayOwnEmailForm)), jade);

// Admin changing user's email  
get("/manager/users/:uid/account/email",
    withCsrfToken(withOrgAdmin(AccountEmailController::displayAdminEmailForm)), jade);

// Shared API endpoint
post("/manager/api/account/changeemail",
    asJson(withUser(AccountEmailController::submitForm)));
```

**Key Features:**
- ✅ Two separate handler methods (`displayOwnEmailForm` and `displayAdminEmailForm`)
- ✅ Single shared `displayForm()` method with `contextMode` parameter
- ✅ Extracts `uid` from path parameter (`:uid`) in admin route
- ✅ Uses `withOrgAdmin()` wrapper to ensure only org admins can access admin route
- ✅ Passes `contextMode` ("own" or "admin") to template
- ✅ Populates `targetUserName` for admin context display
- ✅ Single shared API endpoint that accepts optional `uid` in request body

**Model Data Passed to Template:**
```json
{
  "currentEmail": "user@example.com",
  "targetUserId": 123,
  "targetUserName": "docker",
  "contextMode": "admin",
  "pageInstructions": "...",
  "buttonLabel": "Update",
  "csrfToken": "..."
}
```

### 2. Jade Template (`account-email.jade`)

**Changes:**
- ✅ Added `targetUserName` to renderer props
- ✅ Added `contextMode` flag to renderer props
- Single template serves both routes

```jade
module.renderer(
    'account-email-form', '#{docsLocale}',
    {
        currentEmail: '#{currentEmail}',
        targetUserId: #{targetUserId},
        targetUserName: '#{targetUserName}',
        contextMode: '#{contextMode}'
    }
)
```

### 3. React Components

#### `index.tsx` - Renderer Entry Point
```typescript
type RendererProps = {
  currentEmail?: string;
  targetUserId?: number;
  targetUserName?: string;
  contextMode?: "own" | "admin";
};

export const renderer = (
  id: string,
  locale: string,
  { currentEmail = "", targetUserId, targetUserName = "", contextMode = "own" }: RendererProps = {}
) => {
  // Passes all props to AccountEmailForm component
};
```

#### `form.tsx` - Form Component
**Context-Aware Features:**
- ✅ Conditional page title based on `contextMode`
  - Admin: `"admin.changeemail.title"` (with username parameter)
  - Own: `"yourchangeemail.jsp.title"`

- ✅ Conditional instructions based on `contextMode`
  - Admin: `"admin.changeemail.instructions"`
  - Own: `"yourchangeemail.instructions"`

- ✅ Display target username when in admin mode
  - Shows: "Changing email for: [username]"

- ✅ Passes `uid` to API only when in admin mode
  - In own mode: no uid
  - In admin mode: `userId` from props

```typescript
const handleSubmit = async (event: React.FormEvent<HTMLFormElement>) => {
  const response = await Network.post(
    "/rhn/account/changeemail",
    JSON.stringify({
      email: email.trim(),
      uid: props.userId,  // Only populated in admin context
    }),
    "application/json"
  );
};
```

## Localization Keys Required

New localization strings needed in `.properties` files:
```properties
# Admin context titles
admin.changeemail.title=Change Email Address for User {0}
admin.changeemail.instructions=Enter the new email address for the user
admin.changeemail.for=Changing email for: {0}
```

## Benefits of This Implementation

| Aspect | Benefit |
|--------|---------|
| **Code Reuse** | Single template + single React component for both routes |
| **Maintainability** | Template logic centralized; no duplication |
| **URL Clarity** | Two distinct URLs clearly show the operation context |
| **Security** | Different auth wrappers per route (`withUser` vs `withOrgAdmin`) |
| **Extensibility** | Easy to add new contexts by adding routes pointing to same template |
| **Explicit Context** | React component receives clear `contextMode` flag |
| **Browser History** | Separate URLs provide proper browser history |

## Comparison to Single Route Approach

| Factor | Single Route | Two Routes (Implemented) |
|--------|-------------|------------------------|
| Code lines | ~25 | ~40 |
| URL semantics | `/changeemail?uid=optional` | `/changeemail` + `/users/:uid/email` |
| Template files | 1 | 1 |
| Route handlers | 1 | 2 |
| Context clarity | Implicit | Explicit with flag |
| Auth enforcement | Single wrapper | Two wrappers (better control) |
| Browser history | Single URL | Clear separation |

## Files Modified

1. **Java Backend:**
   - `java/core/src/main/java/com/suse/manager/webui/controllers/users/AccountEmailController.java`
     - ✅ Added `withOrgAdmin` import
     - ✅ Registered `/manager/users/:uid/account/email` route
     - ✅ Created `displayOwnEmailForm()` and `displayAdminEmailForm()` handlers
     - ✅ Updated `displayForm()` signature
     - ✅ Updated `submitForm()` to handle uid from request body
     - ✅ Added `uid` field to `EmailChangeRequest` class

2. **Template:**
   - `java/core/src/main/resources/com/suse/manager/webui/templates/users/account-email.jade`
     - ✅ Added `targetUserName` to renderer props
     - ✅ Added `contextMode` to renderer props

3. **React/TypeScript:**
   - `web/html/src/manager/account/email/index.tsx`
     - ✅ Added `targetUserName` to RendererProps type
     - ✅ Added `contextMode` to RendererProps type
     - ✅ Passed new props to AccountEmailForm

   - `web/html/src/manager/account/email/form.tsx`
     - ✅ Added `userName` and `contextMode` to Props interface
     - ✅ Implemented `getPageTitle()` method with context-aware logic
     - ✅ Implemented `getInstructions()` method with context-aware logic
     - ✅ Added conditional rendering for target username
     - ✅ Updated API call to send `uid` when in admin context

## Testing Recommendations

### Manual Testing Paths

1. **Own Email Change:**
   - Navigate to `/manager/account/changeemail`
   - Verify title: "Change Email Address" (or localized equivalent)
   - Verify instructions shown
   - Change email and verify success

2. **Admin Email Change:**
   - Navigate to `/manager/users/{uid}/account/email` (requires org admin role)
   - Verify title includes target username
   - Verify "Changing email for: [username]" shown below title
   - Change email and verify success

3. **Authorization:**
   - Verify non-org-admin users cannot access `/manager/users/:uid/account/email`
   - Verify users can only change their own email on `/manager/account/changeemail`

### Unit Tests
- Route registration with correct auth wrappers
- Context mode detection and passing
- Email validation in both modes
- Permission enforcement via `withOrgAdmin` wrapper

## Future Enhancements

1. **Admin User Details UI Migration:**
   - When user details page is migrated to React, add link from Addresses tab to new admin route
   - Route: `/manager/users/:uid/account/email`

2. **Menu Item Addition:**
   - Could add admin menu item linking to user list with email change action
   - Alternatively: Add link from user details page React component when it's developed

3. **Batch Operations:**
   - Extend API to support batch email changes (future enhancement)

## Notes

- The implementation follows the established codebase patterns for context-aware operations
- Uses the same context flag approach as image management (`isAdmin` pattern in `content_management/view.jade`)
- No breaking changes to existing user email change functionality
- Backward compatible: old route at `/manager/account/changeemail` still works
- Ready for integration with React-based admin user details page when available


