'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Panel = require("../components/panel").Panel;
const Messages = require("../components/messages").Messages;
const Network = require("../utils/network");
const {SubmitButton, Button} = require("../components/buttons");
const Input = require("../components/input");
const Utils = require("../utils/functions").Utils;

/* global storeId */

const typeMap = {
  "registry": "Registry",
  "os_image": "OS Image"
};

const msgMap = {};

class CreateImageStore extends React.Component {

  constructor(props) {
    super(props);
    this.defaultModel = {
      storeType: "registry",
      useCredentials: false
    };

    this.state = {
      storeTypes: [
        "registry"
      ],
      model: Object.assign({}, this.defaultModel),
      messages: []
    };

    ["setValues", "isLabelUnique", "onUpdate", "onCreate", "onFormChange", "onValidate",
      "clearFields"]
      .forEach(method => this[method] = this[method].bind(this));

    if(this.isEdit()) {
      this.setValues(storeId);
    }
  }

  isEdit() {
    return storeId ? true : false;
  }

  setValues(id) {
    Network.get("/rhn/manager/api/cm/imagestores/" + id).promise.then(res => {
      if(res.success) {
        var data = res.data;
        this.setState({
          model: data,
          initLabel: data.label
        });
      } else {
        window.location = "/rhn/manager/cm/imagestores/create";
      }
    });
  }

  isLabelUnique(label) {
    if(this.state.initLabel && this.state.initLabel === label) {
      return true;
    }

    return Network.get("/rhn/manager/api/cm/imagestores/find/" + label)
      .promise.then(res => !res.success).catch(() => false);
  }

  onUpdate(model) {
    if(!this.isEdit()) {
      return false;
    }

    return Network.post(
      "/rhn/manager/api/cm/imagestores/update/" + storeId,
      JSON.stringify(model),
      "application/json"
    ).promise.then(data => {
      if(data.success) {
        Utils.urlBounce("/rhn/manager/cm/imagestores");
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

    return Network.post(
      "/rhn/manager/api/cm/imagestores/create",
      JSON.stringify(model),
      "application/json"
    ).promise.then(data => {
      if(data.success) {
        Utils.urlBounce("/rhn/manager/cm/imagestores");
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

  renderTypeInputs(type) {
    switch (type) {
    case "registry":
      return [
        <Input.Check key="useCredentials" name="useCredentials" label={t("Use credentials")} divClass="col-md-6 col-md-offset-3"/>,
        <Input.Text key="username" name="username" label={t("Username")} labelClass="col-md-3" divClass="col-md-6" disabled={!this.state.model.useCredentials} required/>,
        <Input.Password key="password" name="password" label={t("Password")} labelClass="col-md-3" divClass="col-md-6" disabled={!this.state.model.useCredentials} required/>
      ];
    case "os_image":
      // No type-specific input for Kiwi image store
      return [];
    default:
      return <div>If you see this please report a bug.</div>;
    }
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
      <Panel title={this.isEdit() ? t("Edit Image Store: '" + this.state.initLabel + "'") : t("Create Image Store")} icon="fa fa-pencil">
        {this.state.messages}
        <Input.Form model={this.state.model} className="image-store-form"
          onChange={this.onFormChange}
          onSubmit={(e) => this.isEdit() ? this.onUpdate(e) : this.onCreate(e)}
          onValidate={this.onValidate}>
          <Input.Select labelClass="col-md-3" divClass="col-md-6" label={t("Store Type")} name="storeType" required disabled={this.isEdit()}>
            {
              this.state.storeTypes.map(k =>
                <option key={k} value={k}>{ typeMap[k] }</option>
              )
            }
          </Input.Select>
          <Input.Text name="label" label={t("Label")} required validators={this.isLabelUnique} invalidHint={t("Label is required and must be unique.")} labelClass="col-md-3" divClass="col-md-6"/>
          <Input.Text name="uri" label={t("URI")} required labelClass="col-md-3" divClass="col-md-6"/>
          { this.renderTypeInputs(this.state.model.storeType) }
          <div className="form-group">
            <div className="col-md-offset-3 col-md-6">
              { this.renderButtons() }
            </div>
          </div>
        </Input.Form>
      </Panel>
    );
  }
}

ReactDOM.render(
  <CreateImageStore />,
  document.getElementById('image-store-edit')
)
