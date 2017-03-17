'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Panel = require("../components/panel").Panel;
const Messages = require("../components/messages").Messages;
const Network = require("../utils/network");
const {SubmitButton, Button} = require("../components/buttons");

const typeMap = {
    "dockerfile": { name: "Dockerfile", storeType: "registry" }
};

const msgMap = {
    "invalid_type": "Invalid image type."
};

class CreateImageProfile extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            imageTypes: [
                "dockerfile"
            ],
            messages: [],
            imageStores: [],
            imageType: "dockerfile",
            imageStore: "",
            path: "",
            label: "",
            activationKey: "",
            customData: {}
        };

        ["getChannels", "handleTokenChange", "handleChange", "handleImageTypeChange",
            "clearFields", "getImageStores"]
                .forEach(method => this[method] = this[method].bind(this));

        this.getImageStores(typeMap[this.state.imageType].storeType);
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
                    label: data.label,
                    activationKey: data.activationKey ? data.activationKey.key : undefined,
                    path: data.path,
                    imageType: data.imageType,
                    imageStore: data.store,
                    initLabel: data.label,
                    customData: data.customData
                });
                this.getChannels(data.activationKey.key);
            } else {
                window.location = "/rhn/manager/cm/imageprofiles/create";
            }
        });
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
            if(res.activationKey != this.state.activationKey)
                return false;

            this.setState({
                channels: res
            });
        });
    }

    handleTokenChange(event) {
        this.handleChange(event);
        this.getChannels(event.target.value);
    }

    handleChange(event) {
        const target = event.target;

        this.setState({
            [target.name]: target.value
        });
    }

    handleImageTypeChange(event) {
        this.handleChange(event);
        const val = event.target.value;

        this.getImageStores(typeMap[val].storeType);
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

    onUpdate(event) {
        event.preventDefault();

        if(!this.isEdit()) {
            return false;
        }

        const payload = {
            label: this.state.label,
            path: this.state.path,
            imageType: this.state.imageType,
            storeLabel: this.state.imageStore,
            activationKey: this.state.activationKey,
            customData: this.state.customData
        };

        return Network.post(
            "/rhn/manager/api/cm/imageprofiles/" + profileId,
            JSON.stringify(payload),
            "application/json"
        ).promise.then(data => {
            if(data.success) {
                window.location = "/rhn/manager/cm/imageprofiles";
            } else {
                this.setState({
                    messages: <Messages items={data.messages.map(msg => {
                        return {severity: "error", text: msgMap[msg]};
                    })}/>
                });
            }
        });
    }

    onCreate(event) {
        event.preventDefault();

        if(this.isEdit()) {
            return false;
        }

        const payload = {
            label: this.state.label,
            path: this.state.path,
            imageType: this.state.imageType,
            storeLabel: this.state.imageStore,
            activationKey: this.state.activationKey,
            customData: this.state.customData
        };
        return Network.post(
            "/rhn/manager/api/cm/imageprofiles",
            JSON.stringify(payload),
            "application/json"
        ).promise.then(data => {
            if(data.success) {
                window.location = "/rhn/manager/cm/imageprofiles";
            } else {
                this.setState({
                    messages: <Messages items={data.messages.map(msg => {
                        return {severity: "error", text: msgMap[msg]};
                    })}/>
                });
            }
        });
    }

    clearFields() {
      this.setState({
          label: "",
          path: "",
          imageStore: "",
          activationKey: "",
          customData: {}
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

    renderField(name, label, value, hidden = false, required = true, placeholder = "") {
        return <div className="form-group">
            <label className="col-md-3 control-label">
                {label}
                { required ? <span className="required-form-field"> *</span> : undefined }
                :
            </label>
            <div className="col-md-6">
                <input name={name} placeholder={placeholder} className="form-control" type={hidden ? "password" : "text"} value={value} onChange={this.handleChange}/>
            </div>
        </div>;
    }

    renderTypeInputs(type, state) {
        switch (type) {
            case "dockerfile":
                return [
                    this.renderStoreSelect(),
                    this.renderField("path", t("Path"), this.state.path, false, true, "giturl#branch:dockerfile_location")
                ];
            default:
                return <div>if you see this please report a bug</div>;
        }
    }

    renderImageTypeSelect() {
        return <div className="form-group">
            <label className="col-md-3 control-label">Image Type<span className="required-form-field"> *</span>:</label>
            <div className="col-md-6">
               <select value={this.state.imageType} onChange={this.handleImageTypeChange} className="form-control" name="imageType" disabled={this.isEdit() ? "disabled" : undefined }>
                 {
                     this.state.imageTypes.map(k =>
                        <option key={k} value={k}>{ typeMap[k].name }</option>
                     )
                 }
               </select>
            </div>
        </div>;
    }

    renderTokenSelect() {
        return <div className="form-group">
            <label className="col-md-3 control-label">Activation Key:</label>
            <div className="col-md-6">
               <select value={this.state.activationKey} onChange={this.handleTokenChange} className="form-control" name="activationKey">
                 <option key="0" value="">None</option>
                 {
                     activationKeys.map(k =>
                        <option key={k} value={k}>{k}</option>
                     )
                 }
               </select>
               { this.state.channels &&
                    ( this.state.channels.base ?
                        <div className="help-block">
                            <ul className="list-unstyled">
                                <li>{this.state.channels.base.name}</li>
                                <ul>
                                    {
                                        this.state.channels.children.map(c => <li>{c.name}</li>)
                                    }
                                </ul>
                            </ul>
                        </div>
                    :
                        <div className="help-block">
                            <span><em>{t("There are no channels assigned to this key.")}</em></span>
                        </div>
                    )
               }
            </div>
        </div>;
    }

    renderStoreSelect() {
        return <div className="form-group">
            <label className="col-md-3 control-label">Target Image Store<span className="required-form-field"> *</span>:</label>
            <div className="col-md-6">
               <select value={this.state.imageStore} onChange={this.handleChange} className="form-control" name="imageStore">
                 <option key="0" disabled="disabled">{t("Select an image store")}</option>
                 {
                     this.state.imageStores.map(k =>
                        <option key={k.id} value={k.label}>{ k.label }</option>
                     )
                 }
               </select>
            </div>
        </div>;
    }

    renderCustomDataFields() {
        const fields = Object.entries(this.state.customData).map(d => {
            const key = customDataKeys.find(k => k.label === d[0]);

            return key && (
                <div className="form-group">
                    <label className="col-md-3 control-label">
                        {key.label}:
                    </label>
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
                </div>);
        });

        const select = <div className="form-group">
                <label className="col-md-3 control-label">Custom Info Values:</label>
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
            </div>;

        return [select, fields];
    }

    renderButtons() {
        var buttons = [
            <Button id="clear-btn" className="btn-default pull-right" icon="fa-eraser" text={t("Clear fields")} handler={this.clearFields}/>
        ];
        if(this.isEdit()) {
            buttons.unshift(<SubmitButton id="update-btn" className="btn-success" icon="fa-edit" text={t("Update")}/>);
        } else {
            buttons.unshift(<SubmitButton id="create-btn" className="btn-success" icon="fa-plus" text={t("Create")}/>);
        }

        return buttons;
    }

    render() {
        return (
        <Panel title={this.isEdit() ? t("Edit Image Profile: '" + this.state.initLabel + "'") : t("Create Image Profile")} icon="fa fa-pencil">
            {this.state.messages}
            <form className="image-profile-form" onSubmit={(e) => this.isEdit() ? this.onUpdate(e) : this.onCreate(e)}>
                <div className="form-horizontal">
                    { this.renderField("label", t("Label"), this.state.label) }
                    { this.renderImageTypeSelect() }
                    { this.renderTypeInputs(this.state.imageType) }
                    { this.renderTokenSelect() }
                    <hr/>
                    { this.renderCustomDataFields() }
                    <div className="form-group">
                        <div className="col-md-offset-3 col-md-6">
                            { this.renderButtons() }
                        </div>
                    </div>
                </div>
            </form>
        </Panel>
        )
    }
}

ReactDOM.render(
  <CreateImageProfile />,
  document.getElementById('image-profile-edit')
)
