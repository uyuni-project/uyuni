# Implementation Checklist & Next Steps

## ✅ Completed Implementation Tasks

### Java Backend
- [x] Add `withOrgAdmin` import to AccountEmailController
- [x] Register new route: `/manager/users/:uid/account/email`
- [x] Create `displayOwnEmailForm()` handler method
- [x] Create `displayAdminEmailForm()` handler method
- [x] Refactor `displayForm()` to accept contextMode and uid parameters
- [x] Add `targetUserName` to model passed to template
- [x] Add `contextMode` to model passed to template
- [x] Update `submitForm()` to handle uid from request body
- [x] Add `uid` field to `EmailChangeRequest` class
- [x] Verify no Java compilation errors

### Template
- [x] Update `account-email.jade` to pass `targetUserName` to renderer
- [x] Update `account-email.jade` to pass `contextMode` to renderer
- [x] Single template for both routes (no duplication)

### React/TypeScript Frontend
- [x] Add `contextMode` to `RendererProps` type in index.tsx
- [x] Add `targetUserName` to `RendererProps` type in index.tsx
- [x] Pass new props to `AccountEmailForm` component
- [x] Add `contextMode` to `Props` interface in form.tsx
- [x] Add `userName` to `Props` interface in form.tsx
- [x] Implement `getPageTitle()` with context-aware logic
- [x] Implement `getInstructions()` with context-aware logic
- [x] Add conditional rendering for target username display
- [x] Update form submission to include `uid` in admin mode
- [x] Verify no TypeScript compilation errors

### Documentation
- [x] Created IMPLEMENTATION_SUMMARY_TWO_ROUTES.md
- [x] Created IMPLEMENTATION_FLOW_DIAGRAM.md
- [x] This checklist and next steps document

---

## 🔍 Pre-Build Verification

### Required Actions Before Building

1. **Localization Strings** (⚠️ REQUIRED)
   - [ ] Add missing i18n keys to `web/po/*.po` files or UI translations:
     ```properties
     # If using Java message properties:
     admin.changeemail.title=Change Email Address for User {0}
     admin.changeemail.instructions=Enter the new email address for the user.
     admin.changeemail.for=Changing email for: {0}
     ```
   - If `t()` function resolves to existing keys, this may be optional
   - Test with fallback messages to verify functionality

2. **Route Registration** (✅ AUTOMATIC)
   - Routes registered via `AccountEmailController.initRoutes(jade)`
   - Called in `Router.java` at line 167
   - No additional Router.java changes needed

---

## 🛠️ Build and Test

### Step 1: Build Frontend
```bash
cd /home/rmateus/projects/suma/spacewalk/web
npm run clean
npm install
npm run build
# or
npm run all
```

### Step 2: Build Java Backend
```bash
cd /home/rmateus/projects/suma/spacewalk/java
mvn clean package
```

### Step 3: Deploy to Test Server
```bash
# Follow existing deployment procedures
# or use docker if available
```

### Step 4: Manual Testing

#### Test 1: User's Own Email Change
1. Login as regular user
2. Navigate to: `/rhn/account/changeemail`
3. Verify:
   - [ ] Title shows "Change Email Address" (or localized equivalent)
   - [ ] Instructions displayed
   - [ ] Email field pre-filled with current email
   - [ ] No username display below title
   - [ ] Change email and verify success
   - [ ] Page reloads after 2 seconds
   - [ ] New email is reflected in user profile

#### Test 2: Admin Email Change
1. Login as organization admin
2. Navigate to: `/rhn/manager/users/{uid}/account/email` (replace uid with test user)
3. Verify:
   - [ ] Title includes target username (e.g., "Change Email Address for User docker")
   - [ ] "Changing email for: [username]" text displayed
   - [ ] Email field pre-filled with target user's current email
   - [ ] Can change email successfully
   - [ ] Page reloads after 2 seconds
   - [ ] Target user's email is updated

#### Test 3: Authorization Enforcement
1. Login as regular user
2. Try to access: `/rhn/manager/users/{other_uid}/account/email`
3. Verify:
   - [ ] Access denied (403 or redirected to home)
   - [ ] Error message shown

#### Test 4: URL Semantics
1. Verify two distinct URLs in browser history:
   - [ ] `/rhn/account/changeemail` for own email
   - [ ] `/rhn/manager/users/{uid}/account/email` for admin

#### Test 5: Error Handling
1. Test empty email:
   - [ ] Error message shown: "Email is required"
   
2. Test invalid email format:
   - [ ] Error message shown: "Invalid email address"
   
3. Test same email as current:
   - [ ] Error message shown: "Email address is the same as current"
   
4. Test network error:
   - [ ] Graceful error handling with user message

### Step 5: Browser Developer Tools Testing
1. Open Network tab in DevTools
2. Submit email change form
3. Verify:
   - [ ] POST request to `/rhn/manager/api/account/changeemail`
   - [ ] In own mode: JSON body contains `email` only (no `uid`)
   - [ ] In admin mode: JSON body contains both `email` and `uid`
   - [ ] Response is JSON with success/error status

---

## 📋 Files Modified Summary

| File | Changes | Lines |
|------|---------|-------|
| `java/.../AccountEmailController.java` | Routes, handlers, context logic | +~50 |
| `java/.../templates/account-email.jade` | Added props | +2 |
| `web/.../account/email/index.tsx` | Added context props | +4 |
| `web/.../account/email/form.tsx` | Context-aware rendering | +30 |
| **Total** | **Implementation** | **~86 lines** |

---

## 🚀 Future Enhancements

### Phase 2: Admin User Details UI Integration
When admin user details page is migrated to React:
1. Add "Change Email" button/link in Addresses tab
2. Link to `/rhn/manager/users/{uid}/account/email`
3. Or open modal with email change form

### Phase 3: Navigation Updates
- [ ] Update MenuTree.java if needed for admin context
- [ ] Add link in admin user details page (when available)
- [ ] Consider batch email change operations

### Phase 4: Performance & Analytics
- [ ] Add logging for admin email changes
- [ ] Track usage metrics
- [ ] Monitor error rates

---

## 📞 Troubleshooting

### Issue: Jade template not rendering new props
**Solution:** Clear browser cache and rebuild frontend
```bash
npm run clean && npm install && npm run build
```

### Issue: `contextMode` prop undefined in React
**Solution:** Verify Jade template includes the prop in renderer call
- Check: `account-email.jade` line with `contextMode: '#{contextMode}'`

### Issue: Admin route returns 403 Forbidden
**Solution:** Verify user has OrgAdmin role
- Check: User details → Roles section
- Or use test user with admin role already assigned

### Issue: Email not updating in database
**Solution:** Check UserManager.storeUser() for errors
- Verify database permissions
- Check logs for SQL errors
- Verify targetUser object is correct

### Issue: "Cannot find name 't'" TypeScript error at runtime
**Solution:** Verify i18n is properly initialized
- Global `t()` function should be available
- Check: Other components use `t()` successfully
- Fallback: Use English strings if translations missing

---

## 🎯 Success Criteria

Implementation is considered **successful** when:

1. ✅ Both routes accessible without errors
2. ✅ Own email change works correctly
3. ✅ Admin email change works with proper auth
4. ✅ Context mode is properly displayed (title, instructions)
5. ✅ Target username shown in admin context
6. ✅ No SQL injection or auth bypass vulnerabilities
7. ✅ Error messages clear and helpful
8. ✅ Page reloads after successful change
9. ✅ Form validation working on both frontend and backend
10. ✅ Two distinct URLs in browser history

---

## 📝 Rollback Plan

If issues arise:
1. Revert `AccountEmailController.java` to previous version
2. Revert `account-email.jade` to previous version
3. Revert `index.tsx` and `form.tsx` to previous version
4. Rebuild and redeploy

Original functionality will be restored as only one route (`/manager/account/changeemail`) would be active.

---

## 📞 Questions & Support

**Q: Why two routes instead of one?**
- A: Provides URL clarity, better auth enforcement, and better browser history. See `IMPLEMENTATION_SUMMARY_TWO_ROUTES.md` for comparison.

**Q: Will this break existing functionality?**
- A: No. The original route `/manager/account/changeemail` still works. New route is an addition.

**Q: Can users access admin route?**
- A: No. `withOrgAdmin()` wrapper ensures only org admins can access `/manager/users/:uid/account/email`.

**Q: How is the localization working?**
- A: Uses global `t()` function that translates keys to messages. New keys must be added to translation files.

---

## 📚 Related Documentation

- IMPLEMENTATION_SUMMARY_TWO_ROUTES.md - Architecture and benefits
- IMPLEMENTATION_FLOW_DIAGRAM.md - Visual flow diagrams
- AGENTS.md - Project AI coding guidelines
- CONTRIBUTING.md - Development standards


