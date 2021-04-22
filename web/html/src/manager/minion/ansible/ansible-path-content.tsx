import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { Messages, Utils } from "components/messages";
import { Panel } from "components/panels/Panel";
import Network from "utils/network";


type PropsType = {
  minionServerId: number;
  pathContentType: string;
};

type StateType = {
  minionServerId: number;
  pathContentType: string;
  errors: string[];
};

class AnsiblePathContent extends React.Component<PropsType, StateType> {
  constructor(props) {
    super(props);

    console.log(props);
    this.state = {
      minionServerId: props.minionServerId,
      pathContentType: props.pathContentType,
      errors: [],
    };

    // Network.get("/rhn/manager/api/systems/details/ansible/paths/" + props.minionServerId)
    // .promise.then(data => {
    //   this.setState({  });
    // });
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
