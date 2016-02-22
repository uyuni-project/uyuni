"use strict";

var React = require("react")
var StatePersistedMixin = require("./util").StatePersistedMixin

var Table = React.createClass({
  mixins: [StatePersistedMixin],

  propTypes: {
    headers: React.PropTypes.arrayOf(React.PropTypes.node).isRequired,
    rows: React.PropTypes.arrayOf(React.PropTypes.node).isRequired,
    rowFilter: React.PropTypes.func, // (row, searchString) -> boolean
    filterPlaceholder: React.PropTypes.string,
    rowComparator: React.PropTypes.func, // (row1, row2, sortColumnIndex, ascending) -> -1/0/+1
    sortableColumnIndexes: React.PropTypes.arrayOf(React.PropTypes.number), // required with rowComparator
  },

  getInitialState: function() {
    return {
      currentPage: 1,
      itemsPerPage: 15,
      filterText: "",
      sortColumnIndex: 0,
      sortAscending: true
    };
  },

  componentWillReceiveProps: function(nextProps) {
    var lastPage = Math.ceil(nextProps.rows.length / this.state.itemsPerPage);
    if (this.state.currentPage > lastPage) {
      this.setState({currentPage: lastPage});
    }
  },

  getProcessedRows: function() {
    const filter = this.props.rowFilter ?
      (row) => this.props.rowFilter(row, this.state.filterText) :
      (row) => true
    ;

    const comparator = this.props.rowComparator ?
      (a, b) => this.props.rowComparator(a, b, this.state.sortColumnIndex, this.state.sortAscending) :
      (a, b) => 0
    ;

    return this.props.rows.filter(filter).sort(comparator);
  },

  lastPage: function(filterText, itemsPerPage) {
    const rowCount = this.props.rowFilter ?
      this.props.rows.filter((row) => this.props.rowFilter(row, filterText)).length :
      this.props.rows.length;

    const lastPage = Math.ceil(rowCount / itemsPerPage);
    if (lastPage == 0) {
      return 1;
    }
    return lastPage;
  },

  onPageChange:function(page) {
    this.setState({currentPage: page});
  },

  onItemsPerPageChange: function(itemsPerPage) {
    this.setState({itemsPerPage: itemsPerPage});

    var lastPage = this.lastPage(this.state.filterText, itemsPerPage);
    if (this.state.currentPage > lastPage) {
      this.setState({currentPage: lastPage});
    }
  },

  onFilterTextChange: function(filterText) {
    this.setState({filterText: filterText});

    var lastPage = this.lastPage(filterText, this.state.itemsPerPage);
    if (this.state.currentPage > lastPage) {
      this.setState({currentPage: lastPage});
    }
  },

  onSortColumnIndexChange: function(sortColumnIndex) {
    this.setState({
      sortColumnIndex: sortColumnIndex,
      sortAscending: this.state.sortColumnIndex != sortColumnIndex || !this.state.sortAscending
    });
  },

  render: function() {
    const rows = this.getProcessedRows();
    const itemsPerPage = this.state.itemsPerPage;
    const currentPage = this.state.currentPage;
    const firstItemIndex = (currentPage - 1) * itemsPerPage;

    const itemCount = rows.length;
    const fromItem = itemCount > 0 ? firstItemIndex + 1 : 0;
    const toItem = firstItemIndex + itemsPerPage <= itemCount ? firstItemIndex + itemsPerPage : itemCount;
    const itemCounter = <span>{t("Items {0} - {1} of {2}", fromItem, toItem, itemCount)}</span>

    const filterText = this.state.filterText;

    const filterField = this.props.rowFilter ?
      <FilterField
        onChange={this.onFilterTextChange}
        defaultValue={filterText}
        placeholder={this.props.filterPlaceholder}
      /> :
      null
    ;

    return (
      <div className="spacewalk-list">
        <div className="panel panel-default">
          <div className="panel-heading">
            <div className="spacewalk-list-head-addons">
              <div className="spacewalk-list-filter table-search-wrapper">
                {filterField} {itemCounter}
              </div>
              <div className="spacewalk-list-head-addons-extra table-items-per-page-wrapper">
                <ItemsPerPageSelector
                  currentValue={itemsPerPage}
                  onChange={this.onItemsPerPageChange}
                /> {t("items per page")}
              </div>
            </div>
          </div>
          <div className="table-responsive">
            <table className="table table-striped">
              <TableHeader content={
                this.props.headers.map((header, index) => {
                  const sortable = this.props.sortableColumnIndexes &&
                    this.props.sortableColumnIndexes.indexOf(index) >= 0;

                  if (sortable) {
                    var className;
                    if (index == this.state.sortColumnIndex) {
                      className = (this.state.sortAscending ? "asc" : "desc") + "Sort";
                    }
                    return <SortableTableHeader className={className} content={header}
                      orderBy={() => this.onSortColumnIndexChange(index)} />;
                  }
                  else {
                    return <th>{header}</th>;
                  }
                })
              } />
              <tbody className="table-content">
                {rows.slice(firstItemIndex, firstItemIndex + itemsPerPage)}
              </tbody>
            </table>
          </div>
          <div className="panel-footer">
            <div className="spacewalk-list-bottom-addons">
              <PaginationBlock
                currentPage={currentPage}
                lastPage={this.lastPage(filterText, itemsPerPage)}
                onPageChange={this.onPageChange}
              />
            </div>
          </div>
        </div>
      </div>
    );
  }
});

var TableRow = (props) => <tr className={props.className}>{props.columns}</tr>;

var TableCell = (props) => <td>{props.content}</td>;

var PaginationBlock = (props) => {
  const currentPage = props.currentPage;
  const lastPage = props.lastPage;
  const onPageChange = props.onPageChange;

  const pagination = lastPage > 1 ?
    <div className="spacewalk-list-pagination">
      <div className="spacewalk-list-pagination-btns btn-group">
        <PaginationButton onClick={() => onPageChange(1)} toPage={1} disabled={currentPage == 1} text={t("First")} />
        <PaginationButton onClick={() => onPageChange(currentPage - 1)} disabled={currentPage == 1} text={t("Prev")} />
        <PaginationButton onClick={() => onPageChange(currentPage + 1)} disabled={currentPage == lastPage} text={t("Next")} />
        <PaginationButton onClick={() => onPageChange(lastPage)} disabled={currentPage == lastPage} text={t("Last")} />
      </div>
    </div> :
    null
  ;

  return (
    <div>
      <div className="table-page-information">{t("Page {0} of {1}", currentPage, lastPage)}</div>
      {pagination}
    </div>
  );
};

var PaginationButton = (props) =>
  <button type="button" className="btn btn-default"
    disabled={props.disabled} onClick={props.onClick}>
    {props.text}
  </button>
;

var ItemsPerPageSelector = (props) =>
  <select className="display-number"
    defaultValue={props.currentValue}
    onChange={(e) => props.onChange(parseInt(e.target.value))}>
      {[5,10,15,25,50,100,250,500].map((o) => <option value={o}>{o}</option>)}
  </select>
;

var TableHeader = (props) => <thead><tr>{props.content}</tr></thead>;

var SortableTableHeader = (props) =>
  <th className={props.className}>
    <a className="orderBy" onClick={props.orderBy}>{props.content}</a>
  </th>
;

var FilterField = (props) =>
  <input className="form-control table-input-search"
    value={props.defaultValue}
    placeholder={props.placeholder}
    type="text"
    onChange={(e) => props.onChange(e.target.value)}
  />
;

module.exports = {
    Table : Table,
    TableCell : TableCell,
    TableRow : TableRow
}
