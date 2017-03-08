'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const Panel = require("../components/panel").Panel;
const Messages = require("../components/messages").Messages;
const Network = require("../utils/network");
const {SubmitButton, LinkButton} = require("../components/buttons");

const typeMap = {
    "dockerfile": "Dockerfile"
};

const msgMap = {
  "unknown_error": t("Some unknown error has been occured."),
  "build_scheduled": t("The image build has been scheduled.")
};

class BuildImage extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            tag: tag || "",
            host: "",
            profileId: "",
            profile: {
                label: "",
                imageType: "",
                activationKey: "",
                store: "",
                path: ""
            },
            profiles: [],
            hosts: [],
            messages: []
        };

        ["getProfiles", "getProfileDetails", "getBuildHosts", "handleChange",
            "handleProfileChange", "changeProfile", "onBuild", "renderField",
            "renderProfileSelect", "renderProfileSummary", "renderButtons"]
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
                if(data.profileId != this.state.profileId)
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
                this.setState({host: hostId});
            }
        });
    }

    handleChange(event) {
        const target = event.target;

        this.setState({
            [target.name]: target.value
        });
    }

    handleProfileChange(event) {
        this.changeProfile(event.target.value);
    }

    changeProfile(id) {
        this.setState({ profileId: id });

        if(id) {
            this.getProfileDetails(id);
        } else {
            this.setState({ profile: { label: "" }});
        }
    }

    onBuild(event) {
        event.preventDefault();
        const payload = {
            tag: this.state.tag,
            buildHostId: this.state.host
        };
        Network.post("/rhn/manager/api/cm/build/" + this.state.profileId,
            JSON.stringify(payload),
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

    renderField(name, label, value, placeholder, hidden = false, required = true) {
        return <div className="form-group">
            <label className="col-md-3 control-label">
                {label}
                { required ? <span className="required-form-field"> *</span> : undefined }
                :
            </label>
            <div className="col-md-9">
                <input name={name} placeholder={placeholder} className="form-control" type={hidden ? "password" : "text"} value={value} onChange={this.handleChange}/>
            </div>
        </div>;
    }

    renderBuildHostSelect() {
        return <div className="form-group">
            <label className="col-md-3 control-label">Build Host<span className="required-form-field"> *</span>:</label>
            <div className="col-md-9">
               <select value={this.state.host} onChange={this.handleChange} className="form-control" name="host">
                 <option key="0" disabled="disabled" value="">Select a build host</option>
                 {
                     this.state.hosts.map(h =>
                        <option key={h.id} value={h.id}>{ h.name }</option>
                     )
                 }
               </select>
            </div>
        </div>;
    }

    renderProfileSelect() {
        return <div className="form-group">
            <label className="col-md-3 control-label">Build Profile<span className="required-form-field"> *</span>:</label>
            <div className="col-md-9">
               <select value={this.state.profileId} onChange={this.handleProfileChange} className="form-control" name="profileId">
                 <option key="0" disabled="disabled" value="">Select a build profile</option>
                 {
                     this.state.profiles.map(k =>
                        <option key={k.profileId} value={k.profileId}>{ k.label }</option>
                     )
                 }
               </select>
            </div>
        </div>;
    }

    renderProfileSummary() {
        var p = this.state.profile;
        var pselected = p.label ? true : false;
        return <div className="col-md-6">
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

    renderButtons() {
        var buttons = [];
        buttons.push(<SubmitButton id="submit-btn" className="btn-success" icon="fa-cogs" text={t("Build")}/>);

        return buttons;
    }

    render() {
        return (
        <Panel title={t("Build Image")} icon="fa fa-cogs">
            {this.state.messages}
            <form className="image-build-form" onSubmit={ this.onBuild }>
                <div className="col-md-6 form-horizontal">
                    { this.renderField("tag", t("Tag"), this.state.tag, "latest", false, false) }
                    { this.renderProfileSelect() }
                    { this.renderBuildHostSelect() }
                    <div className="form-group">
                        <div className="col-md-offset-3 col-md-9">
                            { this.renderButtons() }
                        </div>
                    </div>
                </div>
                { this.renderProfileSummary() }
            </form>
        </Panel>
        )
    }
}

ReactDOM.render(
    <BuildImage/>,
    document.getElementById("image-build")
)