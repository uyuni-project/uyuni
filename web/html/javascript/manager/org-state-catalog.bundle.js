(function e(t,n,r){function s(o,u){if(!n[o]){if(!t[o]){var a=typeof require=="function"&&require;if(!u&&a)return a(o,!0);if(i)return i(o,!0);var f=new Error("Cannot find module '"+o+"'");throw f.code="MODULE_NOT_FOUND",f}var l=n[o]={exports:{}};t[o][0].call(l.exports,function(e){var n=t[o][1][e];return s(n?n:e)},l,l.exports,e,t,n,r)}return n[o].exports}var i=typeof require=="function"&&require;for(var o=0;o<r.length;o++)s(r[o]);return s})({1:[function(require,module,exports){
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

},{"react":"react"}],2:[function(require,module,exports){
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

},{"react":"react"}],3:[function(require,module,exports){
'use strict';

var React = require("react");
var StatePersistedMixin = require("./util").StatePersistedMixin;

var Table = React.createClass({
  displayName: "Table",

  mixins: [StatePersistedMixin],

  getInitialState: function getInitialState() {
    return {
      "currentPage": 1, "itemsPerPage": 15,
      "searchField": "",
      "columnIndex": 0, "ascending": true
    };
  },

  componentWillReceiveProps: function componentWillReceiveProps(nextProps) {
    var columnIndex;
    if (this.props.sortableColumns) {
      columnIndex = this.props.sortableColumns[0];
    }
    var lastPage = Math.ceil(nextProps.rows.length / nextProps.itemsPerPage);
    if (this.state.currentPage > lastPage) {
      this.setState({ "currentPage": lastPage, "columnIndex": columnIndex });
    }
  },

  orderByColumn: function orderByColumn(columnIndex) {
    var ascending = this.state.ascending;
    if (this.state.columnIndex == columnIndex) {
      ascending = !ascending;
    } else {
      ascending = true;
    }
    this.setState({ "columnIndex": columnIndex, "ascending": ascending });
  },

  getRows: function getRows(unfilteredRows, searchValue) {
    var _this = this;

    var rows = this.props.dataFilter && searchValue.length > 0 ? unfilteredRows.filter(function (row) {
      return _this.props.dataFilter(row, searchValue);
    }) : unfilteredRows;
    if (this.props.rowComparator) {
      var columnIndex = this.state.columnIndex;
      var ascending = this.state.ascending;
      rows.sort(function (a, b) {
        return _this.props.rowComparator(a, b, columnIndex, ascending);
      });
    }
    return rows;
  },

  lastPage: function lastPage(rows, itemsPerPage) {
    var lastPage = Math.ceil(rows.length / itemsPerPage);
    if (lastPage == 0) {
      return 1;
    }
    return lastPage;
  },

  goToPage: function goToPage(page) {
    this.setState({ "currentPage": page });
  },

  onItemsPerPageChange: function onItemsPerPageChange(itemsPerPage) {
    this.setState({ "itemsPerPage": itemsPerPage });
    var lastPage = this.lastPage(this.getRows(this.props.rows, this.state.searchField), itemsPerPage);
    if (this.state.currentPage > lastPage) {
      this.setState({ "currentPage": lastPage });
    }
  },

  onSearchFieldChange: function onSearchFieldChange(searchValue) {
    this.setState({ "searchField": searchValue });
    var lastPage = this.lastPage(this.getRows(this.props.rows, searchValue), this.state.itemsPerPage);
    if (this.state.currentPage > lastPage) {
      this.setState({ "currentPage": lastPage });
    }
  },

  render: function render() {
    var _this2 = this;

    var rows = this.getRows(this.props.rows, this.state.searchField);
    var itemsPerPage = this.state.itemsPerPage;
    var itemCount = rows.length;
    var lastPage = this.lastPage(rows, itemsPerPage);
    var currentPage = this.state.currentPage;

    var firstItemIndex = (currentPage - 1) * itemsPerPage;

    var fromItem = itemCount > 0 ? firstItemIndex + 1 : 0;
    var toItem = firstItemIndex + itemsPerPage <= itemCount ? firstItemIndex + itemsPerPage : itemCount;

    var pagination;
    if (lastPage > 1) {
      pagination = React.createElement(
        "div",
        { className: "spacewalk-list-pagination" },
        React.createElement(
          "div",
          { className: "spacewalk-list-pagination-btns btn-group" },
          React.createElement(PaginationButton, { onClick: this.goToPage, toPage: 1, disabled: currentPage == 1, text: t("First") }),
          React.createElement(PaginationButton, { onClick: this.goToPage, toPage: currentPage - 1, disabled: currentPage == 1, text: t("Prev") }),
          React.createElement(PaginationButton, { onClick: this.goToPage, toPage: currentPage + 1, disabled: currentPage == lastPage, text: t("Next") }),
          React.createElement(PaginationButton, { onClick: this.goToPage, toPage: lastPage, disabled: currentPage == lastPage, text: t("Last") })
        )
      );
    }

    var searchField;
    if (this.props.dataFilter) {
      searchField = React.createElement(SearchField, {
        onChange: this.onSearchFieldChange,
        defaultValue: this.state.searchField,
        placeholder: this.props.searchPlaceholder
      });
    }

    return React.createElement(
      "div",
      { className: "panel panel-default" },
      React.createElement(
        "div",
        { className: "panel-heading" },
        React.createElement(
          "div",
          { className: "spacewalk-list-head-addons" },
          React.createElement(
            "div",
            { className: "spacewalk-list-filter table-search-wrapper" },
            searchField,
            " ",
            t("Items {0} - {1} of {2}", fromItem, toItem, itemCount)
          ),
          React.createElement(
            "div",
            { className: "spacewalk-list-head-addons-extra table-items-per-page-wrapper" },
            React.createElement(PageSelector, { className: "display-number",
              options: [5, 10, 15, 25, 50, 100, 250, 500],
              currentValue: itemsPerPage,
              onChange: this.onItemsPerPageChange
            }),
            " ",
            t("items per page")
          )
        )
      ),
      React.createElement(
        "div",
        { className: "table-responsive" },
        React.createElement(
          "table",
          { className: "table table-striped" },
          React.createElement(TableHeader, {
            content: this.props.headers.map(function (header, index) {
              var className;
              if (index == _this2.state.columnIndex) {
                className = (_this2.state.ascending ? "asc" : "desc") + "Sort";
              }
              return _this2.props.sortableColumns && _this2.props.sortableColumns.filter(function (element) {
                return element == index;
              }).length > 0 ? React.createElement(TableHeaderCellOrder, { className: className, content: header,
                orderBy: _this2.orderByColumn, columnIndex: index }) : React.createElement(TableHeaderCell, { className: className, content: header });
            })
          }),
          React.createElement(
            "tbody",
            { className: "table-content" },
            rows.filter(function (element, i) {
              return i >= firstItemIndex && i < firstItemIndex + itemsPerPage;
            })
          )
        )
      ),
      React.createElement(
        "div",
        { className: "panel-footer" },
        React.createElement(
          "div",
          { className: "spacewalk-list-bottom-addons" },
          React.createElement(
            "div",
            { className: "table-page-information" },
            t("Page {0} of {1}", currentPage, lastPage)
          ),
          pagination
        )
      )
    );
  }
});

var PaginationButton = React.createClass({
  displayName: "PaginationButton",

  onClick: function onClick() {
    this.props.onClick(this.props.toPage);
  },

  render: function render() {
    return React.createElement(
      "button",
      { type: "button", className: "btn btn-default",
        disabled: this.props.disabled, onClick: this.onClick },
      this.props.text
    );
  }
});

var PageSelector = React.createClass({
  displayName: "PageSelector",

  handleOnChange: function handleOnChange(e) {
    this.props.onChange(parseInt(e.target.value));
  },

  render: function render() {
    return React.createElement(
      "select",
      { className: this.props.className,
        defaultValue: this.props.currentValue,
        onChange: this.handleOnChange },
      this.props.options.map(function (o) {
        return React.createElement(
          "option",
          { value: o },
          o
        );
      })
    );
  }
});

var TableHeader = React.createClass({
  displayName: "TableHeader",

  render: function render() {
    return React.createElement(
      "thead",
      null,
      React.createElement(
        "tr",
        null,
        this.props.content
      )
    );
  }
});

var TableHeaderCellOrder = React.createClass({
  displayName: "TableHeaderCellOrder",

  handleClick: function handleClick() {
    if (this.props.columnIndex != null) {
      this.props.orderBy(this.props.columnIndex);
    }
  },

  render: function render() {
    return React.createElement(
      "th",
      { className: this.props.className },
      React.createElement(
        "a",
        { className: "orderBy", onClick: this.handleClick },
        this.props.content
      )
    );
  }
});

var TableHeaderCell = React.createClass({
  displayName: "TableHeaderCell",

  render: function render() {
    return React.createElement(
      "th",
      { className: this.props.className },
      this.props.content
    );
  }
});

var TableRow = React.createClass({
  displayName: "TableRow",

  render: function render() {
    return React.createElement(
      "tr",
      { className: this.props.className },
      this.props.columns
    );
  }
});

var TableCell = React.createClass({
  displayName: "TableCell",

  render: function render() {
    return React.createElement(
      "td",
      null,
      this.props.content
    );
  }
});

var SearchField = React.createClass({
  displayName: "SearchField",

  handleChange: function handleChange(e) {
    this.props.onChange(e.target.value);
  },

  render: function render() {
    return React.createElement("input", { className: "form-control table-input-search",
      value: this.props.defaultValue,
      placeholder: this.props.placeholder,
      type: "text",
      onChange: this.handleChange });
  }
});

module.exports = {
  Table: Table,
  TableCell: TableCell,
  TableRow: TableRow,
  SearchField: SearchField,
  TableHeaderCell: TableHeaderCell,
  TableHeaderCellOrder: TableHeaderCellOrder,
  PageSelector: PageSelector,
  PaginationButton: PaginationButton
};

},{"./util":4,"react":"react"}],4:[function(require,module,exports){
'use strict';

var StatePersistedMixin = {
  componentWillMount: function componentWillMount() {
    if (this.props.loadState) {
      if (this.props.loadState()) {
        this.state = this.props.loadState();
      }
    }
  },
  componentWillUnmount: function componentWillUnmount() {
    if (this.props.saveState) {
      this.props.saveState(this.state);
    }
  }
};

module.exports = {
  StatePersistedMixin: StatePersistedMixin
};

},{}],5:[function(require,module,exports){
'use strict';

var React = require("react");
var TableComponent = require("../components/table");
var PanelComponent = require("../components/panel");
var Messages = require("../components/messages").Messages;

var Table = TableComponent.Table;
var TableCell = TableComponent.TableCell;
var TableRow = TableComponent.TableRow;
var Panel = PanelComponent.Panel;
var PanelButton = PanelComponent.PanelButton;

var StateCatalog = React.createClass({
    displayName: "StateCatalog",

    getInitialState: function getInitialState() {
        var st = {
            "serverData": []
        };
        //        if (this.props.flashMessages) {
        //            st["flashMessages"] = this.props.flashMessages;
        //        }
        return st;
    },

    refreshServerData: function refreshServerData() {
        var _this = this;

        $.get("/rhn/manager/state_catalog/data", function (data) {
            _this.setState({ "serverData": data });
        });
    },

    componentWillMount: function componentWillMount() {
        this.refreshServerData();
    },

    //    componentDidMount: function() {
    //        var views = 1;
    //        if(this.state.flashMessagesViews) {
    //            views = this.state.flashMessagesViews + 1;
    //        }
    //        this.setState({ "flashMessagesViews": views });
    //    },
    //
    //    shouldComponentUpdate: function() {
    //        return typeof this.state.flashMessagesViews === "undefined" ||
    //            this.state.flashMessagesViews > 1;
    //    },

    compareRows: function compareRows(a, b, columnIndex, order) {
        var orderCondition = order ? 1 : -1;
        var aValue = a.props["raw_data"];
        var bValue = b.props["raw_data"];
        var result = aValue.localeCompare(bValue);
        return result * orderCondition;
    },

    render: function render() {
        var button = React.createElement(PanelButton, { text: "Create state", icon: "fa-plus", action: "/rhn/manager/state_catalog/state" });

        var msg = null;
        //        if(typeof this.state.flashMessages !== "undefined" && this.state.flashMessages.length > 0) {
        //            msg = <Messages items={this.props.flashMessages}/>
        //        }

        if (typeof this.props.flashMessages !== "undefined") {
            //            var items = {severity: "error", text: "alalalal"};
            msg = React.createElement(Messages, { items: this.props.flashMessages });
        }
        return React.createElement(
            Panel,
            { title: "States Catalog", icon: "spacewalk-icon-virtual-host-manager", button: button },
            msg,
            React.createElement(
                "div",
                null,
                React.createElement(
                    "div",
                    { className: "spacewalk-list" },
                    React.createElement(Table, { headers: [t("State")],
                        rows: statesToRows(this.state.serverData),
                        loadState: this.props.loadState,
                        saveState: this.props.saveState,
                        rowComparator: this.compareRows,
                        sortableColumns: [0],
                        dataFilter: function dataFilter(tableRow, searchValue) {
                            return tableRow.props["raw_data"].toLowerCase().indexOf(searchValue.toLowerCase()) > -1;
                        },
                        searchPlaceholder: t("Filter by state name:")
                    })
                )
            )
        );
    }

});

function statesToRows(serverData) {
    return serverData.map(function (s) {
        var link = React.createElement(
            "a",
            { href: "/rhn/manager/state_catalog/state/" + s },
            s
        );
        var columns = [React.createElement(TableCell, { content: link })];
        return React.createElement(TableRow, { columns: columns, raw_data: s });
    });
}

React.render(React.createElement(StateCatalog, { flashMessages: flashMessage() }), document.getElementById('state-catalog'));

},{"../components/messages":1,"../components/panel":2,"../components/table":3,"react":"react"}]},{},[5]);
