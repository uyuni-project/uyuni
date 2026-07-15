import type { ReactNode } from "react";

import { FromNow } from "components/datetime";

// These values have to match those defined in com.suse.manager.attestation.IbmInputDataValidator.java
export const HOST_KEY_DOCUMENT_FIELD = "host_key_document";
export const SECURE_EXECUTION_HEADER_FIELD = "secure_execution_header";

export type Settings = {
  enabled: boolean;
  environmentType: string;
  attestOnBoot: boolean;
  attestOnSchedule: boolean | undefined;
  inputData: Record<string, any>;
};

export type AttestationResult = {
  id: number;
  resultType: string;
  resultTypeLabel: string;
  status: string;
  statusDescription: string;
  description: string;
  details: string;
  processOutput: string;
  attestationTime: Date;
};

export type AttestationReport = {
  id: number;
  systemId: number;
  systemName: string;
  environmentType: string;
  environmentTypeLabel: string;
  environmentTypeDescription: string;
  status: string;
  statusDescription: string;
  creationTime: Date;
  modificationTime: Date;
  attestationTime: Date;
  actionId: number | null;
  actionName: string | null;
  actionScheduledBy: string | null;
  results: AttestationResult[];
};

export function renderTime(time: Date): ReactNode {
  if (time === null) {
    return t("N/A");
  }

  return <FromNow value={time} />;
}

export function renderStatus(status: string, description: string): ReactNode {
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
