/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { useState } from "react";

import { PATCH_STATUS_LABEL } from "manager/audit/cveaudit/patch-status";

import { pageSize } from "core/user-preferences";

import { IconTag } from "components/icontag";
import { Select } from "components/input";
import { Messages, Utils as MessagesUtils } from "components/messages/messages";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

// See java/core/src/main/java/com/redhat/rhn/manager/audit/SystemCVEAuditManager.java (OvalState)
export type OvalState = "ENABLED" | "DISABLED" | "NOT_COVERED";

// See java/core/src/main/java/com/redhat/rhn/manager/audit/PatchStatus.java
// NOT_AFFECTED and UNKNOWN are never returned for a CVE listed on a system
export type SystemCveStatus = Exclude<keyof typeof PATCH_STATUS_LABEL, "NOT_AFFECTED" | "UNKNOWN">;

// See java/core/src/main/java/com/suse/oval/OvalSeverity.java
export type SystemCveSeverity = "CRITICAL" | "IMPORTANT" | "MODERATE" | "LOW" | "UNKNOWN";

export const ALL_SEVERITIES: SystemCveSeverity[] = ["CRITICAL", "IMPORTANT", "MODERATE", "LOW", "UNKNOWN"];

const DEFAULT_SEVERITIES: SystemCveSeverity[] = ["CRITICAL", "IMPORTANT"];

export const SEVERITY_LABEL: Record<SystemCveSeverity, string> = {
  CRITICAL: t("Critical"),
  IMPORTANT: t("Important"),
  MODERATE: t("Moderate"),
  LOW: t("Low"),
  UNKNOWN: t("Unknown"),
};

export type SystemCveErrata = {
  id: number;
  advisory: string;
  channelLabel: string;
  assigned: boolean;
};

export type SystemCveRow = {
  id: number;
  cveName: string;
  status: SystemCveStatus;
  severity: SystemCveSeverity;
  errata: SystemCveErrata[];
};

export const ALL_STATUSES: SystemCveStatus[] = [
  "AFFECTED_PATCH_UNAVAILABLE",
  "AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI",
  "AFFECTED_PARTIAL_PATCH_APPLICABLE",
  "AFFECTED_PATCH_INAPPLICABLE",
  "AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT",
  "AFFECTED_FULL_PATCH_APPLICABLE",
  "PATCHED",
];

type StatusOption = { value: SystemCveStatus; label: string };

const STATUS_OPTIONS: StatusOption[] = ALL_STATUSES.map((status) => ({
  value: status,
  label: PATCH_STATUS_LABEL[status].label,
}));

type SeverityOption = { value: SystemCveSeverity; label: string };

const SEVERITY_OPTIONS: SeverityOption[] = ALL_SEVERITIES.map((severity) => ({
  value: severity,
  label: SEVERITY_LABEL[severity],
}));

/**
 * An empty selection means all statuses, or any severity. The values are kept in the order of `ALL_STATUSES`
 * and `ALL_SEVERITIES`, for a stable URL.
 */
export const getSystemCveDataUrl = (
  serverId: number,
  statuses: SystemCveStatus[],
  severities: SystemCveSeverity[] = []
): string => {
  const selectedStatuses =
    statuses.length > 0 ? ALL_STATUSES.filter((status) => statuses.includes(status)) : ALL_STATUSES;
  const severitySegment =
    severities.length > 0 ? ALL_SEVERITIES.filter((severity) => severities.includes(severity)).join(",") : "all";
  return `/rhn/manager/api/systems/${serverId}/details/audit/cve/${selectedStatuses.join(",")}/${severitySegment}`;
};

/** External CVE details pages, the same ones the CVE Audit page links to */
export const getMitreCveUrl = (cveName: string): string =>
  "https://cve.mitre.org/cgi-bin/cvename.cgi?name=" + cveName.replace(/^CVE-/, "");

export const getSuseCveUrl = (cveName: string): string => "https://www.suse.com/security/cve/" + cveName + ".html";

type Props = {
  serverId: number;
  ovalState: OvalState;
};

export function SystemCve({ serverId, ovalState }: Props) {
  const [statuses, setStatuses] = useState<SystemCveStatus[]>([]);
  const [severities, setSeverities] = useState<SystemCveSeverity[]>(DEFAULT_SEVERITIES);

  if (ovalState === "DISABLED") {
    return (
      <Messages
        items={MessagesUtils.info(
          t(
            "OVAL based CVE auditing is disabled. Set 'java.cve_audit.enable_oval_metadata = true' in the server configuration and synchronize the OVAL data to list the CVEs affecting this system."
          )
        )}
      />
    );
  }

  if (ovalState === "NOT_COVERED") {
    return (
      <Messages items={MessagesUtils.info(t("No OVAL data is available for the operating system of this system."))} />
    );
  }

  return (
    <div>
      <div className="row mb-10">
        <div className="col-md-6">
          <Select<StatusOption, SystemCveStatus[]>
            name="system-cve-status-filter"
            label={t("Status")}
            placeholder={t("All statuses")}
            isMulti
            isClearable
            options={STATUS_OPTIONS}
            value={statuses}
            onChange={(value) => setStatuses(value ?? [])}
            formatOptionLabel={(option: StatusOption) => (
              <span>
                <i className={"fa " + PATCH_STATUS_LABEL[option.value].className} /> {option.label}
              </span>
            )}
          />
        </div>
        <div className="col-md-6">
          <Select<SeverityOption, SystemCveSeverity[]>
            name="system-cve-severity-filter"
            label={t("Severity")}
            placeholder={t("All severities")}
            isMulti
            isClearable
            options={SEVERITY_OPTIONS}
            value={severities}
            onChange={(value) => setSeverities(value ?? [])}
          />
        </div>
      </div>
      <Table
        data={getSystemCveDataUrl(serverId, statuses, severities)}
        identifier={(row: SystemCveRow) => row.id}
        selectable={false}
        initialItemsPerPage={pageSize}
        initialSortColumnKey="cveName"
        initialSortDirection={-1}
        defaultSearchField="cveName"
        searchField={<SearchField placeholder={t("Filter by CVE")} />}
        emptyText={t("No CVEs found for this system.")}
      >
        <Column
          columnKey="status"
          header={t("Status")}
          sortable
          cell={(row: SystemCveRow) => (
            <span>
              <i
                className={"fa fa-big " + PATCH_STATUS_LABEL[row.status].className}
                title={PATCH_STATUS_LABEL[row.status].label}
              />{" "}
              {PATCH_STATUS_LABEL[row.status].label}
            </span>
          )}
        />
        <Column
          columnKey="severity"
          header={t("Severity")}
          sortable
          cell={(row: SystemCveRow) => SEVERITY_LABEL[row.severity]}
        />
        <Column
          columnKey="cveName"
          header={t("CVE")}
          sortable
          cell={(row: SystemCveRow) => (
            <>
              <div>{row.cveName}</div>
              <div>
                <a
                  target="_blank"
                  rel="noopener noreferrer"
                  href={getMitreCveUrl(row.cveName)}
                  title={t("MITRE CVE link")}
                  data-senna-off="true"
                >
                  <IconTag type="external-link" />
                  {t("MITRE")}
                </a>{" "}
                <a
                  target="_blank"
                  rel="noopener noreferrer"
                  href={getSuseCveUrl(row.cveName)}
                  title={t("SUSE Security CVE link")}
                  data-senna-off="true"
                >
                  <IconTag type="external-link" />
                  {t("SUSE")}
                </a>
              </div>
            </>
          )}
        />
        <Column
          columnKey="errata"
          header={t("Patches")}
          cell={(row: SystemCveRow) =>
            row.errata.length === 0
              ? "-"
              : row.errata.map((errata) => (
                  <div key={errata.id}>
                    <a href={"/rhn/errata/details/Details.do?eid=" + errata.id}>{errata.advisory}</a>
                    {" (" + errata.channelLabel + (errata.assigned ? "" : ", " + t("not assigned")) + ")"}
                  </div>
                ))
          }
        />
      </Table>
    </div>
  );
}
