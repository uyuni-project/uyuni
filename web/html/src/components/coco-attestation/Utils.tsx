import React from "react";

import { FromNow } from "components/datetime";
import { CronTimes, RecurringType } from "components/picker/recurring-event-picker";

export type Settings = {
  enabled: boolean;
  environmentType: string;
  attestOnBoot: boolean;
  attestOnSchedule: boolean;
  scheduleName?: string;
  scheduleType?: RecurringType;
  scheduleCron?: string;
  scheduleCronTimes?: CronTimes;
};

export type AttestationResult = {
  id: number;
  resultType: string;
  resultTypeLabel: string;
  status: string;
  statusDescription: string;
  description: string;
  details: string;
  attestationTime: Date;
};

export type AttestationReport = {
  id: number;
  systemId: number;
  systemName: string;
  status: string;
  statusDescription: string;
  creationTime: Date;
  modificationTime: Date;
  attestationTime: Date;
  actionId: number | null;
  actionName: string | null;
  actionScheduledBy: string | null;
  results: Array<AttestationResult>;
};

export function renderTime(time: Date): React.ReactNode {
  if (time === null) {
    return t("N/A");
  }

  return <FromNow value={time} />;
}

export function renderStatus(status: string, description: string): React.ReactNode {
  let icon, textStyle;

  switch (status) {
    case "PENDING":
      icon = "fa fa-spinner fa-spin";
      break;

    case "SUCCEEDED":
      icon = "fa fa-check";
      textStyle = "text-success";
      break;

    case "FAILED":
      icon = "fa fa-times";
      textStyle = "text-danger";
      break;
  }

  return (
    <span className={textStyle}>
      {icon && <i className={icon} />}
      {t(description)}
    </span>
  );
}
