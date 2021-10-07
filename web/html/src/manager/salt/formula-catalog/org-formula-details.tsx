import * as React from "react";
import { TopPanel } from "components/panels/TopPanel";
import Network from "utils/network";
import SpaRenderer from "core/spa/spa-renderer";

// See java/code/src/com/suse/manager/webui/templates/formula_catalog/formula.jade
declare global {
  interface Window {
    formulaName?: any;
  }
}

type Props = {};

type State = {
  metadata: any;
};

class FormulaDetail extends React.Component<Props, State> {
  constructor(props, context) {
    super(props, context);
    this.getServerData();

    this.state = {
      metadata: {},
    };
  }

  getServerData = () => {
    Network.get("/rhn/manager/api/formula-catalog/formula/" + window.formulaName + "/data").then((data) => {
      this.setState({ metadata: data });
    });
  };

  generateMetadata = () => {
    var metadata: React.ReactNode[] = [];
    for (var item in this.state.metadata) {
      metadata.push(
        <div className="form-group" key={item}>
          <label className="col-md-3 control-label">{item}:</label>
          <div className="col-md-6">{this.generateMetadataItem(item, this.state.metadata[item])}</div>
        </div>
      );
    }
    return metadata;
  };

  generateMetadataItem = (name, item) => {
    if (typeof item == "string")
      return <textarea className="form-control" name={name} value={item} readOnly disabled />;
    else if (typeof item == "object") {
      var text = "";
      var rows = 1;
      for (var key in item) {
        text += key + ": " + item[key] + "\n";
        rows++;
      }
      return <textarea className="form-control" name={name} value={text} rows={rows} readOnly disabled />;
    } else {
      return <textarea className="form-control" name={name} value={JSON.stringify(item)} readOnly disabled />;
    }
  };

  render() {
    return (
      <TopPanel title={"View Formula: " + window.formulaName} icon="spacewalk-icon-salt-add">
        <form className="form-horizontal">
          <div className="form-group">
            <label className="col-md-3 control-label">Name:</label>
            <div className="col-md-6">
              <input
                className="form-control"
                type="text"
                name="name"
                ref="formulaName"
                value={window.formulaName}
                readOnly
                disabled
              />
            </div>
          </div>
          {this.generateMetadata()}
        </form>
      </TopPanel>
    );
  }
}

export const renderer = (id) => SpaRenderer.renderNavigationReact(<FormulaDetail />, document.getElementById(id));
