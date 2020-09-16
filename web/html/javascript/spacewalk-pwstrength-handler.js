// Setup the password strength meter
function setupPasswordStrengthMeter() {
    "use strict";
    var options = {};
    options.common = {
        minChar: 5,
        usernameField: "#loginname",
        onKeyUp: function (evt) {
            jQuery('input[name="desiredpassword"]').popover('show');
            //when there are no errors the popover disappears
            if (jQuery('ul.error-list').is(':empty')) {
                jQuery('input[name="desiredpassword"]').popover('destroy');
            }
            //update the tick next to the desiredpassword input field
            updateTickIcon();
        }
    };
    options.rules = {
        activated:
        {
            wordTwoCharacterClasses: true,
            wordRepetitions: true
        },
        scores:
        {
            wordRepetitions: -20,
            wordSequences: -20
        }
    };
    options.ui = {
        showPopover: true,
        showErrors: true,
        spanError: function (options, key) {
            var text = options.ui.errorMessages[key];
            return text;
        },
        errorMessages:
        {
            password_too_short: '<dl><dt><i class="fa fa-exclamation-circle fa-1-5x text-danger"></i>The Password is too short.</dt><dd>must be at least 5 characters</dd></dl>',
            same_as_username: '<dl><dt><i class="fa fa-exclamation-triangle fa-1-5x text-warning"></i>Password contains username</dt></dl>',
            email_as_password: '<dl><dt><i class="fa fa-exclamation-triangle fa-1-5x text-warning"></i>Password contains email address</dt></dl>',
            repeated_character: '<dl><dt><i class="fa fa-exclamation-triangle fa-1-5x text-warning"></i>Try to avoid repetitions</dt></dl>',
            sequence_found: '<dl><dt><i class="fa fa-exclamation-triangle fa-1-5x text-warning"></i>Your Password contains sequences</dt></dl>',
            two_character_classes: '<dl><dt><i class="fa fa-exclamation-triangle fa-1-5x text-warning"></i>Use different character classes</dt></dl>'
        },
        showVerdicts: false,
        container: '#pwstrenghtfield',
        viewports: {
            progress: '#pwstrenghtfield'
        }
    };
    jQuery('input[name="desiredpassword"]').pwstrength(options);
}

// check if password >= 5 characters
// check if confirm password input field matches with password input field
// swap icons in the input-group-addon
function updateTickIcon() {
    var desiredpassVal = jQuery.trim(jQuery('input[name="desiredpassword"]').val());
    var desiredpassConfirmVal = jQuery.trim(jQuery('#confirmpass').val());
    var placeholderAttr = jQuery('input[name="desiredpassword"]').attr('placeholder');
    function success(element) {
        element.removeClass("fa-times-circle text-danger");
        element.addClass("fa-check-circle text-success");
    }
    function danger(element) {
        element.removeClass("fa-check-circle text-success");
        element.addClass("fa-times-circle text-danger");
    }

    // on the edit user page
    if ((typeof placeholderAttr !== 'undefined' && placeholderAttr !== false)) {
        // icons are green
        success(jQuery("#desiredtick"));
        success(jQuery("#confirmtick"));
        if (desiredpassVal.length > 0 && desiredpassVal.length < 5) {
            danger(jQuery("#desiredtick"));
            danger(jQuery("#confirmtick"));
        }
        else if (desiredpassVal != desiredpassConfirmVal) {
            danger(jQuery("#confirmtick"));
        }
    }
    // on create user pages
    else {
        // icons are red
        danger(jQuery("#desiredtick"));
        danger(jQuery("#confirmtick"));
        if (desiredpassVal.length >= 5) {
            success(jQuery("#desiredtick"));
        }
        if (desiredpassVal == desiredpassConfirmVal && desiredpassVal.length >= 5) {
            success(jQuery("#confirmtick"));
        }
    }
}

// document ready handler
jQuery(document).ready(function() {
    setupPasswordStrengthMeter();
});
