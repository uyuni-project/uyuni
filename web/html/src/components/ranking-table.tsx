import * as React from "react";
import { DEPRECATED_unsafeEquals } from "utils/legacy";
import _cloneDeep from "lodash/cloneDeep";

type Instance = JQuery & {};
type Sortable = <T>(arg0: T, options?: any) => T extends string ? string[] : Instance;

declare global {
  interface JQuery {
    sortable: Sortable;
  }
}

function channelIcon(channel) {
  let iconClass, iconTitle;
  if (channel.type === "state") {
    iconClass = "fa spacewalk-icon-salt-add";
    iconTitle = t("State Configuration Channel");
  } else {
    iconClass = "fa spacewalk-icon-software-channels";
    iconTitle = t("Normal Configuration Channel");
  }

  return <i className={iconClass} title={iconTitle} />;
}

type RankingTableProps = {
  items: any[];
  onUpdate?: (newItems: any[]) => any;
  emptyMsg: string;
};

type RankingTableState = {
  items: any[];
};

class RankingTable extends React.Component<RankingTableProps, RankingTableState> {
  defaultEmptyMsg = t("There are no entries to show.");
  node: Element | null = null;

  constructor(props) {
    super(props);
    this.state = {
      items: this.props.items,
    };
  }

  cloneItems() {
    return this.state.items.map((i) => _cloneDeep(i));
  }

  handleUpdate() {
    if (!this.node) {
      console.error("Failed to find node");
      return;
    }

    const newItems = this.cloneItems();
    const ids = jQuery(this.node).sortable("toArray", { attribute: "data-id" });
    if (ids.length > 0) {
      ids.forEach((id, ix) => {
        const item = newItems.find((elm) => elm.label === id);
        item.position = ix + 1;
      });
    }

    jQuery(this.node).sortable("cancel");
    this.setState({ items: newItems });

    if (this.props.onUpdate) {
      this.props.onUpdate(newItems);
    }
  }

  componentDidMount() {
    if (!this.node) {
      console.error("Failed to find node");
      return;
    }

    jQuery(this.node).sortable({
      update: this.handleUpdate.bind(this),
      items: "a",
    });

    this.handleUpdate();
  }

  getElements() {
    const sortedItems = this.state.items.sort((a, b) =>
      DEPRECATED_unsafeEquals(a.position, undefined)
        ? 1
        : DEPRECATED_unsafeEquals(b.position, undefined)
        ? -1
        : a.position - b.position
    );

    return sortedItems.map((i) => {
      // TODO: Provide a callback as prop for optional mapping and generify this default implementation
      const icon = channelIcon(i);
      return (
        // eslint-disable-next-line jsx-a11y/anchor-is-valid
        <a href="#" className="list-group-item" key={i.label} data-id={i.label}>
          <i className="fa fa-sort" />
          {icon}
          {i.name} ({i.label})
        </a>
      );
    });
  }

  render() {
    return (
      <div>
        {this.state.items.length > 0 ? (
          <div ref={(node) => (this.node = node)} className="list-group">
            {this.getElements()}
          </div>
        ) : (
          <div className="alert alert-info">{this.props.emptyMsg || this.defaultEmptyMsg}</div>
        )}
      </div>
    );
  }
}

export { RankingTable };
