// make sure not to submit the placeholder (or parts of it)
// as a password when editing a user
document.observe("dom:loaded", function() {
    var fields = $$('input[type=password]');
    // Return true if all password fields are empty
    function isPasswordFieldsEmpty(fields) {
        var empty = true;
        fields.each(function(field) {
            if (field.getValue() != '') {
                empty = false;
                return false;
            }
        });
        return empty;
    }
    // Add function to Array class to write values of all
    // Array elements at once
    Array.prototype.setArrayValues = function(val) {
        var i, n = this.length;
        for (i = 0; i < n; ++i) {
            this[i].value = val;
        }
    };
    // PLACEHOLDER needs to be in sync with PLACEHOLDER_PASSWORD
    // in the UserActionHelper Java class.
    var PLACEHOLDER = "******";
    fields.each(function(field) {
        field.observe('focus', function(e) {
            if (this.getValue() == PLACEHOLDER) {
                fields.setArrayValues('');
            }
        }).observe('blur', function(e) {
            if (isPasswordFieldsEmpty(fields)) {
                fields.setArrayValues(PLACEHOLDER);
            }
        });
    });
});
