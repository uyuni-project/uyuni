"use strict";

const React = require("react");
const StatePersistedMixin = require("./util").StatePersistedMixin;

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
    const itemCounter = <span>{t("Items {0} - {1} of {2}", props.fromItem,
            props.toItem, props.itemCount)}</span>;

    const children = React.Children.map(props.children,
            (child) => React.cloneElement(child, { criteria: props.criteria, onSearch: props.onSearch }));

    return <div className="spacewalk-list-filter table-search-wrapper">
                {children} {itemCounter}
             </div>
};

const SearchField = React.createClass({
  render: function() {
    return <input className="form-control table-input-search"
      value={this.props.criteria}
      placeholder={this.props.placeholder}
      type="text"
      onChange={(e) => this.props.onSearch(e.target.value)}
    />
  }
});

const Column = React.createClass({
  render: function() {
     let content = null;
     if (typeof this.props.cell === "function") {
        content = this.props.cell(this.props.data, this.props.criteria);
     } else {
        content = this.props.cell;
     }

     return (<td>{content}</td>)
  }
});

const Header = React.createClass({
  render: function() {
     const thStyle = this.props.width ?
       { width: this.props.width } : null;

     if (this.props.comparator) {
        const thClass = this.props.sortDirection == 0 ?
          "" :
          (this.props.sortDirection > 0 ? "ascSort" : "descSort");

        const newDirection = this.props.sortDirection == 0 ?
          1 :
          this.props.sortDirection * -1;

        return (<th style={ thStyle } className={ thClass }>
            <a className="orderBy"
              onClick={() => this.props.onSortChange(this.props.columnKey, newDirection)}>
              {this.props.children}
            </a>
        </th>);
     }

     return <th style={ thStyle }>{this.props.children}</th>;
  }
});


const Button = React.createClass({
  trigger: function() {
  	this.props.handler(this.props.data);
  },

  render: function() {
     return <button onClick={this.trigger}>{this.props.label}</button>
  }
});

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

const Table = React.createClass({
  mixins: [StatePersistedMixin],

  getInitialState: function() {
    return {
      currentPage: 1,
      itemsPerPage: this.props.initalItemsPerPage || 15,
      criteria: null,
      sortColumnKey: this.props.initialSortColumnKey || null,
      sortDirection: 1
    };
  },

  componentWillReceiveProps: function(nextProps) {
    this.onPageCountChange(nextProps.data, this.state.criteria, this.state.itemsPerPage);
  },

  getLastPage: function(data, criteria, itemsPerPage) {
    const rowCount = data.filter(this.getFilter(criteria)).length;

    const lastPage = Math.ceil(rowCount / itemsPerPage);
    return lastPage > 0 ? lastPage : 1;
  },

  getFilter: function(criteria) {
    const searchField = this.props.searchField;
    if (searchField) {
      const filter = searchField.props.filter;
      if (filter) {
        return ((datum) => filter(datum, criteria));
      }
    }
    return (datum) => true;
  },

  getProcessedData: function() {
    const comparators = React.Children.toArray(this.props.children)
      .filter((child) => child.type === Column)
      .filter((column) => column.props.columnKey == this.state.sortColumnKey)
      .map((column) => column.props.comparator);

    const comparator = comparators.length > 0 ?
      comparators[0] : ((a, b) => 0);

    return this.props.data
        .filter(this.getFilter(this.state.criteria))
        .sort((a, b) => comparator(a, b, this.state.sortColumnKey, this.state.sortDirection));
  },

  onSearch: function(criteria) {
    this.setState({criteria: criteria});
    this.onPageCountChange(this.props.data, criteria, this.state.itemsPerPage);
  },

  onItemsPerPageChange: function(itemsPerPage) {
    this.setState({itemsPerPage: itemsPerPage});
    this.onPageCountChange(this.props.data, this.state.criteria, itemsPerPage);
  },

  onPageCountChange: function(data, criteria, itemsPerPage) {
    const lastPage = this.getLastPage(data, criteria, itemsPerPage);
    if (this.state.currentPage > lastPage) {
      this.setState({currentPage: lastPage});
    }
  },

  onPageChange: function(page) {
    this.setState({currentPage: page});
  },

  onSortChange: function(sortColumnKey, sortDirection) {
    this.setState({
      sortColumnKey: sortColumnKey,
      sortDirection: sortDirection
    });
  },

  render: function() {
    const headers = React.Children.toArray(this.props.children)
        .filter((child) => child.type === Column)
        .map((column, index) => {
            if (column.props.header) {
                const sortDirection = column.props.columnKey == this.state.sortColumnKey ?
                  this.state.sortDirection :
                  0;
                return <Header
                    key={index}
                    columnKey={column.props.columnKey}
                    sortDirection={sortDirection}
                    onSortChange={this.onSortChange}
                    width={column.props.width}
                    comparator={column.props.comparator}>
                        {column.props.header}
                    </Header>;
            } else {
                return <Header key={index}/>;
            }
        });

    const rows = this.getProcessedData().map((datum, index) => {
        let cells = React.Children.toArray(this.props.children)
            .filter((child) => child.type === Column)
            .map((column) => React.cloneElement(column, {data: datum, criteria: this.state.criteria})
        );

        let rowClass = this.props.cssClassFunction ? this.props.cssClassFunction(datum, index) : "";
        let evenOddClass = (index % 2) === 0 ? "list-row-even" : "list-row-odd";
        return <tr className={rowClass + " " + evenOddClass} key={this.props.identifier(datum)} >{cells}</tr>;
    });

    const itemsPerPage = this.state.itemsPerPage;
    const currentPage = this.state.currentPage;
    const firstItemIndex = (currentPage - 1) * itemsPerPage;

    const itemCount = rows.length;
    const fromItem = itemCount > 0 ? firstItemIndex + 1 : 0;
    const toItem = firstItemIndex + itemsPerPage <= itemCount ? firstItemIndex + itemsPerPage : itemCount;

    return (
      <div className="spacewalk-list">
        <div className="panel panel-default">
          <div className="panel-heading">
            <div className="spacewalk-list-head-addons">
            <SearchPanel
              fromItem={fromItem}
              toItem={toItem}
              itemCount={itemCount}
              criteria={this.state.criteria}
              onSearch={this.onSearch}
            >{this.props.searchField}
            </SearchPanel>
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
                <table className="table table-striped vertical-middle">
                   <thead>
                      <tr>{headers}</tr>
                   </thead>
                   <tbody>
                      {rows.slice(firstItemIndex, firstItemIndex + itemsPerPage)}
                   </tbody>
                </table>
            </div>
        </div>

        <div className="panel-footer">
            <div className="spacewalk-list-bottom-addons">
              <PaginationBlock key="paginationBlock"
                currentPage={this.state.currentPage}
                lastPage={this.getLastPage(this.props.data, this.state.criteria, this.state.itemsPerPage)}
                onPageChange={this.onPageChange}
              />
            </div>
          </div>
        </div>
      </div>
    );
  }
});

module.exports = {
    Table : Table,
    Column : Column,
    SearchField: SearchField,
    Highlight: Highlight
}
