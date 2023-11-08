import * as React from "react";

import { default as ReactSelect } from "react-select";

import SpaRenderer from "core/spa/spa-renderer";

import { Button, SubmitButton } from "components/buttons";
import { Form } from "components/input/form/Form";
import { FormGroup } from "components/input/FormGroup";
import { Label } from "components/input/Label";
import { Select } from "components/input/select/Select";
import { Text } from "components/input/text/Text";
import { Messages } from "components/messages/messages";
import { TopPanel } from "components/panels/TopPanel";

import { Utils } from "utils/functions";
import { DEPRECATED_unsafeEquals } from "utils/legacy";
import Network from "utils/network";

// See com/suse/manager/webui/templates/audit/create-tailoring-file.jade
type Props = {};
type State = {
  model: any;
  isInvalid?: boolean;
};

class TailoringFile extends React.Component<Props, State> {
  form?: HTMLFormElement;
  constructor(props) {
    super(props);
    this.state = {
      model: Object.assign({}, this.defaultModel),
    };
  }

  onUpload = (model) => {
    const formData = new FormData(this.form);
    Network.post("/rhn/manager/api/audit/scap/tailoring-file/create", formData, "multipart/form-data", false)
      .then((res) => {
        Utils.urlBounce("/rhn/manager/audit/scap/tailoring-files");
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
        text={t("Upload")}
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
        title={t("Upload Tailoring File")}
        icon="spacewalk-icon-manage-configuration-files"
      >
        <Form
          model={this.state.model}
          className="tailoring-file-form"
          onChange={this.onFormChange}
          onSubmit={(e) => (this.onUpload(e))}
          onValidate={this.onValidate}
          formRef={this.bindForm}
        >
          <Text
            name="name"
            label={t("Name")}
            required
            labelClass="col-md-3"
            divClass="col-md-6"
          />
          <FormGroup>
            <Label name={t("Tailoring File")} className="col-md-3" required />
            <div className="col-md-6">
              <input name="tailoring_file" type="file" onChange={this.handleTailoringFileUpload} className="form-control" accept=".xml" />
            </div>
          </FormGroup>
          <hr />
          <div className="form-group">
            <div className="col-md-offset-3 col-md-6">{this.renderButtons()}</div>
          </div>
        </Form>
      </TopPanel>
    );
  }
}
export const renderer = () => {
  return SpaRenderer.renderNavigationReact(<TailoringFile />, document.getElementById("scap-create-tailoring-file"));
}
