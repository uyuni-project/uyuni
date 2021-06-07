import * as React from "react";
import { TopPanel } from "components/panels/TopPanel";
import { Messages } from "components/messages";
import { Utils as MessagesUtils } from "components/messages";
import Network from "utils/network";
import { SubmitButton, LinkButton } from "components/buttons";
import { Utils } from "utils/functions";
import { Form } from "components/input/Form";
import { FormGroup } from "components/input/FormGroup";
import { Select } from "components/input/Select";
import { Text } from "components/input/Text";
import { ActionLink, ActionChainLink } from "components/links";
import { ActionChain, ActionSchedule } from "components/action-schedule";
import SpaRenderer from "core/spa/spa-renderer";
import { DEPRECATED_unsafeEquals } from "utils/legacy";

// See java/code/src/com/suse/manager/webui/templates/content_management/build.jade
declare global {
  interface Window {
    profileId?: any;
    hostId?: any;
    version?: any;
    timezone?: any;
    localTime?: any;
    actionChains?: any;
  }
}

const typeMap = {
  dockerfile: { name: "Dockerfile", buildType: "container_build_host" },
  kiwi: { name: "Kiwi", buildType: "osimage_build_host" },
};

const msgMap = {
  unknown_error: t("An unknown error has occurred."),
  build_scheduled: t("The image build has been scheduled."),
  taskomatic_error: t(
    "There was an error while scheduling a task. Please make sure that the task scheduler is running."
  ),
};

type Props = {};

type State = {
  model: {
    version: string;
    earliest: any;
    profileId?: any;
    buildHostId?: any;
    actionChain?: any;
  };
  profile: any;
  profiles: any[];
  hosts: any[];
  messages?: any;
  actionChain?: any;
  isInvalid?: boolean;
};

class BuildImage extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      model: {
        version: window.version || "",
        earliest: Utils.dateWithTimezone(window.localTime),
      },
      profile: {},
      profiles: [],
      hosts: [],
      messages: [],
    };

    [
      "handleProfileChange",
      "onFormChange",
      "onValidate",
      "onBuild",
      "onDateTimeChanged",
      "onActionChainChanged",
    ].forEach(method => (this[method] = this[method].bind(this)));

    this.getProfiles();
  }

  getProfiles() {
    Network.get("/rhn/manager/api/cm/imageprofiles").then(res => {
      this.setState({
        profiles: res,
      });

      if (window.profileId) {
        this.changeProfile(window.profileId);
      }
    });
  }

  getProfileDetails(profileId) {
    if (!profileId) return true;

    Network.get("/rhn/manager/api/cm/imageprofiles/" + profileId).then(res => {
      if (res.success) {
        var data = res.data;

        // Prevent out-of-order async results
        if (!DEPRECATED_unsafeEquals(data.profileId, this.state.model.profileId)) return false;

        this.setState({
          profile: {
            label: data.label,
            imageType: data.imageType,
            activationKey: data.activationKey,
            channels: data.channels,
            store: data.store,
            path: data.path,
          },
        });

        // Get build hosts for the build type
        this.getBuildHosts(typeMap[data.imageType].buildType);
      } else {
        //TODO: Handle error
      }
    });
  }

  getBuildHosts(type) {
    Network.get("/rhn/manager/api/cm/build/hosts/" + type).then(res => {
      this.setState({
        hosts: res,
      });

      if (window.hostId) {
        const model = this.state.model;
        model.buildHostId = window.hostId;
        this.setState({ model: model });
      }
    });
  }

  handleProfileChange(name, value) {
    this.changeProfile(value);
  }

  changeProfile(id) {
    const model = Object.assign({}, this.state.model);
    model.profileId = id;
    this.setState({ model: model });

    if (id) {
      this.getProfileDetails(id);
    } else {
      this.setState({ profile: { label: "" } });
    }
  }

  getBounceUrl() {
    const params: any = {};
    if (this.state.model.profileId) {
      params.profile = this.state.model.profileId;
    }
    if (this.state.model.buildHostId) {
      params.host = this.state.model.buildHostId;
    }
    if (this.state.model.version) {
      params.version = this.state.model.version;
    }
    const qstr = $.param(params);
    return encodeURIComponent("/rhn/manager/cm/build" + (qstr ? "?" + qstr : ""));
  }

  onFormChange(model) {
    this.setState({
      model: model,
    });
  }

  onValidate(isValid) {
    this.setState({
      isInvalid: !isValid,
    });
  }

  onDateTimeChanged(date: Date) {
     const model: State['model'] = Object.assign({}, this.state.model, {
      earliest: date,
      actionChain: null,
     });
    this.setState({
      actionChain: null,
      model,
    });
  }

  onActionChainChanged(actionChain: ActionChain | null) {
    const model: State['model'] = Object.assign({}, this.state.model, {
      actionChain: actionChain?.text,
    });
    this.setState({
      actionChain,
      model,
    });
  }

  onBuild(model) {
    Network.post(
      "/rhn/manager/api/cm/build/" + this.state.model.profileId,
      JSON.stringify(model)
    ).then(data => {
      if (data.success) {
        const msg = MessagesUtils.info(
          this.state.model.actionChain ? (
            <span>
              {t("Action has been successfully added to the Action Chain ")}
              <ActionChainLink id={data.data}>{this.state.model.actionChain}</ActionChainLink>.
            </span>
          ) : (
            <span>
              {t("Building the image has been ")}
              <ActionLink id={data.data}>{t("scheduled")}.</ActionLink>
            </span>
          )
        );

        this.setState({
          messages: msg,
        });
        window.location.href = "/rhn/manager/cm/images";
      } else {
        this.setState({
          messages: (
            <Messages
              items={data.messages.map(msg => {
                return { severity: "error", text: msgMap[msg] };
              })}
            />
          ),
        });
      }
    });
  }

  renderProfileSummary() {
    var p = this.state.profile;
    var pselected = p.label ? true : false;
    return (
      <div className="col-md-5">
        <div className="panel panel-default">
          <div className="panel-heading">
            <h4>{t("Profile Summary")}</h4>
          </div>
          <div className="panel-body">
            <div className="table-responsive">
              <table className="table table-condensed">
                {!pselected ? (
                  <tbody>
                    <tr>
                      <td>
                        <i className="fa fa-info-circle" />
                        {t("No profile selected")}
                      </td>
                    </tr>
                  </tbody>
                ) : (
                  <tbody>
                    <tr>
                      <th>{t("Label")}</th>
                      <td>{p.label}</td>
                    </tr>
                    <tr>
                      <th>{t("Image Type")}</th>
                      <td>{typeMap[p.imageType].name}</td>
                    </tr>
                    <tr>
                      <th>{t("Image Store")}</th>
                      <td>{p.store}</td>
                    </tr>
                    <tr>
                      <th>{t("Path")}</th>
                      <td>{p.path}</td>
                    </tr>
                    <tr>
                      <th>{t("Activation Key")}</th>
                      {p.activationKey ? (
                        <td>
                          <a href={"/rhn/activationkeys/Edit.do?tid=" + p.activationKey.id} title={p.activationKey.key}>
                            {p.activationKey.key}
                          </a>
                        </td>
                      ) : (
                        <td>-</td>
                      )}
                    </tr>
                    {p.activationKey && (
                      <tr>
                        <th>{t("Software Channels")}</th>
                        {p.channels && p.channels.base ? (
                          <td>
                            <ul className="list-unstyled">
                              <li>
                                <a
                                  href={"/rhn/channels/ChannelDetail.do?cid=" + p.channels.base.id}
                                  title={p.channels.base.name}
                                >
                                  {p.channels.base.name}
                                </a>
                              </li>
                              <li>
                                <ul>
                                  {p.channels.children.map(ch => (
                                    <li key={ch.id}>
                                      <a href={"/rhn/channels/ChannelDetail.do?cid=" + ch.id} title={ch.name}>
                                        {ch.name}
                                      </a>
                                    </li>
                                  ))}
                                </ul>
                              </li>
                            </ul>
                          </td>
                        ) : (
                          <td>-</td>
                        )}
                      </tr>
                    )}
                  </tbody>
                )}
              </table>
            </div>
            {pselected && (
              <LinkButton
                icon="fa-edit"
                href={
                  "/rhn/manager/cm/imageprofiles/edit/" +
                  this.state.model.profileId +
                  "?url_bounce=" +
                  this.getBounceUrl()
                }
                className="btn-xs btn-default pull-right"
                text="Edit"
              />
            )}
          </div>
        </div>
      </div>
    );
  }

  render() {
    return (
      <TopPanel
        title={t("Build Image")}
        icon="fa spacewalk-icon-manage-configuration-files"
        helpUrl="reference/images/images-build.html"
      >
        <Messages items={this.state.messages} />
        <Form
          model={this.state.model}
          className="image-build-form"
          onChange={this.onFormChange}
          onSubmit={this.onBuild}
          onValidate={this.onValidate}
          divClass="col-md-7"
        >
          <Select
            name="profileId"
            required
            label={t("Image Profile")}
            onChange={this.handleProfileChange}
            labelClass="col-md-3"
            divClass="col-md-9"
            invalidHint={
              <span>
                Image Profile is required.&nbsp;
                <a href={"/rhn/manager/cm/imageprofiles/create?url_bounce=" + this.getBounceUrl()}>Create a new one</a>.
              </span>
            }
            options={this.state.profiles}
            getOptionValue={option => option.profileId}
          />

          {this.state.profile.imageType === "dockerfile" && (
            <Text name="version" label={t("Version")} labelClass="col-md-3" divClass="col-md-9" placeholder="latest" />
          )}

          <Select
            name="buildHostId"
            required
            label={t("Build Host")}
            labelClass="col-md-3"
            divClass="col-md-9"
            getOptionLabel={option => option.name}
            getOptionValue={option => option.id}
            options={this.state.hosts}
          />

          <ActionSchedule
            timezone={window.timezone}
            localTime={localTime}
            earliest={this.state.model.earliest}
            actionChains={window.actionChains}
            actionType="image.build"
            onActionChainChanged={this.onActionChainChanged}
            onDateTimeChanged={this.onDateTimeChanged}
          />

          <FormGroup>
            <div className="col-md-offset-3 col-md-9">
              <SubmitButton
                id="submit-btn"
                className="btn-success"
                icon="fa-cogs"
                text={t("Build")}
                disabled={this.state.isInvalid}
              />
            </div>
          </FormGroup>
        </Form>
        {this.renderProfileSummary()}
      </TopPanel>
    );
  }
}

export const renderer = () => SpaRenderer.renderNavigationReact(<BuildImage />, document.getElementById("image-build"));
