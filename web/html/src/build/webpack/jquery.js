/**
 * Inject jQuery as a global to ensure Webpack will not include it again when imported as a subdependency of some package.
 */
module.exports = jQuery;
