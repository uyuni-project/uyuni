import * as React from "react";
import { AceEditor } from "./ace-editor";
import { LinkButton } from "./buttons";
import { LegacyDialog } from "./dialog/LegacyDialog";

function channelIcon(channel) {
  let iconClass: string, iconTitle: string;
  if (channel.type === "state") {
    iconClass = "fa spacewalk-icon-salt-add";
    iconTitle = t("State Configuration Channel");
  } else {
    iconClass = "fa spacewalk-icon-software-channels";
    iconTitle = t("Normal Configuration Channel");
  }

  return <i className={iconClass} title={iconTitle} />;
}

type SaltStateDialogProps = {
  saltState?: {
    id: string;
    name: string;
    content: React.ReactNode;
  };
  onClose: () => void;
};

export class SaltStateDialog extends React.Component<SaltStateDialogProps> {
  render() {
    let content, icon, title, footer;

    if (this.props.saltState) {
      content = (
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
      <LegacyDialog
        title={title}
        className="modal-lg"
        id="saltStateDialog"
        content={content}
        onClose={this.props.onClose}
        footer={footer}
      />
    );
  }
}
