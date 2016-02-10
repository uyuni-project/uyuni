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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIm5vZGVfbW9kdWxlcy9icm93c2VyaWZ5L25vZGVfbW9kdWxlcy9icm93c2VyLXBhY2svX3ByZWx1ZGUuanMiLCJjb21wb25lbnRzL21lc3NhZ2VzLmpzIiwiY29tcG9uZW50cy9wYW5lbC5qcyIsImNvbXBvbmVudHMvdGFibGUuanMiLCJjb21wb25lbnRzL3V0aWwuanMiLCJtYW5hZ2VyL29yZy1zdGF0ZS1jYXRhbG9nLmpzIl0sIm5hbWVzIjpbXSwibWFwcGluZ3MiOiJBQUFBO0FDQUE7O0FBRUEsSUFBSSxRQUFRLFFBQVEsT0FBUixDQUFSOztBQUVKLElBQUksV0FBVyxNQUFNLFdBQU4sQ0FBa0I7OztBQUMvQixlQUFjO0FBQ1osYUFBUyxRQUFUO0FBQ0EsZUFBVyxTQUFYO0FBQ0EsWUFBUSxNQUFSO0FBQ0EsZUFBVyxTQUFYO0dBSkY7O0FBT0EsbUJBQWlCLDJCQUFXO0FBQzFCLFdBQU8sRUFBUCxDQUQwQjtHQUFYOztBQUlqQixVQUFRLGtCQUFXO0FBQ2pCLFFBQUksT0FBTyxLQUFLLEtBQUwsQ0FBVyxLQUFYLENBQWlCLEdBQWpCLENBQXFCLFVBQVMsSUFBVCxFQUFlO0FBQ25DLGFBQVE7O1VBQUssV0FBVyxpQkFBaUIsS0FBSyxXQUFMLENBQWlCLEtBQUssUUFBTCxDQUFsQyxFQUFoQjtRQUFtRSxLQUFLLElBQUw7T0FBM0UsQ0FEbUM7S0FBZixDQUV0QixJQUZzQixDQUVqQixJQUZpQixDQUFyQixDQUFQLENBRGE7QUFJakIsV0FBUTs7O01BQU0sSUFBTjtLQUFSLENBSmlCO0dBQVg7O0NBWkssQ0FBWDs7QUFxQkosT0FBTyxPQUFQLEdBQWlCO0FBQ2IsWUFBVyxRQUFYO0NBREo7OztBQ3pCQTs7QUFFQSxJQUFJLFFBQVEsUUFBUSxPQUFSLENBQVI7O0FBRUosSUFBSSxjQUFjLE1BQU0sV0FBTixDQUFrQjs7O0FBRWxDLG1CQUFpQiwyQkFBVztBQUMxQixXQUFPLEVBQVAsQ0FEMEI7R0FBWDs7QUFJakIsVUFBUSxrQkFBVztBQUNmLFdBQ0k7O1FB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UyxjQUFULENBQXdCLGVBQXhCLENBRkYiLCJmaWxlIjoiZ2VuZXJhdGVkLmpzIiwic291cmNlUm9vdCI6IiIsInNvdXJjZXNDb250ZW50IjpbIihmdW5jdGlvbiBlKHQsbixyKXtmdW5jdGlvbiBzKG8sdSl7aWYoIW5bb10pe2lmKCF0W29dKXt2YXIgYT10eXBlb2YgcmVxdWlyZT09XCJmdW5jdGlvblwiJiZyZXF1aXJlO2lmKCF1JiZhKXJldHVybiBhKG8sITApO2lmKGkpcmV0dXJuIGkobywhMCk7dmFyIGY9bmV3IEVycm9yKFwiQ2Fubm90IGZpbmQgbW9kdWxlICdcIitvK1wiJ1wiKTt0aHJvdyBmLmNvZGU9XCJNT0RVTEVfTk9UX0ZPVU5EXCIsZn12YXIgbD1uW29dPXtleHBvcnRzOnt9fTt0W29dWzBdLmNhbGwobC5leHBvcnRzLGZ1bmN0aW9uKGUpe3ZhciBuPXRbb11bMV1bZV07cmV0dXJuIHMobj9uOmUpfSxsLGwuZXhwb3J0cyxlLHQsbixyKX1yZXR1cm4gbltvXS5leHBvcnRzfXZhciBpPXR5cGVvZiByZXF1aXJlPT1cImZ1bmN0aW9uXCImJnJlcXVpcmU7Zm9yKHZhciBvPTA7bzxyLmxlbmd0aDtvKyspcyhyW29dKTtyZXR1cm4gc30pIiwiJ3VzZSBzdHJpY3QnO1xuXG52YXIgUmVhY3QgPSByZXF1aXJlKFwicmVhY3RcIik7XG5cbnZhciBNZXNzYWdlcyA9IFJlYWN0LmNyZWF0ZUNsYXNzKHtcbiAgX2NsYXNzTmFtZXMgOiB7XG4gICAgXCJlcnJvclwiOiBcImRhbmdlclwiLFxuICAgIFwic3VjY2Vzc1wiOiBcInN1Y2Nlc3NcIixcbiAgICBcImluZm9cIjogXCJpbmZvXCIsXG4gICAgXCJ3YXJuaW5nXCI6IFwid2FybmluZ1wiLFxuICB9LFxuXG4gIGdldEluaXRpYWxTdGF0ZTogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIHt9O1xuICB9LFxuXG4gIHJlbmRlcjogZnVuY3Rpb24oKSB7XG4gICAgdmFyIG1zZ3MgPSB0aGlzLnByb3BzLml0ZW1zLm1hcChmdW5jdGlvbihpdGVtKSB7XG4gICAgICAgICAgICAgICAgcmV0dXJuICg8ZGl2IGNsYXNzTmFtZT17J2FsZXJ0IGFsZXJ0LScgKyB0aGlzLl9jbGFzc05hbWVzW2l0ZW0uc2V2ZXJpdHldfT57aXRlbS50ZXh0fTwvZGl2Pik7XG4gICAgICAgICAgICB9LmJpbmQodGhpcykpO1xuICAgIHJldHVybiAoPGRpdj57bXNnc308L2Rpdj4pO1xuICB9XG5cbn0pO1xuXG5tb2R1bGUuZXhwb3J0cyA9IHtcbiAgICBNZXNzYWdlcyA6IE1lc3NhZ2VzXG59XG4iLCIndXNlIHN0cmljdCc7XG5cbnZhciBSZWFjdCA9IHJlcXVpcmUoXCJyZWFjdFwiKVxuXG52YXIgUGFuZWxCdXR0b24gPSBSZWFjdC5jcmVhdGVDbGFzcyh7XG5cbiAgZ2V0SW5pdGlhbFN0YXRlOiBmdW5jdGlvbigpIHtcbiAgICByZXR1cm4ge307XG4gIH0sXG5cbiAgcmVuZGVyOiBmdW5jdGlvbigpIHtcbiAgICAgIHJldHVybiAoXG4gICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJzcGFjZXdhbGstdG9vbGJhclwiPlxuICAgICAgICAgICAgPGEgaHJlZj17dGhpcy5wcm9wcy5hY3Rpb259PlxuICAgICAgICAgICAgICA8aSBjbGFzc05hbWU9eydmYSAnICsgdGhpcy5wcm9wcy5pY29ufT48L2k+XG4gICAgICAgICAgICAgIHt0KHRoaXMucHJvcHMudGV4dCl9XG4gICAgICAgICAgICA8L2E+XG4gICAgICAgICAgPC9kaXY+XG4gICAgICAgIClcbiAgfVxufSk7XG5cbnZhciBQYW5lbCA9IFJlYWN0LmNyZWF0ZUNsYXNzKHtcbiAgZ2V0SW5pdGlhbFN0YXRlOiBmdW5jdGlvbigpIHtcbiAgICByZXR1cm4ge307XG4gIH0sXG5cbiAgcmVuZGVyOiBmdW5jdGlvbigpIHtcbiAgICByZXR1cm4gKFxuICAgICAgPGRpdj5cbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJzcGFjZXdhbGstdG9vbGJhci1oMVwiPlxuICAgICAgICAgIHt0aGlzLnByb3BzLmJ1dHRvbn1cbiAgICAgICAgICA8aDE+PGkgY2xhc3NOYW1lPXsnZmEgJyArIHRoaXMucHJvcHMuaWNvbn0+PC9pPnt0KHRoaXMucHJvcHMudGl0bGUpfTwvaDE+XG4gICAgICAgIDwvZGl2PlxuICAgICAgICB7dGhpcy5wcm9wcy5jaGlsZHJlbn1cbiAgICAgIDwvZGl2PlxuICAgICk7XG4gIH1cbn0pO1xuXG5tb2R1bGUuZXhwb3J0cyA9IHtcbiAgICBQYW5lbCA6IFBhbmVsLFxuICAgIFBhbmVsQnV0dG9uIDogUGFuZWxCdXR0b25cbn1cbiIsIid1c2Ugc3RyaWN0JztcblxudmFyIFJlYWN0ID0gcmVxdWlyZShcInJlYWN0XCIpXG52YXIgU3RhdGVQZXJzaXN0ZWRNaXhpbiA9IHJlcXVpcmUoXCIuL3V0aWxcIikuU3RhdGVQZXJzaXN0ZWRNaXhpblxuXG52YXIgVGFibGUgPSBSZWFjdC5jcmVhdGVDbGFzcyh7XG4gIG1peGluczogW1N0YXRlUGVyc2lzdGVkTWl4aW5dLFxuXG4gIGdldEluaXRpYWxTdGF0ZTogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIHtcbiAgICAgIFwiY3VycmVudFBhZ2VcIjogMSwgXCJpdGVtc1BlclBhZ2VcIjogMTUsXG4gICAgICBcInNlYXJjaEZpZWxkXCI6IFwiXCIsXG4gICAgICBcImNvbHVtbkluZGV4XCI6IDAsIFwiYXNjZW5kaW5nXCI6IHRydWVcbiAgICB9O1xuICB9LFxuXG4gIGNvbXBvbmVudFdpbGxSZWNlaXZlUHJvcHM6IGZ1bmN0aW9uKG5leHRQcm9wcykge1xuICAgIHZhciBjb2x1bW5JbmRleDtcbiAgICBpZiAodGhpcy5wcm9wcy5zb3J0YWJsZUNvbHVtbnMpIHtcbiAgICAgIGNvbHVtbkluZGV4ID0gdGhpcy5wcm9wcy5zb3J0YWJsZUNvbHVtbnNbMF07XG4gICAgfVxuICAgIHZhciBsYXN0UGFnZSA9IE1hdGguY2VpbChuZXh0UHJvcHMucm93cy5sZW5ndGggLyBuZXh0UHJvcHMuaXRlbXNQZXJQYWdlKTtcbiAgICBpZiAodGhpcy5zdGF0ZS5jdXJyZW50UGFnZSA+IGxhc3RQYWdlKSB7XG4gICAgICB0aGlzLnNldFN0YXRlKHtcImN1cnJlbnRQYWdlXCI6IGxhc3RQYWdlLCBcImNvbHVtbkluZGV4XCIgOiBjb2x1bW5JbmRleH0pO1xuICAgIH1cbiAgfSxcblxuICBvcmRlckJ5Q29sdW1uOiBmdW5jdGlvbihjb2x1bW5JbmRleCkge1xuICAgIHZhciBhc2NlbmRpbmcgPSB0aGlzLnN0YXRlLmFzY2VuZGluZztcbiAgICBpZiAodGhpcy5zdGF0ZS5jb2x1bW5JbmRleCA9PSBjb2x1bW5JbmRleCkge1xuICAgICAgYXNjZW5kaW5nID0gIWFzY2VuZGluZztcbiAgICB9XG4gICAgZWxzZSB7XG4gICAgICBhc2NlbmRpbmcgPSB0cnVlO1xuICAgIH1cbiAgICB0aGlzLnNldFN0YXRlKHtcImNvbHVtbkluZGV4XCI6IGNvbHVtbkluZGV4LCBcImFzY2VuZGluZ1wiOiBhc2NlbmRpbmd9KTtcbiAgfSxcblxuICBnZXRSb3dzOiBmdW5jdGlvbih1bmZpbHRlcmVkUm93cywgc2VhcmNoVmFsdWUpIHtcbiAgICB2YXIgcm93cyA9IHRoaXMucHJvcHMuZGF0YUZpbHRlciAmJiBzZWFyY2hWYWx1ZS5sZW5ndGggPiAwID9cbiAgICAgIHVuZmlsdGVyZWRSb3dzLmZpbHRlcigocm93KSA9PiB0aGlzLnByb3BzLmRhdGFGaWx0ZXIocm93LCBzZWFyY2hWYWx1ZSkpIDpcbiAgICAgIHVuZmlsdGVyZWRSb3dzO1xuICAgICAgaWYgKHRoaXMucHJvcHMucm93Q29tcGFyYXRvcikge1xuICAgICAgICB2YXIgY29sdW1uSW5kZXggPSB0aGlzLnN0YXRlLmNvbHVtbkluZGV4O1xuICAgICAgICB2YXIgYXNjZW5kaW5nID0gdGhpcy5zdGF0ZS5hc2NlbmRpbmc7XG4gICAgICAgIHJvd3Muc29ydCgoYSwgYikgPT4gdGhpcy5wcm9wcy5yb3dDb21wYXJhdG9yKGEsIGIsIGNvbHVtbkluZGV4LCBhc2NlbmRpbmcpKTtcbiAgICAgIH1cbiAgICByZXR1cm4gcm93cztcbiAgfSxcblxuICBsYXN0UGFnZTogZnVuY3Rpb24ocm93cywgaXRlbXNQZXJQYWdlKSB7XG4gICAgdmFyIGxhc3RQYWdlID0gTWF0aC5jZWlsKHJvd3MubGVuZ3RoIC8gaXRlbXNQZXJQYWdlKTtcbiAgICBpZiAobGFzdFBhZ2UgPT0gMCkge1xuICAgICAgcmV0dXJuIDE7XG4gICAgfVxuICAgIHJldHVybiBsYXN0UGFnZTtcbiAgfSxcblxuICBnb1RvUGFnZTpmdW5jdGlvbihwYWdlKSB7XG4gICAgdGhpcy5zZXRTdGF0ZSh7XCJjdXJyZW50UGFnZVwiOiBwYWdlfSk7XG4gIH0sXG5cbiAgb25JdGVtc1BlclBhZ2VDaGFuZ2U6IGZ1bmN0aW9uKGl0ZW1zUGVyUGFnZSkge1xuICAgIHRoaXMuc2V0U3RhdGUoe1wiaXRlbXNQZXJQYWdlXCI6IGl0ZW1zUGVyUGFnZX0pO1xuICAgIHZhciBsYXN0UGFnZSA9IHRoaXMubGFzdFBhZ2UodGhpcy5nZXRSb3dzKHRoaXMucHJvcHMucm93cywgdGhpcy5zdGF0ZS5zZWFyY2hGaWVsZCksIGl0ZW1zUGVyUGFnZSk7XG4gICAgaWYgKHRoaXMuc3RhdGUuY3VycmVudFBhZ2UgPiBsYXN0UGFnZSkge1xuICAgICAgdGhpcy5zZXRTdGF0ZSh7XCJjdXJyZW50UGFnZVwiOiBsYXN0UGFnZSB9KTtcbiAgICB9XG4gIH0sXG5cbiAgb25TZWFyY2hGaWVsZENoYW5nZTogZnVuY3Rpb24oc2VhcmNoVmFsdWUpIHtcbiAgICB0aGlzLnNldFN0YXRlKHtcInNlYXJjaEZpZWxkXCI6IHNlYXJjaFZhbHVlfSk7XG4gICAgdmFyIGxhc3RQYWdlID0gIHRoaXMubGFzdFBhZ2UodGhpcy5nZXRSb3dzKHRoaXMucHJvcHMucm93cywgc2VhcmNoVmFsdWUpLCB0aGlzLnN0YXRlLml0ZW1zUGVyUGFnZSk7XG4gICAgaWYgKHRoaXMuc3RhdGUuY3VycmVudFBhZ2UgPiBsYXN0UGFnZSkge1xuICAgICAgdGhpcy5zZXRTdGF0ZSh7XCJjdXJyZW50UGFnZVwiOiBsYXN0UGFnZSB9KTtcbiAgICB9XG4gIH0sXG5cbiAgcmVuZGVyOiBmdW5jdGlvbigpIHtcbiAgICB2YXIgcm93cyA9IHRoaXMuZ2V0Um93cyh0aGlzLnByb3BzLnJvd3MsIHRoaXMuc3RhdGUuc2VhcmNoRmllbGQpO1xuICAgIHZhciBpdGVtc1BlclBhZ2UgPSB0aGlzLnN0YXRlLml0ZW1zUGVyUGFnZTtcbiAgICB2YXIgaXRlbUNvdW50ID0gcm93cy5sZW5ndGg7XG4gICAgdmFyIGxhc3RQYWdlID0gdGhpcy5sYXN0UGFnZShyb3dzLCBpdGVtc1BlclBhZ2UpO1xuICAgIHZhciBjdXJyZW50UGFnZSA9IHRoaXMuc3RhdGUuY3VycmVudFBhZ2U7XG5cbiAgICB2YXIgZmlyc3RJdGVtSW5kZXggPSAoY3VycmVudFBhZ2UgLSAxKSAqIGl0ZW1zUGVyUGFnZTtcblxuICAgIHZhciBmcm9tSXRlbSA9IGl0ZW1Db3VudCA+IDAgPyBmaXJzdEl0ZW1JbmRleCArMSA6IDA7XG4gICAgdmFyIHRvSXRlbSA9IGZpcnN0SXRlbUluZGV4ICsgaXRlbXNQZXJQYWdlIDw9IGl0ZW1Db3VudCA/IGZpcnN0SXRlbUluZGV4ICsgaXRlbXNQZXJQYWdlIDogaXRlbUNvdW50O1xuXG4gICAgdmFyIHBhZ2luYXRpb247XG4gICAgaWYgKGxhc3RQYWdlID4gMSkge1xuICAgICAgcGFnaW5hdGlvbiA9IChcbiAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJzcGFjZXdhbGstbGlzdC1wYWdpbmF0aW9uXCI+XG4gICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJzcGFjZXdhbGstbGlzdC1wYWdpbmF0aW9uLWJ0bnMgYnRuLWdyb3VwXCI+XG4gICAgICAgICAgICA8UGFnaW5hdGlvbkJ1dHRvbiBvbkNsaWNrPXt0aGlzLmdvVG9QYWdlfSB0b1BhZ2U9ezF9IGRpc2FibGVkPXtjdXJyZW50UGFnZSA9PSAxfSB0ZXh0PXt0KFwiRmlyc3RcIil9IC8+XG4gICAgICAgICAgICA8UGFnaW5hdGlvbkJ1dHRvbiBvbkNsaWNrPXt0aGlzLmdvVG9QYWdlfSB0b1BhZ2U9e2N1cnJlbnRQYWdlIC0xfSBkaXNhYmxlZD17Y3VycmVudFBhZ2UgPT0gMX0gdGV4dD17dChcIlByZXZcIil9IC8+XG4gICAgICAgICAgICA8UGFnaW5hdGlvbkJ1dHRvbiBvbkNsaWNrPXt0aGlzLmdvVG9QYWdlfSB0b1BhZ2U9e2N1cnJlbnRQYWdlICsgMX0gZGlzYWJsZWQ9e2N1cnJlbnRQYWdlID09IGxhc3RQYWdlfSB0ZXh0PXt0KFwiTmV4dFwiKX0gLz5cbiAgICAgICAgICAgIDxQYWdpbmF0aW9uQnV0dG9uIG9uQ2xpY2s9e3RoaXMuZ29Ub1BhZ2V9IHRvUGFnZT17bGFzdFBhZ2V9IGRpc2FibGVkPXtjdXJyZW50UGFnZSA9PSBsYXN0UGFnZX0gdGV4dD17dChcIkxhc3RcIil9IC8+XG4gICAgICAgICAgPC9kaXY+XG4gICAgICAgIDwvZGl2PlxuICAgICAgKTtcbiAgICB9XG5cbiAgICB2YXIgc2VhcmNoRmllbGQ7XG4gICAgaWYgKHRoaXMucHJvcHMuZGF0YUZpbHRlcikge1xuICAgICAgc2VhcmNoRmllbGQgPSAoXG4gICAgICAgIDxTZWFyY2hGaWVsZFxuICAgICAgICAgIG9uQ2hhbmdlPXt0aGlzLm9uU2VhcmNoRmllbGRDaGFuZ2V9XG4gICAgICAgICAgZGVmYXVsdFZhbHVlPXt0aGlzLnN0YXRlLnNlYXJjaEZpZWxkfVxuICAgICAgICAgIHBsYWNlaG9sZGVyPXt0aGlzLnByb3BzLnNlYXJjaFBsYWNlaG9sZGVyfVxuICAgICAgICAvPlxuICAgICAgKTtcbiAgICB9XG5cbiAgICByZXR1cm4gKFxuICAgICAgPGRpdiBjbGFzc05hbWU9XCJwYW5lbCBwYW5lbC1kZWZhdWx0XCI+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwicGFuZWwtaGVhZGluZ1wiPlxuICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwic3BhY2V3YWxrLWxpc3QtaGVhZC1hZGRvbnNcIj5cbiAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwic3BhY2V3YWxrLWxpc3QtZmlsdGVyIHRhYmxlLXNlYXJjaC13cmFwcGVyXCI+XG4gICAgICAgICAgICAgIHtzZWFyY2hGaWVsZH0ge3QoXCJJdGVtcyB7MH0gLSB7MX0gb2YgezJ9XCIsIGZyb21JdGVtLCB0b0l0ZW0sIGl0ZW1Db3VudCl9XG4gICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICAgIDxkaXYgY2xhc3NOYW1lPVwic3BhY2V3YWxrLWxpc3QtaGVhZC1hZGRvbnMtZXh0cmEgdGFibGUtaXRlbXMtcGVyLXBhZ2Utd3JhcHBlclwiPlxuICAgICAgICAgICAgICA8UGFnZVNlbGVjdG9yIGNsYXNzTmFtZT1cImRpc3BsYXktbnVtYmVyXCJcbiAgICAgICAgICAgICAgICBvcHRpb25zPXtbNSwxMCwxNSwyNSw1MCwxMDAsMjUwLDUwMF19XG4gICAgICAgICAgICAgICAgY3VycmVudFZhbHVlPXtpdGVtc1BlclBhZ2V9XG4gICAgICAgICAgICAgICAgb25DaGFuZ2U9e3RoaXMub25JdGVtc1BlclBhZ2VDaGFuZ2V9XG4gICAgICAgICAgICAgIC8+IHt0KFwiaXRlbXMgcGVyIHBhZ2VcIil9XG4gICAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgICA8L2Rpdj5cbiAgICAgICAgPC9kaXY+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwidGFibGUtcmVzcG9uc2l2ZVwiPlxuICAgICAgICAgIDx0YWJsZSBjbGFzc05hbWU9XCJ0YWJsZSB0YWJsZS1zdHJpcGVkXCI+XG4gICAgICAgICAgICA8VGFibGVIZWFkZXJcbiAgICAgICAgICAgICAgY29udGVudD17XG4gICAgICAgICAgICAgICAgdGhpcy5wcm9wcy5oZWFkZXJzLm1hcCgoaGVhZGVyLCBpbmRleCkgPT4ge1xuICAgICAgICAgICAgICAgICAgdmFyIGNsYXNzTmFtZTtcbiAgICAgICAgICAgICAgICAgIGlmIChpbmRleCA9PSB0aGlzLnN0YXRlLmNvbHVtbkluZGV4KSB7XG4gICAgICAgICAgICAgICAgICAgIGNsYXNzTmFtZSA9ICh0aGlzLnN0YXRlLmFzY2VuZGluZyA/IFwiYXNjXCIgOiBcImRlc2NcIikgKyBcIlNvcnRcIjtcbiAgICAgICAgICAgICAgICAgIH1cbiAgICAgICAgICAgICAgICAgIHJldHVybiAoXG4gICAgICAgICAgICAgICAgICAgICAgKHRoaXMucHJvcHMuc29ydGFibGVDb2x1bW5zICYmXG4gICAgICAgICAgICAgICAgICAgICAgICB0aGlzLnByb3BzLnNvcnRhYmxlQ29sdW1ucy5maWx0ZXIoKGVsZW1lbnQpID0+IGVsZW1lbnQgPT0gaW5kZXgpLmxlbmd0aCA+IDApID9cbiAgICAgICAgICAgICAgICAgICAgICA8VGFibGVIZWFkZXJDZWxsT3JkZXIgY2xhc3NOYW1lPXtjbGFzc05hbWV9IGNvbnRlbnQ9e2hlYWRlcn1cbiAgICAgICAgICAgICAgICAgICAgICAgIG9yZGVyQnk9e3RoaXMub3JkZXJCeUNvbHVtbn0gY29sdW1uSW5kZXg9e2luZGV4fSAvPiA6XG4gICAgICAgICAgICAgICAgICAgICAgPFRhYmxlSGVhZGVyQ2VsbCBjbGFzc05hbWU9e2NsYXNzTmFtZX0gY29udGVudD17aGVhZGVyfSAvPlxuICAgICAgICAgICAgICAgICAgKTtcbiAgICAgICAgICAgICAgICB9KX1cbiAgICAgICAgICAgIC8+XG4gICAgICAgICAgICA8dGJvZHkgY2xhc3NOYW1lPVwidGFibGUtY29udGVudFwiPlxuICAgICAgICAgICAgICB7cm93c1xuICAgICAgICAgICAgICAgIC5maWx0ZXIoKGVsZW1lbnQsIGkpID0+IGkgPj0gZmlyc3RJdGVtSW5kZXggJiYgaSA8IGZpcnN0SXRlbUluZGV4ICsgaXRlbXNQZXJQYWdlKVxuICAgICAgICAgICAgICB9XG4gICAgICAgICAgICAgIDwvdGJvZHk+XG4gICAgICAgICAgPC90YWJsZT5cbiAgICAgICAgPC9kaXY+XG4gICAgICAgIDxkaXYgY2xhc3NOYW1lPVwicGFuZWwtZm9vdGVyXCI+XG4gICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJzcGFjZXdhbGstbGlzdC1ib3R0b20tYWRkb25zXCI+XG4gICAgICAgICAgICA8ZGl2IGNsYXNzTmFtZT1cInRhYmxlLXBhZ2UtaW5mb3JtYXRpb25cIj57dChcIlBhZ2UgezB9IG9mIHsxfVwiLCBjdXJyZW50UGFnZSwgbGFzdFBhZ2UpfTwvZGl2PlxuICAgICAgICAgICAge3BhZ2luYXRpb259XG4gICAgICAgICAgPC9kaXY+XG4gICAgICAgIDwvZGl2PlxuICAgICAgPC9kaXY+XG4gICAgKTtcbiAgfVxufSk7XG5cbnZhciBQYWdpbmF0aW9uQnV0dG9uID0gUmVhY3QuY3JlYXRlQ2xhc3Moe1xuICBvbkNsaWNrOiBmdW5jdGlvbigpIHtcbiAgICB0aGlzLnByb3BzLm9uQ2xpY2sodGhpcy5wcm9wcy50b1BhZ2UpO1xuICB9LFxuXG4gIHJlbmRlcjogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIChcbiAgICAgIDxidXR0b24gdHlwZT1cImJ1dHRvblwiIGNsYXNzTmFtZT1cImJ0biBidG4tZGVmYXVsdFwiXG4gICAgICAgIGRpc2FibGVkPXt0aGlzLnByb3BzLmRpc2FibGVkfSBvbkNsaWNrPXt0aGlzLm9uQ2xpY2t9PlxuICAgICAgICB7dGhpcy5wcm9wcy50ZXh0fVxuICAgICAgPC9idXR0b24+XG4gICAgKTtcbiAgfVxufSk7XG5cbnZhciBQYWdlU2VsZWN0b3IgPSBSZWFjdC5jcmVhdGVDbGFzcyh7XG4gIGhhbmRsZU9uQ2hhbmdlOiBmdW5jdGlvbihlKSB7XG4gICAgdGhpcy5wcm9wcy5vbkNoYW5nZShwYXJzZUludChlLnRhcmdldC52YWx1ZSkpO1xuICB9LFxuXG4gIHJlbmRlcjogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIChcbiAgICAgIDxzZWxlY3QgY2xhc3NOYW1lPXt0aGlzLnByb3BzLmNsYXNzTmFtZX1cbiAgICAgICAgZGVmYXVsdFZhbHVlPXt0aGlzLnByb3BzLmN1cnJlbnRWYWx1ZX1cbiAgICAgICAgb25DaGFuZ2U9e3RoaXMuaGFuZGxlT25DaGFuZ2V9PlxuICAgICAgICB7dGhpcy5wcm9wcy5vcHRpb25zLm1hcChmdW5jdGlvbihvKSB7XG4gICAgICAgICAgcmV0dXJuICg8b3B0aW9uIHZhbHVlPXtvfT57b308L29wdGlvbj4pO1xuICAgICAgICB9KX1cbiAgICAgIDwvc2VsZWN0PlxuICAgICk7XG4gIH1cbn0pO1xuXG52YXIgVGFibGVIZWFkZXIgPSBSZWFjdC5jcmVhdGVDbGFzcyh7XG4gIHJlbmRlcjogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIChcbiAgICAgIDx0aGVhZD48dHI+e3RoaXMucHJvcHMuY29udGVudH08L3RyPjwvdGhlYWQ+XG4gICAgKTtcbiAgfVxufSk7XG5cbnZhciBUYWJsZUhlYWRlckNlbGxPcmRlciA9IFJlYWN0LmNyZWF0ZUNsYXNzKHtcbiAgaGFuZGxlQ2xpY2s6IGZ1bmN0aW9uKCkge1xuICAgIGlmICh0aGlzLnByb3BzLmNvbHVtbkluZGV4ICE9IG51bGwpIHtcbiAgICAgIHRoaXMucHJvcHMub3JkZXJCeSh0aGlzLnByb3BzLmNvbHVtbkluZGV4KTtcbiAgICB9XG4gIH0sXG5cbiAgcmVuZGVyOiBmdW5jdGlvbiAoKSB7XG4gICAgcmV0dXJuICg8dGggY2xhc3NOYW1lPXt0aGlzLnByb3BzLmNsYXNzTmFtZX0+PGEgY2xhc3NOYW1lPVwib3JkZXJCeVwiIG9uQ2xpY2s9e3RoaXMuaGFuZGxlQ2xpY2t9Pnt0aGlzLnByb3BzLmNvbnRlbnR9PC9hPjwvdGg+KTtcbiAgfVxufSk7XG5cblxudmFyIFRhYmxlSGVhZGVyQ2VsbCA9IFJlYWN0LmNyZWF0ZUNsYXNzKHtcbiAgcmVuZGVyOiBmdW5jdGlvbiAoKSB7XG4gICAgcmV0dXJuICg8dGggY2xhc3NOYW1lPXt0aGlzLnByb3BzLmNsYXNzTmFtZX0+e3RoaXMucHJvcHMuY29udGVudH08L3RoPik7XG4gIH1cbn0pO1xuXG52YXIgVGFibGVSb3cgPSBSZWFjdC5jcmVhdGVDbGFzcyh7XG4gIHJlbmRlcjogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIChcbiAgICAgIDx0ciBjbGFzc05hbWU9e3RoaXMucHJvcHMuY2xhc3NOYW1lfT5cbiAgICAgICAge3RoaXMucHJvcHMuY29sdW1uc31cbiAgICAgIDwvdHI+XG4gICAgKTtcbiAgfVxufSk7XG5cbnZhciBUYWJsZUNlbGwgPSBSZWFjdC5jcmVhdGVDbGFzcyh7XG4gIHJlbmRlcjogZnVuY3Rpb24oKSB7XG4gICAgcmV0dXJuIChcbiAgICAgIDx0ZD5cbiAgICAgICAge3RoaXMucHJvcHMuY29udGVudH1cbiAgICAgIDwvdGQ+XG4gICAgKTtcbiAgfVxufSk7XG5cblxudmFyIFNlYXJjaEZpZWxkID0gUmVhY3QuY3JlYXRlQ2xhc3Moe1xuICBoYW5kbGVDaGFuZ2U6IGZ1bmN0aW9uKGUpIHtcbiAgICB0aGlzLnByb3BzLm9uQ2hhbmdlKGUudGFyZ2V0LnZhbHVlKTtcbiAgfSxcblxuICByZW5kZXI6IGZ1bmN0aW9uKCkge1xuICAgIHJldHVybiAoXG4gICAgICA8aW5wdXQgY2xhc3NOYW1lPVwiZm9ybS1jb250cm9sIHRhYmxlLWlucHV0LXNlYXJjaFwiXG4gICAgICAgIHZhbHVlPXt0aGlzLnByb3BzLmRlZmF1bHRWYWx1ZX1cbiAgICAgICAgcGxhY2Vob2xkZXI9e3RoaXMucHJvcHMucGxhY2Vob2xkZXJ9XG4gICAgICAgIHR5cGU9XCJ0ZXh0XCJcbiAgICAgICAgb25DaGFuZ2U9e3RoaXMuaGFuZGxlQ2hhbmdlfSAvPlxuICAgICk7XG4gIH1cbn0pO1xuXG5tb2R1bGUuZXhwb3J0cyA9IHtcbiAgICBUYWJsZSA6IFRhYmxlLFxuICAgIFRhYmxlQ2VsbCA6IFRhYmxlQ2VsbCxcbiAgICBUYWJsZVJvdyA6IFRhYmxlUm93LFxuICAgIFNlYXJjaEZpZWxkIDogU2VhcmNoRmllbGQsXG4gICAgVGFibGVIZWFkZXJDZWxsIDogVGFibGVIZWFkZXJDZWxsLFxuICAgIFRhYmxlSGVhZGVyQ2VsbE9yZGVyIDogVGFibGVIZWFkZXJDZWxsT3JkZXIsXG4gICAgUGFnZVNlbGVjdG9yIDogUGFnZVNlbGVjdG9yLFxuICAgIFBhZ2luYXRpb25CdXR0b24gOiBQYWdpbmF0aW9uQnV0dG9uXG59XG4iLCIndXNlIHN0cmljdCc7XG5cbnZhciBTdGF0ZVBlcnNpc3RlZE1peGluID0ge1xuICBjb21wb25lbnRXaWxsTW91bnQ6IGZ1bmN0aW9uKCkge1xuICAgIGlmICh0aGlzLnByb3BzLmxvYWRTdGF0ZSkge1xuICAgICAgaWYgKHRoaXMucHJvcHMubG9hZFN0YXRlKCkpIHtcbiAgICAgICAgdGhpcy5zdGF0ZSA9IHRoaXMucHJvcHMubG9hZFN0YXRlKCk7XG4gICAgICB9XG4gICAgfVxuICB9LFxuICBjb21wb25lbnRXaWxsVW5tb3VudDogZnVuY3Rpb24oKSB7XG4gICAgaWYgKHRoaXMucHJvcHMuc2F2ZVN0YXRlKSB7XG4gICAgICB0aGlzLnByb3BzLnNhdmVTdGF0ZSh0aGlzLnN0YXRlKTtcbiAgICB9XG4gIH0sXG59O1xuXG5tb2R1bGUuZXhwb3J0cyA9IHtcbiAgICBTdGF0ZVBlcnNpc3RlZE1peGluIDogU3RhdGVQZXJzaXN0ZWRNaXhpblxufVxuIiwiJ3VzZSBzdHJpY3QnO1xuXG52YXIgUmVhY3QgPSByZXF1aXJlKFwicmVhY3RcIik7XG52YXIgVGFibGVDb21wb25lbnQgPSByZXF1aXJlKFwiLi4vY29tcG9uZW50cy90YWJsZVwiKTtcbnZhciBQYW5lbENvbXBvbmVudCA9IHJlcXVpcmUoXCIuLi9jb21wb25lbnRzL3BhbmVsXCIpO1xudmFyIE1lc3NhZ2VzID0gcmVxdWlyZShcIi4uL2NvbXBvbmVudHMvbWVzc2FnZXNcIikuTWVzc2FnZXM7XG5cbnZhciBUYWJsZSA9IFRhYmxlQ29tcG9uZW50LlRhYmxlO1xudmFyIFRhYmxlQ2VsbCA9IFRhYmxlQ29tcG9uZW50LlRhYmxlQ2VsbDtcbnZhciBUYWJsZVJvdyA9IFRhYmxlQ29tcG9uZW50LlRhYmxlUm93O1xudmFyIFBhbmVsID0gUGFuZWxDb21wb25lbnQuUGFuZWw7XG52YXIgUGFuZWxCdXR0b24gPSBQYW5lbENvbXBvbmVudC5QYW5lbEJ1dHRvbjtcblxuXG52YXIgU3RhdGVDYXRhbG9nID0gUmVhY3QuY3JlYXRlQ2xhc3Moe1xuXG4gICAgZ2V0SW5pdGlhbFN0YXRlOiBmdW5jdGlvbigpIHtcbiAgICAgICAgdmFyIHN0ID0ge1xuICAgICAgICAgICAgXCJzZXJ2ZXJEYXRhXCI6IFtdXG4gICAgICAgIH07XG4vLyAgICAgICAgaWYgKHRoaXMucHJvcHMuZmxhc2hNZXNzYWdlcykge1xuLy8gICAgICAgICAgICBzdFtcImZsYXNoTWVzc2FnZXNcIl0gPSB0aGlzLnByb3BzLmZsYXNoTWVzc2FnZXM7XG4vLyAgICAgICAgfVxuICAgICAgICByZXR1cm4gc3Q7XG4gICAgfSxcblxuICAgIHJlZnJlc2hTZXJ2ZXJEYXRhOiBmdW5jdGlvbigpIHtcbiAgICAgICAgJC5nZXQoXCIvcmhuL21hbmFnZXIvc3RhdGVfY2F0YWxvZy9kYXRhXCIsIGRhdGEgPT4ge1xuICAgICAgICAgIHRoaXMuc2V0U3RhdGUoe1wic2VydmVyRGF0YVwiIDogZGF0YX0pO1xuICAgICAgICB9KTtcbiAgICB9LFxuXG4gICAgY29tcG9uZW50V2lsbE1vdW50OiBmdW5jdGlvbigpIHtcbiAgICAgICAgdGhpcy5yZWZyZXNoU2VydmVyRGF0YSgpO1xuICAgIH0sXG5cbi8vICAgIGNvbXBvbmVudERpZE1vdW50OiBmdW5jdGlvbigpIHtcbi8vICAgICAgICB2YXIgdmlld3MgPSAxO1xuLy8gICAgICAgIGlmKHRoaXMuc3RhdGUuZmxhc2hNZXNzYWdlc1ZpZXdzKSB7XG4vLyAgICAgICAgICAgIHZpZXdzID0gdGhpcy5zdGF0ZS5mbGFzaE1lc3NhZ2VzVmlld3MgKyAxO1xuLy8gICAgICAgIH1cbi8vICAgICAgICB0aGlzLnNldFN0YXRlKHsgXCJmbGFzaE1lc3NhZ2VzVmlld3NcIjogdmlld3MgfSk7XG4vLyAgICB9LFxuLy9cbi8vICAgIHNob3VsZENvbXBvbmVudFVwZGF0ZTogZnVuY3Rpb24oKSB7XG4vLyAgICAgICAgcmV0dXJuIHR5cGVvZiB0aGlzLnN0YXRlLmZsYXNoTWVzc2FnZXNWaWV3cyA9PT0gXCJ1bmRlZmluZWRcIiB8fFxuLy8gICAgICAgICAgICB0aGlzLnN0YXRlLmZsYXNoTWVzc2FnZXNWaWV3cyA+IDE7XG4vLyAgICB9LFxuXG4gICAgY29tcGFyZVJvd3M6IGZ1bmN0aW9uKGEsIGIsIGNvbHVtbkluZGV4LCBvcmRlcikge1xuICAgICAgICB2YXIgb3JkZXJDb25kaXRpb24gPSBvcmRlciA/IDEgOiAtMTtcbiAgICAgICAgdmFyIGFWYWx1ZSA9IGEucHJvcHNbXCJyYXdfZGF0YVwiXTtcbiAgICAgICAgdmFyIGJWYWx1ZSA9IGIucHJvcHNbXCJyYXdfZGF0YVwiXTtcbiAgICAgICAgdmFyIHJlc3VsdCA9IGFWYWx1ZS5sb2NhbGVDb21wYXJlKGJWYWx1ZSk7XG4gICAgICAgIHJldHVybiByZXN1bHQgKiBvcmRlckNvbmRpdGlvbjtcbiAgICB9LFxuXG4gICAgcmVuZGVyOiBmdW5jdGlvbigpIHtcbiAgICAgICAgdmFyIGJ1dHRvbiA9IDxQYW5lbEJ1dHRvbiB0ZXh0PVwiQ3JlYXRlIHN0YXRlXCIgaWNvbj1cImZhLXBsdXNcIiBhY3Rpb249XCIvcmhuL21hbmFnZXIvc3RhdGVfY2F0YWxvZy9zdGF0ZVwiLz47XG5cbiAgICAgICAgdmFyIG1zZyA9IG51bGw7XG4vLyAgICAgICAgaWYodHlwZW9mIHRoaXMuc3RhdGUuZmxhc2hNZXNzYWdlcyAhPT0gXCJ1bmRlZmluZWRcIiAmJiB0aGlzLnN0YXRlLmZsYXNoTWVzc2FnZXMubGVuZ3RoID4gMCkge1xuLy8gICAgICAgICAgICBtc2cgPSA8TWVzc2FnZXMgaXRlbXM9e3RoaXMucHJvcHMuZmxhc2hNZXNzYWdlc30vPlxuLy8gICAgICAgIH1cblxuICAgICAgICBpZih0eXBlb2YgdGhpcy5wcm9wcy5mbGFzaE1lc3NhZ2VzICE9PSBcInVuZGVmaW5lZFwiKSB7XG4vLyAgICAgICAgICAgIHZhciBpdGVtcyA9IHtzZXZlcml0eTogXCJlcnJvclwiLCB0ZXh0OiBcImFsYWxhbGFsXCJ9O1xuICAgICAgICAgICAgbXNnID0gPE1lc3NhZ2VzIGl0ZW1zPXt0aGlzLnByb3BzLmZsYXNoTWVzc2FnZXN9Lz47XG4gICAgICAgIH1cbiAgICAgICAgcmV0dXJuIChcbiAgICAgICAgICAgIDxQYW5lbCB0aXRsZT1cIlN0YXRlcyBDYXRhbG9nXCIgaWNvbj1cInNwYWNld2Fsay1pY29uLXZpcnR1YWwtaG9zdC1tYW5hZ2VyXCIgYnV0dG9uPXtidXR0b259PlxuICAgICAgICAgICAgICAgIHttc2d9XG4gICAgICAgICAgICAgICAgPGRpdj5cbiAgICAgICAgICAgICAgICAgICAgPGRpdiBjbGFzc05hbWU9XCJzcGFjZXdhbGstbGlzdFwiPlxuICAgICAgICAgICAgICAgICAgICAgICAgPFRhYmxlIGhlYWRlcnM9e1t0KFwiU3RhdGVcIildfVxuICAgICAgICAgICAgICAgICAgICAgICAgICByb3dzPXtzdGF0ZXNUb1Jvd3ModGhpcy5zdGF0ZS5zZXJ2ZXJEYXRhKX1cbiAgICAgICAgICAgICAgICAgICAgICAgICAgbG9hZFN0YXRlPXt0aGlzLnByb3BzLmxvYWRTdGF0ZX1cbiAgICAgICAgICAgICAgICAgICAgICAgICAgc2F2ZVN0YXRlPXt0aGlzLnByb3BzLnNhdmVTdGF0ZX1cbiAgICAgICAgICAgICAgICAgICAgICAgICAgcm93Q29tcGFyYXRvcj17dGhpcy5jb21wYXJlUm93c31cbiAgICAgICAgICAgICAgICAgICAgICAgICAgc29ydGFibGVDb2x1bW5zPXtbMF19XG4gICAgICAgICAgICAgICAgICAgICAgICAgIGRhdGFGaWx0ZXI9eyh0YWJsZVJvdywgc2VhcmNoVmFsdWUpID0+IHRhYmxlUm93LnByb3BzW1wicmF3X2RhdGFcIl0udG9Mb3dlckNhc2UoKS5pbmRleE9mKHNlYXJjaFZhbHVlLnRvTG93ZXJDYXNlKCkpID4gLTF9XG4gICAgICAgICAgICAgICAgICAgICAgICAgIHNlYXJjaFBsYWNlaG9sZGVyPXt0KFwiRmlsdGVyIGJ5IHN0YXRlIG5hbWU6XCIpfVxuICAgICAgICAgICAgICAgICAgICAgICAgLz5cbiAgICAgICAgICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgICAgICAgPC9kaXY+XG4gICAgICAgICAgICA8L1BhbmVsPlxuICAgICAgICApO1xuICAgIH1cblxufSk7XG5cbmZ1bmN0aW9uIHN0YXRlc1RvUm93cyhzZXJ2ZXJEYXRhKSB7XG4gIHJldHVybiBzZXJ2ZXJEYXRhLm1hcCgocykgPT4ge1xuICAgIHZhciBsaW5rID0gPGEgaHJlZj17XCIvcmhuL21hbmFnZXIvc3RhdGVfY2F0YWxvZy9zdGF0ZS9cIiArIHN9PntzfTwvYT5cbiAgICB2YXIgY29sdW1ucyA9IFtcbiAgICAgIDxUYWJsZUNlbGwgY29udGVudD17bGlua30gLz4sXG4gICAgXTtcbiAgICByZXR1cm4gPFRhYmxlUm93IGNvbHVtbnM9e2NvbHVtbnN9IHJhd19kYXRhPXtzfSAvPlxuICB9KTtcbn1cblxuUmVhY3QucmVuZGVyKFxuICA8U3RhdGVDYXRhbG9nIGZsYXNoTWVzc2FnZXM9e2ZsYXNoTWVzc2FnZSgpfS8+LFxuICBkb2N1bWVudC5nZXRFbGVtZW50QnlJZCgnc3RhdGUtY2F0YWxvZycpXG4pOyJdfQ==
