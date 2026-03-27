# Account/ChangeEmail Migration - Implementation Complete

**Date Completed**: March 26, 2026
**Status**: Ready for Testing & Code Review
**Migration Type**: Struts Framework → Spark Framework with Jade Templates
**Complexity**: LOW
**Recommendation**: Jade Template (NOT React)

## Summary of Changes

This migration modernizes the legacy `/account/ChangeEmail.do` page by:
1. Converting from Struts Actions + JSP to Spark controller + Jade template
2. Implementing JSON-based AJAX responses for better UX
3. Adding HTML5 email validation on the client-side
4. Maintaining full backward compatibility with admin user lookup (uid parameter)
5. Preserving all legacy validation and business logic

## Files Created

### 1. **AccountEmailController.java**
**Location**: `java/core/src/main/java/com/suse/manager/webui/controllers/users/AccountEmailController.java`

**Responsibilities**:
- `initRoutes(JadeTemplateEngine jade)` - Registers Spark routes
  - `GET /rhn/account/changeemail` - Display form with current email
  - `POST /rhn/account/changeemail` - Submit new email (JSON request/response)

- `displayForm(Request, Response, User)` - ModelAndView for form rendering
  - Supports `uid` query parameter for admin user lookups
  - Populates current email, page instructions, and button labels
  - Injects CSRF token

- `submitForm(Request, Response, User)` - JSON response handler
  - Parses JSON request body with email field
  - Validates email is different from current
  - Uses RFC 5321/5322 validation via `InternetAddress`
  - Returns `ResultJson` with success/error message
  - Supports admin changing other users' emails via `uid` parameter

**Key Features**:
- Full error handling with appropriate HTTP status codes
- Localization support (messages from LocalizationService)
- Email format validation matching legacy behavior
- Request validation and null-safety
- Proper exception handling and user feedback

### 2. **account-email.jade**
**Location**: `java/core/src/main/resources/com/suse/manager/webui/templates/users/account-email.jade`

**Features**:
- Responsive Bootstrap form layout (3-col labels, 6-col inputs)
- HTML5 email input type with required attribute
- Client-side validation with visual feedback
- JavaScript AJAX submission handler
- Success/error message areas (toggle visibility)
- CSRF token support
- Form disabling during submission with loading state
- Auto-reload on successful submission (2 second delay)

**Enhancements over legacy JSP**:
- Modern HTML5 form validation
- Real-time client feedback without page reload
- Better accessibility with proper form structure
- Automatic success message display
- Loading state feedback to user

### 3. **AccountEmailControllerTest.java**
**Location**: `java/core/src/test/java/com/suse/manager/webui/controllers/users/AccountEmailControllerTest.java`

**Test Coverage**:
- Email change request JSON parsing
- Valid email address validation
- Invalid email address handling
- Request object creation and mutation

## Files Modified

### 1. **Router.java**
**Location**: `java/core/src/main/java/com/suse/manager/webui/Router.java`

**Changes**:
- Added import: `com.suse.manager.webui.controllers.users.AccountEmailController`
- Added initialization: `AccountEmailController.initRoutes(jade);` (placed in Account Management section)

### 2. **struts-config.xml**
**Location**: `java/webapp/src/main/webapp/WEB-INF/struts-config.xml`

**Changes**:
- **Deprecated** `<form-bean name="changeEmailForm">` (commented with explanation)
- **Deprecated** all legacy action mappings:
  - `/users/ChangeEmail` (admin email change display)
  - `/users/ChangeEmailSubmit` (admin email change submit)
  - `/account/ChangeEmail` (user email change display)
  - `/account/ChangeEmailSubmit` (user email change submit)

All deprecated sections include comments referencing the new Spark endpoint.

## Files Removed/Deprecated (Ready for Deletion)

The following legacy files should be removed after successful migration and testing:

1. **Struts Action Classes**:
   - `java/core/src/main/java/com/redhat/rhn/frontend/action/user/ChangeEmailSetupAction.java`
   - `java/core/src/main/java/com/redhat/rhn/frontend/action/user/ChangeEmailAction.java`
   - `java/core/src/test/java/com/redhat/rhn/frontend/action/user/test/ChangeEmailSetupActionTest.java`

2. **Legacy JSP Templates**:
   - `java/webapp/src/main/webapp/WEB-INF/pages/user/edit/yourchangeemail.jsp`
   - `java/webapp/src/main/webapp/WEB-INF/pages/admin/users/changeemail.jsp`

3. **Struts Configuration** (after full migration):
   - Remove commented-out entries from struts-config.xml

## API Specifications

### GET Endpoint (Display Form)
```
GET /rhn/account/changeemail
Query Parameters:
  - uid (optional): Target user ID (for admin use)

Response: HTML page with Jade template

Example:
  GET /rhn/account/changeemail           # Edit own email
  GET /rhn/account/changeemail?uid=1234  # Admin editing user 1234's email
```

### POST Endpoint (Submit Form)
```
POST /rhn/account/changeemail
Content-Type: application/json
Headers:
  - X-CSRF-Token: <csrf_token>

Request Body:
{
  "email": "newemail@example.com",
  "uid": "1234" (optional, for admin use)
}

Response:
{
  "success": true,
  "data": "email.verified message"
}

OR

{
  "success": false,
  "data": "Error message"
}

HTTP Status Codes:
  - 200 OK: Valid request (check success field for result)
  - 400 Bad Request: Invalid uid parameter format
  - 500 Internal Server Error: Database or unexpected error
```

## Validation Logic

### Email Validation
- **Format**: RFC 5321/5322 standard (via `jakarta.mail.internet.InternetAddress`)
- **Not Empty**: Required field
- **Must Differ**: Cannot be same as current email
- **Client-Side**: HTML5 email input validation
- **Server-Side**: Same validation as legacy system

## Request/Response Examples

### Successful Email Change
```bash
# Request
curl -X POST http://localhost:8080/rhn/account/changeemail \
  -H "Content-Type: application/json" \
  -H "X-CSRF-Token: abc123" \
  -d '{"email": "newemail@example.com"}'

# Response (200 OK)
{
  "success": true,
  "data": "Your email has been verified"
}
```

### Error: Same Email
```bash
# Response (200 OK with error flag)
{
  "success": false,
  "data": "You provided the same email address"
}
```

### Error: Invalid Email
```bash
# Response (200 OK with error flag)
{
  "success": false,
  "data": "The email address provided is invalid: xyz@"
}
```

## Backward Compatibility

### Maintained Features
✓ Email validation matching legacy behavior
✓ User lookup with uid parameter for admin access
✓ Localization support (all messages via LocalizationService)
✓ CSRF protection
✓ Proper error messages and user feedback

### API Changes
- **URL Changed**: `/account/ChangeEmail.do` → `/rhn/account/changeemail`
- **Request Method**: Still POST for submission
- **Response Format**: Changed from page redirect to JSON
- **Request Format**: Changed from form-encoded to JSON body

### Migration Path for Front-End
If URLs are referenced in navigation or links:
```
OLD: /account/ChangeEmail.do?uid=1234
NEW: /rhn/account/changeemail?uid=1234
```

## Testing Checklist

### Unit Tests
- [ ] `AccountEmailControllerTest` passes
- [ ] Email parsing and validation logic works
- [ ] Invalid emails are rejected

### Integration Tests
- [ ] GET `/rhn/account/changeemail` displays form
- [ ] GET with `uid` parameter works (admin access)
- [ ] POST successfully changes email
- [ ] POST with invalid email returns error
- [ ] POST with same email returns error
- [ ] CSRF token validation works
- [ ] Localization messages display correctly

### Manual Testing
- [ ] Access form at `/rhn/account/changeemail`
- [ ] Try changing to valid new email → success
- [ ] Try changing to same email → error message
- [ ] Try invalid email → error message
- [ ] Admin access with `?uid=X` parameter works
- [ ] Database is updated correctly
- [ ] Page auto-reloads on success

### Browser Testing
- [ ] HTML5 validation prevents form submission with invalid email
- [ ] Form remains functional with JavaScript disabled
- [ ] Success/error messages display properly
- [ ] Loading state shows during submission
- [ ] CSRF token is included in requests

## Performance Notes

- **Template Rendering**: Jade template is compiled at startup (minimal overhead)
- **Database Queries**: Single lookup (existing user) + one update (new email)
- **JSON Serialization**: Uses Gson (standard project choice)
- **No N+1 Issues**: Simple 1:1 user lookup

## Security Considerations

✓ **CSRF Protection**: Enforced via `withCsrfToken()` helper
✓ **Authentication**: Required via `withUser()` helper
✓ **Authorization**: Respects existing Uyuni role checks
✓ **Input Validation**: Email format validated (RFC 5321/5322)
✓ **SQL Injection**: No raw SQL (uses ORM via UserManager)
✓ **XSS Protection**: Jade template auto-escapes variables

## Deployment Considerations

### Pre-Deployment
1. Build Maven project: `cd java && mvn clean package`
2. Run tests: `mvn test`
3. Code review of new files
4. Database: No schema changes needed

### Deployment Steps
1. Deploy updated WAR file
2. Restart Tomcat/Java application
3. Monitor logs for errors
4. Test both endpoints from UI

### Post-Deployment
1. Verify `/rhn/account/changeemail` is accessible
2. Test email change functionality
3. Monitor application logs for errors
4. Update any bookmarks/documentation linking old URL

### Rollback Plan
1. If issues occur, revert to previous WAR deployment
2. Clear browser cache to ensure old JSP isn't cached
3. All data remains intact (Struts config changes don't affect data)

## Migration Timeline

| Phase | Task | Status |
|-------|------|--------|
| 1 | Code Implementation | ✅ COMPLETE |
| 2 | Router Registration | ✅ COMPLETE |
| 3 | Deprecate Struts Config | ✅ COMPLETE |
| 4 | Unit Tests | ✅ COMPLETE |
| 5 | Code Review | ⏳ PENDING |
| 6 | QA Testing | ⏳ PENDING |
| 7 | Deploy to Staging | ⏳ PENDING |
| 8 | Integration Testing | ⏳ PENDING |
| 9 | Deploy to Production | ⏳ PENDING |
| 10 | Remove Legacy Code | ⏳ PENDING |

## Known Limitations / Future Enhancements

1. **Rate Limiting**: Could be added via `ThrottlingService` if needed
2. **Audit Logging**: Could log email changes for compliance
3. **Verification Email**: Could send verification before activation
4. **Password Confirmation**: Could require password for sensitive changes
5. **Email Notifications**: Could notify user of email change

## Questions & Clarifications

**Q**: Should we add a redirect from `/account/ChangeEmail.do` to new endpoint?
**A**: Yes, recommended to add a Spark route that redirects old URL to new one.

**Q**: When should legacy action classes be deleted?
**A**: After full testing cycle and confirmation no other code depends on them.

**Q**: Do we need to update all navigation links?
**A**: Yes, but can happen during deployment or shortly after.

---

## Implementation Complete - Ready for Review

This migration is **code-complete** and ready for:
1. **Code Review** by project maintainers
2. **QA Testing** in staging environment
3. **Integration Testing** with full system
4. **Production Deployment**

All legacy code is safely commented/deprecated and can be removed after successful testing.

For questions or issues, refer to the inline code comments in `AccountEmailController.java` and `account-email.jade`.

