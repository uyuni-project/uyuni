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

function validatePassword(password) {
  if (/\s/.test(password)) {
     return false;
   }

  if (password.length < passwordPolicy.minLength) {
    return false;
  }

  if (password.length > passwordPolicy.maxLength) {
    return false;
  }

  if (passwordPolicy.upperCharFlag && !/[A-Z]/.test(password)) {
    return false;
  }

  if (passwordPolicy.lowerCharFlag && !/[a-z]/.test(password)) {
    return false;
  }

  if (passwordPolicy.digitFlag && !/\d/.test(password)) {
    return false;
  }

  if (passwordPolicy.specialCharFlag) {
    const escaped = passwordPolicy.specialChars.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

    // Must contain at least one configured special character
    const allowedRegex = new RegExp("[" + escaped + "]");
    if (!allowedRegex.test(password)) {
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
    items.push(`${!/\s/.test(password) ? "✓" : "-"} No spaces, tabs, or newlines`);

    // Minimum length
    items.push(`${password.length >= passwordPolicy.minLength ? "✓" : "-"} Minimum length ${passwordPolicy.minLength}`);

    // Maximum length
    items.push(`${password.length <= passwordPolicy.maxLength ? "✓" : "-"} Maximum length ${passwordPolicy.maxLength}`);

    // Uppercase
    if (passwordPolicy.upperCharFlag) {
      items.push(`${/[A-Z]/.test(password) ? "✓" : "-"} Uppercase character`);
    }

    // Lowercase
    if (passwordPolicy.lowerCharFlag) {
      items.push(`${/[a-z]/.test(password) ? "✓" : "-"} Lowercase character`);
    }

    // Digit
    if (passwordPolicy.digitFlag) {
      items.push(`${/\d/.test(password) ? "✓" : "-"} Digit`);
    }

    // Special character
    if (passwordPolicy.specialCharFlag) {
      const escaped = passwordPolicy.specialChars.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

      // Must contain at least one configured special character
      const allowedRegex = new RegExp("[" + escaped + "]");

      items.push(
        `${allowedRegex.test(password) ? "✓" : "-"} Special character: ${passwordPolicy.specialChars}`
      );
    }

    // Restrict character occurrences
    if (passwordPolicy.restrictedOccurrenceFlag) {
      const counts = {};
      let valid = true;

      for (const c of password) {
        counts[c] = (counts[c] || 0) + 1;

        if (counts[c] > passwordPolicy.maxCharacterOccurrence) {
          valid = false;
          break;
        }
      }

      items.push(`${valid ? "✓" : "-"} Maximum ${passwordPolicy.maxCharacterOccurrence} occurrences per character`);
    }

    // Restrict consecutive characters
    if (passwordPolicy.consecutiveCharsFlag) {
      const valid = !/(.)\1/.test(password);

      items.push(`${valid ? "✓" : "-"} No consecutive identical characters`);
    }

    return items.join("\n");
  }
  
  function updateTooltip(selector, message) {
    jQuery(selector)
      .attr("title", message)
      .attr("data-bs-original-title", message);
  }
  
  // on the edit user page
  if (typeof placeholderAttr !== "undefined" && placeholderAttr !== false) {
    // No password entered yet
    if (!desiredpassVal) {
      neutral(jQuery("#desiredtick"));
      neutral(jQuery("#confirmtick"));

       updateTooltip("#desiredtick", "Leave blank to keep your current password.");
       updateTooltip("#confirmtick", "Confirm the password");
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
        updateTooltip("#confirmtick", "Confirm the password");
      } else if (validatePassword(desiredpassVal) && desiredpassVal === desiredpassConfirmVal) {
        success(jQuery("#confirmtick"));
      } else {
        danger(jQuery("#confirmtick"));
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
      updateTooltip("#desiredtick", "Password meets all requirements");
    } else {
      danger(jQuery("#desiredtick"));
      updateTooltip("#desiredtick", getPasswordValidationMessage(desiredpassVal));
    }

    // Confirm password icon
    if (!desiredpassConfirmVal) {
      neutral(jQuery("#confirmtick"));
      updateTooltip("#confirmtick", "Confirm the password");
    } else if (validatePassword(desiredpassVal) && desiredpassVal === desiredpassConfirmVal) {
      success(jQuery("#confirmtick"));
    } else {
      danger(jQuery("#confirmtick"));
    }
  }
}

// document ready handler
jQuery(document).ready(function () {
  jQuery
    .getJSON("/rhn/manager/api/admin/config/password-policy")
    .done(function (response) {
      try {
        passwordPolicy = JSON.parse(response.data);
      } catch (e) {
        // Keep defaults if backend response is unexpected
      }
      updateTickIcon();
  })
    .fail(function () {
      // Keep defaults if policy fetch fails
      updateTickIcon();
  });
});
