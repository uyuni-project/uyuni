import * as React from "react";
import SpaRenderer from "core/spa/spa-renderer";
import { Messages, Utils } from "components/messages";
import Network from "utils/network";
import { AnsiblePath } from "./ansible-path-type";
import AccordionPathContent, { PlaybookDetails } from "./accordion-path-content";
import { Loading } from "components/utils/Loading";
import SchedulePlaybook from "./schedule-playbook";

type PropsType = {
  minionServerId: number;
  pathContentType: string;
};

type StateType = {
  minionServerId: number;
  pathContentType: string;
  pathList: AnsiblePath[];
  selectedPlaybook: PlaybookDetails | null;
  errors: string[];
  loading: boolean;
};

class AnsiblePathContent extends React.Component<PropsType, StateType> {
  constructor(props) {
    super(props);

    this.state = {
      minionServerId: props.minionServerId,
      pathContentType: props.pathContentType,
      pathList: [],
      selectedPlaybook: null,
      errors: [],
      loading: true
    };

    Network.get("/rhn/manager/api/systems/details/ansible/paths/" + props.pathContentType + "/" + props.minionServerId)
    .then(blob => {
      if (blob.success) {
        this.setState({ pathList: blob.data, loading: false});
      }
      else {
        this.setState({ errors: [t("An error occurred while loading data. Please check server logs.")], loading: false});
      }
    });
  }

  render () {
    if (this.state.selectedPlaybook) {
      return (
        <SchedulePlaybook
          playbook={this.state.selectedPlaybook}
          onBack={() => this.setState({selectedPlaybook: null})} />
      );
    }

    const errors = this.state.errors.length > 0 ? <Messages items={Utils.error(this.state.errors)} /> : null;
    return (
      <div>
        {errors}
          {
            this.state.loading ?
              <Loading text={t("Loading..")} />
              :
              this.state.pathList?.length > 0 ?
                this.state.pathList.map(p => <AccordionPathContent key={p.id} path={p} onSelectPlaybook={(p) => this.setState({selectedPlaybook: p})} /> )
                :
                <div>
                  {t("Nothing configured. Please add paths in the ")}
                  <a href={"/rhn/manager/systems/details/ansible/control-node?sid=" + this.state.minionServerId}>Control Node Configuration</a>
                </div>
        }
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
