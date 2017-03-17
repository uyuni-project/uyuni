'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Panel = require("../components/panel").Panel;
const Messages = require("../components/messages").Messages;
const Network = require("../utils/network");
const {SubmitButton, LinkButton} = require("../components/buttons");
const DateTimePicker = require("../components/datetimepicker").DateTimePicker;
const Functions = require("../utils/functions");
const Formats = Functions.Formats;
const Input = require("../components/input");

const typeMap = {
    "dockerfile": "Dockerfile"
};

const msgMap = {
  "unknown_error": t("Some unknown error has been occured."),
  "build_scheduled": t("The image build has been scheduled."),
  "taskomatic_error": t("There was an error while scheduling a task. Please make sure that the task scheduler is running.")
};

class BuildImage extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            model: {
                tag: tag || "",
                earliest: Functions.Utils.dateWithTimezone(localTime)
            },
            profile: {},
            profiles: [],
            hosts: [],
            messages: []
        };

        ["handleProfileChange", "onFormChange", "onValidate", "onBuild"]
            .forEach(method => this[method] = this[method].bind(this));

        this.getProfiles();
        this.getBuildHosts();

    }

    getProfiles() {
        Network.get("/rhn/manager/api/cm/imageprofiles").promise.then(res => {
            this.setState({
                profiles: res
            });

            if(profileId) {
                this.changeProfile(profileId);
            }
        });
    }

    getProfileDetails(profileId) {
        if(!profileId) return true;

        Network.get("/rhn/manager/api/cm/imageprofiles/" + profileId).promise.then(res => {
            if(res.success) {
                var data = res.data;

                // Prevent out-of-order async results
                if(data.profileId != this.state.model.profileId)
                    return false;

                this.setState({
                    profile: {
                        label: data.label,
                        imageType: data.imageType,
                        activationKey: data.activationKey,
                        channels: data.channels,
                        store: data.store,
                        path: data.path
                    }
                });
            } else {
                //TODO: Handle error
            }
        });
    }

    getBuildHosts() {
        Network.get("/rhn/manager/api/cm/build/hosts").promise.then(res => {
            this.setState({
                hosts: res
            });

            if(hostId) {
                const model = this.state.model;
                model.buildHostId = hostId;
                this.setState({model: model});
            }
        });
    }

    handleProfileChange(event) {
        this.changeProfile(event.target.value);
    }

    changeProfile(id) {
        const model = this.state.model;
        model.profileId = id;
        this.setState({model: model});

        if(id) {
            this.getProfileDetails(id);
        } else {
            this.setState({ profile: { label: "" }});
        }
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

    onBuild(model) {
        Network.post("/rhn/manager/api/cm/build/" + this.state.model.profileId,
            JSON.stringify(model),
            "application/json"
        ).promise.then(data => {
            if (data.success) {
                this.setState({
                    messages: <Messages items={data.messages.map(msg => {
                        return {severity: "info", text: msgMap[msg]};
                    })}/>
                });
                window.location = "/rhn/manager/cm/images";
            } else {
                this.setState({
                    messages: <Messages items={data.messages.map(msg => {
                        return {severity: "error", text: msgMap[msg]};
                    })}/>
                });
            }
        });
    }

    renderProfileSummary() {
        var p = this.state.profile;
        var pselected = p.label ? true : false;
        return <div className="col-md-5">
                <div className="panel panel-default">
                    <div className="panel-heading">
                        <h4>{t("Profile Summary")}</h4>
                    </div>
                    <div className="panel-body">
                        <div className="table-responsive">
                            <table className="table table-condensed">
                                { !pselected ?
                                    <tbody>
                                        <tr><td>{t("Please select a build profile")}</td></tr>
                                    </tbody>
                                :
                                    <tbody>
                                        <tr><th>{t("Label")}</th><td>{p.label}</td></tr>
                                        <tr><th>{t("Image Type")}</th><td>{typeMap[p.imageType]}</td></tr>
                                        <tr><th>{t("Image Store")}</th><td>{p.store}</td></tr>
                                        <tr><th>{t("Path")}</th><td>{p.path}</td></tr>
                                        <tr>
                                            <th>{t("Activation Key")}</th>
                                            { p.activationKey ? <td><a href={"/rhn/activationkeys/Edit.do?tid=" + p.activationKey.id} title={p.activationKey.key}>{p.activationKey.key}</a></td> : <td>-</td> }
                                        </tr>
                                        { p.activationKey &&
                                            <tr>
                                                <th>{t("Software Channels")}</th>
                                                { p.channels && p.channels.base ?
                                                    <td>
                                                        <ul className="list-unstyled">
                                                            <li>
                                                                <a href={"/rhn/channels/ChannelDetail.do?cid=" + p.channels.base.id} title={p.channels.base.name}>{p.channels.base.name}</a>
                                                            </li>
                                                            <li>
                                                                <ul>
                                                                    {
                                                                        p.channels.children.map(ch => <li><a href={"/rhn/channels/ChannelDetail.do?cid=" + ch.id} title={ch.name}>{ch.name}</a></li>)
                                                                    }
                                                                </ul>
                                                            </li>
                                                        </ul>
                                                    </td>
                                                : <td>-</td> }
                                            </tr>
                                        }
                                    </tbody>
                                }
                            </table>
                        </div>
                        { pselected &&
                            <LinkButton icon="fa-edit" href={"/rhn/manager/cm/imageprofiles/edit/" + this.state.profileId} className="btn-xs btn-default pull-right" text="Edit"/>
                        }
                    </div>
                </div>
            </div>;
    }

    render() {
        return (
        <Panel title={t("Build Image")} icon="fa fa-cogs">
            {this.state.messages}
            <Input.Form model={this.state.model} className="image-build-form"
                    onChange={this.onFormChange} onSubmit={this.onBuild}
                    onValidate={this.onValidate} divClass="col-md-7">

                <Input.Text name="tag" label={t("Tag")} labelClass="col-md-3" divClass="col-md-9" placeholder="latest"/>

                <Input.Select name="profileId" required label={t("Build Profile")}
                        onChange={this.handleProfileChange} labelClass="col-md-3"
                        divClass="col-md-9" invalidHint={<span>Build Profile is required.&nbsp;<a href="/rhn/manager/cm/imageprofiles/create">Create a new one</a>.</span>}>
                    <option key="0" disabled="disabled" value="">Select a build profile</option>
                    {
                        this.state.profiles.map(k =>
                            <option key={k.profileId} value={k.profileId}>{ k.label }</option>
                        )
                    }
                </Input.Select>

                <Input.Select name="buildHostId" required label={t("Build Host")} labelClass="col-md-3" divClass="col-md-9">
                    <option key="0" disabled="disabled" value="">Select a build host</option>
                    {
                        this.state.hosts.map(h =>
                            <option key={h.id} value={h.id}>{ h.name }</option>
                        )
                    }
                </Input.Select>

                <Input.DateTime label={t("Schedule no sooner than")} name="earliest" required labelClass="col-md-3" divClass="col-md-9" timezone={timezone} />

                <Input.FormGroup>
                    <div className="col-md-offset-3 col-md-9">
                        <SubmitButton id="submit-btn" className="btn-success" icon="fa-cogs"
                                text={t("Build")} disabled={this.state.isInvalid}/>
                    </div>
                </Input.FormGroup>

            </Input.Form>
            { this.renderProfileSummary() }
        </Panel>
        )
    }
}

ReactDOM.render(
    <BuildImage/>,
    document.getElementById("image-build")
)