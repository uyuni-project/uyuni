import * as React from "react";
import Network from "utils/network";

function requestHighstate(id) {
  return Network.get("/rhn/manager/api/states/highstate?sid=" + id).promise;
}

type MinionHighstateSingleProps = {
  data: any;
};

type MinionHighstateSingleState = {
  highstate?: any;
}

class MinionHighstateSingle extends React.Component<MinionHighstateSingleProps, MinionHighstateSingleState> {
  state: MinionHighstateSingleState = {};

  getHighstate = () => {
    requestHighstate(this.props.data.id).then(data => {
      this.setState({ highstate: data });
    });
  };

  UNSAFE_componentWillMount() {
    this.getHighstate();
  }

  render() {
    return (
      <div className="panel panel-default">
        <div className="panel-heading">
          <h4>{t("Highstate for {0}", this.props.data.name)}</h4>
        </div>
        <div className="panel-body">
          {this.state.highstate ? <pre>{this.state.highstate}</pre> : <span>Retrieving highstate data...</span>}
        </div>
      </div>
    );
  }
}

type MinionHighstateProps = {
  data: any;
};

type MinionHighstateState = {
  highstate?: any;
  show: boolean;
  loading?: boolean;
};

class MinionHighstate extends React.Component<MinionHighstateProps, MinionHighstateState> {
  state: MinionHighstateState = { show: false };

  getHighstate = () => {
    if (this.state.loading) return;
    this.setState({ loading: true });

    requestHighstate(this.props.data.id).then(data => {
      this.setState({ highstate: data });
    });
  };

  componentDidMount() {
    if (this.state.show) {
      this.getHighstate();
    }
  }

  expand = () => {
    this.getHighstate();
    this.setState({ show: !this.state.show });
  };

  render() {
    return (
      <div className="panel panel-default">
        <div className="panel-heading" onClick={this.expand} style={{ cursor: "pointer" }}>
          <span>{this.props.data.name}</span>
          <div className="pull-right">
            {this.state.show ? (
              <i className="fa fa-right fa-chevron-up fa-1-5x" />
            ) : (
              <i className="fa fa-right fa-chevron-down fa-1-5x" />
            )}
          </div>
        </div>
        {this.state.show && (
          <div className="panel-body">
            {this.state.highstate ? <pre>{this.state.highstate}</pre> : <span>Retrieving highstate data...</span>}
          </div>
        )}
      </div>
    );
  }
}

type DisplayHighstateProps = {
  minions?: any;
};

type DisplayHighstateState = {
  minions?: any;
};

class DisplayHighstate extends React.Component<DisplayHighstateProps, DisplayHighstateState> {
  constructor(props: DisplayHighstateProps) {
    super(props);

    this.state = {
      minions: this.props.minions || window.minions,
    };
  }

  renderMinions = () => {
    const minionList: React.ReactNode[] = [];
    for (const system of this.state.minions) {
      minionList.push(<MinionHighstate data={system} />);
    }
    return minionList;
  };

  render() {
    return (
      <div>
        {this.state.minions.length === 1 ? (
          <MinionHighstateSingle data={this.state.minions[0]} />
        ) : (
          <div className="panel panel-default">
            <div className="panel-heading">
              <h4>Target Systems ({this.state.minions.length})</h4>
            </div>
            {this.state.minions.length === 0 ? (
              <div className="panel-body">{t("There are no applicable systems.")}</div>
            ) : (
              <div className="panel-body" style={{ paddingBottom: 0 }}>
                {this.renderMinions()}
              </div>
            )}
          </div>
        )}
      </div>
    );
  }
}

export { DisplayHighstate };
