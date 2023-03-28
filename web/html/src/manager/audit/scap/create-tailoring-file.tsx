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

// See java/code/src/com/suse/manager/webui/templates/content_management/edit-profile.jade
declare global {
  interface Window {
    profileId?: number;
    activationKeys?: any;
    customDataKeys?: any;
    imageTypesDataFromTheServer?: any;
  }
}

const typeMap = {
  dockerfile: { name: "Dockerfile", storeType: "registry" },
  kiwi: { name: "Kiwi", storeType: "os_image" },
};

const msgMap = {
  invalid_type: "Invalid image type.",
  activation_key_required: "Please give an activation key",
  "": "There was an error.",
};

type Props = {};

type State = {
  imageTypes: any;
  model: any;
  imageStores: any;
  messages: any;
  customData: any;
  initLabel?: any;
  channels?: any;
  storeUri?: any;
  isInvalid?: boolean;
};

class TailoringFile extends React.Component<Props, State> {
  form?: HTMLFormElement;

  constructor(props) {
    super(props);
    this.defaultModel = {
      imageType: "dockerfile",
      customData: {},
    };

    this.state = {
      imageTypes: window.imageTypesDataFromTheServer,
      model: Object.assign({}, this.defaultModel),
      imageStores: [],
      messages: [],
      customData: {},
    };

    //this.getImageStores(typeMap[this.state.model.imageType].storeType);
    if (this.isEdit()) {
      this.setValues(window.profileId);
    }
  }

  isEdit() {
    return window.profileId ? true : false;
  }

  setValues(id) {
    Network.get("/rhn/manager/api/cm/imageprofiles/" + id).then((res) => {
      if (res.success) {
        var data = res.data;
        this.setState({
          model: {
            label: data.label,
            activationKey: data.activationKey ? data.activationKey.key : undefined,
            path: data.path,
            kiwiOptions: data.kiwiOptions,
            imageType: data.imageType,
            imageStore: data.store,
          },
          customData: data.customData,
          initLabel: data.label,
        });
        this.getChannels(data.activationKey.key);
        //this.getImageStores(typeMap[data.imageType].storeType);
        this.handleImageStoreChange(undefined, data.store);
      } else {
        window.location.href = "/rhn/manager/cm/imageprofiles/create";
      }
    });
  }

  getBounceUrl() {
    return encodeURIComponent(
      "/rhn/manager/cm/imageprofiles/" + (this.isEdit() ? "edit/" + window.profileId : "create")
    );
  }

  getChannels(token) {
    if (!token) {
      this.setState({
        channels: undefined,
      });
      return;
    }

    Network.get("/rhn/manager/api/cm/imageprofiles/channels/" + token).then((res) => {
      // Prevent out-of-order async results
      if (!DEPRECATED_unsafeEquals(res.activationKey, this.state.model.activationKey)) return false;

      this.setState({
        channels: res,
      });
    });
  }

  handleTokenChange = (name, value) => {
    this.getChannels(value);
  };

  handleImageTypeChange = (name, value) => {
    const storeType = typeMap[value].storeType;
    this.getImageStores(storeType);
  };

  handleImageStoreChange = (name, storeLabel) => {
    Network.get("/rhn/manager/api/cm/imagestores/find/" + storeLabel).then((res) => {
      this.setState({
        storeUri: res.success && res.data.uri,
      });
    });
  };

  addCustomData(label) {
    if (label) {
      const data = this.state.customData;
      data[label] = "";

      this.setState({
        customData: data,
      });
    }
  }

  removeCustomData(label) {
    if (label) {
      const data = this.state.customData;
      delete data[label];

      this.setState({
        customData: data,
      });
    }
  }

  onUpdate = (model) => {
    if (!this.isEdit()) {
      return false;
    }

    Object.assign(model, { customData: this.state.customData });

    model.label = model.label.trim();
    model.path = model.path.trim();
    return Network.post("/rhn/manager/api/cm/imageprofiles/update/" + window.profileId, model).then((data) => {
      if (data.success) {
        Utils.urlBounce("/rhn/manager/cm/imageprofiles");
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

  clearFields = () => {
    this.setState({
      model: Object.assign({}, this.defaultModel),
    });
  };

  getImageStores(type) {
    return Network.get("/rhn/manager/api/cm/imagestores/type/" + type).then((data) => {
      // Preselect store after retrieval
      const model = Object.assign({}, this.state.model, { imageStore: data[0] && data[0].label });
      const storeUri = data[0] && data[0].uri;

      this.setState({
        imageStores: data,
        model: model,
        storeUri: storeUri,
      });

      return data;
    });
  }

  renderTokenSelect(isRequired) {
    const hint =
      this.state.channels &&
      (this.state.channels.base ? (
        <ul className="list-unstyled">
          <li>{this.state.channels.base.name}</li>
          <ul>
            {this.state.channels.children.map((c) => (
              <li key={c.id}>{c.name}</li>
            ))}
          </ul>
        </ul>
      ) : (
        <span>
          <em>{t("There are no channels assigned to this key.")}</em>
        </span>
      ));

    return (
      <Select
        key="activationKey"
        name="activationKey"
        label={t("Activation Key")}
        invalidHint={t("Activation key is required for kiwi images.")}
        onChange={this.handleTokenChange}
        labelClass="col-md-3"
        divClass="col-md-6"
        hint={hint}
        required={isRequired}
        isClearable
      />
    );
  }


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
         helpUrl="reference/images/images-profiles.html"
       >
        {this.state.messages}
        <Form
          model={this.state.model}
          className="image-profile-form"
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
                    <Label name={t("Tailoring File")} className="col-md-3" required={!this.isEdit()} />
                    <div className="col-md-6">
                      <input name="module_kubeconfig" type="file" onChange={this.handleKubeconfigUpload} className="form-control" accept=".xml" />
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
  return SpaRenderer.renderNavigationReact( <TailoringFile/>, document.getElementById("scap-create-tailoring-file"));
}