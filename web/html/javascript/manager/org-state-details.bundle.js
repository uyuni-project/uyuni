(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({"/home/matei/workspace-suma3/spacewalk/web/html/src/components/messages.js":[function(require,module,exports){
'use strict';

var React = require("react");

var Messages = React.createClass({
  displayName: "Messages",

  _classNames: {
    "error": "danger",
    "success": "success",
    "info": "info",
    "warning": "warning"
  },

  getInitialState: function getInitialState() {
    return {};
  },

  render: function render() {
    var msgs = this.props.items.map(function (item) {
      return React.createElement(
        "div",
        { className: 'alert alert-' + this._classNames[item.severity] },
        item.text
      );
    }.bind(this));
    return React.createElement(
      "div",
      null,
      msgs
    );
  }

});

module.exports = {
  Messages: Messages
};

},{"react":"react"}],"/home/matei/workspace-suma3/spacewalk/web/html/src/components/panel.js":[function(require,module,exports){
'use strict';

var React = require("react");

var PanelButton = React.createClass({
  displayName: "PanelButton",

  getInitialState: function getInitialState() {
    return {};
  },

  render: function render() {
    return React.createElement(
      "div",
      { className: "spacewalk-toolbar" },
      React.createElement(
        "a",
        { href: this.props.action },
        React.createElement("i", { className: 'fa ' + this.props.icon }),
        t(this.props.text)
      )
    );
  }
});

var Panel = React.createClass({
  displayName: "Panel",

  getInitialState: function getInitialState() {
    return {};
  },

  render: function render() {
    return React.createElement(
      "div",
      null,
      React.createElement(
        "div",
        { className: "spacewalk-toolbar-h1" },
        this.props.button,
        React.createElement(
          "h1",
          null,
          React.createElement("i", { className: 'fa ' + this.props.icon }),
          t(this.props.title)
        )
      ),
      this.props.children
    );
  }
});

module.exports = {
  Panel: Panel,
  PanelButton: PanelButton
};

},{"react":"react"}],"/home/matei/workspace-suma3/spacewalk/web/html/src/manager/org-state-details.js":[function(require,module,exports){
'use strict';

var React = require("react");

var Panel = require("../components/panel").Panel;
var Messages = require("../components/messages").Messages;

var Button = React.createClass({
    displayName: "Button",

    render: function render() {
        return React.createElement(
            "button",
            { type: "button", className: 'btn ' + this.props.className, onClick: this.props.handler },
            React.createElement("i", { className: 'fa ' + this.props.icon }),
            this.props.text
        );
    }
});

var StateDetail = React.createClass({
    displayName: "StateDetail",

    _titles: {
        "add": t("Add state"),
        "edit": t("Edit state"),
        "delete": t("Delete state"),
        "info": t("View state")
    },

    getInitialState: function getInitialState() {
        return {};
    },

    handleCreate: function handleCreate(e) {
        this._save(e, "POST");
    },

    handleUpdate: function handleUpdate(e) {
        this._save(e, "PUT");
    },

    handleDelete: function handleDelete(e) {
        var r = confirm(t("Are you sure you want to delete state '{0}' ?", this.props.sls.name));
        if (r == true) {
            this._save(e, "DELETE");
        }
    },

    _save: function _save(e, httpMethod) {
        var formData = {};
        formData['name'] = React.findDOMNode(this.refs.stateName).value.trim();
        formData['content'] = React.findDOMNode(this.refs.stateContent).value.trim();
        if (this.props.sls.checksum) {
            formData['checksum'] = this.props.sls.checksum;
        }

        $.ajax({
            url: window.location.href + (csrfToken ? "?csrf_token=" + csrfToken : ""),
            dataType: 'json',
            contentType: "application/json",
            type: httpMethod,
            data: JSON.stringify(formData),
            success: function (data) {
                console.log(data);
                this.setState({ messages: [data.message] });
                window.location.href = data.url;
            }.bind(this),

            error: function (xhr, status, err) {
                if (xhr.status == 400) {
                    // validation err
                    var errs = $.parseJSON(xhr.responseText);
                    this.setState({ errors: errs });
                } else if (xhr.status == 500) {
                    this.setState({ errors: [t("An internal server error occurred")] });
                } else {
                    console.error(status, err.toString());
                }
            }.bind(this)
        });
    },

    render: function render() {
        var errs = null;
        if (this.state.errors) {
            errs = React.createElement(Messages, { items: this.state.errors.map(function (e) {
                    return { severity: "error", text: e };
                }) });
            //            errs = this.state.errors.map( function(e) {
            //                    return (<div className="alert alert-danger">{t(e)}</div>)
            //                   })
        }

        //        if (this.state.messages) {
        //            errs = this.state.messages.map( function(e) {
        //                    return (<div className="alert alert-info">{t(e)}</div>)
        //                   });
        //        }

        var buttons = [];
        if (this.props.sls.action == "edit") {
            buttons.push(React.createElement(Button, { className: "btn-success", icon: "fa-plus", text: t("Save state"), handler: this.handleUpdate }), React.createElement(Button, { className: "btn-danger", icon: "fa-trash", text: t("Delete state"), handler: this.handleDelete }));
        } else {
            buttons.push(React.createElement(Button, { className: "btn-success", icon: "fa-plus", text: t("Create state"), handler: this.handleCreate }));
        }
        // TODO show readonly if action==delete or info
        return React.createElement(
            Panel,
            { title: this._titles[this.props.sls.action], icon: "spacewalk-icon-virtual-host-manager" },
            errs,
            React.createElement(
                "form",
                { className: "form-horizontal" },
                React.createElement(
                    "div",
                    { className: "form-group" },
                    React.createElement(
                        "label",
                        { className: "col-md-3 control-label" },
                        "Name:"
                    ),
                    React.createElement(
                        "div",
                        { className: "col-md-6" },
                        React.createElement("input", { className: "form-control", type: "text", name: "name", ref: "stateName",
                            defaultValue: this.props.sls.name })
                    )
                ),
                React.createElement(
                    "div",
                    { className: "form-group" },
                    React.createElement(
                        "label",
                        { className: "col-md-3 control-label" },
                        "Content:"
                    ),
                    React.createElement(
                        "div",
                        { className: "col-md-6" },
                        React.createElement("textarea", { className: "form-control", rows: "20", name: "content", ref: "stateContent",
                            defaultValue: this.props.sls.content })
                    )
                ),
                React.createElement(
                    "div",
                    { className: "form-group" },
                    React.createElement(
                        "div",
                        { className: "col-md-offset-3 col-md-6" },
                        buttons
                    )
                )
            )
        );
    }
});

React.render(React.createElement(StateDetail, { sls: stateData() }), document.getElementById('state-details'));

},{"../components/messages":"/home/matei/workspace-suma3/spacewalk/web/html/src/components/messages.js","../components/panel":"/home/matei/workspace-suma3/spacewalk/web/html/src/components/panel.js","react":"react"}]},{},["/home/matei/workspace-suma3/spacewalk/web/html/src/manager/org-state-details.js"])
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyaWZ5L25vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJjb21wb25lbnRzL21lc3NhZ2VzLmpzIiwiY29tcG9uZW50cy9wYW5lbC5qcyIsIm1hbmFnZXIvb3JnLXN0YXRlLWRldGFpbHMuanMiXSwibmFtZXMiOltdLCJtYXBwaW5ncyI6IkFBQUE7QUNBQTs7QUFFQSxJQUFJLFFBQVEsUUFBUSxPQUFSLENBQVI7O0FBRUosSUFBSSxXQUFXLE1BQU0sV0FBTixDQUFrQjs7O0FBQy9CLGVBQWM7QUFDWixhQUFTLFFBQVQ7QUFDQSxlQUFXLFNBQVg7QUFDQSxZQUFRLE1BQVI7QUFDQSxlQUFXLFNBQVg7R0FKRjs7QUFPQSxtQkFBaUIsMkJBQVc7QUFDMUIsV0FBTyxFQUFQLENBRDBCO0dBQVg7O0FBSWpCLFVBQVEsa0JBQVc7QUFDakIsUUFBSSxPQUFPLEtBQUssS0FBTCxDQUFXLEtBQVgsQ0FBaUIsR0FBakIsQ0FBcUIsVUFBUyxJQUFULEVBQWU7QUFDbkMsYUFBUTs7VUFBSyxXQUFXLGlCQUFpQixLQUFLLFdBQUwsQ0FBaUIsS0FBSyxRQUFMLENBQWxDLEVBQWhCO1FBQW1FLEtBQUssSUFBTDtPQUEzRSxDQURtQztLQUFmLENBRXRCLElBRnNCLENBRWpCLElBRmlCLENBQXJCLENBQVAsQ0FEYTtBQUlqQixXQUFROzs7TUFBTSxJQUFOO0tBQVIsQ0FKaUI7R0FBWDs7Q0FaSyxDQUFYOztBQXFCSixPQUFPLE9BQVAsR0FBaUI7QUFDYixZQUFXLFFBQVg7Q0FESjs7O0FDekJBOztBQUVBLElBQUksUUFBUSxRQUFRLE9BQVIsQ0FBUjs7QUFFSixJQUFJLGNBQWMsTUFBTSxXQUFOLENBQWtCOzs7QUFFbEMsbUJBQWlCLDJCQUFXO0FBQzFCLFdBQU8sRUFBUCxDQUQwQjtHQUFYOztBQUlqQixVQUFRLGtCQUFXO0FBQ2YsV0FDSTs7UUFBSyxXQUFVLG1CQUFWLEVBQUw7TUFDRTs7VUFBRyxNQUFNLEtBQUssS0FB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QUFZLE1BQUssTUFBTCxFQUFZLEtBQUksV0FBSjtBQUNwRCwwQ0FBYyxLQUFLLEtBQUwsQ0FBVyxHQUFYLENBQWUsSUFBZixFQURsQixDQURKO3FCQUZKO2lCQURKO2dCQVNJOztzQkFBSyxXQUFVLFlBQVYsRUFBTDtvQkFDSTs7MEJBQU8sV0FBVSx3QkFBVixFQUFQOztxQkFESjtvQkFFSTs7MEJBQUssV0FBVSxVQUFWLEVBQUw7d0JBQ0ksa0NBQVUsV0FBVSxjQUFWLEVBQXlCLE1BQUssSUFBTCxFQUFVLE1BQUssU0FBTCxFQUFlLEtBQUksY0FBSjtBQUN4RCwwQ0FBYyxLQUFLLEtBQUwsQ0FBVyxHQUFYLENBQWUsT0FBZixFQURsQixDQURKO3FCQUZKO2lCQVRKO2dCQWlCSTs7c0JBQUssV0FBVSxZQUFWLEVBQUw7b0JBQ0k7OzBCQUFLLFdBQVUsMEJBQVYsRUFBTDt3QkFDSyxPQURMO3FCQURKO2lCQWpCSjthQUZKO1NBREEsQ0E5QmU7S0FBWDtDQS9ETSxDQUFkOztBQThISixNQUFNLE1BQU4sQ0FDRSxvQkFBQyxXQUFELElBQWEsS0FBSyxXQUFMLEVBQWIsQ0FERixFQUVFLFNBQVMsY0FBVCxDQUF3QixlQUF4QixDQUZGIiwiZmlsZSI6ImdlbmVyYXRlZC5qcyIsInNvdXJjZVJvb3QiOiIiLCJzb3VyY2VzQ29udGVudCI6WyIoZnVuY3Rpb24gZSh0LG4scil7ZnVuY3Rpb24gcyhvLHUpe2lmKCFuW29dKXtpZighdFtvXSl7dmFyIGE9dHlwZW9mIHJlcXVpcmU9PVwiZnVuY3Rpb25cIiYmcmVxdWlyZTtpZighdSYmYSlyZXR1cm4gYShvLCEwKTtpZihpKXJldHVybiBpKG8sITApO3ZhciBmPW5ldyBFcnJvcihcIkNhbm5vdCBmaW5kIG1vZHVsZSAnXCIrbytcIidcIik7dGhyb3cgZi5jb2RlPVwiTU9EVUxFX05PVF9GT1VORFwiLGZ9dmFyIGw9bltvXT17ZXhwb3J0czp7fX07dFtvXVswXS5jYWxsKGwuZXhwb3J0cyxmdW5jdGlvbihlKXt2YXIgbj10W29dWzFdW2VdO3JldHVybiBzKG4/bjplKX0sbCxsLmV4cG9ydHMsZSx0LG4scil9cmV0dXJuIG5bb10uZXhwb3J0c312YXIgaT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2Zvcih2YXIgbz0wO288ci5sZW5ndGg7bysrKXMocltvXSk7cmV0dXJuIHN9KSIsIid1c2Ugc3RyaWN0JztcblxudmFyIFJlYWN0ID0gcmVxdWlyZShcInJlYWN0XCIpO1xuXG52YXIgTWVzc2FnZXMgPSBSZWFjdC5jcmVhdGVDbGFzcyh7XG4gIF9jbGFzc05hbWVzIDoge1xuICAgIFwiZXJyb3JcIjogXCJkYW5nZXJcIixcbiAgICBcInN1Y2Nlc3NcIjogXCJzdWNjZXNzXCIsXG4gICAgXCJpbmZvXCI6IFwiaW5mb1wiLFxuICAgIFwid2FybmluZ1wiOiBcIndhcm5pbmdcIixcbiAgfSxcblxuICBnZXRJbml0aWFsU3RhdGU6IGZ1bmN0aW9uKCkge1xuICAgIHJldHVybiB7fTtcbiAgfSxcblxuICByZW5kZXI6IGZ1bmN0aW9uKCkge1xuICAgIHZhciBtc2dzID0gdGhpcy5wcm9wcy5pdGVtcy5tYXAoZnVuY3Rpb24oaXRlbSkge1xuICAgICAgICAgICAgICAgIHJldHVybiAoPGRpdiBjbGFzc05hbWU9eydhbGVydCBhbGVydC0nICsgdGhpcy5fY2xhc3NOYW1lc1tpdGVtLnNldmVyaXR5XX0+e2l0ZW0udGV4dH08L2Rpdj4pO1xuICAgICAgICAgICAgfS5iaW5kKHRoaXMpKTtcbiAgICByZXR1cm4gKDxkaXY+e21zZ3N9PC9kaXY+KTtcbiAgfVxuXG59KTtcblxubW9kdWxlLmV4cG9ydHMgPSB7XG4gICAgTWVzc2FnZXMgOiBNZXNzYWdlc1xufVxuIiwiJ3VzZSBzdHJpY3QnO1xuXG52YXIgUmVhY3QgPSByZXF1aXJlKFwicmVhY3RcIilcblxudmFyIFBhbmVsQnV0dG9uID0gUmVhY3QuY3JlYXRlQ2xhc3Moe1xuXG4gIGdldEluaXRpYWxTdGF0ZTogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIHt9O1xuICB9LFxuXG4gIHJlbmRlcjogZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gKFxuICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwic3BhY2V3YWxrLXRvb2xiYXJcIj5cbiAgICAgICAgICAgIDxhIGhyZWY9e3RoaXMucHJvcHMuYWN0aW9ufT5cbiAgICAgICAgICAgICAgPGkgY2xhc3NOYW1lPXsnZmEgJyArIHRoaXMucHJvcHMuaWNvbn0+PC9pPlxuICAgICAgICAgICAgICB7dCh0aGlzLnByb3BzLnRleHQpfVxuICAgICAgICAgICAgPC9hPlxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICApXG4gIH1cbn0pO1xuXG52YXIgUGFuZWwgPSBSZWFjdC5jcmVhdGVDbGFzcyh7XG4gIGdldEluaXRpYWxTdGF0ZTogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIHt9O1xuICB9LFxuXG4gIHJlbmRlcjogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXY+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwic3BhY2V3YWxrLXRvb2xiYXItaDFcIj5cbiAgICAgICAgICB7dGhpcy5wcm9wcy5idXR0b259XG4gICAgICAgICAgPGgxPjxpIGNsYXNzTmFtZT17J2ZhICcgKyB0aGlzLnByb3BzLmljb259PjwvaT57dCh0aGlzLnByb3BzLnRpdGxlKX08L2gxPlxuICAgICAgICA8L2Rpdj5cbiAgICAgICAge3RoaXMucHJvcHMuY2hpbGRyZW59XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59KTtcblxubW9kdWxlLmV4cG9ydHMgPSB7XG4gICAgUGFuZWwgOiBQYW5lbCxcbiAgICBQYW5lbEJ1dHRvbiA6IFBhbmVsQnV0dG9uXG59XG4iLCIndXNlIHN0cmljdCc7XG5cbnZhciBSZWFjdCA9IHJlcXVpcmUoXCJyZWFjdFwiKVxuXG52YXIgUGFuZWwgPSByZXF1aXJlKFwiLi4vY29tcG9uZW50cy9wYW5lbFwiKS5QYW5lbFxudmFyIE1lc3NhZ2VzID0gcmVxdWlyZShcIi4uL2NvbXBvbmVudHMvbWVzc2FnZXNcIikuTWVzc2FnZXNcblxudmFyIEJ1dHRvbiA9IFJlYWN0LmNyZWF0ZUNsYXNzKHtcblxuICAgIHJlbmRlcjogZnVuY3Rpb24oKSB7XG4gICAgICAgIHJldHVybiAoXG4gICAgICAgICAgICA8YnV0dG9uIHR5cGU9XCJidXR0b25cIiBjbGFzc05hbWU9eydidG4gJyArIHRoaXMucHJvcHMuY2xhc3NOYW1lfSBvbkNsaWNrPXt0aGlzLnByb3BzLmhhbmRsZXJ9PlxuICAgICAgICAgICAgICAgIDxpIGNsYXNzTmFtZT17J2ZhICcgKyB0aGlzLnByb3BzLmljb259Lz57dGhpcy5wcm9wcy50ZXh0fVxuICAgICAgICAgICAgPC9idXR0b24+XG4gICAgICAgIClcbiAgICB9XG59KVxuXG52YXIgU3RhdGVEZXRhaWwgPSBSZWFjdC5jcmVhdGVDbGFzcyh7XG5cbiAgICBfdGl0bGVzOiB7XG4gICAgICAgIFwiYWRkXCI6IHQoXCJBZGQgc3RhdGVcIiksXG4gICAgICAgIFwiZWRpdFwiOiB0KFwiRWRpdCBzdGF0ZVwiKSxcbiAgICAgICAgXCJkZWxldGVcIjogdChcIkRlbGV0ZSBzdGF0ZVwiKSxcbiAgICAgICAgXCJpbmZvXCI6IHQoXCJWaWV3IHN0YXRlXCIpXG4gICAgfSxcblxuICAgIGdldEluaXRpYWxTdGF0ZTogZnVuY3Rpb24oKSB7XG4gICAgICAgIHJldHVybiB7fTtcbiAgICB9LFxuXG4gICAgaGFuZGxlQ3JlYXRlOiBmdW5jdGlvbihlKSB7XG4gICAgICAgIHRoaXMuX3NhdmUoZSwgXCJQT1NUXCIpXG4gICAgfSxcblxuICAgIGhhbmRsZVVwZGF0ZTogZnVuY3Rpb24oZSkge1xuICAgICAgICB0aGlzLl9zYXZlKGUsIFwiUFVUXCIpXG4gICAgfSxcblxuICAgIGhhbmRsZURlbGV0ZTogZnVuY3Rpb24oZSkge1xuICAgICAgICB2YXIgciA9IGNvbmZpcm0odChcIkFyZSB5b3Ugc3VyZSB5b3Ugd2FudCB0byBkZWxldGUgc3RhdGUgJ3swfScgP1wiLCB0aGlzLnByb3BzLnNscy5uYW1lKSk7XG4gICAgICAgIGlmIChyID09IHRydWUpIHtcbiAgICAgICAgICAgIHRoaXMuX3NhdmUoZSwgXCJERUxFVEVcIilcbiAgICAgICAgfVxuICAgIH0sXG5cbiAgICBfc2F2ZTogZnVuY3Rpb24oZSwgaHR0cE1ldGhvZCkge1xuICAgICAgICB2YXIgZm9ybURhdGEgPSB7fTtcbiAgICAgICAgZm9ybURhdGFbJ25hbWUnXSA9IFJlYWN0LmZpbmRET01Ob2RlKHRoaXMucmVmcy5zdGF0ZU5hbWUpLnZhbHVlLnRyaW0oKTtcbiAgICAgICAgZm9ybURhdGFbJ2NvbnRlbnQnXSA9IFJlYWN0LmZpbmRET01Ob2RlKHRoaXMucmVmcy5zdGF0ZUNvbnRlbnQpLnZhbHVlLnRyaW0oKTtcbiAgICAgICAgaWYgKHRoaXMucHJvcHMuc2xzLmNoZWNrc3VtKSB7XG4gICAgICAgICAgICBmb3JtRGF0YVsnY2hlY2tzdW0nXSA9IHRoaXMucHJvcHMuc2xzLmNoZWNrc3VtO1xuICAgICAgICB9XG5cbiAgICAgICAgJC5hamF4KHtcbiAgICAgICAgICB1cmw6IHdpbmRvdy5sb2NhdGlvbi5ocmVmICsgKGNzcmZUb2tlbiA/IFwiP2NzcmZfdG9rZW49XCIgKyBjc3JmVG9rZW4gOiBcIlwiKSxcbiAgICAgICAgICBkYXRhVHlwZTogJ2pzb24nLFxuICAgICAgICAgIGNvbnRlbnRUeXBlOiBcImFwcGxpY2F0aW9uL2pzb25cIixcbiAgICAgICAgICB0eXBlOiBodHRwTWV0aG9kLFxuICAgICAgICAgIGRhdGE6IEpTT04uc3RyaW5naWZ5KGZvcm1EYXRhKSxcbiAgICAgICAgICBzdWNjZXNzOiBmdW5jdGlvbihkYXRhKSB7XG4gICAgICAgICAgICBjb25zb2xlLmxvZyhkYXRhKVxuICAgICAgICAgICAgdGhpcy5zZXRTdGF0ZSh7bWVzc2FnZXM6IFtkYXRhLm1lc3NhZ2VdfSlcbiAgICAgICAgICAgIHdpbmRvdy5sb2NhdGlvbi5ocmVmID0gZGF0YS51cmxcbiAgICAgICAgICB9LmJpbmQodGhpcyksXG5cbiAgICAgICAgICBlcnJvcjogZnVuY3Rpb24oeGhyLCBzdGF0dXMsIGVycikge1xuICAgICAgICAgICAgaWYgKHhoci5zdGF0dXMgPT0gNDAwKSB7XG4gICAgICAgICAgICAgICAgLy8gdmFsaWRhdGlvbiBlcnJcbiAgICAgICAgICAgICAgICB2YXIgZXJycyA9ICQucGFyc2VKU09OKHhoci5yZXNwb25zZVRleHQpO1xuICAgICAgICAgICAgICAgIHRoaXMuc2V0U3RhdGUoe2Vycm9yczogZXJyc30pXG4gICAgICAgICAgICB9IGVsc2UgaWYgKHhoci5zdGF0dXMgPT0gNTAwKSB7XG4gICAgICAgICAgICAgICAgdGhpcy5zZXRTdGF0ZSh7ZXJyb3JzOiBbdChcIkFuIGludGVybmFsIHNlcnZlciBlcnJvciBvY2N1cnJlZFwiKV19KVxuICAgICAgICAgICAgfSBlbHNlIHtcbiAgICAgICAgICAgICAgICBjb25zb2xlLmVycm9yKHN0YXR1cywgZXJyLnRvU3RyaW5nKCkpO1xuICAgICAgICAgICAgfVxuICAgICAgICAgIH0uYmluZCh0aGlzKVxuICAgICAgICB9KTtcblxuICAgIH0sXG5cbiAgICByZW5kZXI6IGZ1bmN0aW9uKCkge1xuICAgICAgICB2YXIgZXJycyA9IG51bGw7XG4gICAgICAgIGlmICh0aGlzLnN0YXRlLmVycm9ycykge1xuICAgICAgICAgICAgZXJycyA9IDxNZXNzYWdlcyBpdGVtcz17dGhpcy5zdGF0ZS5lcnJvcnMubWFwKGZ1bmN0aW9uKGUpIHtcbiAgICAgICAgICAgICAgICByZXR1cm4ge3NldmVyaXR5OiBcImVycm9yXCIsIHRleHQ6IGV9O1xuICAgICAgICAgICAgfSl9Lz47XG4vLyAgICAgICAgICAgIGVycnMgPSB0aGlzLnN0YXRlLmVycm9ycy5tYXAoIGZ1bmN0aW9uKGUpIHtcbi8vICAgICAgICAgICAgICAgICAgICByZXR1cm4gKDxkaXYgY2xhc3NOYW1lPVwiYWxlcnQgYWxlcnQtZGFuZ2VyXCI+e3QoZSl9PC9kaXY+KVxuLy8gICAgICAgICAgICAgICAgICAgfSlcblxuICAgICAgICB9XG5cbi8vICAgICAgICBpZiAodGhpcy5zdGF0ZS5tZXNzYWdlcykge1xuLy8gICAgICAgICAgICBlcnJzID0gdGhpcy5zdGF0ZS5tZXNzYWdlcy5tYXAoIGZ1bmN0aW9uKGUpIHtcbi8vICAgICAgICAgICAgICAgICAgICByZXR1cm4gKDxkaXYgY2xhc3NOYW1lPVwiYWxlcnQgYWxlcnQtaW5mb1wiPnt0KGUpfTwvZGl2Pilcbi8vICAgICAgICAgICAgICAgICAgIH0pO1xuLy8gICAgICAgIH1cblxuICAgICAgICB2YXIgYnV0dG9ucyA9IFtdO1xuICAgICAgICBpZiAodGhpcy5wcm9wcy5zbHMuYWN0aW9uID09IFwiZWRpdFwiKSB7XG4gICAgICAgICAgICBidXR0b25zLnB1c2goXG4gICAgICAgICAgICAgICAgPEJ1dHRvbiBjbGFzc05hbWU9XCJidG4tc3VjY2Vzc1wiIGljb249XCJmYS1wbHVzXCIgdGV4dD17dChcIlNhdmUgc3RhdGVcIil9IGhhbmRsZXI9e3RoaXMuaGFuZGxlVXBkYXRlfS8+LFxuICAgICAgICAgICAgICAgIDxCdXR0b24gY2xhc3NOYW1lPVwiYnRuLWRhbmdlclwiIGljb249XCJmYS10cmFzaFwiIHRleHQ9e3QoXCJEZWxldGUgc3RhdGVcIil9IGhhbmRsZXI9e3RoaXMuaGFuZGxlRGVsZXRlfS8+XG4gICAgICAgICAgICApO1xuICAgICAgICB9IGVsc2Uge1xuICAgICAgICAgICAgYnV0dG9ucy5wdXNoKFxuICAgICAgICAgICAgICAgIDxCdXR0b24gY2xhc3NOYW1lPVwiYnRuLXN1Y2Nlc3NcIiBpY29uPVwiZmEtcGx1c1wiIHRleHQ9e3QoXCJDcmVhdGUgc3RhdGVcIil9IGhhbmRsZXI9e3RoaXMuaGFuZGxlQ3JlYXRlfS8+XG4gICAgICAgICAgICAgICAgKTtcbiAgICAgICAgfVxuICAgICAgICAvLyBUT0RPIHNob3cgcmVhZG9ubHkgaWYgYWN0aW9uPT1kZWxldGUgb3IgaW5mb1xuICAgICAgICByZXR1cm4gKFxuICAgICAgICA8UGFuZWwgdGl0bGU9e3RoaXMuX3RpdGxlc1t0aGlzLnByb3BzLnNscy5hY3Rpb25dfSBpY29uPVwic3BhY2V3YWxrLWljb24tdmlydHVhbC1ob3N0LW1hbmFnZXJcIj5cbiAgICAgICAgICAgIHtlcnJzfVxuICAgICAgICAgICAgPGZvcm0gY2xhc3NOYW1lPVwiZm9ybS1ob3Jpem9udGFsXCI+XG4gICAgICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJmb3JtLWdyb3VwXCI+XG4gICAgICAgICAgICAgICAgICAgIDxsYWJlbCBjbGFzc05hbWU9XCJjb2wtbWQtMyBjb250cm9sLWxhYmVsXCI+TmFtZTo8L2xhYmVsPlxuICAgICAgICAgICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1tZC02XCI+XG4gICAgICAgICAgICAgICAgICAgICAgICA8aW5wdXQgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCIgdHlwZT1cInRleHRcIiBuYW1lPVwibmFtZVwiIHJlZj1cInN0YXRlTmFtZVwiXG4gICAgICAgICAgICAgICAgICAgICAgICAgICAgZGVmYXVsdFZhbHVlPXt0aGlzLnByb3BzLnNscy5uYW1lfS8+XG4gICAgICAgICAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAgICAgIDwvZGl2PlxuXG4gICAgICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJmb3JtLWdyb3VwXCI+XG4gICAgICAgICAgICAgICAgICAgIDxsYWJlbCBjbGFzc05hbWU9XCJjb2wtbWQtMyBjb250cm9sLWxhYmVsXCI+Q29udGVudDo8L2xhYmVsPlxuICAgICAgICAgICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1tZC02XCI+XG4gICAgICAgICAgICAgICAgICAgICAgICA8dGV4dGFyZWEgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sXCIgcm93cz1cIjIwXCIgbmFtZT1cImNvbnRlbnRcIiByZWY9XCJzdGF0ZUNvbnRlbnRcIlxuICAgICAgICAgICAgICAgICAgICAgICAgICAgIGRlZmF1bHRWYWx1ZT17dGhpcy5wcm9wcy5zbHMuY29udGVudH0vPlxuICAgICAgICAgICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICAgICAgICA8L2Rpdj5cblxuICAgICAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwiZm9ybS1ncm91cFwiPlxuICAgICAgICAgICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cImNvbC1tZC1vZmZzZXQtMyBjb2wtbWQtNlwiPlxuICAgICAgICAgICAgICAgICAgICAgICAge2J1dHRvbnN9XG4gICAgICAgICAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAgICAgIDwvZGl2PlxuXG4gICAgICAgICAgICA8L2Zvcm0+XG4gICAgICAgIDwvUGFuZWw+XG4gICAgICAgIClcblxuICAgIH1cbn0pO1xuXG5SZWFjdC5yZW5kZXIoXG4gIDxTdGF0ZURldGFpbCBzbHM9e3N0YXRlRGF0YSgpfS8+LFxuICBkb2N1bWVudC5nZXRFbGVtZW50QnlJZCgnc3RhdGUtZGV0YWlscycpXG4pOyJdfQ==
