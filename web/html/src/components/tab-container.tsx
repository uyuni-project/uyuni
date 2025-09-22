import * as React from "react";

type Props = {
  labels: React.ReactNode[];
  /** Must start with # */
  hashes: string[];
  tabs: React.ReactNode[];
  initialActiveTabHash: string;
  /** Takes a hash parameter */
  onTabHashChange: (hash: string) => any;
};

class TabContainer extends React.Component<Props> {
  UNSAFE_componentWillReceiveProps(nextProps: Props) {
    this.setState({ activeTabHash: this.sanitizeHash(nextProps.initialActiveTabHash, nextProps.hashes) });
  }

  sanitizeHash = (hash: string, hashArr?: string[]) => {
    hashArr = hashArr || this.props.hashes;

    if (hashArr.indexOf(hash) >= 0) {
      return hash;
    }
    return hashArr[0];
  };

  onActiveTabChange = (hash, event) => {
    event.preventDefault();

    this.setState({ activeTabHash: hash });
    if (this.props.onTabHashChange) {
      this.props.onTabHashChange(hash);
    }
  };

  state = { activeTabHash: this.sanitizeHash(this.props.initialActiveTabHash) };

  render() {
    const labels = this.props.hashes.map((hash, i) => {
      const label = this.props.labels[i];
      return (
        <TabLabel
          onClick={(event) => this.onActiveTabChange(hash, event)}
          text={label}
          active={this.state.activeTabHash === hash}
          hash={hash}
          key={hash}
        />
      );
    });

    const tab = this.props.tabs[this.props.hashes.indexOf(this.state.activeTabHash)];

    return (
      <div>
        <div className="spacewalk-content-nav">
          <ul className="nav nav-tabs">{labels}</ul>
        </div>
        {tab}
      </div>
    );
  }
}

type TabLabelProps = {
  active?: boolean;
  hash?: string;
  onClick?: (...args: any[]) => any;
  text: React.ReactNode;
};

const TabLabel = (props: TabLabelProps) => (
  <li className={props.active ? "active" : ""}>
    <a className="js-spa" href={props.hash || "#"} onClick={props.onClick}>
      {props.text}
    </a>
  </li>
);

export { TabContainer, TabLabel };
