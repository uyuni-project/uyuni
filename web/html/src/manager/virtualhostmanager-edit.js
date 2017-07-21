'use strict';

const React = require("react");
const {SubmitButton, Button} = require("../components/buttons");
const Input = require("../components/input");

class VirtualHostManagerEdit extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            model: {},
            messages: []
        };

        ["onFormChange", "onValidate", "clearFields"]
            .forEach(method => this[method] = this[method].bind(this));

        if(this.isEdit()) {
            this.setValues(this.props.item);
        }
    }

    setValues(item) {
        //TODO: Set values for edit
    }

    isEdit() {
        return this.props.item ? true : false;
    }

    onUpdate(model) {
        if(!this.isEdit()) {
            return false;
        }

        return Network.post(
            "/rhn/manager/api/vhms/update/" + this.props.item.id,
            JSON.stringify(model),
            "application/json"
        ).promise.then(data => {
            if(data.success) {
                Utils.urlBounce("/rhn/manager/vhms");
            } else {
                this.props.onMessage(data.messages);
            }
        });
    }

    onCreate(model) {
        if(this.isEdit()) {
            return false;
        }

        return Network.post(
            "/rhn/manager/api/vhms/create/" + this.props.type,
            JSON.stringify(model),
            "application/json"
        ).promise.then(data => {
            if(data.success) {
                Utils.urlBounce("/rhn/manager/vhms");
            } else {
                this.props.onMessage(data.messages);
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
            model: {}
        });
    }

    renderButtons() {
        var buttons = [
            <Button id="clear-btn" className="btn-default pull-right" icon="fa-eraser" text={t("Clear fields")} handler={this.clearFields}/>
        ];
        if(this.isEdit()) {
            buttons.unshift(<SubmitButton id="update-btn" className="btn-success" icon="fa-edit" text={t("Update")} disabled={this.state.isInvalid}/>);
        } else {
            buttons.unshift(<SubmitButton id="create-btn" className="btn-success" icon="fa-plus" text={t("Create")} disabled={this.state.isInvalid}/>);
        }

        return buttons;
    }

    render() {
        return (
        <Input.Form model={this.state.model} className="virtualhostmanager-form"
            onChange={this.onFormChange}
            onSubmit={(e) => this.isEdit() ? this.onUpdate(e) : this.onCreate(e)}
            onValidate={this.onValidate}
        >
            { type === "file" ?
                <FileBasedFormFields/>
            : (type === "vmware" ?
                <VMWareBasedFormFields/>
            : (type === "kubernetes" ?
                <KubernetesFormFields/> : null)
                )
            }
            <div className="form-group">
                <div className="col-md-offset-3 col-md-6">
                    {this.renderButtons()}
                </div>
            </div>
        </Input.Form>
        );
    }
}

function FileBasedFormFields(props) {
    return (
        <div>
            <Input.Text name="label" label={t("Label")} required labelClass="col-md-3" divClass="col-md-6"/>
            <Input.Text name="url" label={t("URL")} required labelClass="col-md-3" divClass="col-md-6"/>
        </div>
    );
}

function VMWareBasedFormFields(props) {
    return (
        <div>
        //TODO: VMware form
        </div>
    );
}

function KubernetesFormFields(props) {
    return (
        <div>
        //TODO: kubernetes form
        </div>
    );
}

module.exports = {
    VirtualHostManagerEdit: VirtualHostManagerEdit
};