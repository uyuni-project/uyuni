import { hot } from "react-hot-loader/root";

import * as React from "react";

import * as ChannelUtils from "core/channels/utils/channels-dependencies.utils";

import { ActionSchedule } from "components/action-schedule";
import { ActionChain } from "components/action-schedule";
import { AsyncButton, Button } from "components/buttons";
import { ActionChainLink, ActionLink, ChannelAnchorLink } from "components/links";
import { Messages } from "components/messages";
import { Utils as MessagesUtils } from "components/messages";
import { BootstrapPanel } from "components/panels/BootstrapPanel";
import { Toggler } from "components/toggler";

import { localizedMoment } from "utils";
import { DEPRECATED_unsafeEquals } from "utils/legacy";
import Network from "utils/network";
import { JsonResult } from "utils/network";

declare var actionChains: Array<ActionChain>;

const msgMap = {
  taskomatic_error: t("Error scheduling job in Taskomatic. Please check the logs."),
  base_not_found_or_not_authorized: t("Base channel not found or not authorized."),
  child_not_found_or_not_authorized: t("Child channel not found or not authorized."),
  invalid_channel_id: t("Invalid channel id"),
};

type ChannelDto = {
  id: number;
  name: string;
  custom: boolean;
  subscribable: boolean;
  recommended: boolean;
  compatibleChannelPreviousSelection?: number;
};

type SystemChannelsProps = {
  serverId: number;
};

type SystemChannelsState = {
  messages: Array<any>;
  earliest: moment.Moment;
  originalBase: ChannelDto | null | undefined;
  selectedBase: ChannelDto | null | undefined;
  selectedChildrenIds: Map<number, Set<number>>; // base channel id -> set<child channel id>
  availableBase: Array<ChannelDto>;
  // mapping channel ids to their DTOs
  availableChildren: Map<number, Map<number, ChannelDto>>;
  mandatoryChannelsRaw: any;
  // channel dependencies: which child channels are required by a child channel?
  requiredChannels: Map<number, Set<number>>;
  // channel dependencies: by which child channels is a child channel required?
  requiredByChannels: Map<number, Set<number>>;
  assignedChildrenIds: Set<number>; // channels which are already assigned
  page: number;
  scheduled: boolean;
  actionChain: ActionChain | null | undefined;
  dependencyDataAvailable: boolean;
};

class SystemChannels extends React.Component<SystemChannelsProps, SystemChannelsState> {
  constructor(props: SystemChannelsProps) {
    super(props);
    this.state = {
      messages: [],
      earliest: localizedMoment(),
      originalBase: null,
      selectedBase: null,
      selectedChildrenIds: new Map(),
      availableBase: [],
      availableChildren: new Map([[this.getNoBase().id, new Map([])]]),
      requiredChannels: new Map(),
      requiredByChannels: new Map(),
      assignedChildrenIds: new Set(),
      page: 1,
      scheduled: false,
      actionChain: null,
      dependencyDataAvailable: false,
      mandatoryChannelsRaw: { [this.getNoBase().id]: [] },
    };
  }

  componentDidMount() {
    this.updateView();
  }

  updateView = () => {
    Network.get(`/rhn/manager/api/systems/${this.props.serverId}/channels`)
      .then((data) => {
        const base: ChannelDto = data.data && data.data.base ? data.data.base : this.getNoBase();
        const childrenIds = data.data.children ? data.data.children.map((c) => c.id) : [];
        this.setState({
          originalBase: base,
          selectedBase: base,
          selectedChildrenIds: new Map([[base.id, new Set(childrenIds)]]),
          assignedChildrenIds: new Set(childrenIds),
        });
        if (data.data && data.data.base) {
          this.getAccessibleChildren(data.data.base.id);
        } else {
          this.setState({
            dependencyDataAvailable: true,
          });
        }
      })
      .catch(this.handleResponseError);

    Network.get(`/rhn/manager/api/systems/${this.props.serverId}/channels-available-base`)
      .then((data) => {
        this.setState({
          availableBase: data.data,
        });
      })
      .catch(this.handleResponseError);
  };

  getAccessibleChildren = (newBaseId: number) => {
    if (!this.state.availableChildren.has(newBaseId)) {
      const shouldIncludeOldBaseChannelIdParam =
        this.state.originalBase && this.state.originalBase.id !== this.getNoBase().id;
      const queryString =
        shouldIncludeOldBaseChannelIdParam && this.state.originalBase
          ? `?oldBaseChannelId=${this.state.originalBase.id}`
          : "";
      Network.get(
        `/rhn/manager/api/systems/${this.props.serverId}/channels/${newBaseId}/accessible-children${queryString}`
      )
        .then((data: JsonResult<Array<ChannelDto>>) => {
          const newChildren = new Map(
            data.data.sort((a, b) => a.name.localeCompare(b.name)).map((channel) => [channel.id, channel])
          );
          this.state.availableChildren.set(newBaseId, newChildren);
          this.setState({
            availableChildren: this.state.availableChildren,
          });
          const channelIds: number[] = Array.from(newChildren.keys());
          channelIds.push(newBaseId);

          // wait for fetching to be completed and then pre select channels
          this.fetchMandatoryChannelsByChannelIds(channelIds).then(() =>
            this.preSelectCompatibleChannels(newBaseId, Array.from(newChildren.values()))
          );
        })
        .catch(this.handleResponseError);
    } else {
      const channelIdsSet = this.state.availableChildren.get(newBaseId) || new Set<any>();
      const channelIds: number[] = Array.from(channelIdsSet.keys());
      channelIds.push(newBaseId);

      // wait for fetching to be completed and then pre select channels
      this.fetchMandatoryChannelsByChannelIds(channelIds).then(() =>
        this.preSelectCompatibleChannels(newBaseId, Array.from(channelIdsSet.values()))
      );
    }
  };

  preSelectCompatibleChannels = (newBaseId: number, newChildren: Array<ChannelDto>) => {
    // we only want to apply the pre selection if it's the first time changing to that channel.
    // After that the user selection has priority
    if (!this.state.selectedChildrenIds.has(newBaseId)) {
      const preSelectedChildrenIds = newChildren
        .filter(
          (c) =>
            c.compatibleChannelPreviousSelection &&
            this.state.assignedChildrenIds.has(c.compatibleChannelPreviousSelection)
        )
        .map((c) => c.id);
      this.state.selectedChildrenIds.set(newBaseId, new Set(preSelectedChildrenIds));
      this.setState({
        selectedChildrenIds: this.state.selectedChildrenIds,
      });
      this.enableAllRecommended();

      // force all mandatory channels being selected (bsc#1211062)
      const availableChildren = this.getAvailableChildren();
      const mandatoryChannels = this.state.requiredChannels.get(newBaseId);
      const selectedChildren = this.getSelectedChildren() || [];
      Array.from(availableChildren.values())
        .filter(
          (c) =>
            mandatoryChannels &&
            mandatoryChannels.has(c.id) &&
            selectedChildren &&
            !selectedChildren.some((child) => child.id === c.id)
        )
        .forEach((c) => this.selectChildChannel(c.id, true));
    }
  };

  fetchMandatoryChannelsByChannelIds(channelIds: Array<number>) {
    return new Promise((resolve, reject) => {
      const mandatoryChannelsNotCached = channelIds.filter((channelId) => !this.state.mandatoryChannelsRaw[channelId]);
      if (mandatoryChannelsNotCached.length > 0) {
        Network.post("/rhn/manager/api/admin/mandatoryChannels", mandatoryChannelsNotCached)
          .then((data: JsonResult<Map<number, Array<number>>>) => {
            const allTheNewMandatoryChannelsData = Object.assign({}, this.state.mandatoryChannelsRaw, data.data);
            let { requiredChannels, requiredByChannels } =
              ChannelUtils.processChannelDependencies(allTheNewMandatoryChannelsData);

            this.setState({
              dependencyDataAvailable: true,
              mandatoryChannelsRaw: allTheNewMandatoryChannelsData,
              requiredChannels,
              requiredByChannels,
            });
            resolve(undefined);
          })
          .catch(this.handleResponseError);
      } else {
        this.setState({
          dependencyDataAvailable: true,
        });
        resolve(undefined);
      }
    });
  }

  handleResponseError = (jqXHR: JQueryXHR, arg: string = "") => {
    const msg = Network.responseErrorMessage(jqXHR, (status, msg) => (msgMap[msg] ? t(msgMap[msg], arg) : null));
    this.setState({ messages: this.state.messages.concat(msg) });
  };

  handleBaseChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    const baseId: number = parseInt(event.target.value, 10);
    this.setState({
      selectedBase:
        baseId > -1 ? this.state.availableBase.find((c) => DEPRECATED_unsafeEquals(c.id, baseId)) : this.getNoBase(),
      dependencyDataAvailable: baseId > -1 ? false : true,
    });

    this.getAccessibleChildren(Number(event.target.value));
  };

  getNoBase = () => {
    return { id: -1, name: t("(none, disable service)"), custom: false, subscribable: true, recommended: false };
  };

  handleChildChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    this.selectChildChannel(parseInt(event.target.value, 10), event.target.checked);
  };

  selectChildChannel = (childChannelId: number, select: boolean) => {
    const child: ChannelDto | null | undefined = this.getAvailableChildren().get(childChannelId);
    if (child == null || this.state.selectedBase == null) {
      return;
    }
    const selectedChildrenIds = this.state.selectedChildrenIds.get(this.state.selectedBase.id);
    if (selectedChildrenIds) {
      if (select) {
        const dependingChannelIds: Set<number> = this.state.requiredChannels.get(child.id) || new Set();
        Array.from(dependingChannelIds)
          .filter((channelId) => channelId !== child.id)
          .forEach((channelId) => selectedChildrenIds.add(channelId));
        selectedChildrenIds.add(child.id);
      } else {
        // unselect
        const dependingChannelIds = this.state.requiredByChannels.get(child.id) || [];
        Array.from(dependingChannelIds)
          .filter((channelId) => channelId !== child.id)
          .forEach((channelId) => selectedChildrenIds.delete(channelId));
        selectedChildrenIds.delete(child.id);
      }
    }
    this.setState({
      selectedChildrenIds: this.state.selectedChildrenIds,
    });
  };

  handleNext = () => {
    this.setState({
      page: 2,
    });
  };

  handlePrevious = () => {
    this.setState({
      page: 1,
    });
  };

  getSelectedChildren = (): ChannelDto[] | null | undefined => {
    if (
      this.state.selectedBase &&
      this.state.selectedBase.id &&
      this.state.selectedChildrenIds.has(this.state.selectedBase.id)
    ) {
      const selectedChildrenIds = this.state.selectedChildrenIds.get(this.state.selectedBase.id) || new Set();
      const availableChildren = this.getAvailableChildren() || new Map();
      return Array.from(selectedChildrenIds)
        .map((channelId): ChannelDto | null | undefined => availableChildren.get(channelId))
        .filter(Boolean);
    }
    return null;
  };

  getAvailableChildren = (): Map<number, ChannelDto> => {
    if (
      this.state.selectedBase &&
      this.state.selectedBase.id &&
      this.state.availableChildren.has(this.state.selectedBase.id)
    ) {
      return this.state.availableChildren.get(this.state.selectedBase.id) || new Map();
    }
    return new Map();
  };

  areAllMandatoryChildrenSelected = () => {
    if (
      this.state.selectedBase &&
      this.state.selectedBase.id &&
      this.state.requiredChannels.get(this.state.selectedBase.id)
    ) {
      let baseId = this.state.selectedBase.id;
      let mandatoryChannels = this.state.requiredChannels.get(baseId) || new Set();
      let alreadyAssignedChildrenIds = this.state.assignedChildrenIds;
      return Array.from(mandatoryChannels).every((mc) => alreadyAssignedChildrenIds.has(mc));
    }
  };

  disableAllRecommended = () => {
    const selectedChildren = this.getSelectedChildren() || [];
    const selectedRecommendedChildren = selectedChildren.filter((channel) => channel.recommended);
    selectedRecommendedChildren.forEach((channel) => this.selectChildChannel(channel.id, false));
  };

  enableAllRecommended = () => {
    const selectedChildrenIds = (this.getSelectedChildren() || []).map((channel) => channel.id);
    const availableChildren = this.getAvailableChildren();
    const unselectedRecommendedChildren = Array.from(availableChildren.values()).filter(
      (channel) => channel.recommended && !selectedChildrenIds.includes(channel.id)
    );
    unselectedRecommendedChildren.forEach((channel) => this.selectChildChannel(channel.id, true));
  };

  toggleRecommended = () => {
    if (this.areRecommendedChildrenSelected()) {
      this.disableAllRecommended();
    } else {
      this.enableAllRecommended();
    }
  };

  areRecommendedChildrenSelected = (): boolean => {
    const selectedChildrenIds = (this.getSelectedChildren() || []).map((channel) => channel.id);
    const availableChildren = this.getAvailableChildren();
    const recommendedChildren = Array.from(availableChildren.values()).filter((channel) => channel.recommended);
    const selectedRecommendedChildren = recommendedChildren.filter((channel) =>
      selectedChildrenIds.includes(channel.id)
    );
    const unselectedRecommendedChildren = recommendedChildren.filter(
      (channel) => !selectedChildrenIds.includes(channel.id)
    );

    return selectedRecommendedChildren.length > 0 && unselectedRecommendedChildren.length === 0;
  };

  handleConfirm = () => {
    let selectedChildrenList = this.getSelectedChildren();
    return Network.post(`/rhn/manager/api/systems/${this.props.serverId}/channels`, {
      base: this.state.selectedBase,
      children: selectedChildrenList,
      earliest: this.state.earliest,
      actionChain: this.state.actionChain ? this.state.actionChain.text : null,
    })
      .then((data) => {
        if (data.success) {
          const msg = MessagesUtils.info(
            this.state.actionChain ? (
              <span>
                {t("Action has been successfully added to the Action Chain ")}
                <ActionChainLink id={data.data}>
                  {this.state.actionChain ? this.state.actionChain.text : ""}
                </ActionChainLink>
                .
              </span>
            ) : (
              <span>
                {t("Changing the channels has been ")}
                <ActionLink id={data.data}>{t("scheduled")}.</ActionLink>
              </span>
            )
          );

          this.setState({
            messages: msg,
            scheduled: true,
            page: 1,
          });
        } else {
          this.setState({
            messages: MessagesUtils.error(data.messages),
          });
        }
      })
      .catch(this.handleResponseError);
  };

  onDateTimeChanged = (value: moment.Moment) => {
    this.setState({
      earliest: value,
      actionChain: null,
    });
  };

  onActionChainChanged = (actionChain: ActionChain | null | undefined) => {
    this.setState({ actionChain: actionChain });
  };

  dependenciesTooltip = (channelId: number) => {
    const availableChildren: Map<number, ChannelDto> = this.getAvailableChildren();

    const resolveChannelNames = (channelIds: Set<number> | null | undefined): Array<string> => {
      return Array.from(channelIds || new Set<number>())
        .map((channelIdToResolve) => availableChildren.get(channelIdToResolve))
        .filter(Boolean)
        .map((channel) => channel.name);
    };
    return (
      ChannelUtils.dependenciesTooltip(
        resolveChannelNames(this.state.requiredChannels.get(channelId)),
        resolveChannelNames(this.state.requiredByChannels.get(channelId))
      ) ?? undefined
    );
  };

  render() {
    return (
      <span>
        <Messages items={this.state.messages} />
        {DEPRECATED_unsafeEquals(this.state.page, 1) ? this.renderSelectionPage() : this.renderConfirmPage()}
      </span>
    );
  }

  renderSelectionPage = () => {
    let baseChannels: React.ReactNode[] = [];
    let childChannels;
    const isNoneChecked = -1 === (this.state.selectedBase && this.state.selectedBase.id);
    baseChannels.push(
      <div className="radio">
        <input
          type="radio"
          value="-1"
          id="base_none"
          checked={isNoneChecked}
          disabled={!isNoneChecked && this.state.dependencyDataAvailable !== true}
          onChange={this.handleBaseChange}
        />
        <label htmlFor="base_none">{t("(none, disable service)")}</label>
        <hr />
      </div>
    );
    if (this.state.availableBase) {
      const baseOptions = this.state.availableBase.filter((c) => !c.custom);
      const customOptions = this.state.availableBase.filter((c) => c.custom);

      if (baseOptions.length > 0) {
        baseChannels.push(
          <div>
            <h4>{t("SUSE Channels")}</h4>
            {baseOptions.map((c) => {
              const isChecked = c.id === (this.state.selectedBase && this.state.selectedBase.id);

              return (
                <div className="radio">
                  <input
                    type="radio"
                    value={c.id}
                    id={"base_" + c.id}
                    checked={isChecked}
                    disabled={!isChecked && this.state.dependencyDataAvailable !== true}
                    onChange={this.handleBaseChange}
                  />
                  <label htmlFor={"base_" + c.id}>{c.name}</label>
                  <ChannelAnchorLink id={c.id} newWindow={true} />
                </div>
              );
            })}
            <hr />
          </div>
        );
      }
      if (customOptions.length > 0) {
        baseChannels.push(
          <div>
            <h4>{t("Custom Channels")}</h4>
            {customOptions.map((c) => {
              const isChecked = c.id === (this.state.selectedBase && this.state.selectedBase.id);

              return (
                <div className="radio">
                  <input
                    type="radio"
                    value={c.id}
                    id={"base_" + c.id}
                    checked={isChecked}
                    disabled={!isChecked && this.state.dependencyDataAvailable !== true}
                    onChange={this.handleBaseChange}
                  />
                  <label htmlFor={"base_" + c.id}>{c.name}</label>
                  <ChannelAnchorLink id={c.id} newWindow={true} />
                </div>
              );
            })}
            <hr />
          </div>
        );
      }
    }

    const availableChildren = this.getAvailableChildren();
    if (availableChildren && DEPRECATED_unsafeEquals(this.state.dependencyDataAvailable, true)) {
      let selectedChildrenList = this.getSelectedChildren();
      let mandatoryChannels = this.state.selectedBase && this.state.requiredChannels.get(this.state.selectedBase.id);

      childChannels = Array.from(availableChildren.values()).map((c) => (
        <div className="checkbox">
          <input
            type="checkbox"
            value={c.id}
            id={"child_" + c.id}
            checked={Boolean(
              (mandatoryChannels && mandatoryChannels.has(c.id)) ||
                (selectedChildrenList && selectedChildrenList.some((child) => child.id === c.id))
            )}
            disabled={Boolean(!c.subscribable || (mandatoryChannels && mandatoryChannels.has(c.id)))}
            onChange={this.handleChildChange}
          />
          <label title={this.dependenciesTooltip(c.id)} htmlFor={"child_" + c.id}>
            {c.name}
          </label>{" "}
          &nbsp;
          {this.dependenciesTooltip(c.id) ? (
            // eslint-disable-next-line jsx-a11y/anchor-is-valid
            <a href="#">
              <i className="fa fa-info-circle spacewalk-help-link" title={this.dependenciesTooltip(c.id)}></i>
            </a>
          ) : null}
          &nbsp;
          {c.recommended ? (
            <span className="recommended-tag-base" title={"This channel is recommended"}>
              {t("recommended")}
            </span>
          ) : null}
          {mandatoryChannels && mandatoryChannels.has(c.id) ? (
            <span className="mandatory-tag-base" title={"This channel is mandatory"}>
              {t("mandatory")}
            </span>
          ) : null}
          <ChannelAnchorLink id={c.id} newWindow={true} />
        </div>
      ));
    }

    return (
      <BootstrapPanel
        footer={
          <div className="btn-group">
            <Button
              id="btn-next"
              disabled={!this.state.dependencyDataAvailable}
              text={t("Next")}
              className="btn-default"
              icon="fa-arrow-right"
              handler={this.handleNext}
            />
          </div>
        }
      >
        <span>
          <div className="row channel-for-system">
            <div className="col-md-6">
              <BootstrapPanel
                title={t("Base Channel")}
                icon="spacewalk-icon-software-channels"
                header={
                  <div className="page-summary">
                    {t(
                      "You can change the base software channel your system is subscribed to. The system will be unsubscribed from all software channels, and subscribed to the new base software channel."
                    )}
                  </div>
                }
              >
                <div style={{ overflow: "auto" }}>
                  <div style={{ float: "right" }}>
                    <Toggler
                      handler={this.toggleRecommended.bind(this)}
                      value={this.areRecommendedChildrenSelected()}
                      text={t("include recommended")}
                      disabled={!Array.from(availableChildren.values()).some((channel) => channel.recommended)}
                    />
                  </div>
                </div>
                <hr />
                <div>{baseChannels} </div>
              </BootstrapPanel>
            </div>
            <div className="col-md-6">
              <BootstrapPanel
                title={t("Child Channels")}
                icon="spacewalk-icon-software-channels"
                header={
                  <div className="page-summary">
                    {t(
                      "This system is subscribed to the checked channels beneath, if any. Disabled checkboxes indicate channels that can't be manually subscribed or unsubscribed from."
                    )}
                  </div>
                }
              >
                <div>
                  {this.state.selectedBase && this.state.selectedBase.name}
                  {this.state.selectedBase && this.state.selectedBase.id > -1 && (
                    <ChannelAnchorLink id={this.state.selectedBase.id} newWindow={true} />
                  )}
                </div>
                <hr />
                {childChannels && childChannels.length > 0 ? (
                  <div>{childChannels} </div>
                ) : (
                  <div>
                    {this.state.selectedBase && this.state.selectedBase.id > -1 ? (
                      this.state.dependencyDataAvailable === true ? (
                        <span>
                          <i className="fa fa-exclamation-triangle fa-1-5x" title={t("No child channels available.")} />
                          {t("No child channels available.")}
                        </span>
                      ) : (
                        <span>
                          <i className="fa fa-spinner fa-spin fa-1-5x" title={t("Loading...")} />
                          {t("Loading...")}
                        </span>
                      )
                    ) : undefined}
                  </div>
                )}
              </BootstrapPanel>
              {!this.areAllMandatoryChildrenSelected() ? (
                <span className="help-block">
                  {" "}
                  <strong>Important:</strong> All the 'mandatory' channels should be subscribed to have a consistent set
                  of channels.
                </span>
              ) : null}
            </div>
          </div>
        </span>
      </BootstrapPanel>
    );
  };

  renderConfirmPage = () => {
    const selectedChildrenList = this.getSelectedChildren();
    const availableChildren = this.getAvailableChildren();

    return (
      <BootstrapPanel
        title={t("Confirm Software Channel Change")}
        icon="spacewalk-icon-software-channels"
        footer={
          <div className="btn-group">
            <Button
              id="btn-prev"
              className="btn-default"
              icon="fa-arrow-left"
              text={t("Prev")}
              handler={this.handlePrevious}
              disabled={this.state.scheduled}
            />
            <AsyncButton
              id="btn-confirm"
              defaultType="btn-success"
              text={t("Confirm")}
              action={this.handleConfirm}
              disabled={this.state.scheduled}
            />
          </div>
        }
      >
        <div>
          <div>
            {this.state.selectedBase && this.state.selectedBase.name}
            {this.state.selectedBase && this.state.selectedBase.id > -1 && (
              <ChannelAnchorLink id={this.state.selectedBase.id} newWindow={true} />
            )}
            <hr />
          </div>
          <div>
            {availableChildren &&
              Array.from(availableChildren.values()).map((c) => (
                <div className="checkbox">
                  <input
                    type="checkbox"
                    value={c.id}
                    checked={Boolean(selectedChildrenList && selectedChildrenList.some((child) => child.id === c.id))}
                    disabled={true}
                  />
                  <label>{c.name}</label>
                  <ChannelAnchorLink id={c.id} />
                </div>
              ))}
            {availableChildren.size === 0 && this.state.selectedBase && this.state.selectedBase.id > -1 ? (
              <div>
                <i className="fa fa-exclamation-triangle fa-1-5x" title={t("No child channels available.")} />
                {t("No child channels available.")}
              </div>
            ) : undefined}
          </div>

          <ActionSchedule
            actionChains={actionChains}
            earliest={this.state.earliest}
            onActionChainChanged={this.onActionChainChanged}
            onDateTimeChanged={this.onDateTimeChanged}
            systemIds={[this.props.serverId]}
            actionType="channels.subscribe"
          />
        </div>
      </BootstrapPanel>
    );
  };
}

const HotSubscribeChannels = hot(SystemChannels);

export { HotSubscribeChannels as SubscribeChannels };
