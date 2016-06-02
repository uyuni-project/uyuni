"use strict";

const React = require("react");

const PaginationBlock = (props) => {
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

const PaginationButton = (props) =>
  <button type="button" className="btn btn-default"
    disabled={props.disabled} onClick={props.onClick}>
    {props.text}
  </button>
;

const ItemsPerPageSelector = (props) =>
  <select className="display-number"
    defaultValue={props.currentValue}
    onChange={(e) => props.onChange(parseInt(e.target.value))}>
      {[5,10,15,25,50,100,250,500].map((o) => <option value={o} key={o}>{o}</option>)}
  </select>
;


class Table extends React.Component {

  constructor(props) {
    super(props);
    ["_onChangeSearch", "_onSearch", "sort"].forEach(method => this[method] = this[method].bind(this));
    this.state = {search: "", data: props.data, filtered: false};
  }

  _onChangeSearch(event) {
      this.setState({search: event.target.value});
  }

  _onSearch(event) {
    var filteredData = this.props.searchFn(this.state.data, this.state.search);
    this.setState({data: filteredData, filtered: true});
  }

  sort(columnKey, sortFn, sortDirection) {
    var sortedData = sortFn(this.state.data, sortDirection);
    this.setState({data: sortedData, sortKey: columnKey});
  }

  onItemsPerPageChange() {

  }

  onPageChange() {

  }

  render() {
      let headers = [];
      React.Children.forEach(this.props.children,
        (column) => {
          React.Children.forEach(column.props.children, (child) => {
             if (child.type === Header) {
                var head = React.cloneElement(child, {columnKey: column.props.columnKey, table: this});
                headers.push(head);
             }
          });
      });

  	let rows = this.state.data.map((element) => {
  			let cells = React.Children.map(this.props.children,
        	     (column) => React.cloneElement(column, {data: element, table: this})
     		);
    		return <tr>{cells}</tr>;
    });

    var currentPage = 1;
    var itemsPerPage = 10;

    return (
      <div className="spacewalk-list">
        <div className="panel panel-default">
          <div className="panel-heading">
            <div className="spacewalk-list-head-addons">
              <div className="spacewalk-list-filter table-search-wrapper">
                <input type="text" value={this.state.search} onChange={this._onChangeSearch}/>
                <span>{t("Items {0} - {1} of {2}", 1, 1, 1)}</span>
              </div>
              <div className="spacewalk-list-head-addons-extra table-items-per-page-wrapper">
                <ItemsPerPageSelector
                  currentValue={itemsPerPage}
                  onChange={this.onItemsPerPageChange}
                /> {t("items per page")}
              </div>
            </div>
          </div>

        <div>

            <button onClick={this._onSearch}>Search</button>
            <div className="table-responsive">
                <table className="table table-striped">
                   <thead>
                      <tr>{headers}</tr>
                   </thead>
                   <tbody>
                      {rows}
                   </tbody>
                </table>
            </div>
        </div>

        <div className="panel-footer">
            <div className="spacewalk-list-bottom-addons">
              <PaginationBlock
                currentPage={currentPage}
                lastPage={1}
                onPageChange={this.onPageChange}
              />
            </div>
          </div>
        </div>
      </div>
    );
  }

}

class Column extends React.Component {

  render() {
      let children = [];
      React.Children.forEach(this.props.children,
       (child) => {if (child.type !== Header) {
         children.push(child);
       }});

      let widgets = children.map(
        (widget) => React.cloneElement(widget, {data: this.props.data, table: this.props.table})
      );
     return <td>{widgets}</td>;
  }

}

class Header extends React.Component {

  constructor(props) {
    super(props);
    ["_sort"].forEach(method => this[method] = this[method].bind(this));
    this.state = {sortDirection: null};
  }

  _sort() {
    var sortDir = this.state.sortDirection;
    if (!sortDir) {
        sortDir = 1;
    } else {
        sortDir = -sortDir;
    }
    this.props.table.sort(this.props.columnKey, this.props.sortFn, sortDir);
    this.setState({sortDirection: sortDir});
  }

  render() {
     if (this.props.sortFn && this.props.table) {
        var thClass = null;
        if (this.props.table.state.sortKey == this.props.columnKey) {
            thClass = !this.state.sortDirection ? "" : (this.state.sortDirection > 0 ? "ascSort" : "descSort")
        }
        return (<th className={ thClass }>
            <a className="orderBy" onClick={this._sort}>{this.props.children}</a>
        </th>);
     }
     return <th>{this.props.children}</th>;
  }

}


class Button extends React.Component {

  constructor(props) {
    super(props);
    ["trigger"].forEach(method => this[method] = this[method].bind(this));
  }

  trigger() {
  	this.props.handler(this.props.data);
  }

  render() {
     return <button onClick={this.trigger}>{this.props.label}</button>
  }
}

class Output extends React.Component {

  render() {
     let text = "";
     if (typeof this.props.value === "function") {
     	   text = this.props.value(this.props.data, this.props.table);
     } else {
         text = this.props.text;
     }
     return <span>{ text }</span>;
  }

}


/*class SortableHeader extends React.Component {

  constructor(props) {
    super(props);
    this._onSortChange = this._onSortChange.bind(this);
    this.state = {sortDirection: 1};
  }

  _onSortChange() {
     this.props.sortFn(this.state.sortDirection);
     this.setState({sortDirection: -this.sortDirection})
  }

  render() {
     return <a onClick={this._onSortChange}>{this.props.children}</a>;
  }

}*/

module.exports = {
    STable : Table,
    SColumn : Column,
    SHeader : Header,
    SOutput : Output
}












