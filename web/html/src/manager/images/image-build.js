/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const { TopPanel } = require('components/panels/TopPanel');
const Messages = require("components/messages").Messages;
const MessagesUtils = require("components/messages").Utils;
const Network = require("utils/network");
const {SubmitButton, LinkButton} = require("components/buttons");
const Functions = require("utils/functions");
const { Form } = require('components/input/Form');
const { FormGroup } = require('components/input/FormGroup');
const { Select } = require('components/input/Select');
const { Text } = require('components/input/Text');
const {ActionLink, ActionChainLink} = require("components/links");
const {ActionSchedule} = require("components/action-schedule");

/* global profileId, hostId, version, localTime, timezone, actionChains */
const typeMap = {
  "dockerfile": { name: "Dockerfile", buildType: "container_build_host" },
  "kiwi": { name: "Kiwi", buildType: "osimage_build_host" },
};

const msgMap = {
  "unknown_error": t("An unknown error has occurred."),
  "build_scheduled": t("The image build has been scheduled."),
  "taskomatic_error": t("There was an error while scheduling a task. Please make sure that the task scheduler is running.")
};

class BuildImage extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      model: {
        version: version || "",
        earliest: Functions.Utils.dateWithTimezone(localTime)
      },
      profile: {},
      profiles: [],
      hosts: [],
      messages: []
    };

    ["handleProfileChange", "onFormChange", "onValidate", "onBuild", "onDateTimeChanged", "onActionChainChanged"]
      .forEach(method => this[method] = this[method].bind(this));

    this.getProfiles();
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

        // Get build hosts for the build type
        this.getBuildHosts(typeMap[data.imageType].buildType);
      } else {
        //TODO: Handle error
      }
    });
  }

  getBuildHosts(type) {
    Network.get("/rhn/manager/api/cm/build/hosts/" + type, "application/json").promise
      .then(res => {
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

  handleProfileChange(name, value) {
    this.changeProfile(value);
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

  getBounceUrl() {
    const params = {};
    if(this.state.model.profileId) {
      params.profile = this.state.model.profileId;
    }
    if(this.state.model.buildHostId) {
      params.host = this.state.model.buildHostId;
    }
    if(this.state.model.version) {
      params.version = this.state.model.version;
    }
    const qstr = $.param(params);
    return encodeURIComponent("/rhn/manager/cm/build" + (qstr ? "?" + qstr : ""));
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

  onDateTimeChanged(date) {
      this.state.model.earliest = date;
      this.state.model.actionChain = null;
      this.setState({
        actionChain: null
      });
  }

  onActionChainChanged(actionChain) {
    this.state.model.actionChain = actionChain.text;
    this.setState({
      actionChain: actionChain
    });
  }

  onBuild(model) {
    Network.post("/rhn/manager/api/cm/build/" + this.state.model.profileId,
      JSON.stringify(model),
      "application/json"
    ).promise.then(data => {
      if (data.success) {
        const msg = MessagesUtils.info(this.state.model.actionChain ?
               <span>{t("Action has been successfully added to the Action Chain ")}
                    <ActionChainLink id={data.data}>{this.state.model.actionChain}</ActionChainLink>.</span> :
                 <span>{t("Building the image has been ")}
                    <ActionLink id={data.data}>{t("scheduled")}.</ActionLink></span>);

        this.setState({
          messages: msg
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
                  <tr><td><i className="fa fa-info-circle"/>{t("No profile selected")}</td></tr>
                </tbody>
                :
                <tbody>
                  <tr><th>{t("Label")}</th><td>{p.label}</td></tr>
                  <tr><th>{t("Image Type")}</th><td>{typeMap[p.imageType].name}</td></tr>
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
                                                          p.channels.children.map(ch => <li key={ch.id}><a href={"/rhn/channels/ChannelDetail.do?cid=" + ch.id} title={ch.name}>{ch.name}</a></li>)
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
                            <LinkButton icon="fa-edit" href={"/rhn/manager/cm/imageprofiles/edit/" + this.state.model.profileId + "?url_bounce=" + this.getBounceUrl()} className="btn-xs btn-default pull-right" text="Edit"/>
          }
        </div>
      </div>
    </div>;
  }

  render() {
    return (
      <TopPanel title={t("Build Image")} icon="fa spacewalk-icon-manage-configuration-files" helpUrl="/docs/reference/images/images-build.html">
        <Messages items={this.state.messages}/>
        <Form model={this.state.model} className="image-build-form"
          onChange={this.onFormChange} onSubmit={this.onBuild}
          onValidate={this.onValidate} divClass="col-md-7">

          <Select name="profileId" required label={t("Image Profile")}
            onChange={this.handleProfileChange} labelClass="col-md-3"
            divClass="col-md-9" invalidHint={<span>Image Profile is required.&nbsp;<a href={"/rhn/manager/cm/imageprofiles/create" + "?url_bounce=" + this.getBounceUrl()}>Create a new one</a>.</span>}>
            <option key="0" disabled="disabled" value="">Select an image profile</option>
            {
              this.state.profiles.map(k =>
                <option key={k.profileId} value={k.profileId}>{ k.label }</option>
              )
            }
          </Select>

          { this.state.profile.imageType === "dockerfile" &&
            <Text name="version" label={t("Version")} labelClass="col-md-3" divClass="col-md-9" placeholder="latest"/>
          }

          <Select name="buildHostId" required label={t("Build Host")} labelClass="col-md-3" divClass="col-md-9">
            <option key="0" disabled="disabled" value="">Select a build host</option>
            {
              this.state.hosts.map(h =>
                <option key={h.id} value={h.id}>{ h.name }</option>
              )
            }
          </Select>

          <ActionSchedule timezone={timezone} localTime={localTime}
             earliest={this.state.model.earliest}
             actionChains={actionChains}
             actionChain={this.state.model.actionChain}
             onActionChainChanged={this.onActionChainChanged}
             onDateTimeChanged={this.onDateTimeChanged}/>

          <FormGroup>
            <div className="col-md-offset-3 col-md-9">
              <SubmitButton id="submit-btn" className="btn-success" icon="fa-cogs"
                text={t("Build")} disabled={this.state.isInvalid}/>
            </div>
          </FormGroup>

        </Form>
        { this.renderProfileSummary() }
      </TopPanel>
    )
  }
}

ReactDOM.render(
  <BuildImage/>,
  document.getElementById("image-build")
)
