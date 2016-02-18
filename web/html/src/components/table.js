"use strict";

var React = require("react")
var StatePersistedMixin = require("./util").StatePersistedMixin

var Table = React.createClass({
  mixins: [StatePersistedMixin],

  getInitialState: function() {
    return {
      "currentPage": 1, "itemsPerPage": 15,
      "searchField": "",
      "columnIndex": 0, "ascending": true
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
    var ascending = this.state.ascending;
    if (this.state.columnIndex == columnIndex) {
      ascending = !ascending;
    }
    else {
      ascending = true;
    }
    this.setState({"columnIndex": columnIndex, "ascending": ascending});
  },

  getRows: function(unfilteredRows, searchValue) {
    var rows = this.props.dataFilter && searchValue.length > 0 ?
      unfilteredRows.filter((row) => this.props.dataFilter(row, searchValue)) :
      unfilteredRows;
      if (this.props.rowComparator) {
        var columnIndex = this.state.columnIndex;
        var ascending = this.state.ascending;
        rows.sort((a, b) => this.props.rowComparator(a, b, columnIndex, ascending));
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

  onItemsPerPageChange: function(itemsPerPage) {
    this.setState({"itemsPerPage": itemsPerPage});
    var lastPage = this.lastPage(this.getRows(this.props.rows, this.state.searchField), itemsPerPage);
    if (this.state.currentPage > lastPage) {
      this.setState({"currentPage": lastPage });
    }
  },

  onSearchFieldChange: function(searchValue) {
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
            <PaginationButton onClick={() => this.goToPage(1)} toPage={1} disabled={currentPage == 1} text={t("First")} />
            <PaginationButton onClick={() => this.goToPage(currentPage -1)} disabled={currentPage == 1} text={t("Prev")} />
            <PaginationButton onClick={() => this.goToPage(currentPage +1)} disabled={currentPage == lastPage} text={t("Next")} />
            <PaginationButton onClick={() => this.goToPage(lastPage)} disabled={currentPage == lastPage} text={t("Last")} />
          </div>
        </div>
      );
    }

    var searchField;
    if (this.props.dataFilter) {
      searchField = (
        <SearchField
          onChange={this.onSearchFieldChange}
          defaultValue={this.state.searchField}
          placeholder={this.props.searchPlaceholder}
        />
      );
    }

    return (
      <div className="spacewalk-list">
        <div className="panel panel-default">
          <div className="panel-heading">
            <div className="spacewalk-list-head-addons">
              <div className="spacewalk-list-filter table-search-wrapper">
                {searchField} {t("Items {0} - {1} of {2}", fromItem, toItem, itemCount)}
              </div>
              <div className="spacewalk-list-head-addons-extra table-items-per-page-wrapper">
                <PageSelector className="display-number"
                  options={[5,10,15,25,50,100,250,500]}
                  currentValue={itemsPerPage}
                  onChange={this.onItemsPerPageChange}
                /> {t("items per page")}
              </div>
            </div>
          </div>
          <div className="table-responsive">
            <table className="table table-striped">
              <TableHeader
                content={
                  this.props.headers.map((header, index) => {
                    var className;
                    if (index == this.state.columnIndex) {
                      className = (this.state.ascending ? "asc" : "desc") + "Sort";
                    }
                    return (
                        (this.props.sortableColumns &&
                          this.props.sortableColumns.filter((element) => element == index).length > 0) ?
                        <TableHeaderCellOrder className={className} content={header}
                          orderBy={this.orderByColumn} columnIndex={index} /> :
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
      </div>
    );
  }
});

var PaginationButton = React.createClass({
  render: function() {
    return (
      <button type="button" className="btn btn-default"
        disabled={this.props.disabled} onClick={this.props.onClick}>
        {this.props.text}
      </button>
    );
  }
});

var PageSelector = React.createClass({
  handleOnChange: function(e) {
    this.props.onChange(parseInt(e.target.value));
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

module.exports = {
    Table : Table,
    TableCell : TableCell,
    TableRow : TableRow,
    SearchField : SearchField,
    TableHeaderCell : TableHeaderCell,
    TableHeaderCellOrder : TableHeaderCellOrder,
    PageSelector : PageSelector,
    PaginationButton : PaginationButton
}
