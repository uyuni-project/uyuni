// make sure not to submit the placeholder (or parts of it) as a password when editing a user
$(document).ready(function () {
    // Return true if all password fields are empty
    function isPasswordFieldsEmpty() {
        var empty = true;
        $('input:password').each(function(index) {
            if ($(this).val() != '') {
                empty = false;
                return false;
            }
        });
        return empty;
    }

    // PLACEHOLDER needs to be in sync with PLACEHOLDER_PASSWORD 
    // in the UserActionHelper Java class.
    var PLACEHOLDER = "******";
    $('input:password').focus(function() {
        if ($(this).val() == PLACEHOLDER) {
            $('input:password').val('');
            updateTickIcon();
        }
    }).blur(function() {
        if (isPasswordFieldsEmpty()) {
            $('input:password').val(PLACEHOLDER);
            updateTickIcon();
        }
    });
});
