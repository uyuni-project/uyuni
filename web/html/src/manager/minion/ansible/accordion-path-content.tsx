import * as React from "react";
import { AnsiblePath } from "./ansible-path-type";
import { Panel } from "components/panels/Panel";

type PropsType = {
  path: AnsiblePath;
};

type StateType = {
  open: boolean; 
  content: Map<string, any>;
};

class AccordionPathContent extends React.Component<PropsType, StateType> {
  constructor(props) {
    super(props);

    this.state = { open: false, content: new Map<string, any>() };
  }

  onOpen() {
    this.loadPathContent(this.props.path);
    this.setState({ open: !this.state.open });
  }

  loadPathContent(path: AnsiblePath) {
    // Network.get("/rhn/manager/api/systems/details/ansible/path/" + path.id)
    // .promise.then(data => {
      this.setState({ content: this.state.content.set(path.path, ["element 1", "element 2"]) });
    // });
  }

  render() {
    const header =
      <div className="accordion-toggle" onClick={() => this.onOpen()}>
        { this.props.path.path }
      </div>;
    return (
      <Panel header={header} headingLevel="h5">   
        <div>
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
