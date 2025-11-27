import { type ReactComponentElement, type ReactElement, type ReactNode, Children, Component, createRef, } from "react";

import _isEqual from "lodash/isEqual";

import { pageSize } from "core/user-preferences";

import { cloneReactElement, Loading } from "components/utils";

import { AsyncDataProvider, Comparator, PageControl, PagedData, SimpleDataProvider } from "utils/data-providers";
import { Utils } from "utils/functions";
import { DEPRECATED_unsafeEquals } from "utils/legacy";

import { ItemsPerPageSelector, PaginationBlock } from "../pagination";
import { Header } from "./Header";
import { SearchField } from "./SearchField";
import { SearchPanel } from "./SearchPanel";
import { SelectedRowDetails } from "./SelectedRowDetails";

type ChildrenArgsProps = {
  currItems: any[];
  headers: ReactNode;
  handleSelect: (...args: any[]) => any;
  selectedItems: any[];
  criteria?: string;
  field?: string;
};

type Props = {
  columns: ReactElement<any>[];

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
  data: any[] | string;

  /** Function extracting the unique key of the row from the data object */
  identifier: (row: any) => any;

  /** the column key name of the initial sorted column */
  initialSortColumnKey?: string;

  /** 1 for ascending, -1 for descending */
  initialSortDirection?: number;

  /** Callback for search input, setting `onSearch` sets `searchField` to a simple search input if none is provided */
  onSearch?: (criteria: string) => void;

  /** the React Object that contains the filter search field */
  searchField?: ReactComponentElement<typeof SearchField>;

  /** Default column to search on */
  defaultSearchField?: string;

  /** Initial search query */
  initialSearch?: string;

  /** the initial number of how many row-per-page to show.*/
  initialItemsPerPage?: number;

  /** Hide header and footer */
  hideHeaderFooter?: string;

  /** enables item selection.
   * tells if a row is selectable.
   * If using an asynchronous provider, the selectAll filtering has to be implemented on the server side too.
   */
  selectable: boolean | ((row: unknown) => boolean);

  /** the handler to call when the table selection is updated. If not provided, the select boxes won't be rendered */
  onSelect?: (items: any[]) => void;

  /** the identifiers for selected items */
  selectedItems?: any[];

  /** Allow items to be deleted */
  deletable?: boolean | ((row: any) => boolean);

  /** The handler to call when an item is deleted. */
  onDelete?: (row: any) => void;

  expandable?: boolean;

  /** The message which is shown when there are no rows to display */
  emptyText?: string;

  /** Indicate whether the data is loading (only effective for tables using SimpleDataProvider) */
  loading?: boolean;

  /** The message which is shown when the data is loading */
  loadingText?: string;

  onLoad?: () => void;

  /** Children node in the table */
  children: (args: ChildrenArgsProps) => ReactNode;

  /** Other filter fields */
  additionalFilters?: ReactNode[];

  /** Title buttons to add next to the items per page selection */
  titleButtons?: ReactNode[];

  /** Sticky table header */
  stickyHeader?: boolean;

  /** Align search fields inline */
  searchPanelInline?: boolean
};

type State = {
  data: any[];
  provider: SimpleDataProvider | AsyncDataProvider;
  currentPage: number;
  itemsPerPage: number;
  totalItems: number;
  criteria?: string;
  field?: string;
  sortColumnKey: string | null;
  sortDirection: number;
  loading: boolean;
};

export class TableDataHandler extends Component<Props, State> {
  static defaultProps = {
    selectable: false,
    deletable: false,
    columns: [],
  };

  constructor(props: Props) {
    super(props);
    this.panelHeaderRef = createRef();
    this.state = {
      data: [],
      provider: this.getProvider(),
      currentPage: 1,
      itemsPerPage: this.props.initialItemsPerPage || pageSize,
      totalItems: 0,
      criteria: this.props.initialSearch,
      field: this.props.defaultSearchField,
      sortColumnKey: this.props.initialSortColumnKey || null,
      sortDirection: this.props.initialSortDirection || 1,
      loading: false,
      headerHeight: null,
    };
  }

  getProvider() {
    const data = this.props.data;
    if (Array.isArray(data)) {
      // Gather comparators from columns
      const comparators: Record<string, Comparator> = this.props.columns.reduce((comparators, col) => {
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
        this.props.loading,
        this.props.selectable
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
      this.state.field,
      this.state.sortColumnKey,
      this.state.sortDirection
    );

    this.setState({ loading: true }, () => {
      this.state.provider.get((promise) => {
        promise
          .then((data) => this.updateData(data))
          .finally(() => {
            this.setState({ loading: false });
          });
      }, pageControl);
    });
  }

  updateData({ items, total, selectedIds }: PagedData) {
    this.setState({ data: items, totalItems: total }, () => {
      if (!DEPRECATED_unsafeEquals(selectedIds, null)) {
        this.props.onSelect?.(selectedIds);
      }
      const lastPage = this.getLastPage();
      if (this.state.currentPage > lastPage) {
        this.setState({ currentPage: lastPage });
      }
    });
  }

  componentDidMount() {
    this.getData();

    if (this.panelHeaderRef.current) {
      this.setState({ headerHeight: this.panelHeaderRef.current.clientHeight });
    }
  }

  componentDidUpdate(prevProps: Props) {
    if (!_isEqual(this.props.data, prevProps.data)) {
      this.setState({ provider: this.getProvider() }, () => this.getData());
    }
    if (this.props.loading !== prevProps.loading) {
      this.setState({ loading: Boolean(this.props.loading) });
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

  onSearch = (criteria: string = ""): void => {
    this.props.onSearch?.(criteria);
    this.setState({ currentPage: 1, criteria: criteria }, () => this.getData());
  };

  onSearchField = (field?: string): void => {
    this.setState({ currentPage: 1, field: field }, () => {
      if (!DEPRECATED_unsafeEquals(this.state.criteria, null) && this.state.criteria !== "") {
        this.getData();
      }
    });
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

  renderTitleButtons = () => {
    return Children.map(this.props.titleButtons, (item: ReactNode) =>
      cloneReactElement(item, {
        search: { field: this.state.field, criteria: this.state.criteria },
      })
    );
  };

  render() {
    // Skip rendering the headers if no header was provided
    const headers =
      this.props.columns.filter((column) => column.props.header).length > 0 &&
      this.props.columns.map((column) => {
        if (column.props.header) {
          const sortDirection = column.props.columnKey === this.state.sortColumnKey ? this.state.sortDirection : 0;
          let comparator = column.props.comparator;
          if (!comparator && !!column.props.sortable) {
            comparator = Utils.sortByText;
          }

          return (
            <Header
              key={column.props.columnKey}
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
          return (
            <Header key={column.props.columnKey} width={column.props.width} className={column.props.headerClass} />
          );
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
    let searchField = this.props.searchField;
    if (!searchField && this.props.onSearch) {
      searchField = <SearchField />;
    }
    const isTableHeaderEmpty = !this.props.titleButtons && !searchField && !this.props.additionalFilters;
    const stickyHeader = this.props.stickyHeader;

    if (this.props.selectable) {
      const isSelectable =
        typeof this.props.selectable === "boolean" ? () => this.props.selectable : this.props.selectable;
      const selectableItems = currItems.filter((item) => isSelectable(item));
      const currIds = selectableItems.map((item) => this.props.identifier(item));
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
      if (headers) {
        headers.unshift(checkbox);
      }
    }

    if (this.props.expandable) {
      const spacer = <Header key="expandable" width="30px" />;
      if (headers) {
        headers.unshift(spacer);
      }
    }

    if (this.props.deletable) {
      const deleteHeader = (
        // Intentionally empty
        <Header key="delete" width="30px" />
      );
      if (headers) {
        headers.push(deleteHeader);
      }
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
    const isSelectable = typeof this.props.selectable !== "undefined" && this.props.selectable !== false;
    const hideHeader = this.props.hideHeaderFooter === "header" || this.props.hideHeaderFooter === "both";
    const hideFooter = this.props.hideHeaderFooter === "footer" || this.props.hideHeaderFooter === "both";
    return (
      <div className={`spacewalk-list ${this.props.stickyHeader ? "overflow-visible" : ""}`}>
        <div className="panel panel-default">
          {!hideHeader && !isTableHeaderEmpty ? (
            <>
              <div ref={this.panelHeaderRef} className={` panel-heading ${this.props.stickyHeader ? "sticky-panel-heading" : ""}`}>
                <div className="spacewalk-list-head-addons align-items-center">
                  <SearchPanel
                    criteria={this.state.criteria}
                    field={this.state.field}
                    onSearch={this.onSearch}
                    onSearchField={this.onSearchField}
                    onClear={handleSearchPanelClear}
                    onSelectAll={handleSearchPanelSelectAll}
                    selectedCount={selectedItems.length}
                    selectable={isSelectable}
                    searchPanelInline={this.props.searchPanelInline}
                  >
                    {searchField}
                    {this.props.additionalFilters}
                  </SearchPanel>
                  <div className="spacewalk-list-head-addons-extra table-items-per-page-wrapper">
                    {this.renderTitleButtons()}
                  </div>
                </div>
              </div>
              <SelectedRowDetails
                itemCount={itemCount}
                onClear={handleSearchPanelClear}
                onSelectAll={handleSearchPanelSelectAll}
                selectable={isSelectable}
                selectedCount={selectedItems.length}
              />
            </>
          ) : null}
          {this.state.loading ? (
            <Loading text={this.props.loadingText} />
          ) : isEmpty ? (
            <div className="panel-body">
              <div className="subheadline">{emptyText}</div>
            </div>
          ) : (
            <div>
              <div className={`table-responsive ${this.props.stickyHeader ? "overflow-visible" : ""}`}>
                {this.props.children({
                  currItems,
                  headers,
                  handleSelect,
                  selectedItems: selectedItems,
                  criteria: this.state.criteria,
                  field: this.state.field,
                  headerHeight: this.state.headerHeight,
                })}
              </div>
            </div>
          )}
          {!hideFooter ? (
            <div className="panel-footer">
              <div className="spacewalk-list-bottom-addons d-flex justify-content-between">
                <ItemsPerPageSelector
                  key="itemsPerPageSelector"
                  currentValue={this.state.itemsPerPage}
                  fromItem={fromItem}
                  toItem={toItem}
                  itemCount={itemCount}
                  onChange={this.onItemsPerPageChange}
                />
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
