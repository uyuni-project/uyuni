import { LinkButton } from "components/buttons";
import { IconTag } from "components/icontag";

const ToolTip = (props) => <span title={props.title}>{props.content}</span>;

const CsvLink = (props) => (
  <div className="spacewalk-csv-download">
    <LinkButton
      className="btn btn-default"
      href={"/rhn/manager/subscription-matching/" + props.name}
      disableSenna
      text={t("Download CSV")}
      icon="spacewalk-icon-download-csv"
    />
  </div>
);

const SystemLabel = (props) => {
  let icon;
  if (props.type === "nonVirtual") {
    icon = <IconTag icon="fa-desktop" />;
  } else if (props.type === "virtualHost") {
    icon = <IconTag icon="spacewalk-icon-virtual-host" />;
  } else if (props.type === "virtualGuest") {
    icon = <IconTag icon="spacewalk-icon-virtual-guest" />;
  } else {
    icon = null;
  }

  return (
    <span>
      {icon} {props.name}
    </span>
  );
};

function humanReadablePolicy(rawPolicy) {
  let message;
  switch (rawPolicy) {
    case "physical_only":
      message = t("Physical deployment only");
      break;
    case "unlimited_virtualization":
      message = t("Unlimited Virtual Machines");
      break;
    case "one_two":
      message = t("1-2 Sockets or 1-2 Virtual Machines");
      break;
    case "instance":
      message = t("Per-instance");
      break;
    default:
      message = rawPolicy;
  }
  return message;
}

const WarningIcon = (props) => (
  <IconTag icon="fa-exclamation-triangle" className={"text-warning" + (props.iconOnRight ? " fa-right" : "")} />
);

export { ToolTip, CsvLink, SystemLabel, humanReadablePolicy, WarningIcon };
