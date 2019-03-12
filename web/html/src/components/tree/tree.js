// @flow
import React from 'react';
import { CustomDiv } from 'components/custom-objects';

type TreeItem = {
  id: string,
  data?: Object,
  children?: Array<string>,
};

type TreeData = {
  rootId: string,
  items: Array<TreeItem>,
};

type Props = {
  data: TreeData,
  renderItem: (item: TreeItem, renderNameColumn: Function) => void,
  header?: React.Node,
  initiallyExpanded?: Array<string>,
  onItemSelectionChanged?: (item: TreeItem, checked: boolean) => void,
  initiallySelected?: Array<string>,
};

type State = {
  visibleSublists: Array<string>,
  selected: Array<string>,
};

export class Tree extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      visibleSublists: this.props.initiallyExpanded || [],
      selected: this.props.initiallySelected || [],
    };
  }

  isSublistVisible = (id: string): boolean => {
    return this.state.visibleSublists.indexOf(id) !== -1;
  }

  isSelected = (id: string): boolean => {
    return this.state.selected.indexOf(id) !== -1;
  }

  handleVisibleSublist = (id: string): void => {
    this.setState(oldState => {
      const { visibleSublists } = oldState;
      return {
        visibleSublists: visibleSublists.indexOf(id) !== -1
          ? visibleSublists.filter(item => item !== id)
          : visibleSublists.concat([id]),
      };
    });
  }

  handleSelectionChange = (changeEvent: Event): void => {
    if (changeEvent.target instanceof HTMLInputElement) {
      const { value: id, checked } = changeEvent.target;
      this.setState(oldState => {
        const { selected } = oldState;
        return { selected: checked ? selected.concat([id]) : selected.filter(itemId => itemId !== id) };
      });

      const item = this.props.data.items.find(item => item.id === id);
      if (this.props.onItemSelectionChanged != null && item != null) {
        this.props.onItemSelectionChanged(item, checked);
      }
    }
  }

  renderItem = (item: TreeItem, idx: number) => {
    const children = this.props.data.items
      .filter(row => (item.children || []).includes(row.id))
      .map((child, childIdx) => this.renderItem(child, childIdx));
    const sublistVisible = this.isSublistVisible(item.id);
    const openSubListIconClass = sublistVisible ? 'fa-angle-down' : 'fa-angle-right';

    const renderNameColumn = (name: string): React.Node => {
      const className = children.length > 0 ? "product-hover pointer" : "";
      return (
          <span
            className={`product-description ${className}`}
            onClick={() => this.handleVisibleSublist(item.id)}
          >
            {name}
          </span>
      );
    };

    return (
      <li key={item.id} className={idx % 2 === 1 ? 'list-row-odd' : 'list-row-even'}>
        <div className="product-details-wrapper" style={{'padding': '.7em'}}>
          {
            this.props.onItemSelectionChanged != null &&
            <CustomDiv className="col" width="2" um="em">
              <input type='checkbox'
                  id={'checkbox-for-' + item.id}
                  value={item.id}
                  onChange={this.handleSelectionChange}
                  checked={this.isSelected(item.id) ? 'checked' : ''}
              />
            </CustomDiv>
          }
          { (children.length > 0) &&
            <CustomDiv className="col" width="2" um="em">
              <i
                className={`fa ${openSubListIconClass} fa-1-5x pointer product-hover`}
                onClick={() => this.handleVisibleSublist(item.id)}
              />
            </CustomDiv>
          }
          {this.props.renderItem(item, renderNameColumn)}
        </div>
        { children.length > 0 && sublistVisible &&
          <ul className="product-list">
          { children }
          </ul>
        }
      </li>
    );
  }

  render() {
    const rootNode = this.props.data.items.find(item => item.id === this.props.data.rootId);
    if (rootNode == null) {
      return <div>{t('Invalid data')}</div>;
    }

    const nodes = this.props.data.items
      .filter(item => (rootNode.children || []).includes(item.id))
      .map((item, idx) => this.renderItem(item, idx));
    if (nodes == null || nodes.length === 0) {
      return <div>{t('No data')}</div>;
    }

    return (
      <ul className="product-list">
        {this.props.header != null && (
          <li className="list-header">
            <div style={{'padding': '.7em'}}>
              {
                this.props.onItemSelectionChanged != null &&
                <CustomDiv key="header1" className="col" width="2" um="em"/>
              }
              <CustomDiv key="header2" className="col" width="2" um="em"/>
              {this.props.header}
            </div>
          </li>
        )}
        {nodes}
      </ul>
    );
  }
}

