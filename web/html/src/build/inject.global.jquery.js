// We need this to make sure webpack will not duplicate jQuery library when included as a transitive dependency
// Webpack.config.js has an alias from "jquery" -> "inject.global.jquery.js"

module.exports = $;
