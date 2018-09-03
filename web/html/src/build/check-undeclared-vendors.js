var fs = require('fs');
var path = require('path');

const extraVendorsNotDeclaredPath = path.resolve("dist/extravendors.notdeclared.bundle.js");

if (fs.existsSync(extraVendorsNotDeclaredPath)) {
  throw new Error (`
    All the javascript libraries used inside spacewalk-web must be declared on the file vendor/vendors.js
    Please check all the libraries in use under the file vendor/vendors.js
    `);
}
