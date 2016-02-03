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

},{}],"/home/matei/workspace-suma3/spacewalk/web/html/src/components/table.js":[function(require,module,exports){
'use strict';

var React = require("react")
var t = require("./react-translation")

var PaginationButton = React.createClass({displayName: "PaginationButton",
  onClick: function() {
    this.props.onClick(this.props.toPage);
  },

  render: function() {
    return (
      React.createElement("button", {type: "button", className: "btn btn-default", 
        disabled: this.props.disabled, onClick: this.onClick}, 
        this.props.text
      )
    );
  }
});

var Select = React.createClass({displayName: "Select",
  handleOnChange: function(e) {
    if (this.props.type && this.props.type == "number") {
      this.props.onChange(parseInt(e.target.value));
    }
    else {
      this.props.onChange(e.target.value);
    }
  },

  render: function() {
    return (
      React.createElement("select", {className: this.props.className, 
        defaultValue: this.props.currentValue, 
        onChange: this.handleOnChange}, 
        this.props.options.map(function(o) {
          return (React.createElement("option", {value: o}, o));
        })
      )
    );
  }
});

var TableHeader = React.createClass({displayName: "TableHeader",
  render: function() {
    return (
      React.createElement("thead", null, React.createElement("tr", null, this.props.content))
    );
  }
});

var TableHeaderCellOrder = React.createClass({displayName: "TableHeaderCellOrder",
  handleClick: function() {
    if (this.props.columnIndex != null) {
      this.props.orderBy(this.props.columnIndex);
    }
  },

  render: function () {
    return (React.createElement("th", {className: this.props.className}, React.createElement("a", {className: "orderBy", onClick: this.handleClick}, this.props.content)));
  }
});


var TableHeaderCell = React.createClass({displayName: "TableHeaderCell",
  render: function () {
    return (React.createElement("th", {className: this.props.className}, this.props.content));
  }
});

var TableRow = React.createClass({displayName: "TableRow",
  render: function() {
    return (
      React.createElement("tr", {className: this.props.className}, 
        this.props.columns
      )
    );
  }
});

var TableCell = React.createClass({displayName: "TableCell",
  render: function() {
    return (
      React.createElement("td", null, 
        this.props.content
      )
    );
  }
});

var QuantityCell = React.createClass({displayName: "QuantityCell",
  render: function() {
    var matched = this.props.matched;
    var total = this.props.total;
    var content = matched + "/" + total;

    return (
      matched == total ?
        React.createElement(TableCell, {content: React.createElement(StrongText, {className: "bg-danger", content: content})}) :
        React.createElement(TableCell, {content: content})
    );
  }
});

var SearchField = React.createClass({displayName: "SearchField",
  handleChange: function(e) {
    this.props.onChange(e.target.value);
  },

  render: function() {
    return (
      React.createElement("input", {className: "form-control table-input-search", 
        value: this.props.defaultValue, 
        placeholder: this.props.placeholder, 
        type: "text", 
        onChange: this.handleChange})
    );
  }
});

var StrongText = React.createClass({displayName: "StrongText",
  render: function() {
    return (
      React.createElement("strong", {className: this.props.className}, 
        this.props.content
      )
    );
  }
});

var ToolTip = React.createClass({displayName: "ToolTip",
  render: function() {
    return (
      React.createElement("span", {title: this.props.title}, 
        this.props.content
      )
    );
  }
});

var CsvLink = React.createClass({displayName: "CsvLink",
  render: function() {
    return (
      React.createElement("div", {className: "spacewalk-csv-download"}, 
        React.createElement("a", {className: "btn btn-link", href: "/rhn/manager/subscription_matching/" + this.props.name}, 
          React.createElement("i", {className: "fa spacewalk-icon-download-csv"}), 
          t("Download CSV")
        )
      )
    );
  }
});

var Table = React.createClass({displayName: "Table",
  mixins: [StatePersistedMixin],

  getInitialState: function() {
    return {
      "currentPage": 1, "itemsPerPage": 15,
      "searchField": "",
      "columnIndex": 0, "order": "asc"
    };
  },

  componentWillReceiveProps: function(nextProps) {
    var columnIndex;
    if (this.props.sortableColumns) {
      columnIndex = this.props.sortableColumns[0];
    }
    var lastPage = Math.ceil(nextProps.rows.length / nextProps.itemsPerPage);
    if (this.state.currentPage > lastPage) {
      this.setState({"currentPage": lastPage, "columnIndex" : columnIndex});
    }
  },

  orderByColumn: function(columnIndex) {
    var order = this.state.order;
    if (this.state.columnIndex == columnIndex) {
      order = order == "asc" ? "desc" : "asc";
    }
    else {
      order = "asc";
    }
    this.setState({"columnIndex": columnIndex, "order": order});
  },

  getRows: function(unfiltered_rows, searchValue) {
    var rows = this.props.dataFilter && searchValue.length > 0 ?
      unfiltered_rows.filter((row) => this.props.dataFilter(row, searchValue)) :
      unfiltered_rows;
      if (this.props.sortRow) {
        var columnIndex = this.state.columnIndex;
        var order = this.state.order;
        rows.sort((a, b) => this.props.sortRow(a, b, columnIndex, order));
      }
    return rows;
  },

  lastPage: function(rows, itemsPerPage) {
    var lastPage = Math.ceil(rows.length / itemsPerPage);
    if (lastPage == 0) {
      return 1;
    }
    return lastPage;
  },

  goToPage:function(page) {
    this.setState({"currentPage": page});
  },

  changeItemsPerPage: function(itemsPerPage) {
    this.setState({"itemsPerPage": itemsPerPage});
    var lastPage = this.lastPage(this.getRows(this.props.rows, this.state.searchField), itemsPerPage);
    if (this.state.currentPage > lastPage) {
      this.setState({"currentPage": lastPage });
    }
  },

  changeSearchField: function(searchValue) {
    this.setState({"searchField": searchValue});
    var lastPage =  this.lastPage(this.getRows(this.props.rows, searchValue), this.state.itemsPerPage);
    if (this.state.currentPage > lastPage) {
      this.setState({"currentPage": lastPage });
    }
  },

  render: function() {
    var rows = this.getRows(this.props.rows, this.state.searchField);
    var itemsPerPage = this.state.itemsPerPage;
    var itemCount = rows.length;
    var lastPage = this.lastPage(rows, itemsPerPage);
    var currentPage = this.state.currentPage;

    var firstItemIndex = (currentPage - 1) * itemsPerPage;

    var fromItem = itemCount > 0 ? firstItemIndex +1 : 0;
    var toItem = firstItemIndex + itemsPerPage <= itemCount ? firstItemIndex + itemsPerPage : itemCount;

    var pagination;
    if (lastPage > 1) {
      pagination = (
        React.createElement("div", {className: "spacewalk-list-pagination"}, 
          React.createElement("div", {className: "spacewalk-list-pagination-btns btn-group"}, 
            React.createElement(PaginationButton, {onClick: this.goToPage, toPage: 1, disabled: currentPage == 1, text: t("First")}), 
            React.createElement(PaginationButton, {onClick: this.goToPage, toPage: currentPage -1, disabled: currentPage == 1, text: t("Prev")}), 
            React.createElement(PaginationButton, {onClick: this.goToPage, toPage: currentPage + 1, disabled: currentPage == lastPage, text: t("Next")}), 
            React.createElement(PaginationButton, {onClick: this.goToPage, toPage: lastPage, disabled: currentPage == lastPage, text: t("Last")})
          )
        )
      );
    }

    var searchField;
    if (this.props.dataFilter) {
      searchField = (
        React.createElement(SearchField, {
          onChange: this.changeSearchField, 
          defaultValue: this.state.searchField, 
          placeholder: this.props.searchPlaceholder}
        )
      );
    }

    var component = this;

    return (
      React.createElement("div", {className: "panel panel-default"}, 
        React.createElement("div", {className: "panel-heading"}, 
          React.createElement("div", {className: "spacewalk-list-head-addons"}, 
            React.createElement("div", {className: "spacewalk-list-filter table-search-wrapper"}, 
              searchField, " ", t("Items {0} - {1} of {2}", fromItem, toItem, itemCount)
            ), 
            React.createElement("div", {className: "spacewalk-list-head-addons-extra table-items-per-page-wrapper"}, 
              React.createElement(Select, {className: "display-number", 
                type: "number", 
                options: [5,10,15,25,50,100,250,500], 
                currentValue: itemsPerPage, 
                onChange: this.changeItemsPerPage}
              ), " ", t("items per page")
            )
          )
        ), 
        React.createElement("div", {className: "table-responsive"}, 
          React.createElement("table", {className: "table table-striped"}, 
            React.createElement(TableHeader, {
              content: 
                this.props.headers.map(function(header, index) {
                  var className;
                  if (index == component.state.columnIndex) {
                    className = component.state.order + "Sort";
                  }
                  return (
                      (component.props.sortableColumns &&
                        component.props.sortableColumns.filter((element) => element == index).length > 0) ?
                      React.createElement(TableHeaderCellOrder, {className: className, content: header, 
                        orderBy: component.orderByColumn, columnIndex: index}) :
                      React.createElement(TableHeaderCell, {className: className, content: header})
                  );
                })}
            ), 
            React.createElement("tbody", {className: "table-content"}, 
              rows
                .filter((element, i) => i >= firstItemIndex && i < firstItemIndex + itemsPerPage)
              
              )
          )
        ), 
        React.createElement("div", {className: "panel-footer"}, 
          React.createElement("div", {className: "spacewalk-list-bottom-addons"}, 
            React.createElement("div", {className: "table-page-information"}, t("Page {0} of {1}", currentPage, lastPage)), 
            pagination
          )
        )
      )
    );
  }
});

var StatePersistedMixin = {
  componentWillMount: function() {
    if (this.props.loadState) {
      if (this.props.loadState()) {
        this.state = this.props.loadState();
      }
    }
  },
  componentWillUnmount: function() {
    if (this.props.saveState) {
      this.props.saveState(this.state);
    }
  },
};

module.exports.Table = Table
module.exports.TableCell = TableCell
module.exports.TableRow = TableRow

},{"./react-translation":"/home/matei/workspace-suma3/spacewalk/web/html/src/components/react-translation.js","react":"react"}],"/home/matei/workspace-suma3/spacewalk/web/html/src/manager/org-state-catalog.js":[function(require,module,exports){
'use strict';

var React = require("react")
var TableComponent = require("../components/table.js")
var PanelComponent = require("../components/panel.js")

var Table = TableComponent.Table
var TableCell = TableComponent.TableCell
var TableRow = TableComponent.TableRow
var Panel = PanelComponent.Panel
var PanelButton = PanelComponent.PanelButton

var StateCatalog = React.createClass({displayName: "StateCatalog",

    getInitialState: function() {
        return {"serverData": []};
    },

    refreshServerData: function() {
        $.get("/rhn/manager/state_catalog/data", data => {
          this.setState({"serverData" : data});
        });
    },

    componentWillMount: function() {
        this.refreshServerData();
    },
//            <ul>
//               { this.state.serverData.map( (e) => {
//                    return <li>{e}</li>
//                })
//               }
//            </ul>
    render: function() {
        var button = React.createElement(PanelButton, {text: "Create state", icon: "fa-plus", action: "/rhn/manager/state_catalog/add"})

        return (
            React.createElement(Panel, {title: "State catalog", icon: "spacewalk-icon-virtual-host-manager", button: button}, 
                React.createElement("div", null, 
                    React.createElement("div", {className: "spacewalk-list"}, 
                        React.createElement(Table, {headers: [t("State")], 
                          rows: statesToRows(this.state.serverData), 
                          loadState: this.props.loadState, 
                          saveState: this.props.saveState, 
                          sortRow: this.sortRow, 
                          sortableColumns: [0]}
                        )
                    )
                )
            )
        );
    }

});

function statesToRows(serverData) {
  return serverData.map((s) => {
    var columns = [
      React.createElement(TableCell, {content: s}),
    ];
    return React.createElement(TableRow, {columns: columns, raw_data: s})
  });
}

React.render(
  React.createElement(StateCatalog, null),
  document.getElementById('state-catalog')
);

},{"../components/panel.js":"/home/matei/workspace-suma3/spacewalk/web/html/src/components/panel.js","../components/table.js":"/home/matei/workspace-suma3/spacewalk/web/html/src/components/table.js","react":"react"}]},{},["/home/matei/workspace-suma3/spacewalk/web/html/src/manager/org-state-catalog.js"]);
