import * as React from "react";



import SpaRenderer from "core/spa/spa-renderer";
import { Messages, Utils } from "components/messages/messages";
import { Panel } from "components/panels/Panel";
import Network from "utils/network";
import { Button, SubmitButton } from "components/buttons";
import { Form } from "components/input/form/Form";
import { FormGroup } from "components/input/FormGroup";
import { Label } from "components/input/Label";
import { Select } from "components/input/select/Select";
import { Text } from "components/input/text/Text";
import { TopPanel } from "components/panels/TopPanel";
import { InnerPanel } from "components/panels/InnerPanel";
import { ActionSchedule } from "components/action-schedule";
import { LinkButton } from "components/buttons";
import { AceEditor } from "components/ace-editor";
import { localizedMoment } from "utils";
import { Utils as MessagesUtils } from "components/messages/messages";
import { ActionLink } from "components/links";

declare global {
  interface Window {
    profileId?: number;
    tailoringFiles?: any;
    remediation?: any;
    tailoringFiles?: any;
  }
}
type PropsType = {
};

const messagesCounterLimit = 3;

type StateType = {
  model: any;
  messages: any;
  isInvalid?: boolean;
  errors: string[];
  tailoringFiles: any;
  remediation: any;
};

function htmlDecode(input) {
  var doc = new DOMParser().parseFromString(input, "text/html");
  return doc.documentElement.textContent;
}


class RuleResultDetail extends React.Component<PropsType, StateType> {
  constructor(props) {
    super(props);

    console.log(props);
    this.state = {
      model: Object.assign({}, this.defaultModel),
      messages: [],
      errors: [],
      profiles: [],
      earliest: localizedMoment(),
      tailoringFiles: window.minions,
      remediation: htmlDecode(window.remediation),
      identifier: window.identifier,

    };

  }
  onDateTimeChanged = (date) => {
    this.setState({ earliest: date });
  };
  onFormChange = (model) => {
    this.setState({
      model: model,
    });
  };

  onValidate = (isValid) => {
    this.setState({
      isInvalid: !isValid,
    });
  };
  onRemediationChange = (remediationContent) => {
    this.setState({ remediation: remediationContent });
  };
  getProfiles(type, name) {
    return Network.get("/rhn/manager/api/audit/profiles/list/" + type + "/" + name).then((data) => {
      this.setState({
        tailoringFiles: data,
      });


      return data;
    });
  };
  clearFields = () => {
    this.setState({
      model: Object.assign({}, this.defaultModel),
    });
  };


  renderButtons() {
    var buttons = [
      <SubmitButton
        key="create-btn"
        id="create-btn"
        className="btn-success"
        text={t("Update remediation")}
        disabled={this.state.isInvalid}
      />,
      <SubmitButton
        key="clear-btn"
        id="clear-btn"
        className="btn-success pull-right"
        text={t("Apply Remediation")}
        handler={this.clearFields}
      />,
    ];
    return buttons;
  }

  onCreate = (model) => {

    return Network.post("/rhn/manager/api/audit/scap/scan/rule-apply-remediation", {
      ids: window.minions?.map((m) => m.id),
      earliest: this.state.earliest,
      remediationContent: this.state.remediation,
      ruleIdentifier: this.state.identifier,
    }).then((data) => {
      const msg = MessagesUtils.info(
        (
          <span>
            {t("Applying the remediation has been ")}
            <ActionLink id={data.value}>{t("scheduled.")}</ActionLink>
          </span>
        )
      );

      const msgs = this.state.messages.concat(msg);

      // Do not spam UI showing old messages
      while (msgs.length > messagesCounterLimit) {
        msgs.shift();
      }

      this.setState({
        messages: msgs,
      });
    })
      .catch(this.handleResponseError);

    return request;
  };
  handleResponseError = (jqXHR) => {
    this.setState({
      messages: Network.responseErrorMessage(jqXHR),
    });
  };
  render() {
    const errors = this.state.errors.length > 0 ? <Messages items={Utils.error(this.state.errors)} /> : null;
    const messages = this.state.messages.length > 0 ? <Messages items={this.state.messages} /> : null;

    const loc = window.location;
    const createLink = loc.pathname.replace("/highstate", "/recurring-states") + loc.search + "#/create";
    const buttonsLeft = [
      <LinkButton icon="fa-plus" href={createLink} className="btn-default" text={t("Create Recurring")} />,

    ];
    return (
      <div>
        {errors}
        {messages}
        <p>
          {this.state.pathContentType}
        </p>
        <Panel headingLevel="h3" title="Details of Rule Result" >
          <Form
            model={this.state.model}
            className="image-profile-form"
            onChange={this.onFormChange}
            onSubmit={(e) => (this.onCreate(e))}
            onValidate={this.onValidate}
          >
            <div className="form-group">
              <label className="col-md-3 control-label">Reference within Document:</label>
              <p className="col-md-6">{window.identifier}</p>

            </div>
            <div className="form-group">
              <label className="col-md-3 control-label">Evaluation Result:</label>
              <p className="col-md-6">{window.result}</p>
            </div>
            <div className="form-group">
              <label className="col-md-3 control-label">Parent Scan:</label>
              <a className="col-md-6" href={window.parentScanUrl} >{window.parentScanProfile}</a>
            </div>
            <div className="form-group">

              <label className="col-md-3 control-label">Remediation:</label>
              <AceEditor
                className="col-lg-6"
                id="remediationContent"
                minLines={20}
                maxLines={40}
                mode="sh"
                content={htmlDecode(window.remediation)}
                onChange={this.onRemediationChange}
              />
            </div>
            <div className="panel-body">
              <ActionSchedule
                earliest={this.state.earliest}
                onDateTimeChanged={this.onDateTimeChanged}
                systemIds={window.minions?.map((m) => m.id)}
                actionType="states.apply"
              />
            </div>
            <div className="form-group">
              <div>{this.renderButtons()}</div>
            </div>
          </Form>
        </Panel>

      </div>
    );
  }
}

export const renderer = () => {
  return SpaRenderer.renderNavigationReact(<RuleResultDetail />, document.getElementById("rule-result-detail"));
}
