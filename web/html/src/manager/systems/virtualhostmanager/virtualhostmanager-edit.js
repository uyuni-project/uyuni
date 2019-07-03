/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const {SubmitButton, Button} = require("components/buttons");
const { Form } = require('components/input/Form');
const { FormGroup } = require('components/input/FormGroup');
const { Label } = require('components/input/Label');
const { Password } = require('components/input/Password');
const { Text } = require('components/input/Text');
const { Select } = require('components/input/Select');
const Network = require("utils/network");
const {Messages} = require("components/messages");
const MessagesUtils = require("components/messages").Utils;
const {Utils} = require("utils/functions");

class VirtualHostManagerEdit extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            model: {
                gathererModule: this.props.type
            },
            messages: []
        };

        ["onFormChange", "onValidate", "clearFields", "renderForm",
         "onCreate", "onUpdate", "renderKubernetesForm", "renderModuleParamsForm",
         "handleKubeconfigUpload", "bindForm", "setKubeconfigContexts",
         "handleResponseError"]
            .forEach(method => this[method] = this[method].bind(this));

        if(this.isEdit()) {
            this.setValues(this.props.item);
        }
    }

    UNSAFE_componentWillMount() {
        Network.get("/rhn/manager/api/vhms/module/" + this.props.type.toLowerCase() + "/params")
            .promise.then(data => {
                this.setState({vhmParams: data.data});
            })
            .catch(this.handleResponseError);
    }

    handleResponseError(jqXHR) {
        this.setState({
            messages:  Network.responseErrorMessage(jqXHR)
        });
    }

    setValues(item) {
        var m = {};
        m["id"] = item.id;
        m["label"] = item.label;
        m["gathererModule"] = item.gathererModule;
        Object.keys(item.config).forEach(cfg => {
            m["module_" + cfg] = item.config[cfg]
        });
        if (item.credentials) {
            m["module_username"] = item.credentials.username;
        }
        if (this.props.type.toLowerCase()  === "kubernetes") {
            this.setKubeconfigContexts(item.id);
        }

        Object.assign(this.state, {model: m});
    }

    setKubeconfigContexts(id) {
        Network.get("/rhn/manager/api/vhms/kubeconfig/" + id + "/contexts")
            .promise.then(data => {
                this.setState({
                    model: Object.assign(this.state.model, {contexts: data.data})
                });
            })
            .catch(this.handleResponseError);
    }


    isEdit() {
        return this.props.item ? true : false;
    }

    onUpdate(model) {
        if(!this.isEdit()) {
            return false;
        }
        let request = null;
        if (this.props.type.toLowerCase()  === "kubernetes") {
            const formData = new FormData(this.form);
            if (formData.get('module_context') === "<default>") {
                // Remove '<default>' placeholder for submit
                formData.set('module_context', "");
            }
            request = Network.post("/rhn/manager/api/vhms/update/kubernetes",
                formData, false, false);
        } else {
            request = Network.post("/rhn/manager/api/vhms/update/" + this.state.model.id,
                $(this.form).serialize());
        }

        return request.promise.then(data => {
            Utils.urlBounce("/rhn/manager/vhms");
        })
        .catch(this.handleResponseError);
    }

    onCreate(model) {
        if(this.isEdit()) {
            return false;
        }
        let request = null;
        if (this.props.type.toLowerCase()  === "kubernetes") {
            const formData = new FormData(this.form);
            if (formData.get('module_context') === "<default>") {
                // Remove '<default>' placeholder for submit
                formData.set('module_context', "");
            }
            request = Network.post("/rhn/manager/api/vhms/create/kubernetes",
                formData, false, false);
        } else {
            request = Network.post("/rhn/manager/api/vhms/create",
                $(this.form).serialize());
        }

        return request.promise.then(data => {
                Utils.urlBounce("/rhn/manager/vhms");
            })
            .catch(this.handleResponseError);
    }

    onFormChange(model) {
        this.setState({
            model: model
        });
    }

    onValidate(isValid) {
        if (this.props.type.toLowerCase() === "kubernetes" && !this.isEdit()) {
            this.setState({
                isInvalid: !isValid || !this.state.validKubeconfig
            });
        } else {
            this.setState({
                isInvalid: !isValid
            });
        }
    }

    clearFields() {
        this.setState({
            model: {}
        });
    }

    renderButtons() {
        var buttons = [
          <div className="btn-group pull-right">
            <Button id="back" className="btn-default" icon="fa-chevron-left" text={t("Back")}  title={t("Back")} handler={this.props.onCancel}/>
            <Button id="clear-btn" className="btn-default" icon="fa-eraser" text={t("Clear fields")} handler={this.clearFields}/>
          </div>
        ];
        if(this.isEdit()) {
            buttons.unshift(<SubmitButton id="update-btn" className="btn-success" icon="fa-edit" text={t("Update")} disabled={this.state.isInvalid}/>);
        } else {
            buttons.unshift(<SubmitButton id="create-btn" className="btn-success" icon="fa-plus" text={t("Create")} disabled={this.state.isInvalid}/>);
        }

        return buttons;
    }

    paramField(name, defaultValue) {
        let required = this.isEdit() ? name != "password" && name != "username" : true;
        if (name.toLowerCase() === "password") {
            return (<Password name={"module_" + name} label={Utils.capitalize(name)} required={required} labelClass="col-md-3" divClass="col-md-6"
                            hint={ this.isEdit() ? "Fill this field to change the password." : null }/>);
        } else {
            return (<Text name={"module_" + name} label={Utils.capitalize(name)} required={required} labelClass="col-md-3" divClass="col-md-6"
                            />);
        }

    }

    renderModuleParamsForm() {
        if (!this.state.vhmParams) {
            return null;
        }
        var fields = Object.keys(this.state.vhmParams)
            .map(param => this.paramField(param, this.state.vhmParams[param]));

        fields.unshift(
            <Text name="label" label={t("Label")} required labelClass="col-md-3" divClass="col-md-6"/>
            );

        fields.unshift(
            <Text name="gathererModule" label={t("Gatherer module")} disabled labelClass="col-md-3" divClass="col-md-6"/>
            );
        return (
            <div>
                { fields }
            </div>
        );
    }

    handleKubeconfigUpload(event) {
        let kubeconfig = event.target.files[0];
        let formData = new FormData();
        formData.append("kubeconfig", kubeconfig);
        Network.post("/rhn/manager/api/vhms/kubeconfig/validate", formData, false, false)
            .promise
            .then(res => {
                const data = res.data;
                if(data.currentContext === "") {
                    // Replace unnamed context with '<default>' to differ it from empty choice
                    data.currentContext = "<default>";
                }
                if (data.contexts) {
                    this.setState({
                        messages: null,
                        validKubeconfig: true,
                        model: Object.assign(this.state.model, {
                               contexts: data.contexts,
                               module_context: data.currentContext
                        })
                    });
                }
            })
            .catch(jqXHR => {
                this.setState({
                    validKubeconfig: false
                });
                this.handleResponseError(jqXHR);
            });
    }

    renderKubernetesForm() {
        var contextSelect;
        if (this.state.model.contexts) {
            contextSelect = <Select name="module_context" label={t("Current Context")} required labelClass="col-md-3" divClass="col-md-6"
                value={this.state.model.module_context}>
                <option value="">---</option>
            { this.state.model.contexts.map(k => k === "" ? "<default>" : k).map(k =>
                <option key={k} value={k} selected={k === this.state.model.module_context ? true : null }>{k}</option>) }
            </Select>
        }

        return (
            <div>
                <Text name="label" label={t("Label")} required labelClass="col-md-3" divClass="col-md-6"/>
                <FormGroup>
                    <Label name={t("Kubeconfig file")} className="col-md-3" required={!this.isEdit()}/>
                    <div className="col-md-6">
                        <input name="module_kubeconfig" type="file" onChange={this.handleKubeconfigUpload} className="col-md-6" />
                    </div>
                </FormGroup>
                { contextSelect }
            </div>
        );
    }

    renderForm() {
        if (this.props.type.toLowerCase()  === "kubernetes") {
            return this.renderKubernetesForm();
        } else if (this.props.type) {
            return this.renderModuleParamsForm();
        }
    }

    bindForm(f) {
        this.form = f;
    }

    render() {
        return (
            <Form model={this.state.model} className="virtualhostmanager-form"
                onChange={this.onFormChange}
                onSubmit={(e) => this.isEdit() ? this.onUpdate(e) : this.onCreate(e)}
                onValidate={this.onValidate}
                formRef={ this.bindForm }
            >
                { this.state.messages ?
                    <Messages items={this.state.messages}/> : null }
                <input type="hidden" name="module" value={this.props.type.toLowerCase()}/>
                { this.state.model.id ?
                   <input type="hidden" name="id" value={this.state.model.id }/>
                   : null }
                {
                    this.props.type ? this.renderForm() : null
                }
                <div className="form-group">
                    <div className="col-md-offset-3 col-md-6">
                        {this.renderButtons()}
                    </div>
                </div>
            </Form>
        );
    }

}

module.exports = {
    VirtualHostManagerEdit: VirtualHostManagerEdit
};
