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

const SearchPanel = (props) => {
    let itemCounter = null;
    let children = null;
    if (props.table) {
        const dataModel = props.table.state.dataModel;
        itemCounter = <span>{t("Items {0} - {1} of {2}", dataModel.getFirstPageItemIndex() + 1,
            dataModel.getLastPageItemIndex(), dataModel.getSize())}</span>;

        children = React.Children.map(props.children,
            (child) => React.cloneElement(child, { table: props.table }));
    }
    return <div className="spacewalk-list-filter table-search-wrapper">
                {children} {itemCounter}
             </div>
};

class SearchField extends React.Component {

  constructor(props) {
    super(props);
    ["onChange"].forEach(method => this[method] = this[method].bind(this));
  }

  onChange(text) {
    this.props.table.onSearch(this.props.filter, text);
  }

  render() {
      return <input className="form-control table-input-search"
        value={this.props.table.state.dataModel.criteria}
        placeholder={this.props.placeholder}
        type="text"
        onChange={(e) => this.onChange(e.target.value)}
      />
  }

}

class SimpleTableDataModel {

  constructor(data, itemsPerPage) {
    this.data = data;
    this.itemsPerPage = itemsPerPage ? itemsPerPage : 15;
    this.currentPage = 1;
    this.currentData = data;
    this.filtered = false;
    this.criteria = null;
    this.comparator = null;
    this.sortDirection = null;
    this.sortColumnKey = null;
    this.initialized = false;
    ["getCriteria", "filter", "sort", "goToPage", "getCurrentPageData", "getLastPage", "getFirstPageItemIndex",
    "getLastPageItemIndex", "getSize", "changeItemsPerPage", "getItemsPerPage", "getCurrentPage", "setItemsPerPage",
    "mergeData", "isFiltered"]
        .forEach(method => this[method] = this[method].bind(this));
  }

  getCriteria() {
    return this.criteria;
  }

  filter(filterFn, criteria) {
    if (!criteria) {
      this.currentData = this.data;
      this.filterFn = null;
      this.criteria = null;
    } else {
      this.currentData = filterFn(this.data, criteria);
      this.filterFn = filterFn;
      this.criteria = criteria;
      this.currentPage = 1;
    }
  }

  sort(columnKey, comparator, sortDirection) {
    this.currentData = this.currentData.sort((a, b) => sortDirection * comparator(a, b, columnKey));
    this.comparator = comparator;
    this.sortDirection = sortDirection;
    this.sortColumnKey = columnKey;
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

  getCurrentPageData() {
     return this.currentData.slice(this.getFirstPageItemIndex(), this.getLastPageItemIndex());
  }

  getLastPage() {
      const lastPage = Math.ceil(this.currentData.length / this.itemsPerPage);
      if (lastPage == 0) {
        return 1;
      }
      return lastPage;
  }

  getFirstPageItemIndex() {
    return (this.currentPage - 1) * this.itemsPerPage;
  }

  getLastPageItemIndex() {
    return this.getFirstPageItemIndex() + this.itemsPerPage > this.currentData.length ?
        this.currentData.length : this.getFirstPageItemIndex() + this.itemsPerPage;
  }

  getSize() {
    return this.currentData.length;
  }

  changeItemsPerPage(newPageSize) {
    this.goToPage(1);
    this.itemsPerPage = newPageSize;
  }

  setItemsPerPage(newPageSize) {
      this.itemsPerPage = newPageSize;
  }

  getItemsPerPage() {
    return this.itemsPerPage;
  }

  getCurrentPage() {
    return this.currentPage;
  }

  isFiltered() {
    return this.filterFn ? true : false;
  }

  mergeData(dataToMerge) {
    this.data = dataToMerge;
    if (this.filterFn) {
        this.currentData = this.filterFn(this.data, this.criteria);
    } else {
        this.currentData = dataToMerge;
    }
    if (this.comparator && this.sortDirection && this.sortColumnKey) {
        this.sort(this.sortColumnKey, this.comparator, this.sortDirection);
    }
    if (this.getFirstPageItemIndex() > this.currentData.length) {
        this.goToPage(1);
    }
  }

}

class Column extends React.Component {

  render() {
     let content = null;
     if (typeof this.props.cell === "function") {
        content = this.props.cell(this.props.data, this.props.table);
     } else {
        content = this.props.cell;
     }

     return (<td>{content}</td>)
  }

}

class Header extends React.Component {

  constructor(props) {
    super(props);
    ["sort"].forEach(method => this[method] = this[method].bind(this));
    this.state = {sortDirection: this.props.currentSortDirection};
  }

  sort() {
    var sortDir = this.state.sortDirection;
    if (!sortDir) {
        sortDir = 1;
    } else {
        sortDir = -sortDir;
    }
    this.props.onSort(this.props.columnKey, this.props.comparator, sortDir);
    this.setState({sortDirection: sortDir});
  }

  render() {
     var thClass = null;
     var thStyle = null;
     if (this.props.width) {
        thStyle = { width: this.props.width };
     }

     if (this.props.comparator && this.props.columnKey) {
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

function Highlight(props) {
  let text = props.text;
  let high = props.highlight;

  if (!props.enabled) {
    return <span key="hl">{text}</span>
  }

  let pos = text.toLocaleLowerCase().indexOf(high.toLocaleLowerCase());
  if (pos < 0) {
    return <span key="hl">{text}</span>
  }

  let chunk1 = text.substring(0, pos);
  let chunk2 = text.substring(pos, pos + high.length);
  let chunk3 = text.substring(pos + high.length, text.length);

  chunk1 = chunk1 ? <span key="m1">{chunk1}</span> : null;
  chunk2 = chunk2 ? <span key="m2" style={{borderRadius: "2px"}}><mark>{ chunk2 }</mark></span> : null;
  chunk3 = chunk3 ? <span key="m3">{chunk3}</span> : null;

  return <span key="hl">{chunk1}{chunk2}{chunk3}</span>;
}


class Table extends React.Component {

  constructor(props) {
    super(props);
    ["onSearch", "sort", "onPageChange", "onItemsPerPageChange", "initialDataModel", "newDataModel"].forEach(method => this[method] = this[method].bind(this));

    this.state = {
        dataModel: this.initialDataModel(props)
    };
  }

  initialDataModel(props) {
    const {data, pageSize, dataModel} = props;
    if (dataModel) {
        // found an external dataModel in props
        if (!dataModel.initialized) {
            this.doInitialSort(dataModel, props);
            dataModel.initialized = true;
        }
        return dataModel;
    }

    // dataModel is internal, go ahead and create an initial one
    return this.newDataModel(props, data, pageSize);
  }

  doInitialSort(dataModel, props) {
    if (props.initialSort) {
        let comparator = React.Children.toArray(props.children)
                .filter((child) => child.type === Column)
                .filter((column) => column.props.columnKey == props.initialSort)
                .map((column) => column.props.comparator);
        if (comparator && comparator.length > 0 && comparator[0]) {
            dataModel.sort(props.initialSort, comparator[0], 1);
        }
    }
  }

  newDataModel(props, data, pageSize) {
    const newDataModel =  new SimpleTableDataModel(data, pageSize);
    this.doInitialSort(newDataModel, props);
    newDataModel.initialized = true;
    return newDataModel;
  }

  onSearch(filter, criteria) {
    this.state.dataModel.filter(filter, criteria);
    this.forceUpdate();
  }

  sort(columnKey, comparator, sortDirection) {
    this.state.dataModel.sort(columnKey, comparator, sortDirection);
    this.forceUpdate();
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
    if (this.props.data != nextProps.data) {
        this.state.dataModel.mergeData(nextProps.data);
        this.forceUpdate();
    }
    else if (this.props.dataModel != nextProps.dataModel) {
        if (!nextProps.dataModel.initialized) {
            this.doInitialSort(nextProps.dataModel, nextProps);
            nextProps.dataModel.initialized = true;
        }
        this.setState({
            dataModel: nextProps.dataModel
        });
    }
  }

  componentWillMount() {
    if (this.props.loadState) {
      if (this.props.loadState()) {
        this.state = this.props.loadState();
      }
    }
  }

  componentWillUnmount() {
    if (this.props.saveState) {
      this.props.saveState(this.state);
    }
  }

  render() {
    let headers = React.Children.toArray(this.props.children)
        .filter((child) => child.type === Column)
        .map((column, index) => {
            if (column.props.header) {
                return <Header
                    key={index}
                    columnKey={column.props.columnKey}
                    currentSortKey={this.state.dataModel.sortColumnKey}
                    currentSortDirection={this.state.dataModel.sortDirection}
                    onSort={this.sort}
                    width={column.props.width}
                    comparator={column.props.comparator}>
                        {column.props.header}
                    </Header>;
            } else {
                return <Header key={index}/>;
            }
        });

  	let rows = this.state.dataModel.getCurrentPageData().map((element, index) => {
        let cells = React.Children.toArray(this.props.children)
            .filter((child) => child.type === Column)
            .map((column) => React.cloneElement(column, {data: element, table: this})
        );

        let rowClass = this.props.cssClassFunction ? this.props.cssClassFunction(element, index) : "";
        let evenOddClass = (index % 2) === 0 ? "list-row-even" : "list-row-odd";
        return <tr className={rowClass + " " + evenOddClass} key={this.props.identifier(element)} >{cells}</tr>;
    });

    let searchPanel = this.props.searchPanel ?
        <SearchPanel table={this}>
            {this.props.searchPanel}
        </SearchPanel>
        : <SearchPanel table={this}/>;

    return (
      <div className="spacewalk-list">
        <div className="panel panel-default">
          <div className="panel-heading">
            <div className="spacewalk-list-head-addons">
              {searchPanel}
              <div className="spacewalk-list-head-addons-extra table-items-per-page-wrapper">
                <ItemsPerPageSelector key="itemsPerPageSelector"
                  currentValue={this.state.dataModel.getItemsPerPage()}
                  onChange={this.onItemsPerPageChange}
                /> {t("items per page")}
              </div>
            </div>
          </div>
        <div>
            <div className="table-responsive">
                <table className="table table-striped vertical-middle">
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
                currentPage={this.state.dataModel.getCurrentPage()}
                lastPage={this.state.dataModel.getLastPage()}
                onPageChange={this.onPageChange}
              />
            </div>
          </div>
        </div>
      </div>
    );
  }

}

Table.defaultProps = { pageSize : 15 };


module.exports = {
    Table : Table,
    Column : Column,
    SearchField: SearchField,
    Highlight: Highlight,
    SimpleTableDataModel: SimpleTableDataModel
}
