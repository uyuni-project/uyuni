'use strict';

const React = require("react");
const ReactDOM = require("react-dom");

const {AsyncButton, Button} = require("../components/buttons");
const Network = require("../utils/network");
const {Messages} = require("../components/messages");
const MessagesUtils = require("../components/messages").Utils;
const {Utils} = require("../utils/functions");

const msgMap = {
  "minion_unreachable": t("Cleanup timed out. Please check if the machine is reachable."),
  "apply_result_missing" : t("No result found in state apply response.")
};

class DeleteSystem extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      messages: [],
      cleanupErr: false
    };
  }

  handleDelete = () => {
    const nocleanup = this.state.cleanupErr ? {"nocleanup": "true"} : {};
    return Network.post(`/rhn/manager/api/systems/${this.props.serverId}/delete`, $.param(nocleanup))
    .promise.then(data => {
        if (data.success) {
          // TODO redirect to systems list and show display msg
          Utils.urlBounce("/rhn/systems/Overview.do");
          // this.setState({
          //   messages: MessagesUtils.info(t("Deleted successfully."))
          // });
        } else {
          this.setState({
            cleanupErr: true,
            messages:
            MessagesUtils.error(
              [t("Minion cleanup failed: ")].concat(data.messages)
            )
          });
        }

    })
    .catch(jqXHR => {
      this.handleResponseError(jqXHR);
    });
  }

  handleResponseError = (jqXHR, arg = "") => {
    this.setState({
      messages:
            Network.responseErrorMessage(
              jqXHR,
              (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null
            )
    });
  }

  render() {
    const btnLabel = this.state.cleanupErr ?
      t("Delete Profile Without Cleanup") :
      t("Delete Profile");
    return (<span>
      { this.state.messages.length > 0 && <Messages items={this.state.messages}/> }
      <h2>Confirm System Profile Deletion</h2>
      <p>This will delete this profile <strong>permanently</strong>.</p>
      <p><strong>If you are uncertain about performing this action, please <a href="/rhn/systems/details/Overview.do?sid=1000010325">review the details</a> for this system.</strong></p>
      <p>Otherwise, confirm the immediate deletion of this system profile by clicking the button below.</p>
      <p><strong>Please click the button only once; it will take a few moments for the deletion process to complete.</strong></p>
      <hr/>
      <AsyncButton id="btn-danger"
        action={this.handleDelete}
        name={btnLabel}/>
    </span>);
  }

}

ReactDOM.render(
  <DeleteSystem serverId={serverId}/>,
  document.getElementById('delete-system')
)
