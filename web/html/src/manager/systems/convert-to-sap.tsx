import * as React from "react";

import { default as ReactSelect } from "react-select";

import SpaRenderer from "core/spa/spa-renderer";

import { Button, SubmitButton } from "components/buttons";
import { Form } from "components/input/Form";
import { FormGroup } from "components/input/FormGroup";
import { Label } from "components/input/Label";
import { Select } from "components/input/Select";
import { Text } from "components/input/Text";
import { Messages } from "components/messages";
import { TopPanel } from "components/panels/TopPanel";
import { InnerPanel } from "components/panels/InnerPanel";

import { Utils } from "utils/functions";
import { DEPRECATED_unsafeEquals } from "utils/legacy";
import Network from "utils/network";

// See com/suse/manager/webui/templates/audit/create-tailoring-file.jade

declare global {
  interface Window {
    profileId?: number;
    activationKeys?: any;
    customDataKeys?: any;
    childChannels?: any;
    activationKeys?: any;
  }
}
type Props = {};
type State = {
  model: any;
  isInvalid?: boolean;
  childChannels: any;
};

class ConvertToSAP extends React.Component<Props, State> {
  form?: HTMLFormElement;
  constructor(props) {
    super(props);
    this.state = {
           model: Object.assign({}, this.defaultModel),
           childChannels: [],
         };
         this.setValues();
  }

  setValues() {
      Network.get("/rhn/manager/systems/details/convert-to-sles-for-sap-get-channels").then((res) => {
        if (res.success) {
          var data = res.data;
          console.log(data);
          this.setState({
            childChannels: data,
          });
        }
      });
    }
  onConvert = (model) => {
     const formData = new FormData(this.form);
     Network.post("/rhn/manager/systems/details/convert-to-sles-for-sap-final", formData, "multipart/form-data", false)
     .then((res) => {
          console.log(res);
       });
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

  renderButtons() {
    var buttons = [
      <SubmitButton
                key="upload-btn"
                id="upload-btn"
                className="btn-success"
                icon="fa-plus"
                text={t("Convert")}
              />,
    ];
    return buttons;
  }
   bindForm = (form: HTMLFormElement) => {
      this.form = form;
    };

  render() {
    return (
      <TopPanel
         title= {t("Convert to SLES for SAP")}
         icon="spacewalk-icon-manage-configuration-files"
       >
        <p>{t("Transform your SLES machine into SLES for SAP with this powerful feature!")}</p>
        <Form
          model={this.state.model}
          className="tailoring-file-form"
          onChange={this.onFormChange}
          onSubmit={(e) => (this.onConvert(e))}
          onValidate={this.onValidate}
          formRef={this.bindForm}
        >


          <hr />
                  <div className="col-md-6">
                  {
                        Array.from(this.state.childChannels.values()).map((c) => (
                          <div className="checkbox">
                            <input
                              type="checkbox"
                              value={c.id}
                              checked
                              disabled={false}
                            />
                            <label>{c.name}</label>

                          </div>
                        ))}


                        </div>
          <div className="form-group">
            <div className="col-md-offset-3 col-md-6">{this.renderButtons()}</div>
          </div>
        </Form>
      </TopPanel>
    );
  }
}
export const renderer = () => {
  SpaRenderer.renderNavigationReact( <ConvertToSAP/>, document.getElementById("convert-to-sles-for-sap"));
}