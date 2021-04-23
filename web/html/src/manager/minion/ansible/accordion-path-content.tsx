import * as React from "react";
import { AnsiblePath } from "./ansible-path-type";
import Network from "utils/network";
import { Messages, Utils } from "components/messages";
import { Loading } from "components/utils/Loading";

type PropsType = {
  path: AnsiblePath;
};

type StateType = {
  open: boolean; 
  content: any;
  errors: string[];
  loading: boolean;
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
      content: null,
      errors: [],
      loading: false,
    };
  }

  onToggle() {
    const path: AnsiblePath = this.props.path;
    if (!this.state.open && this.state.content === null) {
      this.setState({ loading: true });
      Network.get(getURL(path))
      .promise.then(data => {
        if (data.success) {
          this.setState({ content: ["element 1", "element 2"] });
        }
        else {
          this.setState({ errors: data.messages });
        }
        this.setState({ open: true, loading: false });
      });
    }
    else {
      this.setState({ open: false, errors: [] });
    }
  }

  render() {
    const header =
      <div className="panel-heading pointer" onClick={() => this.onToggle()}>
        <h6>
          <i className={this.state.open || this.state.loading ? "fa fa-chevron-up" : "fa fa-chevron-right"} />
          { this.props.path.path }
        </h6>
      </div>;

    const errors = this.state.errors.length > 0 ? <Messages items={Utils.error(this.state.errors)} /> : null;
    return (
      <div className="panel panel-default">
        {header}
        <div>
          {
            this.state.loading?
              <Loading text={t("Loading content..")} />
              :
              this.state.open ?
                <>
                  {errors}
                  <ul>
                    {
                      this.state.content?.map((element: string) =>
                        <li key={element}>
                          {
                            this.props.path.type === "playbook" ?
                              <a>{element}</a>
                            : element
                          }
                        </li>
                    )}
                  </ul>
                </>
                : null
          }
        </div>
      </div>
    )
  }
};

export default AccordionPathContent;
