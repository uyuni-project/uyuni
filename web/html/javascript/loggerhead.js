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
  function postData(data) {
    if (config.url == '') {
      const errorMessage = '[Loggerhead] ERROR: no server enpoint URL set to send the POST request!! ';
      if(config.console.error) {
        console.error(errorMessage);
      }
      return new Promise((resolve, reject) => reject(errorMessage));
    }
    var headers = new Map();
    headers.set('Content-Type', 'application/json; charset=utf-8');
    headers = _context.setHeaders(headers);
    const opts = {
      method: 'POST', mode: 'cors', cache: 'no-cache', credentials: 'same-origin',
      headers: headers,
      redirect: 'follow', referrer: 'no-referrer',
      body: JSON.stringify(data),
    }
    const result = fetch(config.url, opts)
        .then(function (response) { return response.json();})
        .catch(error =>
          {
            if(config.console.error) {
              const errorMessage =
                'Error trying to send log message to `' +
                config.url +
                '`\n\n' +
                'POST JSON data was = ' +
                JSON.stringify(opts) +
                '\n\n' +
                error;
              console.error(errorMessage, error);
            }
          }
        );
    return result;
  }

  // log level functions
  _context.info = function(message) {
    var ret = new Promise(function(resolve, reject) { resolve() });
    if (config.levels.info) {
      ret = postData({'level' : 'info', 'message' : message});
    }
    if(config.console.info) {
      console.info(message);
    }
    return ret;
  }
  _context.debug = function(message) {
    var ret = new Promise(function(resolve, reject) { resolve() });
    if (config.levels.debug) {
      ret = postData({'level' : 'debug', 'message' : message});
    }
    if(config.console.debug) {
      console.debug(message);
    }
    return ret;
  }
  _context.warning = function(message) {
    var ret = new Promise(function(resolve, reject) { resolve() });
    if (config.levels.warning) {
      ret = postData({'level' : 'warning', 'message' : message});
    }
    if(config.console.warning) {
      console.warn(message);
    }
    return ret;
  }
  _context.error = function(message) {
    var ret = new Promise(function(resolve, reject) { resolve() });
    if (config.levels.error) {
      ret = postData({'level' : 'error', 'message' : message});
    }
    if(config.console.error) {
      console.error(message);
    }
    return ret;
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
