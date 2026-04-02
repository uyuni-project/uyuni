# Code Changes Reference

## 1. Java Controller: AccountEmailController.java

### Location
`java/core/src/main/java/com/suse/manager/webui/controllers/users/AccountEmailController.java`

### Import Changes
```java
// ADDED:
import static com.suse.manager.webui.utils.SparkApplicationHelper.withOrgAdmin;
```

### Route Registration
```java
public static void initRoutes(JadeTemplateEngine jade) {
    // Display email change form - user's own email
    get("/manager/account/changeemail",
        withCsrfToken(withUser(AccountEmailController::displayOwnEmailForm)), jade);

    // Display email change form - admin changing user's email
    get("/manager/users/:uid/account/email",
        withCsrfToken(withOrgAdmin(AccountEmailController::displayAdminEmailForm)), jade);

    // Submit email change form (JSON response for AJAX)
    post("/manager/api/account/changeemail",
        asJson(withUser(AccountEmailController::submitForm)));
}
```

### Handler Methods (NEW)
```java
/**
 * Handler for user's own email change form.
 *
 * @param request the HTTP request
 * @param response the HTTP response
 * @param user the currently logged-in user
 * @return ModelAndView containing form data and template
 */
public static ModelAndView displayOwnEmailForm(Request request, Response response, User user) {
    return displayForm(request, user, "own", null);
}

/**
 * Handler for admin email change form.
 *
 * @param request the HTTP request
 * @param response the HTTP response
 * @param user the currently logged-in admin user
 * @return ModelAndView containing form data and template
 */
public static ModelAndView displayAdminEmailForm(Request request, Response response, User user) {
    Long uid = Long.parseLong(request.params(":uid"));
    return displayForm(request, user, "admin", uid);
}
```

### Updated displayForm Method
```java
/**
 * Display the email change form with current email address.
 * Supports both user's own email change and admin changing user's email.
 *
 * @param request the HTTP request
 * @param user the currently logged-in user
 * @param contextMode either "own" (user changing own email) or "admin" (admin changing user's email)
 * @param targetUid the uid of the target user when in admin mode, null for own mode
 * @return ModelAndView containing form data and template
 */
public static ModelAndView displayForm(Request request, User user, String contextMode, Long targetUid) {
    Map<String, Object> model = new HashMap<>();
    LocalizationService ls = LS;

    // Determine target user based on context mode
    User targetUser;
    if ("admin".equals(contextMode)) {
        // Admin route: get user by uid
        targetUser = UserManager.lookupUser(user, targetUid);
        if (targetUser == null) {
            throw new BadParameterException("Invalid uid, target user not found");
        }
    } else {
        // Own route: user changing their own email
        targetUser = user;
    }

    // Populate model for React template
    model.put("currentEmail", targetUser.getEmail());
    model.put("targetUserId", targetUser.getId());
    model.put("targetUserName", targetUser.getLogin());
    model.put("contextMode", contextMode);
    model.put("pageInstructions", ls.getMessage("yourchangeemail.instructions"));
    model.put("buttonLabel", ls.getMessage("message.Update"));
    model.put("csrfToken", request.attribute("csrf_token"));

    // Use React template exclusively
    return new ModelAndView(model, "templates/users/account-email.jade");
}
```

### Updated submitForm Method
```java
/**
 * Submit and process the email change form.
 * Validates email address and updates user record.
 *
 * @param request the HTTP request (expects JSON body with email and optional uid)
 * @param response the HTTP response
 * @param user the currently logged-in user
 * @return JSON response with success or error status
 */
public static String submitForm(Request request, Response response, User user) {
    // Parse request body
    EmailChangeRequest emailRequest = GSON.fromJson(request.body(), EmailChangeRequest.class);
    try {
        String newEmail = emailRequest.getEmail();

        if (newEmail == null || newEmail.trim().isEmpty()) {
            return json(GSON, response, 
                ResultJson.error(LS.getMessage("error.email_required")),
                new TypeToken<>() { });
        }

        newEmail = newEmail.trim();

        // Determine target user (uid in request body indicates admin mode)
        User targetUser;
        Long uid = emailRequest.getUid();
        if (uid != null && uid > 0) {
            // Admin changing user's email
            targetUser = UserManager.lookupUser(user, uid);
            if (targetUser == null) {
                response.status(400);
                return json(GSON, response,
                    ResultJson.error("Invalid uid, target user not found"),
                    new TypeToken<>() { });
            }
        } else {
            // User changing own email
            targetUser = user;
        }

        String currentEmail = targetUser.getEmail();

        // Validate email is different from current
        if (newEmail.equals(currentEmail)) {
            return json(GSON, response,
                ResultJson.error(LS.getMessage("error.same_email")),
                new TypeToken<>() { });
        }

        // Validate email format using RFC 5321/5322 standard
        validateEmailAddress(newEmail);

        // Update user email
        targetUser.setEmail(newEmail);
        UserManager.storeUser(targetUser);

        // Return success response
        return json(GSON, response,
            ResultJson.success(LS.getMessage("email.verified")),
            new TypeToken<>() { });

    } catch (AddressException e) {
        return json(GSON, response,
            ResultJson.error(LS.getMessage("error.addr_invalid", 
                emailRequest.getEmail())),
            new TypeToken<>() { });
    } catch (BadParameterException e) {
        response.status(400);
        return json(GSON, response,
            ResultJson.error(e.getMessage()),
            new TypeToken<>() { });
    } catch (Exception e) {
        response.status(500);
        return json(GSON, response,
            ResultJson.error("An unexpected error occurred: " + e.getMessage()),
            new TypeToken<>() { });
    }
}
```

### Updated EmailChangeRequest Class
```java
/**
 * Request class for email change JSON payload.
 */
public static class EmailChangeRequest {
    private String email;
    private Long uid;

    public EmailChangeRequest(String emailIn) {
        this.email = emailIn;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String emailIn) {
        this.email = emailIn;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uidIn) {
        this.uid = uidIn;
    }
}
```

---

## 2. Jade Template: account-email.jade

### Location
`java/core/src/main/resources/com/suse/manager/webui/templates/users/account-email.jade`

### Changes
```pug
include ../common.jade

#account-email-form

script(type='text/javascript').
    window.csrfToken = "#{csrf_token}";

script(type='text/javascript').
    spaImportReactPage('account/account-email')
        .then(function(module) {
            module.renderer(
                'account-email-form', '#{docsLocale}',
                {
                    currentEmail: '#{currentEmail}',
                    targetUserId: #{targetUserId},
                    targetUserName: '#{targetUserName}',              // ADDED
                    contextMode: '#{contextMode}'                    // ADDED
                }
            )
        });
```

---

## 3. React Component: index.tsx

### Location
`web/html/src/manager/account/email/index.tsx`

### Changes
```typescript
import SpaRenderer from "core/spa/spa-renderer";

import { MessagesContainer } from "components/toastr/toastr";

import { AccountEmailForm } from "./form";

type RendererProps = {
  currentEmail?: string;
  targetUserId?: number;
  targetUserName?: string;                    // ADDED
  contextMode?: "own" | "admin";             // ADDED
};

export const renderer = (
  id: string,
  locale: string,
  {
    currentEmail = "",
    targetUserId,
    targetUserName = "",               // ADDED
    contextMode = "own"                // ADDED
  }: RendererProps = {}
) => {
  SpaRenderer.renderNavigationReact(
    <>
      <MessagesContainer />
      <AccountEmailForm
        currentEmail={currentEmail}
        userId={targetUserId}
        userName={targetUserName}        // ADDED
        contextMode={contextMode}        // ADDED
      />
    </>,
    document.getElementById(id)
  );
};
```

---

## 4. React Component: form.tsx

### Location
`web/html/src/manager/account/email/form.tsx`

### Key Changes

#### Props Interface
```typescript
interface Props {
  userId?: number;
  userName?: string;                    // ADDED
  currentEmail: string;
  contextMode?: "own" | "admin";       // ADDED
  onSuccess?: () => void;
}
```

#### Context-Aware Title & Instructions
```typescript
const getPageTitle = () => {
  if (props.contextMode === "admin" && props.userName) {
    return t("admin.changeemail.title", props.userName);
  }
  return t("yourchangeemail.jsp.title");
};

const getInstructions = () => {
  if (props.contextMode === "admin" && props.userName) {
    return t("admin.changeemail.instructions");
  }
  return t("yourchangeemail.instructions");
};
```

#### Updated Form Submission
```typescript
const response = await Network.post(
  "/rhn/account/changeemail",
  JSON.stringify({
    email: email.trim(),
    uid: props.userId,  // Only populated in admin context
  }),
  "application/json"
);
```

#### Conditional Rendering for Admin Context
```typescript
<div className="panel-heading">
  <h3 className="panel-title">{getPageTitle()}</h3>
  {props.contextMode === "admin" && props.userName && (
    <p className="text-muted">
      {t("admin.changeemail.for", props.userName)}
    </p>
  )}
</div>
```

---

## Summary of Code Changes

### Statistics
- **Files Modified:** 4
- **Java Code Added:** ~50 lines
- **Template Code Added:** 2 lines
- **React Code Added:** ~35 lines
- **Total New Lines:** ~87 lines
- **Lines Deleted/Replaced:** ~20 lines

### Key Implementation Points

| Component | Change | Benefit |
|-----------|--------|---------|
| Routes | Two separate routes | URL clarity, better auth |
| Context Mode | String flag passed through layers | Clear context in React |
| Handler Methods | Separate methods per route | Single Responsibility |
| Template | Single shared template | DRY principle |
| React Component | Context-aware rendering | Proper UX per context |
| API Endpoint | Single endpoint handles both | Backward compatible |

### Backward Compatibility
- ✅ Original route `/manager/account/changeemail` unchanged
- ✅ Existing API endpoint handles both modes
- ✅ No breaking changes to existing code
- ✅ New functionality is purely additive


