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

const FilterField = (props) =>
  <input className="form-control table-input-search"
    value={props.defaultValue}
    placeholder={props.placeholder}
    type="text"
    onChange={(e) => props.onChange(e.target.value)}
  />
;

class DataModel {

  constructor(data, itemsPerPage) {
    this.data = data;
    this.itemsPerPage = itemsPerPage;
    this.currentPage = 1;
    this.currentData = data;
    this.filtered = false;
    this.filteredText = null;
  }

  filter(filterFn, criteria) {
    if (!criteria) {
      this.currentData = this.data;
      this.filtered = false;
      this.filteredText = null;
    } else {
      this.currentData = filterFn(this.data, criteria);
      this.filtered = true;
      this.filteredText = criteria;
      this.currentPage = 1;
    }
  }

  sort(columnKey, sortFn, sortDirection) {
    this.currentData = sortFn(this.currentData, sortDirection, columnKey);
  }

  goToPage(page) {
    if (page < 1) {
        this.currentPage = 1;
        return;
    }
    if (page > this.lastPage) {
        this.currentPage = this.lastPage;
        return;
    }
    this.currentPage = page;
  }

  get currentPageData() {
     return this.currentData.slice(this.firstPageItemIndex, this.lastPageItemIndex);
  }

  get lastPage() {
      const lastPage = Math.ceil(this.currentData.length / this.itemsPerPage);
      if (lastPage == 0) {
        return 1;
      }
      return lastPage;
  }

  get firstPageItemIndex() {
    return (this.currentPage - 1) * this.itemsPerPage;
  }

  get lastPageItemIndex() {
    return this.firstPageItemIndex + this.itemsPerPage > this.currentData.length ?
        this.currentData.length : this.firstPageItemIndex + this.itemsPerPage;
  }

  get size() {
    return this.currentData.length;
  }

  changeItemsPerPage(newPageSize) {
    this.goToPage(1);
    this.itemsPerPage = newPageSize;
  }

}


class Table extends React.Component {

  constructor(props) {
    super(props);
    ["onFilterTextChange", "sort", "onPageChange", "onItemsPerPageChange"].forEach(method => this[method] = this[method].bind(this));
    const pageItemsCount = 15;
    this.state = {
        dataModel: new DataModel(props.data, pageItemsCount),
        itemsPerPage: pageItemsCount
    };
  }

  onFilterTextChange(text) {
    this.state.dataModel.filter(this.props.searchFn, text);
    this.forceUpdate();
  }

  sort(columnKey, sortFn, sortDirection) {
    this.state.dataModel.sort(columnKey, sortFn, sortDirection);
    this.setState({sortKey: columnKey});
  }

  onItemsPerPageChange(pageSize) {
    this.state.dataModel.changeItemsPerPage(pageSize);
    this.forceUpdate();
  }

  onPageChange(page) {
    this.state.dataModel.goToPage(page);
    this.forceUpdate();
  }

  componentWillReceiveProps(nextProps) {
    this.setState({
        dataModel: new DataModel(nextProps.data, 15)
    });
  }

  render() {
      let headers = [];
      React.Children.forEach(this.props.children,
        (column) => {
          var hasHeader = false;
          React.Children.forEach(column.props.children, (child) => {
             if (child.type === Header) {
                var head = React.cloneElement(child, {
                    key: headers.length,
                    columnKey: column.props.columnKey,
                    currentSortKey: this.state.sortKey,
                    onSort: this.sort,
                    width: column.props.width
                    });
                headers.push(head);
                hasHeader = true;
             }
          });
          if (!hasHeader) {
               headers.push(<Header key={headers.length}/>);
          }
      });

  	let rows = this.state.dataModel.currentPageData.map((element) => {
  			let cells = React.Children.map(this.props.children,
        	     (column) => React.cloneElement(column, {data: element, table: this})
     		);
    		return <tr key={this.props.rowKeyFn(element)}>{cells}</tr>;
    });

    const filterField = this.props.searchFn ?
      <FilterField key="filteredField"
        onChange={this.onFilterTextChange}
        defaultValue={this.state.dataModel.filterText}
        placeholder={this.props.filterPlaceholder}
      /> :
      null
    ;
    const itemCounter = <span>{t("Items {0} - {1} of {2}", this.state.dataModel.firstPageItemIndex + 1,
    this.state.dataModel.lastPageItemIndex, this.state.dataModel.size)}</span>;

    return (
      <div className="spacewalk-list">
        <div className="panel panel-default">
          <div className="panel-heading">
            <div className="spacewalk-list-head-addons">
              <div className="spacewalk-list-filter table-search-wrapper">
                {filterField} {itemCounter}
              </div>
              <div className="spacewalk-list-head-addons-extra table-items-per-page-wrapper">
                <ItemsPerPageSelector key="itemsPerPageSelector"
                  currentValue={this.state.itemsPerPage}
                  onChange={this.onItemsPerPageChange}
                /> {t("items per page")}
              </div>
            </div>
          </div>
        <div>
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
              <PaginationBlock key="paginationBlock"
                currentPage={this.state.dataModel.currentPage}
                lastPage={this.state.dataModel.lastPage}
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
        (widget) => React.cloneElement(widget, {key: "widget", data: this.props.data, table: this.props.table})
      );
     return <td>{widgets}</td>;
  }

}

class Header extends React.Component {

  constructor(props) {
    super(props);
    ["sort"].forEach(method => this[method] = this[method].bind(this));
    this.state = {sortDirection: null};
  }

  sort() {
    var sortDir = this.state.sortDirection;
    if (!sortDir) {
        sortDir = 1;
    } else {
        sortDir = -sortDir;
    }
    this.props.onSort(this.props.columnKey, this.props.sortFn, sortDir);
    this.setState({sortDirection: sortDir});
  }

  render() {
     var thClass = null;
     var thStyle = null;
     if (this.props.width) {
        thStyle = { width: this.props.width };
     }

     if (this.props.sortFn && this.props.columnKey) {
        if (this.props.currentSortKey == this.props.columnKey) {
            thClass = !this.state.sortDirection ? "" : (this.state.sortDirection > 0 ? "ascSort" : "descSort")
        }
        return (<th style={ thStyle } className={ thClass }>
            <a className="orderBy" onClick={this.sort}>{this.props.children}</a>
        </th>);
     }

     return <th style={ thStyle }>{this.props.children}</th>;
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

class Cell extends React.Component {

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

module.exports = {
    STable : Table,
    SColumn : Column,
    SHeader : Header,
    SCell : Cell
}
