"use strict";

const React = require("react");
const StatePersistedMixin = require("./util").StatePersistedMixin;
const {PaginationBlock, ItemsPerPageSelector} = require('./pagination');

const SearchPanel = (props) =>
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
              <a href="#" onClick={props.onClear}>{t("Clear")}</a>
              &nbsp;/&nbsp;
          </span>
      }
      { props.selectable &&
          <a href="#" onClick={props.onSelectAll}>{t("Select All")}</a>
      }
    </div>
  </div>
;

const SearchField = (props) =>
  <input className="form-control table-input-search"
    value={props.criteria}
    placeholder={props.placeholder}
    type="text"
    onChange={(e) => props.onSearch(e.target.value)}
    name={props.name}
  />
;

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

const DataHandler = React.createClass({
  mixins: [StatePersistedMixin],

  propTypes: {
    data: React.PropTypes.arrayOf(React.PropTypes.any).isRequired, // any type of data in and array, where each element is an item data
    identifier: React.PropTypes.func.isRequired, // the unique key of the item
    cssClassFunction: React.PropTypes.func, // a function that return a css class for each item
    searchField: React.PropTypes.node, // the React Object that contains the filter search field
    additionalFilters: React.PropTypes.node, // other filters to render but not handled here
    initialItemsPerPage: React.PropTypes.number, // the initial number of how many item-per-page to show
    selectable: React.PropTypes.bool, // enables item selection
    onSelect: React.PropTypes.func, // the handler to call when the table selection is updated. if this function is not provided, the select boxes won't be rendered
    selectedItems: React.PropTypes.array, // the identifiers for selected items
    emptyText: React.PropTypes.string, // The message which is shown when there are no items to display
    loading: React.PropTypes.bool, // if data is loading
    loadingText: React.PropTypes.string, // The message which is shown when the data is loading
  },
  defaultEmptyText: t('There are no entries to show.'),
  defaultLoadingText: t('Loading..'),

  getInitialState: function() {
    return {
      currentPage: 1,
      itemsPerPage: this.props.initialItemsPerPage || 15,
      criteria: null,
      selectedItems: this.props.selectedItems || [],
      selectable: this.props.selectable,
      loading: this.props.loading || false,
    };
  },

  componentWillReceiveProps: function(nextProps) {
    this.onPageCountChange(nextProps.data, this.state.criteria, this.state.itemsPerPage);
    this.setState({
        selectedItems: nextProps.selectedItems || [],
        selectable: Boolean(nextProps.selectable),
        loading: Boolean(nextProps.loading) || false,
    });
  },

  getLastPage: function(data, criteria, itemsPerPage) {
    const itemCount = data.filter(this.getFilter(criteria)).length;

    const lastPage = Math.ceil(itemCount / itemsPerPage);
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
    return this.props.data.filter(this.getFilter(this.state.criteria));
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

  setSelection: function(selection) {
    if(this.props.onSelect) {
      this.props.onSelect(selection);
    }
  },

  render: function() {
    const filteredData = this.getProcessedData();

    const itemsPerPage = this.state.itemsPerPage;
    const currentPage = this.state.currentPage;
    const firstItemIndex = (currentPage - 1) * itemsPerPage;

    const itemCount = filteredData.length;
    const fromItem = itemCount > 0 ? firstItemIndex + 1 : 0;
    const toItem = firstItemIndex + itemsPerPage <= itemCount ? firstItemIndex + itemsPerPage : itemCount;
    const currItems = filteredData.slice(firstItemIndex, firstItemIndex + itemsPerPage);
    const isEmpty = itemCount === 0;

    const handleSelect = (id, sel) => {
        let arr = this.state.selectedItems;
        if(sel) {
            arr = arr.concat([id]);
        } else {
            arr = arr.filter(i => i !== id);
        }
        this.setSelection(arr);
    };

    const dataItems = React.Children.toArray(this.props.children)
        .map(item => React.cloneElement(item, {data: currItems, criteria: this.state.criteria}));

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
              {
                this.props.additionalFilters.map((filter, i) => <span key={'additional-filter-' + i}>{filter}&nbsp;</span>)
              }
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
              <div className="table-responsive">
                {dataItems}
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
  }
});

module.exports = {
    DataHandler : DataHandler,
    SearchField: SearchField,
    Highlight: Highlight
}
