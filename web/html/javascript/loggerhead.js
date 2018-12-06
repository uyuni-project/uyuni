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
      const errorMessage = '[Loggerhead] ERROR: no server enpoint URL set to send the POST request!! ';
      if(config.console.error) {
        console.error(errorMessage);
      }
      return;
    }

    var headers = new Map();
    headers.set('Content-Type', 'application/json; charset=utf-8');

    var xhr = new XMLHttpRequest();
    xhr.open('POST', config.url);
    Array.from(_context.setHeaders(headers).keys()).map(k =>
      xhr.setRequestHeader(k, headers.get(k))
    );
    xhr.onload = function() {
      if (xhr.status !== 200) {
        console.error(JSON.parse(xhr));
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

  function setMapFromObject(fromObj, toMap) {
    const fromMap = new Map(Object.entries(fromObj));
    Array.from(fromMap.keys()).map(k => {
      if (toMap[k] != null && fromMap.get(k) instanceof Object) {
        setMapFromObject(fromMap.get(k), toMap[k]);
      }
      else {
        toMap[k] = fromMap.get(k);
      }
    });
  }

  // configuration parameters setter
  _context.set = function(configObject) {
    setMapFromObject(configObject, config);
  }
})(Loggerhead);
