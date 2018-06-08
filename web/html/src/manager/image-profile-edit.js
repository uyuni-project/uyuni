'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Panel = require("../components/panel").Panel;
const Messages = require("../components/messages").Messages;
const Network = require("../utils/network");
const {SubmitButton, Button} = require("../components/buttons");
const Input = require("../components/input");
const Validation = require("../components/validation");
const Utils = require("../utils/functions").Utils;

/* global profileId, customDataKeys, activationKeys */

const typeMap = {
  "dockerfile": { name: "Dockerfile", storeType: "registry" }
};

const msgMap = {
  "invalid_type": "Invalid image type."
};

class CreateImageProfile extends React.Component {

  constructor(props) {
    super(props);
    this.defaultModel = {
      imageType: "dockerfile",
      customData: {}
    };

    this.state = {
      imageTypes: [
        "dockerfile"
      ],
      model: Object.assign({}, this.defaultModel),
      imageStores: [],
      messages: [],
      customData: {}
    };

    ["handleTokenChange", "handleImageTypeChange", "isLabelValid", "onUpdate",
      "onCreate", "onFormChange", "onValidate", "clearFields"]
      .forEach(method => this[method] = this[method].bind(this));

    this.getImageStores(typeMap[this.state.model.imageType].storeType);
    if(this.isEdit()) {
      this.setValues(profileId);
    }
  }

  isEdit() {
    return profileId ? true : false;
  }

  setValues(id) {
    Network.get("/rhn/manager/api/cm/imageprofiles/" + id).promise.then(res => {
      if(res.success) {
        var data = res.data;
        this.setState({
          model: {
            label: data.label,
            activationKey: data.activationKey ? data.activationKey.key : undefined,
            path: data.path,
            imageType: data.imageType,
            imageStore: data.store
          },
          customData: data.customData,
          initLabel: data.label
        });
        this.getChannels(data.activationKey.key);
      } else {
        window.location = "/rhn/manager/cm/imageprofiles/create";
      }
    });
  }

  getBounceUrl() {
    return encodeURIComponent("/rhn/manager/cm/imageprofiles/" + (this.isEdit() ? "edit/" + profileId : "create"));
  }

  getChannels(token) {
    if(!token) {
      this.setState({
        channels: undefined
      });
      return;
    }

    Network.get("/rhn/manager/api/cm/imageprofiles/channels/" + token).promise.then(res => {
      // Prevent out-of-order async results
      if(res.activationKey != this.state.model.activationKey)
        return false;

      this.setState({
        channels: res
      });
    });
  }

  handleTokenChange(event) {
    this.getChannels(event.target.value);
  }

  handleImageTypeChange(event) {
    this.getImageStores(typeMap[event.target.value].storeType);
  }

  addCustomData(label) {
    if(label) {
      const data = this.state.customData;
      data[label] = "";

      this.setState({
        customData: data
      });
    }
  }

  removeCustomData(label) {
    if(label) {
      const data = this.state.customData;
      delete data[label];

      this.setState({
        customData: data
      });
    }
  }

  isLabelValid(label) {
    if(this.state.initLabel && this.state.initLabel === label) {
      // Initial state (on edit), always valid.
      return true;
    }

    // Should not contain ':' character
    const isValid = label.indexOf(':') === -1;

    // Check for uniqueness
    return Network.get("/rhn/manager/api/cm/imageprofiles/find/" + label)
      .promise.then(res => !res.success && isValid).catch(() => false);
  }

  onUpdate(model) {
    if(!this.isEdit()) {
      return false;
    }

    Object.assign(model, {customData: this.state.customData});

    return Network.post(
      "/rhn/manager/api/cm/imageprofiles/update/" + profileId,
      JSON.stringify(model),
      "application/json"
    ).promise.then(data => {
      if(data.success) {
        Utils.urlBounce("/rhn/manager/cm/imageprofiles");
      } else {
        this.setState({
          messages: <Messages items={data.messages.map(msg => {
            return {severity: "error", text: msgMap[msg]};
          })}/>
        });
      }
    });
  }

  onCreate(model) {
    if(this.isEdit()) {
      return false;
    }

    Object.assign(model, {customData: this.state.customData});

    return Network.post(
      "/rhn/manager/api/cm/imageprofiles/create",
      JSON.stringify(model),
      "application/json"
    ).promise.then(data => {
      if(data.success) {
        Utils.urlBounce("/rhn/manager/cm/imageprofiles");
      } else {
        this.setState({
          messages: <Messages items={data.messages.map(msg => {
            return {severity: "error", text: msgMap[msg]};
          })}/>
        });
      }
    });
  }

  onFormChange(model) {
    this.setState({
      model: model
    });
  }

  onValidate(isValid) {
    this.setState({
      isInvalid: !isValid
    });
  }

  clearFields() {
    this.setState({
      model: Object.assign({}, this.defaultModel)
    });
  }

  getImageStores(type) {
    Network.get("/rhn/manager/api/cm/imagestores/type/" + type, "application/json").promise
      .then(data => {
        this.setState({
          imageStores: data
        });
      });
  }

  renderTypeInputs(type) {
    switch (type) {
    case "dockerfile":
      return [
        <Input.Select key="imageStore" name="imageStore" label={t("Target Image Store")} required
          labelClass="col-md-3" divClass="col-md-6" invalidHint={
            <span>Target Image Store is required.&nbsp;<a href={"/rhn/manager/cm/imagestores/create" + "?url_bounce=" + this.getBounceUrl()}>Create a new one</a>.</span>
          }
        >
          <option value="" disabled key="0">{t("Select an image store")}</option>
          {
            this.state.imageStores.map(k =>
              <option key={k.id} value={k.label}>{ k.label }</option>
            )
          }
        </Input.Select>,
        <Input.Text key="path" name="path" label={t("Path")} required hint={<span>Format: <em>giturl#branch:dockerfile_location</em></span>} labelClass="col-md-3" divClass="col-md-6"/>
      ];
    default:
      return <div>If you see this please report a bug.</div>;
    }
  }

  renderTokenSelect() {
    const hint = this.state.channels && (
      this.state.channels.base ?
        <ul className="list-unstyled">
          <li>{this.state.channels.base.name}</li>
          <ul>
            {
              this.state.channels.children.map(c => <li key={c.id}>{c.name}</li>)
            }
          </ul>
        </ul>
        :
        <span><em>{t("There are no channels assigned to this key.")}</em></span>
    );

    return (
      <Input.Select name="activationKey" label={t("Activation Key")}
        onChange={this.handleTokenChange} labelClass="col-md-3" divClass="col-md-6"
        hint={hint}>
        <option key="0" value="">None</option>
        {
          activationKeys.map(k =>
            <option key={k} value={k}>{k}</option>
          )
        }
      </Input.Select>
    );
  }

  renderCustomDataFields() {
    const fields = Object.entries(this.state.customData).map(d => {
      const key = customDataKeys.find(k => k.label === d[0]);

      return key && (
        <Input.FormGroup>
          <Input.Label className="col-md-3" name={key.label}/>
          <div className="col-md-6">
            <div className="input-group">
              <input name={key.label} className="form-control input-sm" type="text" value={this.state.customData[key.label]}
                onChange={(event) => {
                  const target = event.target;

                  let data = this.state.customData;
                  data[target.name] = target.value;

                  this.setState({
                    customData: data
                  });
                }}
              />
              <span className="input-group-btn">
                <Button title={t("Remove entry")} icon="fa-minus" className="btn-default btn-sm" handler={() => this.removeCustomData(key.label)}/>
              </span>
            </div>
          </div>
        </Input.FormGroup>);
    });

    const select = <Input.FormGroup>
      <Input.Label className="col-md-3" name={t("Custom Info Values")}/>
      <div className="col-md-6">
        <select value="0" onChange={(e) => this.addCustomData(e.target.value)} className="form-control">
          <option key="0" disabled="disabled">{t("Create additional custom info values")}</option>
          {
            customDataKeys
              .filter(k => !Object.keys(this.state.customData).includes(k.label))
              .map(k => <option key={k.label} value={k.label}>{ k.label }</option>)
          }
        </select>
      </div>
    </Input.FormGroup>;

    return [select, fields];
  }

  renderButtons() {
    var buttons = [
      <Button key="clear-btn" id="clear-btn" className="btn-default pull-right" icon="fa-eraser" text={t("Clear fields")} handler={this.clearFields}/>
    ];
    if(this.isEdit()) {
      buttons.unshift(<SubmitButton key="update-btn" id="update-btn" className="btn-success" icon="fa-edit" text={t("Update")} disabled={this.state.isInvalid}/>);
    } else {
      buttons.unshift(<SubmitButton key="create-btn" id="create-btn" className="btn-success" icon="fa-plus" text={t("Create")} disabled={this.state.isInvalid}/>);
    }

    return buttons;
  }

  render() {
    return (
      <Panel title={this.isEdit() ? t("Edit Image Profile: '" + this.state.initLabel + "'") : t("Create Image Profile")} icon="fa fa-pencil">
        {this.state.messages}
        <Input.Form model={this.state.model} className="image-profile-form"
          onChange={this.onFormChange}
          onSubmit={(e) => this.isEdit() ? this.onUpdate(e) : this.onCreate(e)}
          onValidate={this.onValidate}>
          <Input.Text name="label" label={t("Label")} required validators={[this.isLabelValid, Validation.isLowercase()]} invalidHint={t("Label is required and must be a unique lowercase string and it cannot include any colons (:).")} labelClass="col-md-3" divClass="col-md-6"/>
          <Input.Select name="imageType" label={t("Image Type")} required labelClass="col-md-3" divClass="col-md-6" onChange={this.handleImageTypeChange} disabled={this.isEdit()}>
            { this.state.imageTypes.map(k =>
              <option key={k} value={k}>{ typeMap[k].name }</option>) }
          </Input.Select>
          { this.renderTypeInputs(this.state.model.imageType) }
          { this.renderTokenSelect() }
          <hr/>
          { this.renderCustomDataFields() }
          <div className="form-group">
            <div className="col-md-offset-3 col-md-6">
              { this.renderButtons() }
            </div>
          </div>
        </Input.Form>
      </Panel>
    )
  }
}

ReactDOM.render(
  <CreateImageProfile />,
  document.getElementById('image-profile-edit')
)
