/*
* Source code available at
* https://github.com/ncounter/loggerhead/blob/master/for-old-browser/loggerhead.js
*/

'use strict';

var Loggerhead = {};
(function(_context){
  // configuration object
  var config = {
    url: '',
    levels: {info: true, debug: true, warning: true, error: true},
    console: {info: true, debug: true, warning: true, error: true},
  };

  _context.setHeaders = function(headers) {
    return headers;
  }

  // private function
  function postData(data, callback) {
    if (config.url == '') {
      var errorMessage = '[Loggerhead] ERROR: no server enpoint URL set to send the POST request!! ';
      if(config.console.error) {
        console.error(errorMessage);
      }
      return;
    }

    var headers = {};
    headers['Content-Type'] = 'application/json; charset=utf-8';

    var xhr = new XMLHttpRequest();
    xhr.open('POST', config.url);
    Object.keys(_context.setHeaders(headers)).map(k =>
      xhr.setRequestHeader(k, headers[k])
    );
    xhr.onload = function() {
      if (xhr.status !== 200) {
        // try to parse the xhr response, but catch if it fails unless an infinite loop of failure-and-logging would start
        try {
          console.error(JSON.parse(xhr));
        }
        catch {
          console.error("The POST request to the url: '" + config.url + "' was not successfully completed and the response cannot be parsed.");
        }
      }
    };
    xhr.onreadystatechange = function () {
      if (xhr.readyState == 4 && xhr.status == 200) {
        if (callback && typeof callback === "function") {
          callback(JSON.parse(xhr.responseText));
        }
      }
    }
    xhr.send(JSON.stringify(data));

    return xhr;
  }

  // log level functions
  _context.info = function(message, callback) {
    if (config.levels.info) {
      postData({'level' : 'info', 'message' : message}, callback);
    }
    if(config.console.info) {
      console.info(message);
    }
  }
  _context.debug = function(message, callback) {
    if (config.levels.debug) {
      postData({'level' : 'debug', 'message' : message}, callback);
    }
    if(config.console.debug) {
      console.debug(message);
    }
  }
  _context.warning = function(message, callback) {
    if (config.levels.warning) {
      postData({'level' : 'warning', 'message' : message}, callback);
    }
    if(config.console.warning) {
      console.warn(message);
    }
  }
  _context.error = function(message, callback) {
    if (config.levels.error) {
      postData({'level' : 'error', 'message' : message}, callback);
    }
    if(config.console.error) {
      console.error(message);
    }
  }

  function merge(fromObj, toObj) {
    Object.keys(fromObj).map(k => {
      if (toObj[k] != null && fromObj[k] instanceof Object) {
        merge(fromObj[k], toObj[k]);
      }
      else {
        toObj[k] = fromObj[k];
      }
    });
  }

  // configuration parameters setter
  _context.set = function(configObject) {
    merge(configObject, config);
  }
})(Loggerhead);
