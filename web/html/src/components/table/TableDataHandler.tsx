import * as React from "react";

import { Header } from "./Header";
import { ItemsPerPageSelector, PaginationBlock } from "../pagination";
import { SearchPanel } from "./SearchPanel";
import { SearchField } from "./SearchField";
import { PageControl, SimpleDataProvider, AsyncDataProvider } from "utils/data-providers";
import { Utils } from "utils/functions";

import { PagedData, Comparator } from "utils/data-providers";

type ChildrenArgsProps = {
  currItems: Array<any>;
  headers: React.ReactNode;
  handleSelect: Function;
  selectable: boolean;
  selectedItems: Array<any>;
  deletable?: boolean | ((row: any) => boolean);
  criteria?: string;
};

type Props = {
  columns: Array<React.ReactElement<any>>;

  /**
   * Either an array of data items of any type where each element is a row data,
   * or a URI string to a resource endpoint that returns a paged list of data items.
   *
   * The data returned from the endpoint must be in paginated form as the following:
   * ```
   * {
   *    items: [..],
   *    total: TOTAL_ITEMS
   * }
   * ```
   *
   * See: utils/data-providers/paged-data-endpoint.js for async usage
   */
  data: Array<any> | string;

  /** Function extracting the unique key of the row from the data object */
  identifier: (row: any) => any;

  /** the column key name of the initial sorted column */
  initialSortColumnKey?: string;

  /** 1 for ascending, -1 for descending */
  initialSortDirection?: number;

  /** the React Object that contains the filter search field */
  searchField?: React.ReactComponentElement<typeof SearchField>;

  /** the initial number of how many row-per-page to show. If it's 0 table header and footer are hidden */
  initialItemsPerPage?: number;

  /** enables item selection */
  selectable: boolean;

  /** the handler to call when the table selection is updated. If not provided, the select boxes won't be rendered */
  onSelect?: (items: Array<any>) => void;

  /** the identifiers for selected items */
  selectedItems?: Array<any>;

  /** Allow items to be deleted */
  deletable?: boolean | ((row: any) => boolean);

  /** The handler to call when an item is deleted. */
  onDelete?: (row: any) => void;

  /** The message which is shown when there are no rows to display */
  emptyText?: string;

  /** Indicate whether the data is loading (only effective for tables using SimpleDataProvider) */
  loading?: boolean;

  /** The message which is shown when the data is loading */
  loadingText?: string;

  /** Children node in the table */
  children: (args: ChildrenArgsProps) => React.ReactNode;

  /** Other filter fields */
  additionalFilters?: Array<React.ReactNode>;
};

type State = {
  data: Array<any>;
  provider: SimpleDataProvider | AsyncDataProvider;
  currentPage: number;
  itemsPerPage: number;
  totalItems: number;
  criteria?: string;
  sortColumnKey: string | null;
  sortDirection: number;
  loading: boolean;
};

export class TableDataHandler extends React.Component<Props, State> {
  static defaultProps = {
    selectable: false,
    deletable: false,
    columns: [],
  };

  constructor(props: Props) {
    super(props);
    this.state = {
      data: [],
      provider: this.getProvider(),
      currentPage: 1,
      itemsPerPage: this.props.initialItemsPerPage || window.userPrefPageSize || 15,
      totalItems: 0,
      criteria: undefined,
      sortColumnKey: this.props.initialSortColumnKey || null,
      sortDirection: this.props.initialSortDirection || 1,
      loading: false,
    };
  }

  getProvider() {
    const data = this.props.data;
    if (Array.isArray(data)) {
      // Gather comparators from columns
      const comparators: {
        [key: string]: Comparator;
      } = this.props.columns.reduce((comparators, col) => {
        if (col.props.columnKey) {
          comparators[col.props.columnKey] = col.props.comparator;
        }
        return comparators;
      }, {});

      return new SimpleDataProvider(
        data,
        this.props.identifier,
        this.props.searchField?.props.filter,
        comparators,
        this.props.loading
      );
    } else if (typeof data === "string") {
      return new AsyncDataProvider(data);
    } else {
      throw new Error("Invalid data type.");
    }
  }

  getData() {
    if (!this.state.provider) {
      return;
    }

    const currPage = (this.state.currentPage - 1) * this.state.itemsPerPage + 1;
    const pageControl = new PageControl(
      currPage,
      this.state.itemsPerPage,
      this.state.criteria,
      this.state.sortColumnKey,
      this.state.sortDirection
    );

    this.setState({ loading: true }, () => {
      this.state.provider.get((promise) => {
        promise.then((data) => this.updateData(data)).finally(() => this.setState({ loading: false }));
      }, pageControl);
    });
  }

  updateData({ items, total }: PagedData) {
    this.setState({ data: items, totalItems: total }, () => {
      const lastPage = this.getLastPage();
      if (this.state.currentPage > lastPage) {
        this.setState({ currentPage: lastPage });
      }
    });
  }

  componentDidMount() {
    this.getData();
  }

  componentDidUpdate(prevProps: Props) {
    if (this.props.data !== prevProps.data) {
      this.setState({ provider: this.getProvider() }, () => this.getData());
    }
  }

  componentWillUnmount() {
    if (this.context && this.context.saveState) {
      this.context.saveState(this.state);
    }
  }

  UNSAFE_componentWillMount() {
    if (this.context && this.context.loadState) {
      if (this.context.loadState()) {
        this.setState(this.context.loadState(), () => this.getData());
      }
    }
  }

  getLastPage = (): number => {
    const rowCount = this.state.totalItems;
    const lastPage = Math.ceil(rowCount / this.state.itemsPerPage);
    return lastPage > 0 ? lastPage : 1;
  };

  onSearch = (criteria?: string): void => {
    this.setState({ currentPage: 1, criteria: criteria }, () => this.getData());
  };

  onItemsPerPageChange = (itemsPerPage: number): void => {
    this.setState({ itemsPerPage: itemsPerPage }, () => this.getData());
  };

  onPageChange = (page: number): void => {
    this.setState({ currentPage: page }, () => this.getData());
  };

  onSortChange = (sortColumnKey: string | null, sortDirection: number): void => {
    this.setState(
      {
        sortColumnKey: sortColumnKey,
        sortDirection: sortDirection,
      },
      () => this.getData()
    );
  };

  setSelection = (selection: any): void => {
    if (this.props.onSelect) {
      this.props.onSelect(selection);
    }
  };

  render() {
    // Skip rendering the headers if no header was provided
    const headers =
      this.props.columns.filter((column) => column.props.header).length > 0 &&
      this.props.columns.map((column, index) => {
        if (column.props.header) {
          const sortDirection = column.props.columnKey === this.state.sortColumnKey ? this.state.sortDirection : 0;
          let comparator = column.props.comparator;
          if (!comparator && column.props.sortable) {
            comparator = Utils.sortByText;
          }

          return (
            <Header
              key={index}
              columnKey={column.props.columnKey}
              sortDirection={sortDirection}
              onSortChange={this.onSortChange.bind(this)}
              width={column.props.width}
              comparator={comparator}
              className={column.props.headerClass}
            >
              {column.props.header}
            </Header>
          );
        } else {
          return <Header key={index} width={column.props.width} className={column.props.headerClass} />;
        }
      });

    const itemsPerPage = this.state.itemsPerPage;
    const currentPage = this.state.currentPage;
    const firstItemIndex = (currentPage - 1) * itemsPerPage;

    const currItems = this.state.data;
    const selectedItems = this.props.selectedItems || [];
    const itemCount = this.state.totalItems || 0;
    const fromItem = itemCount > 0 ? firstItemIndex + 1 : 0;
    const toItem = firstItemIndex + itemsPerPage <= itemCount ? firstItemIndex + itemsPerPage : itemCount;
    const isEmpty = itemCount === 0;

    if (this.props.selectable) {
      const currIds = currItems.map((item) => this.props.identifier(item));

      const handleSelectAll = (sel) => {
        let arr = selectedItems;
        if (sel) {
          arr = arr.concat(currIds.filter((id) => !arr.includes(id)));
        } else {
          arr = arr.filter((id) => !currIds.includes(id));
        }
        this.setSelection(arr);
      };

      const allSelected = currIds.length > 0 && currIds.every((id) => selectedItems.includes(id));
      const checkbox = (
        <Header key="check" width="30px">
          <input type="checkbox" checked={allSelected} onChange={(e) => handleSelectAll(e.target.checked)} />
        </Header>
      );
      headers && headers.unshift(checkbox);
    }

    if (this.props.deletable) {
      const deleteHeader = (
        // Intentionally empty
        <Header key="delete" width="30px" />
      );
      headers && headers.push(deleteHeader);
    }

    const handleSelect = (id, sel) => {
      let arr = selectedItems;
      if (sel) {
        arr = arr.concat([id]);
      } else {
        arr = arr.filter((i) => i !== id);
      }
      this.setSelection(arr);
    };

    const handleSearchPanelClear = () => {
      this.setSelection([]);
    };

    const handleSearchPanelSelectAll = () => {
      this.setState({ loading: true }, () => {
        this.state.provider.getIds(
          (promise) =>
            promise
              .then((data) => {
                const selected = selectedItems;
                this.setSelection(selected.concat(data.filter((id) => !selected.includes(id))));
              })
              .finally(() => this.setState({ loading: false })),
          this.state.criteria
        );
      });
    };

    const emptyText = this.props.emptyText || t("There are no entries to show.");
    const loadingText = this.props.loadingText || t("Loading...");

    return (
      <div className="spacewalk-list">
        <div className="panel panel-default">
          {this.props.initialItemsPerPage !== 0 ? (
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
                  selectedCount={selectedItems.length}
                  selectable={this.props.selectable}
                >
                  {this.props.searchField}
                  {this.props.additionalFilters?.map((filter, i) => (
                    <span key={"additional-filter-" + i}>{filter}&nbsp;</span>
                  ))}
                </SearchPanel>
                <div className="spacewalk-list-head-addons-extra table-items-per-page-wrapper">
                  <ItemsPerPageSelector
                    key="itemsPerPageSelector"
                    currentValue={this.state.itemsPerPage}
                    onChange={this.onItemsPerPageChange}
                  />{" "}
                  {t("items per page")}
                </div>
              </div>
            </div>
          ) : null}
          {this.state.loading ? (
            <div className="panel-body text-center">
              <i className="fa fa-spinner fa-spin fa-1-5x"></i>
              <h4>{loadingText}</h4>
            </div>
          ) : isEmpty ? (
            <div className="panel-body">
              <div className="subheadline">{emptyText}</div>
            </div>
          ) : (
            <div>
              <div className="table-responsive">
                {this.props.children({
                  currItems,
                  headers,
                  handleSelect,
                  selectable: this.props.selectable,
                  selectedItems: selectedItems,
                  deletable: this.props.deletable,
                  criteria: this.state.criteria,
                })}
              </div>
            </div>
          )}
          {this.props.initialItemsPerPage !== 0 ? (
            <div className="panel-footer">
              <div className="spacewalk-list-bottom-addons">
                <PaginationBlock
                  key="paginationBlock"
                  currentPage={this.state.currentPage}
                  lastPage={this.getLastPage()}
                  onPageChange={this.onPageChange}
                />
              </div>
            </div>
          ) : null}
        </div>
      </div>
    );
  }
}
