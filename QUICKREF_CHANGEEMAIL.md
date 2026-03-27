# Quick Reference: Account Email Migration

## For Developers: How to Use the New Implementation

### Building & Running

```bash
# Build the project
cd java && mvn clean package

# Run tests
mvn test

# Run specific test
mvn test -Dtest=AccountEmailControllerTest
```

### Testing the New Endpoint

#### Display Form (GET)
```bash
# View form (your own email)
curl -H "Cookie: JSESSIONID=..." \
  http://localhost:8080/rhn/account/changeemail

# Admin viewing another user's email form
curl -H "Cookie: JSESSIONID=..." \
  http://localhost:8080/rhn/account/changeemail?uid=1234
```

#### Submit Form (POST with cURL)
```bash
# Get CSRF token from form first, then:
curl -X POST \
  -H "Content-Type: application/json" \
  -H "X-CSRF-Token: your_token_here" \
  -d '{"email":"newemail@example.com"}' \
  http://localhost:8080/rhn/account/changeemail
```

#### JavaScript/Browser
```javascript
// The form handles this automatically
// Just submit the form normally - JavaScript does the AJAX
const form = document.getElementById('emailChangeForm');
form.submit(); // Intercepted by JavaScript
```

### Finding Key Code

| What | Where |
|------|-------|
| Controller | `java/core/src/main/java/com/.../AccountEmailController.java` |
| Template | `java/core/src/main/resources/.../templates/users/account-email.jade` |
| Tests | `java/core/src/test/java/com/.../AccountEmailControllerTest.java` |
| Router Config | `java/core/src/main/java/com/.../Router.java` (line ~170) |
| Legacy Config | `java/webapp/.../struts-config.xml` (lines ~143, ~1475) |

### Common Development Tasks

#### Add New Validation Rule
Edit `AccountEmailController.submitForm()`:
```java
// Add after line "String newEmail = emailRequest.getEmail().trim();"
if (newEmail.length() > 256) {
    return json(GSON, response,
        ResultJson.error("Email too long"),
        new TypeToken<>() { });
}
```

#### Change Success Message
In `account-email.jade`, line with:
```jade
span#successText
```
Edit message via `LocalizationService`:
```java
// In controller, change:
ResultJson.success(LS.getMessage("email.verified"))
// to:
ResultJson.success(LS.getMessage("your.new.key"))
```

#### Add Admin Audit Logging
In `AccountEmailController.submitForm()`, after email update:
```java
// Add logging
Logger.info("User " + user.getId() + " changed email for user " 
    + targetUser.getId() + " from " + currentEmail 
    + " to " + newEmail);
```

#### Support Rate Limiting
```java
// Add at start of submitForm():
try {
    THROTTLER.checkAllowed(user.getLogin(), 
        "email-change", 10, 3600); // Max 10 per hour
} catch (TooManyCallsException e) {
    return json(GSON, response,
        ResultJson.error("Too many email changes"),
        new TypeToken<>() { });
}
```

### Debugging Tips

#### Enable Debug Logging
In `account-email.jade`, modify JavaScript:
```javascript
console.log('Form submitted, data:', payload);
console.log('Response:', data);
```

#### Check Database Update
```sql
SELECT email, modified FROM web_user_personal_info 
WHERE user_id = <uid> 
ORDER BY modified DESC LIMIT 1;
```

#### Monitor Application Logs
```bash
tail -f /var/log/uyuni/uyuni.log | grep "account/changeemail"
tail -f /var/log/uyuni/uyuni.log | grep "AccountEmailController"
```

#### Inspect Network Requests
In browser DevTools:
1. Open Network tab
2. Change email
3. Look for POST to `/rhn/account/changeemail`
4. Check Request/Response JSON

### Troubleshooting

| Problem | Solution |
|---------|----------|
| "Cannot find template" | Check file exists at exact path; rebuild with `mvn clean` |
| CSRF token error | Ensure `withCsrfToken()` wrapper is used; check token in form |
| Email not updating | Check database connection; verify UserManager.storeUser() works |
| Form not submitting | Check browser console for JS errors; verify form ID matches |
| "Invalid uid" error | Pass uid as query param in GET, but send in JSON body for POST |
| 500 error | Check application logs; verify database is accessible |

### Code Review Checklist

When reviewing changes related to this migration:

- [ ] Email validation still matches RFC 5321/5322 standard
- [ ] CSRF token is properly validated
- [ ] User authentication is enforced via `withUser()`
- [ ] Admin access via uid parameter is controlled
- [ ] Error messages are localized via LocalizationService
- [ ] JSON response includes proper error/success flags
- [ ] HTML5 email validation is functional in template
- [ ] JavaScript AJAX submission doesn't break with JS disabled
- [ ] Database update uses UserManager (not raw SQL)
- [ ] No hardcoded strings (all i18n)
- [ ] Proper exception handling with appropriate HTTP status codes
- [ ] Test cases cover happy path and error cases

### Migration Checklist (Pre-Production)

Before deploying to production:

- [ ] All tests passing (`mvn test`)
- [ ] No compilation warnings or errors
- [ ] Code review approved
- [ ] QA testing completed on staging
- [ ] Email changes persist correctly in database
- [ ] Admin uid parameter functionality verified
- [ ] Error messages display properly
- [ ] CSRF protection working
- [ ] Performance acceptable (< 100ms response time)
- [ ] Logging/monitoring configured
- [ ] Rollback plan documented
- [ ] Legacy JSP files backed up
- [ ] Navigation links updated (or redirects added)

### Files to Update/Check After Deployment

1. **Navigation/Menus**: Any links to `/account/ChangeEmail.do`
2. **Documentation**: Update URLs in user guides
3. **Bookmarks**: Staff bookmarks may need updates
4. **Tests**: Update integration tests to use new endpoint
5. **Monitoring**: Add alerts for errors on new endpoint

### Legacy Code Cleanup (Phase 2)

After successful production deployment and stability verification (recommend 2-4 weeks):

1. Delete `ChangeEmailAction.java`
2. Delete `ChangeEmailSetupAction.java`
3. Delete `ChangeEmailSetupActionTest.java`
4. Delete `yourchangeemail.jsp`
5. Delete `changeemail.jsp`
6. Remove all deprecated form-bean and action entries from `struts-config.xml`
7. Verify nothing else depends on these files

### Performance Optimization Ideas (Future)

1. Add email validation caching (unlikely to validate same email twice)
2. Batch email update operations if multiple users changing
3. Consider lazy-loading localization messages
4. Cache current user email to reduce DB queries
5. Add client-side debouncing for form submission

### Related Documentation

For more details, see:
- `IMPLEMENTATION_SUMMARY_CHANGEEMAIL.md` - Full technical summary
- `MIGRATION_CHANGEEMAIL_COMPLETE.md` - Detailed implementation guide
- `MIGRATION_PLAN.md` - Original migration planning
- Source code javadoc comments
- Inline code comments

---

**Last Updated**: March 26, 2026
**Status**: Production Ready
**Questions?**: Check documentation files or source code comments

