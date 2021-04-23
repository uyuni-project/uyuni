import * as React from "react";
import { AnsiblePath } from "./ansible-path-type";
import { Panel } from "components/panels/Panel";
import Network from "utils/network";
import { Messages, Utils } from "components/messages";

type PropsType = {
  path: AnsiblePath;
};

type StateType = {
  open: boolean; 
  content: Map<string, any>;
  errors: string[];
};

function getURL(path: AnsiblePath) {
  let baseUrl: string;
  if (path.type === "playbook") {
    baseUrl = "/rhn/manager/api/systems/details/ansible/discover-playbooks/";
  }
  else {
    baseUrl = "/rhn/manager/api/systems/details/ansible/introspect-inventory/";
  }
  return baseUrl + path.id;
}

class AccordionPathContent extends React.Component<PropsType, StateType> {
  constructor(props) {
    super(props);

    this.state = {
      open: false,
      content: new Map<string, any>(),
      errors: [],
    };
  }

  onOpen() {
    if (!this.state.open) {
      const path: AnsiblePath = this.props.path;
      if (Object.keys(this.state.content).includes(path.path)) {
        this.setState({ open: true });
      }
      else {
        Network.get(getURL(path))
        .promise.then(data => {
          if (data.success) {
            this.setState({ content: this.state.content.set(path.path, ["element 1", "element 2"]), open: true});
          }
          else {
            this.setState({ errors: data.messages });
          }
        });
      }
    }
    else {
      this.setState({ open: false });
    }
  }

  render() {
    const header =
      <div className="accordion-toggle" onClick={() => this.onOpen()}>
        { this.props.path.path }
      </div>;

    const errors = this.state.errors.length > 0 ? <Messages items={Utils.error(this.state.errors)} /> : null;
    return (
      <Panel header={header} headingLevel="h5">
        <div>
          {errors}
          { this.state.open ? 
              <ul>
                {
                  this.state.content.get(this.props.path.path).map((element: string) =>
                    <li key={element}>
                      {
                        this.props.path.type === "playbook" ? 
                          <a>{element}</a>
                        : element
                      }
                    </li>
                )}
              </ul>
            : null
          }
        </div>
      </Panel>
    )
  }
};

export default AccordionPathContent;
