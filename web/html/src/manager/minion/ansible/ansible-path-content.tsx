import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { Messages, Utils } from "components/messages";
import Network from "utils/network";
import { AnsiblePath } from "./ansible-path-type";
import AccordionPathContent from "./accordion-path-content";

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
        { this.state.pathList.map(p => <AccordionPathContent key={p.id} path={p} /> ) }
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
