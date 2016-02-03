'use strict';

var React = require("react")
var t = require("./react-translation")

var PaginationButton = React.createClass({
  onClick: function() {
    this.props.onClick(this.props.toPage);
  },

  render: function() {
    return (
      <button type="button" className="btn btn-default"
        disabled={this.props.disabled} onClick={this.onClick}>
        {this.props.text}
      </button>
    );
  }
});

var Select = React.createClass({
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
      <select className={this.props.className}
        defaultValue={this.props.currentValue}
        onChange={this.handleOnChange}>
        {this.props.options.map(function(o) {
          return (<option value={o}>{o}</option>);
        })}
      </select>
    );
  }
});

var TableHeader = React.createClass({
  render: function() {
    return (
      <thead><tr>{this.props.content}</tr></thead>
    );
  }
});

var TableHeaderCellOrder = React.createClass({
  handleClick: function() {
    if (this.props.columnIndex != null) {
      this.props.orderBy(this.props.columnIndex);
    }
  },

  render: function () {
    return (<th className={this.props.className}><a className="orderBy" onClick={this.handleClick}>{this.props.content}</a></th>);
  }
});


var TableHeaderCell = React.createClass({
  render: function () {
    return (<th className={this.props.className}>{this.props.content}</th>);
  }
});

var TableRow = React.createClass({
  render: function() {
    return (
      <tr className={this.props.className}>
        {this.props.columns}
      </tr>
    );
  }
});

var TableCell = React.createClass({
  render: function() {
    return (
      <td>
        {this.props.content}
      </td>
    );
  }
});

var QuantityCell = React.createClass({
  render: function() {
    var matched = this.props.matched;
    var total = this.props.total;
    var content = matched + "/" + total;

    return (
      matched == total ?
        <TableCell content={<StrongText className="bg-danger" content={content} />} /> :
        <TableCell content={content} />
    );
  }
});

var SearchField = React.createClass({
  handleChange: function(e) {
    this.props.onChange(e.target.value);
  },

  render: function() {
    return (
      <input className="form-control table-input-search"
        value={this.props.defaultValue}
        placeholder={this.props.placeholder}
        type="text"
        onChange={this.handleChange} />
    );
  }
});

var StrongText = React.createClass({
  render: function() {
    return (
      <strong className={this.props.className}>
        {this.props.content}
      </strong>
    );
  }
});

var ToolTip = React.createClass({
  render: function() {
    return (
      <span title={this.props.title}>
        {this.props.content}
      </span>
    );
  }
});

var CsvLink = React.createClass({
  render: function() {
    return (
      <div className="spacewalk-csv-download">
        <a className="btn btn-link" href={"/rhn/manager/subscription_matching/" + this.props.name}>
          <i className="fa spacewalk-icon-download-csv"></i>
          {t("Download CSV")}
        </a>
      </div>
    );
  }
});

var Table = React.createClass({
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
        <div className="spacewalk-list-pagination">
          <div className="spacewalk-list-pagination-btns btn-group">
            <PaginationButton onClick={this.goToPage} toPage={1} disabled={currentPage == 1} text={t("First")} />
            <PaginationButton onClick={this.goToPage} toPage={currentPage -1} disabled={currentPage == 1} text={t("Prev")} />
            <PaginationButton onClick={this.goToPage} toPage={currentPage + 1} disabled={currentPage == lastPage} text={t("Next")} />
            <PaginationButton onClick={this.goToPage} toPage={lastPage} disabled={currentPage == lastPage} text={t("Last")} />
          </div>
        </div>
      );
    }

    var searchField;
    if (this.props.dataFilter) {
      searchField = (
        <SearchField
          onChange={this.changeSearchField}
          defaultValue={this.state.searchField}
          placeholder={this.props.searchPlaceholder}
        />
      );
    }

    var component = this;

    return (
      <div className="panel panel-default">
        <div className="panel-heading">
          <div className="spacewalk-list-head-addons">
            <div className="spacewalk-list-filter table-search-wrapper">
              {searchField} {t("Items {0} - {1} of {2}", fromItem, toItem, itemCount)}
            </div>
            <div className="spacewalk-list-head-addons-extra table-items-per-page-wrapper">
              <Select className="display-number"
                type="number"
                options={[5,10,15,25,50,100,250,500]}
                currentValue={itemsPerPage}
                onChange={this.changeItemsPerPage}
              /> {t("items per page")}
            </div>
          </div>
        </div>
        <div className="table-responsive">
          <table className="table table-striped">
            <TableHeader
              content={
                this.props.headers.map(function(header, index) {
                  var className;
                  if (index == component.state.columnIndex) {
                    className = component.state.order + "Sort";
                  }
                  return (
                      (component.props.sortableColumns &&
                        component.props.sortableColumns.filter((element) => element == index).length > 0) ?
                      <TableHeaderCellOrder className={className} content={header}
                        orderBy={component.orderByColumn} columnIndex={index} /> :
                      <TableHeaderCell className={className} content={header} />
                  );
                })}
            />
            <tbody className="table-content">
              {rows
                .filter((element, i) => i >= firstItemIndex && i < firstItemIndex + itemsPerPage)
              }
              </tbody>
          </table>
        </div>
        <div className="panel-footer">
          <div className="spacewalk-list-bottom-addons">
            <div className="table-page-information">{t("Page {0} of {1}", currentPage, lastPage)}</div>
            {pagination}
          </div>
        </div>
      </div>
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