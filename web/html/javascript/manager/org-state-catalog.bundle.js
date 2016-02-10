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

},{"react":"react"}],"/home/matei/workspace-suma3/spacewalk/web/html/src/components/table.js":[function(require,module,exports){
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

},{"./util":"/home/matei/workspace-suma3/spacewalk/web/html/src/components/util.js","react":"react"}],"/home/matei/workspace-suma3/spacewalk/web/html/src/components/util.js":[function(require,module,exports){
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

},{}],"/home/matei/workspace-suma3/spacewalk/web/html/src/manager/org-state-catalog.js":[function(require,module,exports){
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

},{"../components/messages":"/home/matei/workspace-suma3/spacewalk/web/html/src/components/messages.js","../components/panel":"/home/matei/workspace-suma3/spacewalk/web/html/src/components/panel.js","../components/table":"/home/matei/workspace-suma3/spacewalk/web/html/src/components/table.js","react":"react"}]},{},["/home/matei/workspace-suma3/spacewalk/web/html/src/manager/org-state-catalog.js"])
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyaWZ5L25vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJjb21wb25lbnRzL21lc3NhZ2VzLmpzIiwiY29tcG9uZW50cy9wYW5lbC5qcyIsImNvbXBvbmVudHMvdGFibGUuanMiLCJjb21wb25lbnRzL3V0aWwuanMiLCJtYW5hZ2VyL29yZy1zdGF0ZS1jYXRhbG9nLmpzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUFBO0FDQUE7O0FBRUEsSUFBSSxRQUFRLFFBQVEsT0FBUixDQUFSOztBQUVKLElBQUksV0FBVyxNQUFNLFdBQU4sQ0FBa0I7OztBQUMvQixlQUFjO0FBQ1osYUFBUyxRQUFUO0FBQ0EsZUFBVyxTQUFYO0FBQ0EsWUFBUSxNQUFSO0FBQ0EsZUFBVyxTQUFYO0dBSkY7O0FBT0EsbUJBQWlCLDJCQUFXO0FBQzFCLFdBQU8sRUFBUCxDQUQwQjtHQUFYOztBQUlqQixVQUFRLGtCQUFXO0FBQ2pCLFFBQUksT0FBTyxLQUFLLEtBQUwsQ0FBVyxLQUFYLENBQWlCLEdBQWpCLENBQXFCLFVBQVMsSUFBVCxFQUFlO0FBQ25DLGFBQVE7O1VBQUssV0FBVyxpQkFBaUIsS0FBSyxXQUFMLENBQWlCLEtBQUssUUFBTCxDQUFsQyxFQUFoQjtRQUFtRSxLQUFLLElBQUw7T0FBM0UsQ0FEbUM7S0FBZixDQUV0QixJQUZzQixDQUVqQixJQUZpQixDQUFyQixDQUFQLENBRGE7QUFJakIsV0FBUTs7O01BQU0sSUFBTjtLQUFSLENBSmlCO0dBQVg7O0NBWkssQ0FBWDs7QUFxQkosT0FBTyxPQUFQLEdBQWlCO0FBQ2IsWUFBVyxRQUFYO0NBREo7OztBQ3pCQTs7QUFFQSxJQUFJLFFBQVEsUUFBUSxPQUFSLENBQVI7O0FBRUosSUFBSSxjQUFjLE1BQU0sV0FBTixDQUFrQjs7O0FBRWxDLG1CQUFpQiwyQkFBVztBQUMxQixXQUFPLEVBQVAsQ0FEMEI7R0FBWDs7QUFJakIsVUFBUSxrQkFBVztBQUNmLFdBQ0k7O1FB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UyxjQUFULENBQXdCLGVBQXhCLENBRkYiLCJmaWxlIjoiZ2VuZXJhdGVkLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXNDb250ZW50IjpbIihmdW5jdGlvbiBlKHQsbixyKXtmdW5jdGlvbiBzKG8sdSl7aWYoIW5bb10pe2lmKCF0W29dKXt2YXIgYT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2lmKCF1JiZhKXJldHVybiBhKG8sITApO2lmKGkpcmV0dXJuIGkobywhMCk7dmFyIGY9bmV3IEVycm9yKFwiQ2Fubm90IGZpbmQgbW9kdWxlICdcIitvK1wiJ1wiKTt0aHJvdyBmLmNvZGU9XCJNT0RVTEVfTk9UX0ZPVU5EXCIsZn12YXIgbD1uW29dPXtleHBvcnRzOnt9fTt0W29dWzBdLmNhbGwobC5leHBvcnRzLGZ1bmN0aW9uKGUpe3ZhciBuPXRbb11bMV1bZV07cmV0dXJuIHMobj9uOmUpfSxsLGwuZXhwb3J0cyxlLHQsbixyKX1yZXR1cm4gbltvXS5leHBvcnRzfXZhciBpPXR5cGVvZiByZXF1aXJlPT1cImZ1bmN0aW9uXCImJnJlcXVpcmU7Zm9yKHZhciBvPTA7bzxyLmxlbmd0aDtvKyspcyhyW29dKTtyZXR1cm4gc30pIiwiJ3VzZSBzdHJpY3QnO1xuXG52YXIgUmVhY3QgPSByZXF1aXJlKFwicmVhY3RcIik7XG5cbnZhciBNZXNzYWdlcyA9IFJlYWN0LmNyZWF0ZUNsYXNzKHtcbiAgX2NsYXNzTmFtZXMgOiB7XG4gICAgXCJlcnJvclwiOiBcImRhbmdlclwiLFxuICAgIFwic3VjY2Vzc1wiOiBcInN1Y2Nlc3NcIixcbiAgICBcImluZm9cIjogXCJpbmZvXCIsXG4gICAgXCJ3YXJuaW5nXCI6IFwid2FybmluZ1wiLFxuICB9LFxuXG4gIGdldEluaXRpYWxTdGF0ZTogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIHt9O1xuICB9LFxuXG4gIHJlbmRlcjogZnVuY3Rpb24oKSB7XG4gICAgdmFyIG1zZ3MgPSB0aGlzLnByb3BzLml0ZW1zLm1hcChmdW5jdGlvbihpdGVtKSB7XG4gICAgICAgICAgICAgICAgcmV0dXJuICg8ZGl2IGNsYXNzTmFtZT17J2FsZXJ0IGFsZXJ0LScgKyB0aGlzLl9jbGFzc05hbWVzW2l0ZW0uc2V2ZXJpdHldfT57aXRlbS50ZXh0fTwvZGl2Pik7XG4gICAgICAgICAgICB9LmJpbmQodGhpcykpO1xuICAgIHJldHVybiAoPGRpdj57bXNnc308L2Rpdj4pO1xuICB9XG5cbn0pO1xuXG5tb2R1bGUuZXhwb3J0cyA9IHtcbiAgICBNZXNzYWdlcyA6IE1lc3NhZ2VzXG59IiwiJ3VzZSBzdHJpY3QnO1xuXG52YXIgUmVhY3QgPSByZXF1aXJlKFwicmVhY3RcIilcblxudmFyIFBhbmVsQnV0dG9uID0gUmVhY3QuY3JlYXRlQ2xhc3Moe1xuXG4gIGdldEluaXRpYWxTdGF0ZTogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIHt9O1xuICB9LFxuXG4gIHJlbmRlcjogZnVuY3Rpb24oKSB7XG4gICAgICByZXR1cm4gKFxuICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwic3BhY2V3YWxrLXRvb2xiYXJcIj5cbiAgICAgICAgICAgIDxhIGhyZWY9e3RoaXMucHJvcHMuYWN0aW9ufT5cbiAgICAgICAgICAgICAgPGkgY2xhc3NOYW1lPXsnZmEgJyArIHRoaXMucHJvcHMuaWNvbn0+PC9pPlxuICAgICAgICAgICAgICB7dCh0aGlzLnByb3BzLnRleHQpfVxuICAgICAgICAgICAgPC9hPlxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICApXG4gIH1cbn0pO1xuXG52YXIgUGFuZWwgPSBSZWFjdC5jcmVhdGVDbGFzcyh7XG4gIGdldEluaXRpYWxTdGF0ZTogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIHt9O1xuICB9LFxuXG4gIHJlbmRlcjogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXY+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwic3BhY2V3YWxrLXRvb2xiYXItaDFcIj5cbiAgICAgICAgICB7dGhpcy5wcm9wcy5idXR0b259XG4gICAgICAgICAgPGgxPjxpIGNsYXNzTmFtZT17J2ZhICcgKyB0aGlzLnByb3BzLmljb259PjwvaT57dCh0aGlzLnByb3BzLnRpdGxlKX08L2gxPlxuICAgICAgICA8L2Rpdj5cbiAgICAgICAge3RoaXMucHJvcHMuY2hpbGRyZW59XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59KTtcblxubW9kdWxlLmV4cG9ydHMgPSB7XG4gICAgUGFuZWwgOiBQYW5lbCxcbiAgICBQYW5lbEJ1dHRvbiA6IFBhbmVsQnV0dG9uXG59IiwiJ3VzZSBzdHJpY3QnO1xuXG52YXIgUmVhY3QgPSByZXF1aXJlKFwicmVhY3RcIilcbnZhciBTdGF0ZVBlcnNpc3RlZE1peGluID0gcmVxdWlyZShcIi4vdXRpbFwiKS5TdGF0ZVBlcnNpc3RlZE1peGluXG5cbnZhciBUYWJsZSA9IFJlYWN0LmNyZWF0ZUNsYXNzKHtcbiAgbWl4aW5zOiBbU3RhdGVQZXJzaXN0ZWRNaXhpbl0sXG5cbiAgZ2V0SW5pdGlhbFN0YXRlOiBmdW5jdGlvbigpIHtcbiAgICByZXR1cm4ge1xuICAgICAgXCJjdXJyZW50UGFnZVwiOiAxLCBcIml0ZW1zUGVyUGFnZVwiOiAxNSxcbiAgICAgIFwic2VhcmNoRmllbGRcIjogXCJcIixcbiAgICAgIFwiY29sdW1uSW5kZXhcIjogMCwgXCJhc2NlbmRpbmdcIjogdHJ1ZVxuICAgIH07XG4gIH0sXG5cbiAgY29tcG9uZW50V2lsbFJlY2VpdmVQcm9wczogZnVuY3Rpb24obmV4dFByb3BzKSB7XG4gICAgdmFyIGNvbHVtbkluZGV4O1xuICAgIGlmICh0aGlzLnByb3BzLnNvcnRhYmxlQ29sdW1ucykge1xuICAgICAgY29sdW1uSW5kZXggPSB0aGlzLnByb3BzLnNvcnRhYmxlQ29sdW1uc1swXTtcbiAgICB9XG4gICAgdmFyIGxhc3RQYWdlID0gTWF0aC5jZWlsKG5leHRQcm9wcy5yb3dzLmxlbmd0aCAvIG5leHRQcm9wcy5pdGVtc1BlclBhZ2UpO1xuICAgIGlmICh0aGlzLnN0YXRlLmN1cnJlbnRQYWdlID4gbGFzdFBhZ2UpIHtcbiAgICAgIHRoaXMuc2V0U3RhdGUoe1wiY3VycmVudFBhZ2VcIjogbGFzdFBhZ2UsIFwiY29sdW1uSW5kZXhcIiA6IGNvbHVtbkluZGV4fSk7XG4gICAgfVxuICB9LFxuXG4gIG9yZGVyQnlDb2x1bW46IGZ1bmN0aW9uKGNvbHVtbkluZGV4KSB7XG4gICAgdmFyIGFzY2VuZGluZyA9IHRoaXMuc3RhdGUuYXNjZW5kaW5nO1xuICAgIGlmICh0aGlzLnN0YXRlLmNvbHVtbkluZGV4ID09IGNvbHVtbkluZGV4KSB7XG4gICAgICBhc2NlbmRpbmcgPSAhYXNjZW5kaW5nO1xuICAgIH1cbiAgICBlbHNlIHtcbiAgICAgIGFzY2VuZGluZyA9IHRydWU7XG4gICAgfVxuICAgIHRoaXMuc2V0U3RhdGUoe1wiY29sdW1uSW5kZXhcIjogY29sdW1uSW5kZXgsIFwiYXNjZW5kaW5nXCI6IGFzY2VuZGluZ30pO1xuICB9LFxuXG4gIGdldFJvd3M6IGZ1bmN0aW9uKHVuZmlsdGVyZWRSb3dzLCBzZWFyY2hWYWx1ZSkge1xuICAgIHZhciByb3dzID0gdGhpcy5wcm9wcy5kYXRhRmlsdGVyICYmIHNlYXJjaFZhbHVlLmxlbmd0aCA+IDAgP1xuICAgICAgdW5maWx0ZXJlZFJvd3MuZmlsdGVyKChyb3cpID0+IHRoaXMucHJvcHMuZGF0YUZpbHRlcihyb3csIHNlYXJjaFZhbHVlKSkgOlxuICAgICAgdW5maWx0ZXJlZFJvd3M7XG4gICAgICBpZiAodGhpcy5wcm9wcy5yb3dDb21wYXJhdG9yKSB7XG4gICAgICAgIHZhciBjb2x1bW5JbmRleCA9IHRoaXMuc3RhdGUuY29sdW1uSW5kZXg7XG4gICAgICAgIHZhciBhc2NlbmRpbmcgPSB0aGlzLnN0YXRlLmFzY2VuZGluZztcbiAgICAgICAgcm93cy5zb3J0KChhLCBiKSA9PiB0aGlzLnByb3BzLnJvd0NvbXBhcmF0b3IoYSwgYiwgY29sdW1uSW5kZXgsIGFzY2VuZGluZykpO1xuICAgICAgfVxuICAgIHJldHVybiByb3dzO1xuICB9LFxuXG4gIGxhc3RQYWdlOiBmdW5jdGlvbihyb3dzLCBpdGVtc1BlclBhZ2UpIHtcbiAgICB2YXIgbGFzdFBhZ2UgPSBNYXRoLmNlaWwocm93cy5sZW5ndGggLyBpdGVtc1BlclBhZ2UpO1xuICAgIGlmIChsYXN0UGFnZSA9PSAwKSB7XG4gICAgICByZXR1cm4gMTtcbiAgICB9XG4gICAgcmV0dXJuIGxhc3RQYWdlO1xuICB9LFxuXG4gIGdvVG9QYWdlOmZ1bmN0aW9uKHBhZ2UpIHtcbiAgICB0aGlzLnNldFN0YXRlKHtcImN1cnJlbnRQYWdlXCI6IHBhZ2V9KTtcbiAgfSxcblxuICBvbkl0ZW1zUGVyUGFnZUNoYW5nZTogZnVuY3Rpb24oaXRlbXNQZXJQYWdlKSB7XG4gICAgdGhpcy5zZXRTdGF0ZSh7XCJpdGVtc1BlclBhZ2VcIjogaXRlbXNQZXJQYWdlfSk7XG4gICAgdmFyIGxhc3RQYWdlID0gdGhpcy5sYXN0UGFnZSh0aGlzLmdldFJvd3ModGhpcy5wcm9wcy5yb3dzLCB0aGlzLnN0YXRlLnNlYXJjaEZpZWxkKSwgaXRlbXNQZXJQYWdlKTtcbiAgICBpZiAodGhpcy5zdGF0ZS5jdXJyZW50UGFnZSA+IGxhc3RQYWdlKSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtcImN1cnJlbnRQYWdlXCI6IGxhc3RQYWdlIH0pO1xuICAgIH1cbiAgfSxcblxuICBvblNlYXJjaEZpZWxkQ2hhbmdlOiBmdW5jdGlvbihzZWFyY2hWYWx1ZSkge1xuICAgIHRoaXMuc2V0U3RhdGUoe1wic2VhcmNoRmllbGRcIjogc2VhcmNoVmFsdWV9KTtcbiAgICB2YXIgbGFzdFBhZ2UgPSAgdGhpcy5sYXN0UGFnZSh0aGlzLmdldFJvd3ModGhpcy5wcm9wcy5yb3dzLCBzZWFyY2hWYWx1ZSksIHRoaXMuc3RhdGUuaXRlbXNQZXJQYWdlKTtcbiAgICBpZiAodGhpcy5zdGF0ZS5jdXJyZW50UGFnZSA+IGxhc3RQYWdlKSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtcImN1cnJlbnRQYWdlXCI6IGxhc3RQYWdlIH0pO1xuICAgIH1cbiAgfSxcblxuICByZW5kZXI6IGZ1bmN0aW9uKCkge1xuICAgIHZhciByb3dzID0gdGhpcy5nZXRSb3dzKHRoaXMucHJvcHMucm93cywgdGhpcy5zdGF0ZS5zZWFyY2hGaWVsZCk7XG4gICAgdmFyIGl0ZW1zUGVyUGFnZSA9IHRoaXMuc3RhdGUuaXRlbXNQZXJQYWdlO1xuICAgIHZhciBpdGVtQ291bnQgPSByb3dzLmxlbmd0aDtcbiAgICB2YXIgbGFzdFBhZ2UgPSB0aGlzLmxhc3RQYWdlKHJvd3MsIGl0ZW1zUGVyUGFnZSk7XG4gICAgdmFyIGN1cnJlbnRQYWdlID0gdGhpcy5zdGF0ZS5jdXJyZW50UGFnZTtcblxuICAgIHZhciBmaXJzdEl0ZW1JbmRleCA9IChjdXJyZW50UGFnZSAtIDEpICogaXRlbXNQZXJQYWdlO1xuXG4gICAgdmFyIGZyb21JdGVtID0gaXRlbUNvdW50ID4gMCA/IGZpcnN0SXRlbUluZGV4ICsxIDogMDtcbiAgICB2YXIgdG9JdGVtID0gZmlyc3RJdGVtSW5kZXggKyBpdGVtc1BlclBhZ2UgPD0gaXRlbUNvdW50ID8gZmlyc3RJdGVtSW5kZXggKyBpdGVtc1BlclBhZ2UgOiBpdGVtQ291bnQ7XG5cbiAgICB2YXIgcGFnaW5hdGlvbjtcbiAgICBpZiAobGFzdFBhZ2UgPiAxKSB7XG4gICAgICBwYWdpbmF0aW9uID0gKFxuICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cInNwYWNld2Fsay1saXN0LXBhZ2luYXRpb25cIj5cbiAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cInNwYWNld2Fsay1saXN0LXBhZ2luYXRpb24tYnRucyBidG4tZ3JvdXBcIj5cbiAgICAgICAgICAgIDxQYWdpbmF0aW9uQnV0dG9uIG9uQ2xpY2s9e3RoaXMuZ29Ub1BhZ2V9IHRvUGFnZT17MX0gZGlzYWJsZWQ9e2N1cnJlbnRQYWdlID09IDF9IHRleHQ9e3QoXCJGaXJzdFwiKX0gLz5cbiAgICAgICAgICAgIDxQYWdpbmF0aW9uQnV0dG9uIG9uQ2xpY2s9e3RoaXMuZ29Ub1BhZ2V9IHRvUGFnZT17Y3VycmVudFBhZ2UgLTF9IGRpc2FibGVkPXtjdXJyZW50UGFnZSA9PSAxfSB0ZXh0PXt0KFwiUHJldlwiKX0gLz5cbiAgICAgICAgICAgIDxQYWdpbmF0aW9uQnV0dG9uIG9uQ2xpY2s9e3RoaXMuZ29Ub1BhZ2V9IHRvUGFnZT17Y3VycmVudFBhZ2UgKyAxfSBkaXNhYmxlZD17Y3VycmVudFBhZ2UgPT0gbGFzdFBhZ2V9IHRleHQ9e3QoXCJOZXh0XCIpfSAvPlxuICAgICAgICAgICAgPFBhZ2luYXRpb25CdXR0b24gb25DbGljaz17dGhpcy5nb1RvUGFnZX0gdG9QYWdlPXtsYXN0UGFnZX0gZGlzYWJsZWQ9e2N1cnJlbnRQYWdlID09IGxhc3RQYWdlfSB0ZXh0PXt0KFwiTGFzdFwiKX0gLz5cbiAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgPC9kaXY+XG4gICAgICApO1xuICAgIH1cblxuICAgIHZhciBzZWFyY2hGaWVsZDtcbiAgICBpZiAodGhpcy5wcm9wcy5kYXRhRmlsdGVyKSB7XG4gICAgICBzZWFyY2hGaWVsZCA9IChcbiAgICAgICAgPFNlYXJjaEZpZWxkXG4gICAgICAgICAgb25DaGFuZ2U9e3RoaXMub25TZWFyY2hGaWVsZENoYW5nZX1cbiAgICAgICAgICBkZWZhdWx0VmFsdWU9e3RoaXMuc3RhdGUuc2VhcmNoRmllbGR9XG4gICAgICAgICAgcGxhY2Vob2xkZXI9e3RoaXMucHJvcHMuc2VhcmNoUGxhY2Vob2xkZXJ9XG4gICAgICAgIC8+XG4gICAgICApO1xuICAgIH1cblxuICAgIHJldHVybiAoXG4gICAgICA8ZGl2IGNsYXNzTmFtZT1cInBhbmVsIHBhbmVsLWRlZmF1bHRcIj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJwYW5lbC1oZWFkaW5nXCI+XG4gICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJzcGFjZXdhbGstbGlzdC1oZWFkLWFkZG9uc1wiPlxuICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJzcGFjZXdhbGstbGlzdC1maWx0ZXIgdGFibGUtc2VhcmNoLXdyYXBwZXJcIj5cbiAgICAgICAgICAgICAge3NlYXJjaEZpZWxkfSB7dChcIkl0ZW1zIHswfSAtIHsxfSBvZiB7Mn1cIiwgZnJvbUl0ZW0sIHRvSXRlbSwgaXRlbUNvdW50KX1cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJzcGFjZXdhbGstbGlzdC1oZWFkLWFkZG9ucy1leHRyYSB0YWJsZS1pdGVtcy1wZXItcGFnZS13cmFwcGVyXCI+XG4gICAgICAgICAgICAgIDxQYWdlU2VsZWN0b3IgY2xhc3NOYW1lPVwiZGlzcGxheS1udW1iZXJcIlxuICAgICAgICAgICAgICAgIG9wdGlvbnM9e1s1LDEwLDE1LDI1LDUwLDEwMCwyNTAsNTAwXX1cbiAgICAgICAgICAgICAgICBjdXJyZW50VmFsdWU9e2l0ZW1zUGVyUGFnZX1cbiAgICAgICAgICAgICAgICBvbkNoYW5nZT17dGhpcy5vbkl0ZW1zUGVyUGFnZUNoYW5nZX1cbiAgICAgICAgICAgICAgLz4ge3QoXCJpdGVtcyBwZXIgcGFnZVwiKX1cbiAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgIDwvZGl2PlxuICAgICAgICA8L2Rpdj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJ0YWJsZS1yZXNwb25zaXZlXCI+XG4gICAgICAgICAgPHRhYmxlIGNsYXNzTmFtZT1cInRhYmxlIHRhYmxlLXN0cmlwZWRcIj5cbiAgICAgICAgICAgIDxUYWJsZUhlYWRlclxuICAgICAgICAgICAgICBjb250ZW50PXtcbiAgICAgICAgICAgICAgICB0aGlzLnByb3BzLmhlYWRlcnMubWFwKChoZWFkZXIsIGluZGV4KSA9PiB7XG4gICAgICAgICAgICAgICAgICB2YXIgY2xhc3NOYW1lO1xuICAgICAgICAgICAgICAgICAgaWYgKGluZGV4ID09IHRoaXMuc3RhdGUuY29sdW1uSW5kZXgpIHtcbiAgICAgICAgICAgICAgICAgICAgY2xhc3NOYW1lID0gKHRoaXMuc3RhdGUuYXNjZW5kaW5nID8gXCJhc2NcIiA6IFwiZGVzY1wiKSArIFwiU29ydFwiO1xuICAgICAgICAgICAgICAgICAgfVxuICAgICAgICAgICAgICAgICAgcmV0dXJuIChcbiAgICAgICAgICAgICAgICAgICAgICAodGhpcy5wcm9wcy5zb3J0YWJsZUNvbHVtbnMgJiZcbiAgICAgICAgICAgICAgICAgICAgICAgIHRoaXMucHJvcHMuc29ydGFibGVDb2x1bW5zLmZpbHRlcigoZWxlbWVudCkgPT4gZWxlbWVudCA9PSBpbmRleCkubGVuZ3RoID4gMCkgP1xuICAgICAgICAgICAgICAgICAgICAgIDxUYWJsZUhlYWRlckNlbGxPcmRlciBjbGFzc05hbWU9e2NsYXNzTmFtZX0gY29udGVudD17aGVhZGVyfVxuICAgICAgICAgICAgICAgICAgICAgICAgb3JkZXJCeT17dGhpcy5vcmRlckJ5Q29sdW1ufSBjb2x1bW5JbmRleD17aW5kZXh9IC8+IDpcbiAgICAgICAgICAgICAgICAgICAgICA8VGFibGVIZWFkZXJDZWxsIGNsYXNzTmFtZT17Y2xhc3NOYW1lfSBjb250ZW50PXtoZWFkZXJ9IC8+XG4gICAgICAgICAgICAgICAgICApO1xuICAgICAgICAgICAgICAgIH0pfVxuICAgICAgICAgICAgLz5cbiAgICAgICAgICAgIDx0Ym9keSBjbGFzc05hbWU9XCJ0YWJsZS1jb250ZW50XCI+XG4gICAgICAgICAgICAgIHtyb3dzXG4gICAgICAgICAgICAgICAgLmZpbHRlcigoZWxlbWVudCwgaSkgPT4gaSA+PSBmaXJzdEl0ZW1JbmRleCAmJiBpIDwgZmlyc3RJdGVtSW5kZXggKyBpdGVtc1BlclBhZ2UpXG4gICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgPC90Ym9keT5cbiAgICAgICAgICA8L3RhYmxlPlxuICAgICAgICA8L2Rpdj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJwYW5lbC1mb290ZXJcIj5cbiAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cInNwYWNld2Fsay1saXN0LWJvdHRvbS1hZGRvbnNcIj5cbiAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwidGFibGUtcGFnZS1pbmZvcm1hdGlvblwiPnt0KFwiUGFnZSB7MH0gb2YgezF9XCIsIGN1cnJlbnRQYWdlLCBsYXN0UGFnZSl9PC9kaXY+XG4gICAgICAgICAgICB7cGFnaW5hdGlvbn1cbiAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgPC9kaXY+XG4gICAgICA8L2Rpdj5cbiAgICApO1xuICB9XG59KTtcblxudmFyIFBhZ2luYXRpb25CdXR0b24gPSBSZWFjdC5jcmVhdGVDbGFzcyh7XG4gIG9uQ2xpY2s6IGZ1bmN0aW9uKCkge1xuICAgIHRoaXMucHJvcHMub25DbGljayh0aGlzLnByb3BzLnRvUGFnZSk7XG4gIH0sXG5cbiAgcmVuZGVyOiBmdW5jdGlvbigpIHtcbiAgICByZXR1cm4gKFxuICAgICAgPGJ1dHRvbiB0eXBlPVwiYnV0dG9uXCIgY2xhc3NOYW1lPVwiYnRuIGJ0bi1kZWZhdWx0XCJcbiAgICAgICAgZGlzYWJsZWQ9e3RoaXMucHJvcHMuZGlzYWJsZWR9IG9uQ2xpY2s9e3RoaXMub25DbGlja30+XG4gICAgICAgIHt0aGlzLnByb3BzLnRleHR9XG4gICAgICA8L2J1dHRvbj5cbiAgICApO1xuICB9XG59KTtcblxudmFyIFBhZ2VTZWxlY3RvciA9IFJlYWN0LmNyZWF0ZUNsYXNzKHtcbiAgaGFuZGxlT25DaGFuZ2U6IGZ1bmN0aW9uKGUpIHtcbiAgICB0aGlzLnByb3BzLm9uQ2hhbmdlKHBhcnNlSW50KGUudGFyZ2V0LnZhbHVlKSk7XG4gIH0sXG5cbiAgcmVuZGVyOiBmdW5jdGlvbigpIHtcbiAgICByZXR1cm4gKFxuICAgICAgPHNlbGVjdCBjbGFzc05hbWU9e3RoaXMucHJvcHMuY2xhc3NOYW1lfVxuICAgICAgICBkZWZhdWx0VmFsdWU9e3RoaXMucHJvcHMuY3VycmVudFZhbHVlfVxuICAgICAgICBvbkNoYW5nZT17dGhpcy5oYW5kbGVPbkNoYW5nZX0+XG4gICAgICAgIHt0aGlzLnByb3BzLm9wdGlvbnMubWFwKGZ1bmN0aW9uKG8pIHtcbiAgICAgICAgICByZXR1cm4gKDxvcHRpb24gdmFsdWU9e299PntvfTwvb3B0aW9uPik7XG4gICAgICAgIH0pfVxuICAgICAgPC9zZWxlY3Q+XG4gICAgKTtcbiAgfVxufSk7XG5cbnZhciBUYWJsZUhlYWRlciA9IFJlYWN0LmNyZWF0ZUNsYXNzKHtcbiAgcmVuZGVyOiBmdW5jdGlvbigpIHtcbiAgICByZXR1cm4gKFxuICAgICAgPHRoZWFkPjx0cj57dGhpcy5wcm9wcy5jb250ZW50fTwvdHI+PC90aGVhZD5cbiAgICApO1xuICB9XG59KTtcblxudmFyIFRhYmxlSGVhZGVyQ2VsbE9yZGVyID0gUmVhY3QuY3JlYXRlQ2xhc3Moe1xuICBoYW5kbGVDbGljazogZnVuY3Rpb24oKSB7XG4gICAgaWYgKHRoaXMucHJvcHMuY29sdW1uSW5kZXggIT0gbnVsbCkge1xuICAgICAgdGhpcy5wcm9wcy5vcmRlckJ5KHRoaXMucHJvcHMuY29sdW1uSW5kZXgpO1xuICAgIH1cbiAgfSxcblxuICByZW5kZXI6IGZ1bmN0aW9uICgpIHtcbiAgICByZXR1cm4gKDx0aCBjbGFzc05hbWU9e3RoaXMucHJvcHMuY2xhc3NOYW1lfT48YSBjbGFzc05hbWU9XCJvcmRlckJ5XCIgb25DbGljaz17dGhpcy5oYW5kbGVDbGlja30+e3RoaXMucHJvcHMuY29udGVudH08L2E+PC90aD4pO1xuICB9XG59KTtcblxuXG52YXIgVGFibGVIZWFkZXJDZWxsID0gUmVhY3QuY3JlYXRlQ2xhc3Moe1xuICByZW5kZXI6IGZ1bmN0aW9uICgpIHtcbiAgICByZXR1cm4gKDx0aCBjbGFzc05hbWU9e3RoaXMucHJvcHMuY2xhc3NOYW1lfT57dGhpcy5wcm9wcy5jb250ZW50fTwvdGg+KTtcbiAgfVxufSk7XG5cbnZhciBUYWJsZVJvdyA9IFJlYWN0LmNyZWF0ZUNsYXNzKHtcbiAgcmVuZGVyOiBmdW5jdGlvbigpIHtcbiAgICByZXR1cm4gKFxuICAgICAgPHRyIGNsYXNzTmFtZT17dGhpcy5wcm9wcy5jbGFzc05hbWV9PlxuICAgICAgICB7dGhpcy5wcm9wcy5jb2x1bW5zfVxuICAgICAgPC90cj5cbiAgICApO1xuICB9XG59KTtcblxudmFyIFRhYmxlQ2VsbCA9IFJlYWN0LmNyZWF0ZUNsYXNzKHtcbiAgcmVuZGVyOiBmdW5jdGlvbigpIHtcbiAgICByZXR1cm4gKFxuICAgICAgPHRkPlxuICAgICAgICB7dGhpcy5wcm9wcy5jb250ZW50fVxuICAgICAgPC90ZD5cbiAgICApO1xuICB9XG59KTtcblxuXG52YXIgU2VhcmNoRmllbGQgPSBSZWFjdC5jcmVhdGVDbGFzcyh7XG4gIGhhbmRsZUNoYW5nZTogZnVuY3Rpb24oZSkge1xuICAgIHRoaXMucHJvcHMub25DaGFuZ2UoZS50YXJnZXQudmFsdWUpO1xuICB9LFxuXG4gIHJlbmRlcjogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIChcbiAgICAgIDxpbnB1dCBjbGFzc05hbWU9XCJmb3JtLWNvbnRyb2wgdGFibGUtaW5wdXQtc2VhcmNoXCJcbiAgICAgICAgdmFsdWU9e3RoaXMucHJvcHMuZGVmYXVsdFZhbHVlfVxuICAgICAgICBwbGFjZWhvbGRlcj17dGhpcy5wcm9wcy5wbGFjZWhvbGRlcn1cbiAgICAgICAgdHlwZT1cInRleHRcIlxuICAgICAgICBvbkNoYW5nZT17dGhpcy5oYW5kbGVDaGFuZ2V9IC8+XG4gICAgKTtcbiAgfVxufSk7XG5cbm1vZHVsZS5leHBvcnRzID0ge1xuICAgIFRhYmxlIDogVGFibGUsXG4gICAgVGFibGVDZWxsIDogVGFibGVDZWxsLFxuICAgIFRhYmxlUm93IDogVGFibGVSb3csXG4gICAgU2VhcmNoRmllbGQgOiBTZWFyY2hGaWVsZCxcbiAgICBUYWJsZUhlYWRlckNlbGwgOiBUYWJsZUhlYWRlckNlbGwsXG4gICAgVGFibGVIZWFkZXJDZWxsT3JkZXIgOiBUYWJsZUhlYWRlckNlbGxPcmRlcixcbiAgICBQYWdlU2VsZWN0b3IgOiBQYWdlU2VsZWN0b3IsXG4gICAgUGFnaW5hdGlvbkJ1dHRvbiA6IFBhZ2luYXRpb25CdXR0b25cbn0iLCIndXNlIHN0cmljdCc7XG5cbnZhciBTdGF0ZVBlcnNpc3RlZE1peGluID0ge1xuICBjb21wb25lbnRXaWxsTW91bnQ6IGZ1bmN0aW9uKCkge1xuICAgIGlmICh0aGlzLnByb3BzLmxvYWRTdGF0ZSkge1xuICAgICAgaWYgKHRoaXMucHJvcHMubG9hZFN0YXRlKCkpIHtcbiAgICAgICAgdGhpcy5zdGF0ZSA9IHRoaXMucHJvcHMubG9hZFN0YXRlKCk7XG4gICAgICB9XG4gICAgfVxuICB9LFxuICBjb21wb25lbnRXaWxsVW5tb3VudDogZnVuY3Rpb24oKSB7XG4gICAgaWYgKHRoaXMucHJvcHMuc2F2ZVN0YXRlKSB7XG4gICAgICB0aGlzLnByb3BzLnNhdmVTdGF0ZSh0aGlzLnN0YXRlKTtcbiAgICB9XG4gIH0sXG59O1xuXG5tb2R1bGUuZXhwb3J0cyA9IHtcbiAgICBTdGF0ZVBlcnNpc3RlZE1peGluIDogU3RhdGVQZXJzaXN0ZWRNaXhpblxufSIsIid1c2Ugc3RyaWN0JztcblxudmFyIFJlYWN0ID0gcmVxdWlyZShcInJlYWN0XCIpO1xudmFyIFRhYmxlQ29tcG9uZW50ID0gcmVxdWlyZShcIi4uL2NvbXBvbmVudHMvdGFibGVcIik7XG52YXIgUGFuZWxDb21wb25lbnQgPSByZXF1aXJlKFwiLi4vY29tcG9uZW50cy9wYW5lbFwiKTtcbnZhciBNZXNzYWdlcyA9IHJlcXVpcmUoXCIuLi9jb21wb25lbnRzL21lc3NhZ2VzXCIpLk1lc3NhZ2VzO1xuXG52YXIgVGFibGUgPSBUYWJsZUNvbXBvbmVudC5UYWJsZTtcbnZhciBUYWJsZUNlbGwgPSBUYWJsZUNvbXBvbmVudC5UYWJsZUNlbGw7XG52YXIgVGFibGVSb3cgPSBUYWJsZUNvbXBvbmVudC5UYWJsZVJvdztcbnZhciBQYW5lbCA9IFBhbmVsQ29tcG9uZW50LlBhbmVsO1xudmFyIFBhbmVsQnV0dG9uID0gUGFuZWxDb21wb25lbnQuUGFuZWxCdXR0b247XG5cblxudmFyIFN0YXRlQ2F0YWxvZyA9IFJlYWN0LmNyZWF0ZUNsYXNzKHtcblxuICAgIGdldEluaXRpYWxTdGF0ZTogZnVuY3Rpb24oKSB7XG4gICAgICAgIHZhciBzdCA9IHtcbiAgICAgICAgICAgIFwic2VydmVyRGF0YVwiOiBbXVxuICAgICAgICB9O1xuLy8gICAgICAgIGlmICh0aGlzLnByb3BzLmZsYXNoTWVzc2FnZXMpIHtcbi8vICAgICAgICAgICAgc3RbXCJmbGFzaE1lc3NhZ2VzXCJdID0gdGhpcy5wcm9wcy5mbGFzaE1lc3NhZ2VzO1xuLy8gICAgICAgIH1cbiAgICAgICAgcmV0dXJuIHN0O1xuICAgIH0sXG5cbiAgICByZWZyZXNoU2VydmVyRGF0YTogZnVuY3Rpb24oKSB7XG4gICAgICAgICQuZ2V0KFwiL3Jobi9tYW5hZ2VyL3N0YXRlX2NhdGFsb2cvZGF0YVwiLCBkYXRhID0+IHtcbiAgICAgICAgICB0aGlzLnNldFN0YXRlKHtcInNlcnZlckRhdGFcIiA6IGRhdGF9KTtcbiAgICAgICAgfSk7XG4gICAgfSxcblxuICAgIGNvbXBvbmVudFdpbGxNb3VudDogZnVuY3Rpb24oKSB7XG4gICAgICAgIHRoaXMucmVmcmVzaFNlcnZlckRhdGEoKTtcbiAgICB9LFxuXG4vLyAgICBjb21wb25lbnREaWRNb3VudDogZnVuY3Rpb24oKSB7XG4vLyAgICAgICAgdmFyIHZpZXdzID0gMTtcbi8vICAgICAgICBpZih0aGlzLnN0YXRlLmZsYXNoTWVzc2FnZXNWaWV3cykge1xuLy8gICAgICAgICAgICB2aWV3cyA9IHRoaXMuc3RhdGUuZmxhc2hNZXNzYWdlc1ZpZXdzICsgMTtcbi8vICAgICAgICB9XG4vLyAgICAgICAgdGhpcy5zZXRTdGF0ZSh7IFwiZmxhc2hNZXNzYWdlc1ZpZXdzXCI6IHZpZXdzIH0pO1xuLy8gICAgfSxcbi8vXG4vLyAgICBzaG91bGRDb21wb25lbnRVcGRhdGU6IGZ1bmN0aW9uKCkge1xuLy8gICAgICAgIHJldHVybiB0eXBlb2YgdGhpcy5zdGF0ZS5mbGFzaE1lc3NhZ2VzVmlld3MgPT09IFwidW5kZWZpbmVkXCIgfHxcbi8vICAgICAgICAgICAgdGhpcy5zdGF0ZS5mbGFzaE1lc3NhZ2VzVmlld3MgPiAxO1xuLy8gICAgfSxcblxuICAgIGNvbXBhcmVSb3dzOiBmdW5jdGlvbihhLCBiLCBjb2x1bW5JbmRleCwgb3JkZXIpIHtcbiAgICAgICAgdmFyIG9yZGVyQ29uZGl0aW9uID0gb3JkZXIgPyAxIDogLTE7XG4gICAgICAgIHZhciBhVmFsdWUgPSBhLnByb3BzW1wicmF3X2RhdGFcIl07XG4gICAgICAgIHZhciBiVmFsdWUgPSBiLnByb3BzW1wicmF3X2RhdGFcIl07XG4gICAgICAgIHZhciByZXN1bHQgPSBhVmFsdWUubG9jYWxlQ29tcGFyZShiVmFsdWUpO1xuICAgICAgICByZXR1cm4gcmVzdWx0ICogb3JkZXJDb25kaXRpb247XG4gICAgfSxcblxuICAgIHJlbmRlcjogZnVuY3Rpb24oKSB7XG4gICAgICAgIHZhciBidXR0b24gPSA8UGFuZWxCdXR0b24gdGV4dD1cIkNyZWF0ZSBzdGF0ZVwiIGljb249XCJmYS1wbHVzXCIgYWN0aW9uPVwiL3Jobi9tYW5hZ2VyL3N0YXRlX2NhdGFsb2cvc3RhdGVcIi8+O1xuXG4gICAgICAgIHZhciBtc2cgPSBudWxsO1xuLy8gICAgICAgIGlmKHR5cGVvZiB0aGlzLnN0YXRlLmZsYXNoTWVzc2FnZXMgIT09IFwidW5kZWZpbmVkXCIgJiYgdGhpcy5zdGF0ZS5mbGFzaE1lc3NhZ2VzLmxlbmd0aCA+IDApIHtcbi8vICAgICAgICAgICAgbXNnID0gPE1lc3NhZ2VzIGl0ZW1zPXt0aGlzLnByb3BzLmZsYXNoTWVzc2FnZXN9Lz5cbi8vICAgICAgICB9XG5cbiAgICAgICAgaWYodHlwZW9mIHRoaXMucHJvcHMuZmxhc2hNZXNzYWdlcyAhPT0gXCJ1bmRlZmluZWRcIikge1xuLy8gICAgICAgICAgICB2YXIgaXRlbXMgPSB7c2V2ZXJpdHk6IFwiZXJyb3JcIiwgdGV4dDogXCJhbGFsYWxhbFwifTtcbiAgICAgICAgICAgIG1zZyA9IDxNZXNzYWdlcyBpdGVtcz17dGhpcy5wcm9wcy5mbGFzaE1lc3NhZ2VzfS8+O1xuICAgICAgICB9XG4gICAgICAgIHJldHVybiAoXG4gICAgICAgICAgICA8UGFuZWwgdGl0bGU9XCJTdGF0ZXMgQ2F0YWxvZ1wiIGljb249XCJzcGFjZXdhbGstaWNvbi12aXJ0dWFsLWhvc3QtbWFuYWdlclwiIGJ1dHRvbj17YnV0dG9ufT5cbiAgICAgICAgICAgICAgICB7bXNnfVxuICAgICAgICAgICAgICAgIDxkaXY+XG4gICAgICAgICAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwic3BhY2V3YWxrLWxpc3RcIj5cbiAgICAgICAgICAgICAgICAgICAgICAgIDxUYWJsZSBoZWFkZXJzPXtbdChcIlN0YXRlXCIpXX1cbiAgICAgICAgICAgICAgICAgICAgICAgICAgcm93cz17c3RhdGVzVG9Sb3dzKHRoaXMuc3RhdGUuc2VydmVyRGF0YSl9XG4gICAgICAgICAgICAgICAgICAgICAgICAgIGxvYWRTdGF0ZT17dGhpcy5wcm9wcy5sb2FkU3RhdGV9XG4gICAgICAgICAgICAgICAgICAgICAgICAgIHNhdmVTdGF0ZT17dGhpcy5wcm9wcy5zYXZlU3RhdGV9XG4gICAgICAgICAgICAgICAgICAgICAgICAgIHJvd0NvbXBhcmF0b3I9e3RoaXMuY29tcGFyZVJvd3N9XG4gICAgICAgICAgICAgICAgICAgICAgICAgIHNvcnRhYmxlQ29sdW1ucz17WzBdfVxuICAgICAgICAgICAgICAgICAgICAgICAgICBkYXRhRmlsdGVyPXsodGFibGVSb3csIHNlYXJjaFZhbHVlKSA9PiB0YWJsZVJvdy5wcm9wc1tcInJhd19kYXRhXCJdLnRvTG93ZXJDYXNlKCkuaW5kZXhPZihzZWFyY2hWYWx1ZS50b0xvd2VyQ2FzZSgpKSA+IC0xfVxuICAgICAgICAgICAgICAgICAgICAgICAgICBzZWFyY2hQbGFjZWhvbGRlcj17dChcIkZpbHRlciBieSBzdGF0ZSBuYW1lOlwiKX1cbiAgICAgICAgICAgICAgICAgICAgICAgIC8+XG4gICAgICAgICAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAgICAgIDwvZGl2PlxuICAgICAgICAgICAgPC9QYW5lbD5cbiAgICAgICAgKTtcbiAgICB9XG5cbn0pO1xuXG5mdW5jdGlvbiBzdGF0ZXNUb1Jvd3Moc2VydmVyRGF0YSkge1xuICByZXR1cm4gc2VydmVyRGF0YS5tYXAoKHMpID0+IHtcbiAgICB2YXIgbGluayA9IDxhIGhyZWY9e1wiL3Jobi9tYW5hZ2VyL3N0YXRlX2NhdGFsb2cvc3RhdGUvXCIgKyBzfT57c308L2E+XG4gICAgdmFyIGNvbHVtbnMgPSBbXG4gICAgICA8VGFibGVDZWxsIGNvbnRlbnQ9e2xpbmt9IC8+LFxuICAgIF07XG4gICAgcmV0dXJuIDxUYWJsZVJvdyBjb2x1bW5zPXtjb2x1bW5zfSByYXdfZGF0YT17c30gLz5cbiAgfSk7XG59XG5cblJlYWN0LnJlbmRlcihcbiAgPFN0YXRlQ2F0YWxvZyBmbGFzaE1lc3NhZ2VzPXtmbGFzaE1lc3NhZ2UoKX0vPixcbiAgZG9jdW1lbnQuZ2V0RWxlbWVudEJ5SWQoJ3N0YXRlLWNhdGFsb2cnKVxuKTsiXX0=
