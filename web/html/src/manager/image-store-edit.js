'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Panel = require("../components/panel").Panel;
const Messages = require("../components/messages").Messages;
const Network = require("../utils/network");
const {SubmitButton, Button} = require("../components/buttons");

const typeMap = {
    "registry": "Registry"
};

const msgMap = {};

class CreateImageStore extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            storeTypes: [
                "registry"
            ],
            storeType: "registry",
            email: "",
            username: "",
            password: "",
            init_label: "",
            use_credentials: false,
            messages: []
        };

        ["setValues", "handleChange", "handleCheckbox", "onUpdate", "onCreate", "clearFields",
         "renderField", "renderTypeInputs", "renderButtons"]
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
                    storeType: data.store_type,
                    uri: data.uri,
                    label: data.label,
                    init_label: data.label,
                    use_credentials: data.credentials ? true : false
                });

                if(data.credentials) {
                    this.setState({
                        email: data.credentials.email,
                        username: data.credentials.username,
                        password: data.credentials.password,
                    });
                }
            } else {
                window.location = "/rhn/manager/cm/imagestores/create";
            }
        });
    }

    handleChange(event) {
        const target = event.target;

        this.setState({
            [target.name]: target.value
        });
    }

    handleCheckbox(event) {
        const target = event.target;

        this.setState({
            [target.name]: target.checked
        });
    }

    onUpdate(event) {
        event.preventDefault();

        if(!this.isEdit()) {
            return false;
        }

        const payload = {
            label: this.state.label,
            uri: this.state.uri
        };

        if(this.state.use_credentials) {
            payload.credentials = {
                username: this.state.username,
                password: this.state.password,
                email: this.state.email
            }
        }

        return Network.post(
            "/rhn/manager/api/cm/imagestores/" + storeId,
            JSON.stringify(payload),
            "application/json"
        ).promise.then(data => {
            if(data.success) {
                window.location = "/rhn/manager/cm/imagestores";
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
            uri: this.state.uri
        };

        if(this.state.use_credentials) {
            payload.credentials = {
                username: this.state.username,
                password: this.state.password,
                email: this.state.email
            }
        }

        return Network.post(
            "/rhn/manager/api/cm/imagestores",
            JSON.stringify(payload),
            "application/json"
        ).promise.then(data => {
            if(data.success) {
                window.location = "/rhn/manager/cm/imagestores";
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
          uri: "",
          username: "",
          password: "",
          email: ""
      });
    }

    renderTypeInputs(type, state) {
        switch (type) {
            case "registry":
                return [
                    <div className="form-group">
                        <div className="col-md-6 col-md-offset-3">
                            <div className="checkbox">
                                <label>
                                    <input name="use_credentials" type="checkbox" checked={this.state.use_credentials} onChange={this.handleCheckbox}/>
                                    <span>Use credentials</span>
                                </label>
                            </div>
                        </div>
                    </div>,
                    this.renderField("email", t("Email"), this.state.email, false, true, this.state.use_credentials),
                    this.renderField("username", t("Username"), this.state.username, false, true, this.state.use_credentials),
                    this.renderField("password", t("Password"), this.state.password, true, true, this.state.use_credentials)
                ];
            default:
                return <div>if you see this please report a bug</div>;
        }
    }

    renderField(name, label, value, hidden = false, required = true, enabled = true) {
        return <div className="form-group">
            <label className="col-md-3 control-label">
                {label}
                { required ? <span className="required-form-field"> *</span> : undefined }
                :
            </label>
            <div className="col-md-6">
                <input name={name} className="form-control" type={hidden ? "password" : "text"} value={value} onChange={this.handleChange} disabled={!enabled}/>
            </div>
        </div>;
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
        <Panel title={this.isEdit() ? t("Edit Image Store: '" + this.state.init_label + "'") : t("Create Image Store")} icon="fa fa-pencil">
            {this.state.use_credentials}
            {this.state.messages}
            <form className="image-store-form" onSubmit={(e) => this.isEdit() ? this.onUpdate(e) : this.onCreate(e)}>
                <div className="form-horizontal">
                    <div className="form-group">
                        <label className="col-md-3 control-label">Store Type:</label>
                        <div className="col-md-6">
                           <select value={this.state.storeType} onChange={this.handleChange} className="form-control" name="imageStoreType" disabled={this.isEdit() ? "disabled" : undefined}>
                             {
                                 this.state.storeTypes.map(k =>
                                    <option key={k} value={k}>{ typeMap[k] }</option>
                                 )
                             }
                           </select>
                        </div>
                    </div>
                    { this.renderField("label", t("Label"), this.state.label) }
                    { this.renderField("uri", t("URI"), this.state.uri) }
                    { this.renderTypeInputs(this.state.storeType) }
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
  <CreateImageStore />,
  document.getElementById('image-store-edit')
)
