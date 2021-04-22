import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { Messages, Utils } from "components/messages";
import { Panel } from "components/panels/Panel";
import Network from "utils/network";

type AnsiblePath = {
  id: number;
  minionServerId: number;
  type: string;
  path: string;
}

type PropsType = {
  minionServerId: number;
  pathContentType: string;
};

type StateType = {
  minionServerId: number;
  pathContentType: string;
  pathList: AnsiblePath[];
  errors: string[];
};

class AnsiblePathContent extends React.Component<PropsType, StateType> {
  constructor(props) {
    super(props);

    console.log(props);
    this.state = {
      minionServerId: props.minionServerId,
      pathContentType: props.pathContentType,
      pathList: [],
      errors: [],
    };

    Network.get("/rhn/manager/api/systems/details/ansible/paths/" + props.pathContentType + "/" + props.minionServerId)
    .promise.then(data => {
      this.setState({ pathList: data });
    });
  }
  
  render () {
    const errors = this.state.errors.length > 0 ? <Messages items={Utils.error(this.state.errors)} /> : null;
    return (
      <div>
        {errors}
        <p>
          {this.state.pathContentType}
        </p>
        <Panel headingLevel="h3" title="Path content">          
          {
            this.state.pathList.map(p => <div>{p.id} - {p.path} - {p.type} - {p.minionServerId} - </div>)
          }
        </Panel>
      </div>
    );
  }
}

export const renderer = (renderId: string, {id, pathContentType}) => {
  return SpaRenderer.renderNavigationReact(
    <AnsiblePathContent
      minionServerId={id}
      pathContentType={pathContentType} />,
      document.getElementById(renderId)
  
  );
}
