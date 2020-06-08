// @flow
import * as React from 'react';
import { StatePersistedContext } from '../utils/StatePersistedContext';
import { ItemsPerPageSelector } from '../pagination';
import { PaginationBlock } from '../pagination';
import { SearchPanel } from './SearchPanel';
import { SearchField } from './SearchField';
import { Header } from './Header';

type ChildrenArgsProps = {
  currItems: Array<any>,
  headers: React.Node,
  handleSelect: Function,
  selectable: boolean,
  selectedItems: Array<any>,
  criteria?: string,
}

type Props = {
  columns: Array<React.ElementRef<any>>,
  /** any type of data in an array, where each element is a row data */
  data: Array<any>,
  /** Function extracting the unique key of the row from the data object */
  identifier: Function,
  /** the column key name of the initial sorted column */
  initialSortColumnKey?: string,
  /** 1 for ascending, -1 for descending */
  initialSortDirection?: number,
  /** a function that return a css class for each row */
  cssClassFunction?: Function,
  /** the React Object that contains the filter search field */
  searchField?: React.Element<typeof SearchField>,
  /** the initial number of how many row-per-page to show. If it's 0 table header and footer are hidden */
  initialItemsPerPage?: number,
  /** enables item selection */
  selectable: boolean,
  /** the handler to call when the table selection is updated. If not provided, the select boxes won't be rendered */
  onSelect?: (items: Array<any>) => void,
  /** the identifiers for selected items */
  selectedItems?: Array<any>,
  /** The message which is shown when there are no rows to display */
  emptyText?: string,
  /** if data is loading */
  loading?: boolean,
  /** The message which is shown when the data is loading */
  loadingText?: string,
  /** Children node in the table */
  children: (args: ChildrenArgsProps) => React.Node,
  /** Other filter fields */
  additionalFilters?: Array<React.Node>,
};

type State = {
  currentPage: number,
  itemsPerPage: number,
  criteria?: string,
  sortColumnKey: string | null,
  sortDirection: number,
  selectedItems: Array<any>,
  selectable: boolean,
  loading: boolean,
};

export class TableDataHandler extends React.Component<Props, State> {
  static defaultProps = {
    selectable: false,
    columns: [],
  };

  static contextType = StatePersistedContext;

  constructor(props: Props) {
    super(props);
    this.state = {
      currentPage: 1,
      itemsPerPage: this.props.initialItemsPerPage || 15,
      criteria: undefined,
      sortColumnKey: this.props.initialSortColumnKey || null,
      sortDirection: this.props.initialSortDirection || 1,
      selectedItems: this.props.selectedItems || [],
      selectable: this.props.selectable,
      loading: this.props.loading || false,
    };
  };

  UNSAFE_componentWillReceiveProps(nextProps: Props) {
    this.onPageCountChange(nextProps.data, this.state.criteria, this.state.itemsPerPage);
    this.setState({
        selectedItems: nextProps.selectedItems || [],
        selectable: Boolean(nextProps.selectable),
        loading: Boolean(nextProps.loading) || false,
    });
  };

  componentWillUnmount() {
    if (this.context && this.context.saveState) {
      this.context.saveState(this.state);
    }
  };

  UNSAFE_componentWillMount() {
    if (this.context && this.context.loadState) {
      if (this.context.loadState()) {
        this.setState(this.context.loadState());
      }
    }
  };

  getLastPage = (data: any, criteria?: string, itemsPerPage: number) : number => {
    const rowCount = data.filter(this.getFilter(criteria)).length;

    const lastPage = Math.ceil(rowCount / itemsPerPage);
    return lastPage > 0 ? lastPage : 1;
  };

  getFilter = (criteria?: string): (datum: any) => boolean => {
    const searchField = this.props.searchField;
    if (searchField) {
      const filter = searchField.props.filter;
      if (filter) {
        return ((datum) => filter(datum, criteria));
      }
    }
    return (datum) => true;
  };

  getProcessedData = (): Array<any> => {
    const comparators = this.props.columns 
      .filter((column) => column.props.columnKey === this.state.sortColumnKey)
      .map((column) => column.props.comparator);

    const comparator = comparators.length > 0 ?
      comparators[0] : ((a, b, columnKey, sortDirection) => 0);

    return this.props.data
        .filter(this.getFilter(this.state.criteria))
        .sort((a, b) => comparator(a, b, this.state.sortColumnKey, this.state.sortDirection));
  };

  onSearch = (criteria?: string): void => {
    this.setState({criteria: criteria});
    this.onPageCountChange(this.props.data, criteria, this.state.itemsPerPage);
  };

  onItemsPerPageChange = (itemsPerPage: number): void => {
    this.setState({itemsPerPage: itemsPerPage});
    this.onPageCountChange(this.props.data, this.state.criteria, itemsPerPage);
  };

  onPageCountChange = (data: Array<any>, criteria?: string, itemsPerPage: number): void => {
    const lastPage = this.getLastPage(data, criteria, itemsPerPage);
    if (this.state.currentPage > lastPage) {
      this.setState({currentPage: lastPage});
    }
  };

  onPageChange = (page: number): void => {
    this.setState({currentPage: page});
  };

  onSortChange = (sortColumnKey?: string, sortDirection: number): void => {
    this.setState({
      sortColumnKey: sortColumnKey,
      sortDirection: sortDirection
    });
  };

  setSelection = (selection: any): void => {
    if (this.props.onSelect) {
      this.props.onSelect(selection);
    }
  };

  render() {
    const headers = this.props.columns 
      .map((column, index) => {
          if (column.props.header) {
              const sortDirection = column.props.columnKey === this.state.sortColumnKey ?
                this.state.sortDirection :
                0;
              return <Header
                  key={index}
                  columnKey={column.props.columnKey}
                  sortDirection={sortDirection}
                  onSortChange={this.onSortChange.bind(this)}
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
    const isEmpty = itemCount === 0;

    if (this.state.selectable) {
      const currIds = currItems.map(item => this.props.identifier(item));

      const handleSelectAll = (sel) => {
          let arr = this.state.selectedItems;
          if (sel) {
              arr = arr.concat(currIds.filter(id => !arr.includes(id)));
          } else {
              arr = arr.filter(id => !currIds.includes(id));
          }
          this.setSelection(arr);
      };

        const allSelected = currIds.length > 0 && currIds.every(id => this.state.selectedItems.includes(id));
        const checkbox = <Header key="check"><input type="checkbox" checked={allSelected} onChange={(e) => handleSelectAll(e.target.checked)}/></Header>;
        headers.unshift(checkbox);
    }
    
    const handleSelect = (id, sel) => {
        let arr = this.state.selectedItems;
        if (sel) {
            arr = arr.concat([id]);
        } else {
            arr = arr.filter(i => i !== id);
        }
        this.setSelection(arr);
    };

    const handleSearchPanelClear = () => {
        this.setSelection([]);
    }

    const handleSearchPanelSelectAll = () => {
        const selected = this.state.selectedItems;
        this.setSelection(selected.concat(
            filteredData.map(d => this.props.identifier(d))
                .filter(id => !selected.includes(id))));
    }

    const emptyText = this.props.emptyText || t('There are no entries to show.');
    const loadingText = this.props.loadingText || t('Loading...');

    return (
      <div className="spacewalk-list">
        <div className="panel panel-default">
          { this.props.initialItemsPerPage !== 0 ?
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
              {
                this.props.additionalFilters && this.props.additionalFilters.map((filter, i) => <span key={'additional-filter-' + i}>{filter}&nbsp;</span>)
              }
            </SearchPanel>
              <div className="spacewalk-list-head-addons-extra table-items-per-page-wrapper">
                <ItemsPerPageSelector key="itemsPerPageSelector"
                  currentValue={this.state.itemsPerPage}
                  onChange={this.onItemsPerPageChange}
                /> {t("items per page")}
              </div>
            </div>
          </div> : null }
          { this.state.loading ?
            <div className="panel-body text-center">
              <i className='fa fa-spinner fa-spin fa-1-5x'></i>
              <h4>{loadingText}</h4>
            </div>
            :
            (
              isEmpty ?
              <div className="panel-body">
                <div className="subheadline">{emptyText}</div>
              </div>
              :
              <div>
                <div className="table-responsive">
                  {this.props.children({
                    currItems,
                    headers,
                    handleSelect,
                    selectable: this.state.selectable,
                    selectedItems: this.state.selectedItems,
                    criteria: this.state.criteria,
                  })}
                </div>
              </div>
            )
          }
          { this.props.initialItemsPerPage !== 0 ?
          <div className="panel-footer">
            <div className="spacewalk-list-bottom-addons">
              <PaginationBlock key="paginationBlock"
                currentPage={this.state.currentPage}
                lastPage={this.getLastPage(this.props.data, this.state.criteria, this.state.itemsPerPage)}
                onPageChange={this.onPageChange}
              />
            </div>
          </div> : null }
        </div>
      </div>
    );
  }
};
