// @flow

const PropTypes = require('prop-types');
const React = require("react");
const createReactClass = require('create-react-class');
const {StatePersistedMixin} = require("./utils/StatePersistedMixin");
const {PaginationBlock, ItemsPerPageSelector} = require('./pagination');

type SearchPanelProps = {
  /** number representing the number of the first displayed item */
  fromItem: number,
  /** number representing the number of the last displayed item */
  toItem: number,
  /** total number of filtered items */
  itemCount: number,
  /** number of selected items */
  selectedCount: number,
  /** flag indicating whether to show the number of selected items */
  selectable?: boolean,
  /** function called when a search is performed. Takes the criteria as single parameter */
  onSearch: (string) => void,
  /** function called when the clear button is clicked. This should reset the selection. */
  onClear: () => void,
  /** function called when the Select All button is clicked. Should set the selection */
  onSelectAll: () => void,
  /** Search criteria value */
  criteria?: string,
  /** Search field components */
  children?: React.Node,
}

/** Panel containing the search fields for a table */
function SearchPanel(props: SearchPanelProps) {
  return (
    <div className="spacewalk-list-filter table-search-wrapper">
      {
        React.Children.map(props.children,
          (child) => React.cloneElement(child, { criteria: props.criteria, onSearch: props.onSearch }))
      }
      <div className="d-inline-block">
        <span>{t("Items {0} - {1} of {2}", props.fromItem, props.toItem, props.itemCount)}&nbsp;&nbsp;</span>
        { props.selectable && props.selectedCount > 0 &&
            <span>
                {t("({0} selected)", props.selectedCount)}&nbsp;
                <a href="#"onClick={props.onClear}>{t("Clear")}</a>
                &nbsp;/&nbsp;
            </span>
        }
        { props.selectable &&
            <a href="#" onClick={props.onSelectAll}>{t("Select All")}</a>
        }
      </div>
    </div>
  );
};

SearchPanel.defaultProps = {
  selectable: false,
  selectedCount: 0,
}

type SearchFieldProps = {
  /** Search criteria value */
  criteria?: string,
  /** Place holder value to display when nothing has been input */
  placeholder: string,
  /** function called when a search is performed.
   * This is usually passed by the search panel parent component.
   */
  onSearch: (string) => void,
}

/** Text input search field */
function SearchField(props: SearchFieldProps) {
  return (
    <input className="form-control table-input-search"
      value={props.criteria}
      placeholder={props.placeholder}
      type="text"
      onChange={(e) => props.onSearch(e.target.value)}
    />
  );
};

type ColumnProps = {
  /** key differenciating a column from its sibblings */
  key?: string,
  /** Content of the cell or function to compute it from the row data */
  cell: React.Node | (data: any, criteria?: string) => React.Node,
  /** Title of the row */
  header?: React.Node,
  /** CSS value for the column width */
  width?: string,
  /** key used to identify the column */
  columnKey?: string,
  /** Row comparison function. See sortBy functions in utils/functions.js */
  comparator?: (a: Object, b: Object, columnKey?: string, sortDirection?: number) => number,
  /** class name to use for the header cell */
  headerClass?: string,
  /** class name to use for the cell */
  columnClass?: string,
  /** the data associated with the row */
  data?: any,
  /** search criteria value */
  criteria?: string,
};

/** Represents a column in the table.
 * This component is also used internally to reprent each cell
 */
function Column(props: ColumnProps) {
  let content = null;
  if (typeof props.cell === "function") {
    content = props.cell(props.data, props.criteria);
  } else {
    content = props.cell;
  }
  return <td className={props.columnClass}>{content}</td>;
}
Column.defaultProps = {
  header: undefined,
  comparator: undefined,
  columnClass: undefined,
  data: undefined,
  criteria: undefined,
};

const Header = (props) => {
  const thStyle = props.width ? { width: props.width } : null;

  let thClass = props.className || "";

  if (props.comparator) {
    thClass += (thClass ? " " : "") + (props.sortDirection == 0 ? "" : (props.sortDirection > 0 ? "ascSort" : "descSort"));
    const newDirection = props.sortDirection == 0 ? 1 : props.sortDirection * -1;

    return (
      <th style={ thStyle } className={ thClass }>
        <a href="#" className="orderBy"
            onClick={() => props.onSortChange(props.columnKey, newDirection)}>
          {props.children}
        </a>
      </th>
    );
  }
  return <th style={ thStyle } className={ thClass }>{props.children}</th>;
}

const Highlight = (props) => {
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

const Table = createReactClass({
  displayName: 'Table',
  mixins: [StatePersistedMixin],

  propTypes: {
    data: PropTypes.arrayOf(PropTypes.any).isRequired, // any type of data in and array, where each element is a row data
    identifier: PropTypes.func.isRequired, // the unique key of the row
    initialSortColumnKey: PropTypes.string, // the column key name of the initial sorted column
    initialSortDirection: PropTypes.number, // 1 for ascending, -1 for descending
    cssClassFunction: PropTypes.func, // a function that return a css class for each row
    searchField: PropTypes.node, // the React Object that contains the filter search field
    initialItemsPerPage: PropTypes.number, // the initial number of how many row-per-page to show
    selectable: PropTypes.bool, // enables item selection
    onSelect: PropTypes.func, // the handler to call when the table selection is updated. if this function is not provided, the select boxes won't be rendered
    selectedItems: PropTypes.array, // the identifiers for selected items
    emptyText: PropTypes.string, // The message which is shown when there are no rows to display
    loading: PropTypes.bool, // if data is loading
    loadingText: PropTypes.string, // The message which is shown when the data is loading
  },

  defaultEmptyText: t('There are no entries to show.'),
  defaultLoadingText: t('Loading..'),

  getInitialState: function() {
    return {
      currentPage: 1,
      itemsPerPage: this.props.initialItemsPerPage || 15,
      criteria: null,
      sortColumnKey: this.props.initialSortColumnKey || null,
      sortDirection: this.props.initialSortDirection || 1,
      selectedItems: this.props.selectedItems || [],
      selectable: this.props.selectable,
      loading: this.props.loading || false,
    };
  },

  UNSAFE_componentWillReceiveProps: function(nextProps) {
    this.onPageCountChange(nextProps.data, this.state.criteria, this.state.itemsPerPage);
    this.setState({
        selectedItems: nextProps.selectedItems || [],
        selectable: Boolean(nextProps.selectable),
        loading: Boolean(nextProps.loading) || false,
    });
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
      .filter((child) => child.type === <Column />.type)
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

  setSelection: function(selection) {
    if(this.props.onSelect) {
      this.props.onSelect(selection);
    }
  },

  render: function() {
    const headers = React.Children.toArray(this.props.children)
        .filter((child) => child.type === <Column />.type)
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
                    comparator={column.props.comparator}
                    className={column.props.headerClass}>
                        {column.props.header}
                    </Header>;
            } else {
                return <Header key={index} className={column.props.headerClass}/>;
            }
        });

    const filteredData = this.getProcessedData();

    const itemsPerPage = this.state.itemsPerPage;
    const currentPage = this.state.currentPage;
    const firstItemIndex = (currentPage - 1) * itemsPerPage;

    const itemCount = filteredData.length;
    const fromItem = itemCount > 0 ? firstItemIndex + 1 : 0;
    const toItem = firstItemIndex + itemsPerPage <= itemCount ? firstItemIndex + itemsPerPage : itemCount;
    const currItems = filteredData.slice(firstItemIndex, firstItemIndex + itemsPerPage);
    const currIds = currItems.map(item => this.props.identifier(item));
    const isEmpty = itemCount === 0;

    const handleSelectAll = (sel) => {
        let arr = this.state.selectedItems;
        if(sel) {
            arr = arr.concat(currIds.filter(id => !arr.includes(id)));
        } else {
            arr = arr.filter(id => !currIds.includes(id));
        }
        this.setSelection(arr);
    };

    if(this.state.selectable) {
        const allSelected = currIds.length > 0 && currIds.every(id => this.state.selectedItems.includes(id));
        const checkbox = <Header key="check"><input type="checkbox" checked={allSelected} onChange={(e) => handleSelectAll(e.target.checked)}/></Header>;
        headers.unshift(checkbox);
    }

    const handleSelect = (id, sel) => {
        let arr = this.state.selectedItems;
        if(sel) {
            arr = arr.concat([id]);
        } else {
            arr = arr.filter(i => i !== id);
        }
        this.setSelection(arr);
    };

    const rows = currItems.map((datum, index) => {
        const cells = React.Children.toArray(this.props.children)
          .filter((child) => child.type === <Column />.type)
          .map((column) => React.cloneElement(column, {data: datum, criteria: this.state.criteria})
        );

        if(this.state.selectable) {
          const checkbox = <Column key="check" cell={
            <input type="checkbox"
                checked={this.state.selectedItems.includes(this.props.identifier(datum))}
                onChange={(e) => handleSelect(this.props.identifier(datum), e.target.checked)}
            />
          }/>;
          cells.unshift(checkbox);
        }

        let rowClass = this.props.cssClassFunction ? this.props.cssClassFunction(datum, index) : "";
        let evenOddClass = (index % 2) === 0 ? "list-row-even" : "list-row-odd";
        return <tr className={rowClass + " " + evenOddClass} key={this.props.identifier(datum)} >{cells}</tr>;
    });

    const handleSearchPanelClear = () => {
        this.setSelection([]);
    }

    const handleSearchPanelSelectAll = () => {
        const selected = this.state.selectedItems;
        this.setSelection(selected.concat(
            filteredData.map(d => this.props.identifier(d))
                .filter(id => !selected.includes(id))));
    }

    const emptyText = this.props.emptyText || this.defaultEmptyText;
    const loadingText= this.props.loadingText || this.defaultLoadingText;

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
              onClear={handleSearchPanelClear}
              onSelectAll={handleSearchPanelSelectAll}
              selectedCount={this.state.selectedItems.length}
              selectable={this.state.selectable}
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
          { this.state.loading ?
            <div className="panel-body text-center">
              <i className='fa fa-spinner fa-spin fa-1-5x'></i>
              <h4>{loadingText}</h4>
            </div>
            :
            (
              isEmpty ?
              <div className="panel-body">
                <h4>{emptyText}</h4>
              </div>
              :
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
            )
          }
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
  },
});

module.exports = {
    Table : Table,
    Column : Column,
    SearchField: SearchField,
    Highlight: Highlight
}
