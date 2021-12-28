import * as React from "react";

import { AceEditor } from "./ace-editor";
import { LinkButton } from "./buttons";
import { PopUp } from "./popup";

function channelIcon(channel) {
  let iconClass, iconTitle;
  if (channel.type === "state") {
    iconClass = "fa spacewalk-icon-salt-add";
    iconTitle = t("State Configuration Channel");
  } else {
    iconClass = "fa spacewalk-icon-software-channels";
    iconTitle = t("Normal Configuration Channel");
  }

  return <i className={iconClass} title={iconTitle} />;
}

type SaltStatePopupProps = {
  saltState?: {
    id: string;
    name: string;
    content: React.ReactNode;
  };
  onClosePopUp: () => any;
};

class SaltStatePopup extends React.Component<SaltStatePopupProps> {
  render() {
    let popUpContent, icon, title, footer;

    if (this.props.saltState) {
      popUpContent = (
        <AceEditor
          className="form-control"
          id="content-state"
          minLines={20}
          maxLines={40}
          readOnly={true}
          mode="yaml"
          content={this.props.saltState.content}
        ></AceEditor>
      );

      icon = this.props.saltState && channelIcon(this.props.saltState);
      title = this.props.saltState && (
        <span>
          {icon}
          {t("Configuration Channel: {0}", this.props.saltState.name)}
        </span>
      );

      footer = (
        <div className="btn-group">
          <LinkButton
            href={"/rhn/configuration/ChannelOverview.do?ccid=" + this.props.saltState.id}
            className="btn-default"
            icon="fa-edit"
            text={t("Edit")}
            title={t("Edit Configuration Channel")}
          />
        </div>
      );
    }

    return (
      <PopUp
        title={title}
        className="modal-lg"
        id="saltStatePopUp"
        content={popUpContent}
        onClosePopUp={this.props.onClosePopUp}
        footer={footer}
      />
    );
  }
}

export { SaltStatePopup };
