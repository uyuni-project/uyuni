"use strict";

const React = require("react")

class Table extends React.Component {
  constructor(props) {
    super(props);
    ["onPageChange", "onRowsPerPageChange"]
    .forEach(method => this[method] = this[method].bind(this));
    this.state = {
        processedData: this.props.data,
        sorting: {
            column: null,
            ascending: true
        },
        paging: {
            rowsPerPage: 5,
            currentPage: 0
        },
        filtering: {
            value: ""
        }
    };
    this.state.processedData = this.processData(
        this.props.data,
        this.props.columns,
        this.state.filtering.value,
        this.state.sorting.column,
        this.state.sorting.ascending
    );
  }

  componentWillReceiveProps(nextProps) {

    const newSortedColumn = this.state.sorting.column === null ? null : nextProps.columns.find(column => {
        return column.header === this.state.sorting.column.header;
    });

    const data = this.processData(
        nextProps.data,
        nextProps.columns,
        this.state.filtering.value,
        newSortedColumn,
        this.state.sorting.ascending
    );
    const lastPage = Math.max(0, Math.ceil(data.length / this.state.paging.rowsPerPage) - 1);
    this.setState({
       processedData: data,
       sorting: {
            column: newSortedColumn === undefined ? null : newSortedColumn,
            ascending: this.state.sorting.ascending
       },
       paging: {
           rowsPerPage: this.state.paging.rowsPerPage,
           currentPage: this.state.paging.currentPage > lastPage ? 0 : this.state.paging.currentPage
       }
    });
  }

  onRowsPerPageChange(rowsPerPage) {
    this.setState({
        paging: {
            rowsPerPage: rowsPerPage,
            currentPage: 0
        }
    });
  }

  onPageChange(page) {
    this.setState({
        paging: {
            rowsPerPage: this.state.paging.rowsPerPage,
            currentPage: page
        }
    });
  }

  onFilterChange(filter) {
    const data = this.processData(
        this.props.data,
        this.props.columns,
        filter,
        this.state.sorting.column,
        this.state.sorting.ascending
    );
    const lastPage = Math.max(0, Math.ceil(data.length / this.state.paging.rowsPerPage) - 1);
    this.setState({
       processedData: data,
       filtering: {
           value: filter
       },
       paging: {
           rowsPerPage: this.state.paging.rowsPerPage,
           currentPage: this.state.paging.currentPage > lastPage ? 0 : this.state.paging.currentPage
       }
    });
  }

  onSortHeaderClicked(column) {
    const ascending = column === this.state.sorting.column ? !this.state.sorting.ascending : true;
    const data = this.processData(
        this.props.data,
        this.props.columns,
        this.state.filtering.value,
        column,
        ascending
    );
    this.setState({
        processedData: data,
        sorting: {
            column: column,
            ascending: ascending
        }
    });
  }

  processData(data, columns, filterValue, sortedColumn, ascending) {
    const filterData = filterValue === undefined ? (x) => x : (x) => {
        const filterAll = (a) => columns
                .filter(c => c.filter !== undefined)
                .find(column => column.filter(column.entryToCell(a), filterValue)) !== undefined;
        return data.filter(filterAll);
    };

    const filteredData = filterData(data);

    const sortedData = sortedColumn === null ? filteredData : filteredData.sort((a, b) => {
        const cellA = sortedColumn.entryToCell(a);
        const cellB = sortedColumn.entryToCell(b);
        return sortedColumn.sort(cellA, cellB) * (ascending ? 1 : -1);
    });

    return sortedData;
  }

  render() {
    const columns = this.props.columns;
    const headers = columns.map( (column, index) => {
        const styles = column.ratio === undefined ? {} : {width: (column.ratio * 100)+"%"}
        if(column.sort !== undefined){
            const classes = this.state.sorting.column !== column ? "" : (this.state.sorting.ascending ? "ascSort" : "descSort")
            return (
              <th className={classes} style={styles} key={column.header}>
                <a className="orderBy" onClick={() => this.onSortHeaderClicked(column)}>{ column.header }</a>
              </th>
            );
        }
        else return <th style={styles} key={column.header}>{ column.header }</th>
    });

    const filterValue = this.state.filtering.value === "" ? undefined : this.state.filtering.value;
    const lastPage = Math.max(0, Math.ceil(this.state.processedData.length / this.state.paging.rowsPerPage) - 1);

    const start = this.state.paging.currentPage * this.state.paging.rowsPerPage;
    const end = Math.min(start + this.state.paging.rowsPerPage, this.state.processedData.length);

    const rows = this.state.processedData.slice(start, end).map((entry, index) => {
        const classes = (index % 2) === 0 ? "list-row-even" : "list-row-odd";
        return (
          <tr className={classes} key={this.props.keyFn(entry)}>{
            columns.map(column => {
                const cell = column.entryToCell(entry);
                const classes = column.className + (column === this.state.sorting.column ? " sortedCol" : "");
                return <td className={classes} key={column.header}>{ column.renderCell(cell, filterValue) }</td>
            })
          }</tr>
        );
    });
    return (
      <div className="spacewalk-list">
        <div className="panel panel-default">
          <div className="panel-heading">
            <div className="spacewalk-list-bottom-addons">
              <input className="form-control table-input-search"
                value={this.state.filtering.value}
                placeholder={"Filter"}
                type="text"
                onChange={(e) => this.onFilterChange(e.target.value)}
              />
              <PaginationBlock
                currentPage={this.state.paging.currentPage}
                lastPage={lastPage}
                onPageChange={this.onPageChange}
              />
            </div>
          </div>
          <div className="table-responsive">
              <table className="table table-striped vertical-middle" style={{tableLayout: "fixed"}}>
                <thead>
                    <tr>{ headers }</tr>
                </thead>
                <tbody className="table-content">
                    { rows }
                </tbody>
              </table>
          </div>
          <div className="panel-footer">
            <div className="spacewalk-list-pagination">
              <div className="spacewalk-list-pagination-btns btn-group">
                  { this.state.processedData.length + " " + (this.state.processedData.length == 1 ? t("Item") : t("Items")) + " " }
                  <select className="display-number"
                    defaultValue={this.state.paging.currentPage}
                    onChange={(e) => this.onRowsPerPageChange(parseInt(e.target.value))}>
                      {[5,10,15,25,50,100,250,500].map((o) => <option value={o} key={o}>{o}</option>)}
                  </select>
                  { " " + t("Items per Page") }
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }
}
Table.propTypes = {
    data: React.PropTypes.array.isRequired,
    keyFn: React.PropTypes.func.isRequired, // entry => key
    columns: React.PropTypes.arrayOf(
        React.PropTypes.shape({
            "header": React.PropTypes.string.isRequired,
            "entryToCell": React.PropTypes.func.isRequired, // function from data entry to column relevant data
            "renderCell": React.PropTypes.func.isRequired, // function from column relevant data to react node
            "sort": React.PropTypes.func, // standard javascript comparator function
            "filter": React.PropTypes.func, // (entry, filterValue) => boolean
            "className": React.PropTypes.string // additive class to the cell <td> or its content
        })
    ).isRequired
};


const PaginationBlock = (props) => {
  const currentPage = props.currentPage;
  const lastPage = props.lastPage;
  const onPageChange = props.onPageChange;

  return (
    <div className="spacewalk-list-pagination">
      <div className="spacewalk-list-pagination-btns btn-group">
          <button className="btn btn-default" disabled={currentPage === 0} onClick={() => onPageChange(0)}>{ t("First") }</button>
          <button className="btn btn-default" disabled={currentPage === 0} onClick={() => onPageChange(currentPage - 1)}>{ t("Prev") }</button>
          <button className="btn btn-default">
                {t("Page {0} of {1}", currentPage + 1, lastPage + 1)}
          </button>
          <button className="btn btn-default" disabled={currentPage === lastPage} onClick={() => onPageChange(currentPage + 1)}>{ t("Next") }</button>
          <button className="btn btn-default" disabled={currentPage === lastPage} onClick={() => onPageChange(lastPage)}>{ t("Last") }</button>
      </div>
    </div>
  );
};

module.exports = {
    Table : Table
}
