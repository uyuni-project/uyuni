/**
 * Translates a string, implemented now as a 'true-bypass',
 * with placeholder replacement like Java's MessageFormat class.
 * Accepts any number of arguments after key.
 */
function t(key) {
    var result = key;

    // Minimal implementation of https://docs.oracle.com/javase/7/docs/api/java/text/MessageFormat.html
    for (var i=1; i<arguments.length; i++) {
        result = result.replace('{' + (i-1) + '}', arguments[i]);
    }

    return result;
}