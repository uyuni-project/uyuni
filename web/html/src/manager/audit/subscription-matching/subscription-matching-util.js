/* eslint-disable */
"use strict";

const React = require("react");

const ToolTip = (props) =>
  <span title={props.title}>
    {props.content}
  </span>
;

const CsvLink = (props) =>
  <div className="spacewalk-csv-download">
    <a className="btn btn-link" href={"/rhn/manager/subscription-matching/" + props.name}>
      <i className="fa spacewalk-icon-download-csv"></i>
      {t("Download CSV")}
    </a>
  </div>
;

const SystemLabel = (props) => {
  var icon;
  if (props.type == "nonVirtual") {
    icon = <i className="fa fa-desktop"></i>;
  }
  else if (props.type == "virtualHost") {
    icon = <i className="fa spacewalk-icon-virtual-host"></i>;
  }
  else if (props.type == "virtualGuest") {
    icon = <i className="fa spacewalk-icon-virtual-guest"></i>;
  }
  else {
    icon = null;
  }

  return <span>{icon} {props.name}</span>;
}

function humanReadablePolicy(rawPolicy) {
  var message;
  switch(rawPolicy) {
    case "physical_only" :
      message = t("Physical deployment only");
      break;
    case "unlimited_virtualization" :
      message = t("Unlimited Virtual Machines");
      break;
    case "one_two" :
      message = t("1-2 Sockets or 1-2 Virtual Machines");
      break;
    case "instance" :
      message = t("Per-instance");
      break;
    default:
      message = rawPolicy;
  }
  return message;
}

const WarningIcon = (props) =>
  <i className={
    "fa fa-exclamation-triangle text-warning" +
    (props.iconOnRight ? " fa-right" : "")}>
  </i>
;

module.exports = {
  ToolTip: ToolTip,
  CsvLink: CsvLink,
  SystemLabel: SystemLabel,
  humanReadablePolicy: humanReadablePolicy,
  WarningIcon: WarningIcon,
}
