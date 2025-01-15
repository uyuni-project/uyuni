import * as React from "react";
import { useState } from "react";

import HighstateSummary from "./highstate-summary";

function MinionHighstateSingle({ minion }: { minion: { id: number; name: string } }) {
  return (
    <div>
      <h3>{t("State Summary for {name}", { name: minion.name })}</h3>
      <HighstateSummary minionId={minion.id} />
    </div>
  );
}

function MinionHighstate({ minion }: { minion: { id: number; name: string } }) {
  const [show, setShow] = useState(false);

  return (
    <div className="panel panel-default" style={{ marginBottom: 10 }}>
      <div
        className="panel-heading"
        onClick={() => {
          setShow(!show);
        }}
        style={{ cursor: "pointer" }}
      >
        <div className="row">
          <strong>{minion.name}</strong>
          <div className="pull-right">
            <i className={`fa fa-right fa-chevron-${show ? "up" : "down"} fa-1-5x`} />
          </div>
        </div>
      </div>
      {show && (
        <div className="panel-body">
          <HighstateSummary minionId={minion.id} />
        </div>
      )}
    </div>
  );
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
    for (const minion of this.state.minions) {
      minionList.push(<MinionHighstate minion={minion} />);
    }
    return minionList;
  };

  render() {
    return (
      <div>
        {this.state.minions.length === 1 ? (
          <MinionHighstateSingle minion={this.state.minions[0]} />
        ) : (
          <div className="row">
            <div className="col-md-12">
              <h3>Target Systems ({this.state.minions.length})</h3>
              {this.state.minions.length === 0 ? (
                <div className="panel panel-default">
                  <div className="panel-body">{t("There are no applicable systems.")}</div>
                </div>
              ) : (
                this.renderMinions()
              )}
            </div>
          </div>
        )}
      </div>
    );
  }
}

export { DisplayHighstate };
