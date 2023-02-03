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

import { Utils } from "utils/functions";
import { DEPRECATED_unsafeEquals } from "utils/legacy";
import Network from "utils/network";

// See java/code/src/com/suse/manager/webui/controllers/image/templates/edit-image-sync.jade
declare global {
  interface Window {
    projectId?: number;
  }
}

const msgMap = {
  not_found: "Sync project not found",
  "": "There was an error.",
};

type Props = {};

type State = {
  model: any;
  imageStores: any;
  messages: any;
  initLabel?: any;
  sourceRegistryUri?: any;
  targetRegistryUri?: any;
  isInvalid?: boolean;
};

class CreateImageSync extends React.Component<Props, State> {
  defaultModel: any;

  constructor(props) {
    super(props);
    this.defaultModel = {
      sourceRegistryUri: "",
      targetRegistryUri: "",
    };

    this.state = {
      model: Object.assign({}, this.defaultModel),
      imageStores: [],
      messages: [],
    };

    this.getImageStores();
    if (this.isEdit()) {
      this.setValues(window.projectId);
    }
  }

  isEdit() {
    return window.projectId ? true : false;
  }

  setValues(id) {
    Network.get("/rhn/manager/api/cm/imagesync/" + id).then((res) => {
      if (res.success) {
        var data = res.data;
        this.setState({
          model: {
            label: data.label,
            sourceRegistry: data.sourceRegistry,
            image: data.image,
            targetRegistry: data.targetRegistry,
          },
          initLabel: data.label,
        });
        this.getImageStores();
        this.handleSourceRegistryChange(data.sourceRegistry);
        this.handleTargetRegistryChange(data.targetRegistry);
      } else {
        window.location.href = "/rhn/manager/cm/imagesync/create";
      }
    });
  }

  getBounceUrl() {
    return encodeURIComponent(
      "/rhn/manager/cm/imagesync/" + (this.isEdit() ? "edit/" + window.projectId : "create")
    );
  }

  handleSourceRegistryChange = (registryLabel) => {
    Network.get("/rhn/manager/api/cm/imagestores/find/" + registryLabel).then((res) => {
      this.setState({
        sourceRegistryUri: res.success && res.data.uri,
      });
    });
  };

  handleTargetRegistryChange = (registryLabel) => {
    Network.get("/rhn/manager/api/cm/imagestores/find/" + registryLabel).then((res) => {
      this.setState({
        targetRegistryUri: res.success && res.data.uri,
      });
    });
  };

  isLabelValid = (label) => {
    if (this.state.initLabel && this.state.initLabel === label) {
      // Initial state (on edit), always valid.
      return true;
    }

    // Should not contain ':' character
    const isValid = label.indexOf(":") === -1;

    // Check for uniqueness
    const ret = Network.get("/rhn/manager/api/cm/imagesync/find/" + label)
      .then((res) => !res.success && isValid)
      .catch(() => false);

    return ret;
  };

  isImageValid = (image) => {
    return true;
  };

  onUpdate = (model) => {
    if (!this.isEdit()) {
      return false;
    }

    model.label = model.label.trim();
    return Network.post("/rhn/manager/api/cm/imagesync/update/" + window.projectId, model).then((data) => {
      if (data.success) {
        Utils.urlBounce("/rhn/manager/cm/imagesync");
      } else {
        this.setState({
          messages: (
            <Messages
              items={data.messages.map((msg) => {
                return { severity: "error", text: msgMap[msg] };
              })}
            />
          ),
        });
      }
    });
  };

  onCreate = (model) => {
    if (this.isEdit()) {
      return false;
    }

    model.label = model.label.trim();
    return Network.post("/rhn/manager/api/cm/imagesync/create", model).then((data) => {
      if (data.success) {
        Utils.urlBounce("/rhn/manager/cm/imagesync");
      } else {
        this.setState({
          messages: (
            <Messages
              items={data.messages.map((msg) => {
                return { severity: "error", text: msgMap[msg] };
              })}
            />
          ),
        });
      }
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

  clearFields = () => {
    this.setState({
      model: Object.assign({}, this.defaultModel),
    });
  };

  getImageStores() {
    return Network.get("/rhn/manager/api/cm/imagestores/type/registry").then((data) => {
      this.setState({
        imageStores: data,
        model: this.state.model,
      });

      return data;
    });
  }

  renderButtons() {
    var buttons = [
      <Button
        key="clear-btn"
        id="clear-btn"
        className="btn-default pull-right"
        icon="fa-eraser"
        text={t("Clear fields")}
        handler={this.clearFields}
      />,
    ];
    if (this.isEdit()) {
      buttons.unshift(
        <SubmitButton
          key="update-btn"
          id="update-btn"
          className="btn-success"
          icon="fa-edit"
          text={t("Update")}
          disabled={this.state.isInvalid}
        />
      );
    } else {
      buttons.unshift(
        <SubmitButton
          key="create-btn"
          id="create-btn"
          className="btn-success"
          icon="fa-plus"
          text={t("Create")}
          disabled={this.state.isInvalid}
        />
      );
    }

    return buttons;
  }

  render() {
    return (
      <TopPanel
        title={this.isEdit() ? t("Edit Image Sync Project: '" + this.state.initLabel + "'") : t("Create Image Sync Project")}
        icon="fa fa-pencil"
      >
        {this.state.messages}
        <Form
          model={this.state.model}
          className="image-sync-form"
          onChange={this.onFormChange}
          onSubmit={(e) => (this.isEdit() ? this.onUpdate(e) : this.onCreate(e))}
          onValidate={this.onValidate}
        >
          <Text
            name="label"
            label={t("Label")}
            required
            validators={[this.isLabelValid]}
            invalidHint={t("Label is required and must be a unique string and it cannot include any colons (:).")}
            labelClass="col-md-3"
            divClass="col-md-6"
          />
          <Select
            key="sourceRegistry"
            name="sourceRegistry"
            label={t("Source Registry")}
            required
            onChange={this.handleSourceRegistryChange}
            labelClass="col-md-3"
            divClass="col-md-6"
            hint={this.state.sourceRegistryUri}
            invalidHint={
              <span key="invalidHint">
                Source registry is required.&nbsp;
                <a href={"/rhn/manager/cm/imagestores/create?url_bounce=" + this.getBounceUrl()}>Create a new one</a>.
              </span>
            }
            isClearable
            options={this.state.imageStores.map((k) => k.label)}
          />
          <Text
            name="image"
            label={t("Image")}
            required
            validators={[this.isImageValid]}
            invalidHint={t("Image is required.")}
            labelClass="col-md-3"
            divClass="col-md-6"
          />
          <Select
            key="targetRegistry"
            name="targetRegistry"
            label={t("Target Registry")}
            required
            onChange={this.handleTargetRegistryChange}
            labelClass="col-md-3"
            divClass="col-md-6"
            hint={this.state.targetRegistryUri}
            invalidHint={
              <span key="invalidHint">
                Target registry is required.&nbsp;
                <a href={"/rhn/manager/cm/imagestores/create?url_bounce=" + this.getBounceUrl()}>Create a new one</a>.
              </span>
            }
            isClearable
            options={this.state.imageStores.map((k) => k.label)}
          />
          <hr />
          <div className="form-group">
            <div className="col-md-offset-3 col-md-6">{this.renderButtons()}</div>
          </div>
        </Form>
      </TopPanel>
    );
  }
}

export const renderer = () =>
  SpaRenderer.renderNavigationReact(<CreateImageSync />, document.getElementById("image-sync-edit"));
