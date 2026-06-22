// Setup the password strength meter
function setupPasswordStrengthMeter() {
  "use strict";
  var options = {};
  options.common = {
    minChar: 7,
    usernameField: "#loginname",
    onKeyUp: function (evt) {
      jQuery('input[name="desiredpassword"]').popover("show");
      //when there are no errors the popover disappears
      if (jQuery("ul.error-list").is(":empty")) {
        jQuery('input[name="desiredpassword"]').popover("destroy");
      }
    },
  };
  options.rules = {
    activated: {
      wordTwoCharacterClasses: true,
      wordRepetitions: true,
    },
    scores: {
      wordRepetitions: -20,
      wordSequences: -20,
    },
  };
  options.ui = {
    showPopover: true,
    showErrors: true,
    spanError: function (options, key) {
      var text = options.ui.errorMessages[key];
      return text;
    },
    errorMessages: {
      password_too_short:
        '<dl><dt><i class="fa fa-exclamation-circle fa-1-5x text-danger"></i>The Password is too short.</dt><dd>must be at least 5 characters</dd></dl>',
      same_as_username:
        '<dl><dt><i class="fa fa-exclamation-triangle fa-1-5x text-warning"></i>Password contains username</dt></dl>',
      email_as_password:
        '<dl><dt><i class="fa fa-exclamation-triangle fa-1-5x text-warning"></i>Password contains email address</dt></dl>',
      repeated_character:
        '<dl><dt><i class="fa fa-exclamation-triangle fa-1-5x text-warning"></i>Try to avoid repetitions</dt></dl>',
      sequence_found:
        '<dl><dt><i class="fa fa-exclamation-triangle fa-1-5x text-warning"></i>Your Password contains sequences</dt></dl>',
      two_character_classes:
        '<dl><dt><i class="fa fa-exclamation-triangle fa-1-5x text-warning"></i>Use different character classes</dt></dl>',
    },
    showVerdicts: false,
    container: "#pwstrenghtfield",
    viewports: {
      progress: "#pwstrenghtfield",
    },
  };
  jQuery('input[name="desiredpassword"]').pwstrength(options);
}

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

    // Must contain at least one allowed special character
    const allowedRegex = new RegExp("[" + escaped + "]");
    if (!allowedRegex.test(password)) {
      return false;
    }

    // Reject any special character not in the allowed list
    const invalidRegex = new RegExp("[^a-zA-Z0-9" + escaped + "]");
    if (invalidRegex.test(password)) {
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
  var desiredpassVal = jQuery.trim(jQuery('input[name="desiredpassword"]').val());
  var desiredpassConfirmVal = jQuery.trim(jQuery("#confirmpass").val());
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

      const regex = new RegExp("[" + escaped + "]");

      items.push(`${regex.test(password) ? "✓" : "-"} Special character: ${passwordPolicy.specialChars}`);
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

  // on the edit user page
  if (typeof placeholderAttr !== "undefined" && placeholderAttr !== false) {
    // No password entered yet
    if (!desiredpassVal) {
      neutral(jQuery("#desiredtick"));
      neutral(jQuery("#confirmtick"));

      jQuery("#desiredtick").attr("title", getPasswordValidationMessage(desiredpassVal));
      jQuery("#confirmtick").attr("title", "Confirm the password");
    }
    // Password entered
    else {
      if (validatePassword(desiredpassVal)) {
        success(jQuery("#desiredtick"));
        jQuery("#desiredtick").attr("title", getPasswordValidationMessage(desiredpassVal));
      } else {
        danger(jQuery("#desiredtick"));
        jQuery("#desiredtick").attr("title", getPasswordValidationMessage(desiredpassVal));
      }

      if (!desiredpassConfirmVal) {
        neutral(jQuery("#confirmtick"));
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
      jQuery("#desiredtick").attr("title", getPasswordValidationMessage(desiredpassVal));
    } else if (validatePassword(desiredpassVal)) {
      success(jQuery("#desiredtick"));
      jQuery("#desiredtick").attr("title", "Password meets all requirements");
    } else {
      danger(jQuery("#desiredtick"));
      jQuery("#desiredtick").attr("title", getPasswordValidationMessage(desiredpassVal));
    }

    jQuery("#desiredtick").attr("title", getPasswordValidationMessage(desiredpassVal));

    // Confirm password icon
    if (!desiredpassConfirmVal) {
      neutral(jQuery("#confirmtick"));
      jQuery("#confirmtick").attr("title", "Confirm the password");
    } else if (validatePassword(desiredpassVal) && desiredpassVal === desiredpassConfirmVal) {
      success(jQuery("#confirmtick"));
    } else {
      danger(jQuery("#confirmtick"));
    }
  }
}

// document ready handler
jQuery(document).ready(function () {
  jQuery.getJSON("/rhn/manager/api/admin/config/password-policy", function (response) {
    passwordPolicy = JSON.parse(response.data);
    updateTickIcon();
  });
});
