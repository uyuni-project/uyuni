//  Password Validation with Configured Password Policy
let passwordPolicy = {
  consecutiveCharsFlag: false,
  digitFlag: false,
  lowerCharFlag: false,
  maxCharacterOccurrence: 2,
  maxLength: 32,
  minLength: 4,
  restrictedOccurrenceFlag: false,
  specialCharFlag: false,
  specialChars: "!$%&()*+,./:;<=>?[]^_{|}~",
  upperCharFlag: false,
};
let passwordPolicyLoaded = false;
let passwordPolicyLoadFailed = false;

const hasUppercase = (value) => /\p{Uppercase}/u.test(value);
const hasLowercase = (value) => /\p{Lowercase}/u.test(value);
const hasDigit = (value) => /\p{Nd}/u.test(value);
const hasSpecialCharacter = (value) =>
  [...value].some((char) => passwordPolicy.specialChars.includes(char));
const hasJavaWhitespace = (value) =>
  /[\t\n\u000B\f\r\u001C-\u001F\u0020\u1680\u2000-\u2006\u2008-\u200A\u2028\u2029\u205F\u3000]/u.test(value);


function validatePassword(password) {
  if (hasJavaWhitespace(password)) {
     return false;
   }

  if (password.length < passwordPolicy.minLength) {
    return false;
  }

  if (password.length > passwordPolicy.maxLength) {
    return false;
  }

  if (passwordPolicy.upperCharFlag && !hasUppercase(password)) {
    return false;
  }

  if (passwordPolicy.lowerCharFlag && !hasLowercase(password)) {
    return false;
  }

  if (passwordPolicy.digitFlag && !hasDigit(password)) {
    return false;
  }

  if (passwordPolicy.specialCharFlag) {
  if (!hasSpecialCharacter(password)) {
    return false;
  }
}

  // Restrict Consecutive Characters
  if (passwordPolicy.consecutiveCharsFlag) {
    if (/(.)\1/.test(password)) {
      return false;
    }
  }

  // Restrict Characters Occurrences
  if (passwordPolicy.restrictedOccurrenceFlag) {
    const counts = {};

    for (let i = 0; i < password.length; i++) {
      const c = password[i];
      counts[c] = (counts[c] || 0) + 1;

      if (counts[c] > passwordPolicy.maxCharacterOccurrence) {
        return false;
      }
    }
  }
 
  return true;
}

// check if password matches with password policy
// check if confirm password input field matches with password input field
// swap icons in the input-group-addon
function updateTickIcon() {
  var desiredpassVal = jQuery('input[name="desiredpassword"]').val() || "";
  var desiredpassConfirmVal = jQuery("#confirmpass").val() || "";
  var placeholderAttr = jQuery('input[name="desiredpassword"]').attr("placeholder");

  function neutral(element) {
    element.removeClass("fa-check-circle fa-times-circle text-success text-danger");
    element.addClass("fa-info-circle");
  }

  function success(element) {
    element.removeClass("fa-info-circle fa-times-circle text-danger");
    element.addClass("fa-check-circle text-success");
  }

  function danger(element) {
    element.removeClass("fa-info-circle fa-check-circle text-success");
    element.addClass("fa-times-circle text-danger");
  }

  function getPasswordValidationMessage(password) {
    const items = [];

    // Whitespace
    items.push(`${!hasJavaWhitespace(password) ? "✓" : "-"} ${t("No spaces, tabs, or newlines")}`);

    // Minimum length
    items.push(`${password.length >= passwordPolicy.minLength ? "✓" : "-"} ${t("Minimum length")} ${passwordPolicy.minLength}`);

    // Maximum length
    items.push(`${password.length <= passwordPolicy.maxLength ? "✓" : "-"} ${t("Maximum length")} ${passwordPolicy.maxLength}`);

    // Uppercase
    if (passwordPolicy.upperCharFlag) {
      items.push(`${hasUppercase(password) ? "✓" : "-"} ${t("Uppercase character")}`);
    }

    // Lowercase
    if (passwordPolicy.lowerCharFlag) {
      items.push(`${hasLowercase(password) ? "✓" : "-"} ${t("Lowercase character")}`);
    }

    // Digit
    if (passwordPolicy.digitFlag) {
      items.push(`${hasDigit(password) ? "✓" : "-"} ${t("Digit")}`);
    }

    // Special character
    if (passwordPolicy.specialCharFlag) {
      // Must contain at least one configured special character
      items.push(
        `${hasSpecialCharacter(password) ? "✓" : "-"} ${t("Special Characters")}: ${passwordPolicy.specialChars}`
      );
    }

    // Restrict character occurrences
    if (passwordPolicy.restrictedOccurrenceFlag) {
      const counts = {};
      let valid = true;

      for (let i = 0; i < password.length; i++) {
        const c = password[i];
        counts[c] = (counts[c] || 0) + 1;

        if (counts[c] > passwordPolicy.maxCharacterOccurrence) {
          valid = false;
          break;
        }
      }

      items.push(
        `${valid ? "✓" : "-"} ${t("Maximum {count} occurrences per character", {
          count: passwordPolicy.maxCharacterOccurrence,
        })}`
      );
      }

    // Restrict consecutive characters
    if (passwordPolicy.consecutiveCharsFlag) {
      const valid = !/(.)\1/.test(password);

      items.push(`${valid ? "✓" : "-"} ${t("No consecutive identical characters")}`);
    }

    return items.join("\n");
  }
  
  function updateTooltip(selector, message) {
    jQuery(selector)
      .attr("title", message)
      .attr("data-bs-original-title", message);
  }

  if (passwordPolicyLoadFailed) {
    neutral(jQuery("#desiredtick"));
    neutral(jQuery("#confirmtick"));
    updateTooltip("#desiredtick", t("Password requirements could not be loaded."));
    updateTooltip("#confirmtick", t("Password requirements could not be loaded."));
    return;
  }

  if (!passwordPolicyLoaded) {
    neutral(jQuery("#desiredtick"));
    neutral(jQuery("#confirmtick"));
    updateTooltip("#desiredtick", t("Loading password requirements."));
    updateTooltip("#confirmtick", t("Loading password requirements."));
    return;
  }
  
  // on the edit user page
  if (typeof placeholderAttr !== "undefined" && placeholderAttr !== false) {
    // No password entered yet
    if (!desiredpassVal) {
      neutral(jQuery("#desiredtick"));
      neutral(jQuery("#confirmtick"));

       updateTooltip("#desiredtick", t("Leave blank to keep your current password."));
       updateTooltip("#confirmtick", t("Confirm the password"));
    }
    // Password entered
    else {
      if (validatePassword(desiredpassVal)) {
        success(jQuery("#desiredtick"));
      } else {
        danger(jQuery("#desiredtick"));
      }
      updateTooltip("#desiredtick", getPasswordValidationMessage(desiredpassVal));

      if (!desiredpassConfirmVal) {
        neutral(jQuery("#confirmtick"));
        updateTooltip("#confirmtick", t("Confirm the password"));
      } else if (validatePassword(desiredpassVal) && desiredpassVal === desiredpassConfirmVal) {
        success(jQuery("#confirmtick"));
        updateTooltip("#confirmtick", t("Password match"));
      } else {
        danger(jQuery("#confirmtick"));
        updateTooltip("#confirmtick", t("Passwords do not match"));
      }
    }
  }
  // on create user pages
  else {
    // Empty state
    if (!desiredpassVal) {
      neutral(jQuery("#desiredtick"));
      updateTooltip("#desiredtick", getPasswordValidationMessage(desiredpassVal));
    } else if (validatePassword(desiredpassVal)) {
      success(jQuery("#desiredtick"));
      updateTooltip("#desiredtick", t("Password meets all requirements"));
    } else {
      danger(jQuery("#desiredtick"));
      updateTooltip("#desiredtick", getPasswordValidationMessage(desiredpassVal));
    }

    // Confirm password icon
    if (!desiredpassConfirmVal) {
      neutral(jQuery("#confirmtick"));
      updateTooltip("#confirmtick", t("Confirm the password"));
    } else if (validatePassword(desiredpassVal) && desiredpassVal === desiredpassConfirmVal) {
      success(jQuery("#confirmtick"));
       updateTooltip("#confirmtick", t("Password match"));
    } else {
      danger(jQuery("#confirmtick"));
      updateTooltip("#confirmtick", t("Passwords do not match"));
    }
  }
}

// document ready handler
jQuery(document).ready(function () {
  jQuery('input[name="desiredpassword"], #confirmpass').on("input", updateTickIcon);
  jQuery
    .getJSON("/rhn/manager/api/admin/config/password-policy")
    .done(function (response) {
      try {
        passwordPolicy = JSON.parse(response.data);
        passwordPolicyLoaded = true;
      } catch (e) {
        passwordPolicyLoadFailed = true;
      }
      updateTickIcon();
  })
    .fail(function () {
      passwordPolicyLoadFailed = true;
      updateTickIcon();
  });
});
