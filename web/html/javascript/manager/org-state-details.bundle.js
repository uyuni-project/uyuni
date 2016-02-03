(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({"/home/matei/workspace-suma3/spacewalk/web/html/src/components/panel.js":[function(require,module,exports){
'use strict';

var React = require("react")
var t = require("./react-translation")

var PanelButton = React.createClass({displayName: "PanelButton",

  getInitialState: function() {
    return {};
  },

  render: function() {
      return (
          React.createElement("div", {className: "spacewalk-toolbar"}, 
            React.createElement("a", {href: this.props.action}, 
              React.createElement("i", {className: 'fa ' + this.props.icon}), 
              t(this.props.text)
            )
          )
        )
  }
});

var Panel = React.createClass({displayName: "Panel",
  getInitialState: function() {
    return {};
  },

  render: function() {
    return (
      React.createElement("div", null, 
        React.createElement("div", {className: "spacewalk-toolbar-h1"}, 
          this.props.button, 
          React.createElement("h1", null, React.createElement("i", {className: 'fa ' + this.props.icon}), t(this.props.title))
        ), 
        this.props.children
      )
    );
  }
});


module.exports = {
    Panel : Panel,
    PanelButton : PanelButton
}

},{"./react-translation":"/home/matei/workspace-suma3/spacewalk/web/html/src/components/react-translation.js","react":"react"}],"/home/matei/workspace-suma3/spacewalk/web/html/src/components/react-translation.js":[function(require,module,exports){
// TODO copied from spacewalk-essentials.js. find a way to share this
/**
 * Translates a string, implemented now as a 'true-bypass',
 * with placeholder replacement like Java's MessageFormat class.
 * Accepts any number of arguments after key.
 */
module.exports = function(key) {
  var result = key;

  // Minimal implementation of https://docs.oracle.com/javase/7/docs/api/java/text/MessageFormat.html
  for (var i=1; i<arguments.length; i++) {
    result = result.replace('{' + (i-1) + '}', arguments[i]);
  }

  return result;
}

},{}],"/home/matei/workspace-suma3/spacewalk/web/html/src/manager/org-state-details.js":[function(require,module,exports){
'use strict';

var React = require("react")
var Panel = require("../components/panel.js").Panel

var StateDetail = React.createClass({displayName: "StateDetail",

//    getInitialState: function() {
//        return this.props.data;
//    },

    render: function() {
        return (
        React.createElement(Panel, {title: "Add state", icon: "spacewalk-icon-virtual-host-manager"}, 
            React.createElement("form", {className: "form-horizontal"}, 
                React.createElement("div", {className: "form-group"}, 
                    React.createElement("label", {className: "col-md-3 control-label"}, "Name:"), 
                    React.createElement("div", {className: "col-md-6"}, 
                        React.createElement("input", {className: "form-control", type: "text", name: "name"})
                    )
                ), 

                React.createElement("div", {className: "form-group"}, 
                    React.createElement("label", {className: "col-md-3 control-label"}, "Content:"), 
                    React.createElement("div", {className: "col-md-6"}, 
                        React.createElement("textarea", {className: "form-control", name: "content"})
                    )
                ), 

                React.createElement("div", {className: "form-group"}, 
                    React.createElement("div", {className: "col-md-offset-3 col-md-6"}, 
                        React.createElement("button", {className: "btn btn-success", type: "submit"}, 
                            React.createElement("i", {className: "fa fa-plus"}), t("Add state")
                        )
                    )
                )

            )
        )
        )

    }
});

React.render(
  React.createElement(StateDetail, {data: formData()}),
  document.getElementById('state-add')
);

},{"../components/panel.js":"/home/matei/workspace-suma3/spacewalk/web/html/src/components/panel.js","react":"react"}]},{},["/home/matei/workspace-suma3/spacewalk/web/html/src/manager/org-state-details.js"]);
