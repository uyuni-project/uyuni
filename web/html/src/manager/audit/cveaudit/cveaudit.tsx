import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { AsyncButton, LinkButton } from "components/buttons";
import { IconTag } from "components/icontag";
import { Messages } from "components/messages";
import { TopPanel } from "components/panels/TopPanel";
import { Column } from "components/table/Column";
import { Highlight } from "components/table/Highlight";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

import { localizedMoment } from "utils";
import { Utils } from "utils/functions";
import { DEPRECATED_unsafeEquals } from "utils/legacy";
import Network from "utils/network";

const AFFECTED_PATCH_INAPPLICABLE = "AFFECTED_PATCH_INAPPLICABLE";
const AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT = "AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT";
const AFFECTED_FULL_PATCH_APPLICABLE = "AFFECTED_FULL_PATCH_APPLICABLE";
const NOT_AFFECTED = "NOT_AFFECTED";
const PATCHED = "PATCHED";
const AFFECTED_PATCH_UNAVAILABLE = "AFFECTED_PATCH_UNAVAILABLE";
const AFFECTED_PARTIAL_PATCH_APPLICABLE = "AFFECTED_PARTIAL_PATCH_APPLICABLE";

const ALL = [
  AFFECTED_PATCH_INAPPLICABLE,
  AFFECTED_FULL_PATCH_APPLICABLE,
  AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT,
  NOT_AFFECTED,
  PATCHED,
  AFFECTED_PATCH_UNAVAILABLE,
  AFFECTED_PARTIAL_PATCH_APPLICABLE
];
const PATCH_STATUS_LABEL = {
  AFFECTED_PATCH_INAPPLICABLE: {
    className: "fa-exclamation-circle text-danger",
    label: t("Affected, patches available in channels which are not assigned"),
  },
  AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT: {
    className: "fa-exclamation-circle text-danger",
    label: t("Affected, patches available in a Product Migration target"),
  },
  AFFECTED_FULL_PATCH_APPLICABLE: {
    className: "fa-exclamation-triangle text-warning",
    label: t("Affected, at least one patch available in an assigned channel"),
  },
  NOT_AFFECTED: {
    className: "fa-circle text-success",
    label: t("Not affected"),
  },
  PATCHED: {
    className: "fa-check-circle text-success",
    label: t("Patched"),
  },
  AFFECTED_PATCH_UNAVAILABLE: {
    className: "fa-exclamation-circle text-danger",
    label: t("Affected, patch is unavailable")
  },
  AFFECTED_PARTIAL_PATCH_APPLICABLE: {
    className: "fa-exclamation-circle text-danger",
    label: t("Affected, partial patch available in assigned channel")
  }
};
const TARGET_IMAGE = "IMAGE";
const TARGET_SERVER = "SERVER";
const CVE_REGEX = /(\d{4})-(\d{4,7})/i;
const CURRENT_YEAR = localizedMoment().year();
const YEARS = (function () {
  const arr: number[] = [];
  for (let i = 1999; i <= CURRENT_YEAR; i++) {
    arr.push(i);
  }
  return arr.reverse();
})();

function cveAudit(cveId, target, statuses) {
  return Network.post("/rhn/manager/api/audit/cve", {
    cveIdentifier: cveId,
    target: target,
    statuses: statuses,
  });
}

type Props = {};

type State = {
  cveNumber: string;
  cveYear: number;
  statuses: string[];
  resultType: string;
  results: any[];
  messages: any[];
  selectedItems: any[];
  target?: any;
  auditExecuted?: boolean;
};

class CVEAudit extends React.Component<Props, State> {
  constructor(props) {
    super(props);

    this.state = {
      cveNumber: "",
      cveYear: CURRENT_YEAR,
      statuses: ALL,
      resultType: TARGET_SERVER,
      results: [],
      messages: [],
      selectedItems: [],
      auditExecuted: false,
    };
  }

  searchData = (datum, criteria) => {
    if (criteria) {
      return datum.name.toLocaleLowerCase().includes(criteria.toLocaleLowerCase());
    }
    return true;
  };

  handleSelectItems = (items) => {
    const removed = this.state.selectedItems.filter((i) => !items.includes(i));
    const isAdd = removed.length === 0;
    const list = isAdd ? items : removed;

    this.setState({ selectedItems: items }, () => {
      const data = {
        label: "system_list",
        values: list,
        checked: isAdd,
      };
      Network.post("/rhn/ajax/item-selector", data);
    });
  };

  onTargetChange = (e) => {
    const value = e.target.value;
    this.setState({
      target: value,
    });
  };

  onCVEYearChange = (e) => {
    const value = e.target.value;
    this.setState({
      cveYear: value,
    });
  };

  onCVEChange = (e) => {
    const value = e.target.value;
    const parts = CVE_REGEX.exec(value);
    if (parts != null && DEPRECATED_unsafeEquals(parts.length, 3)) {
      const year = Number.parseInt(parts[1], 10);
      if (YEARS.includes(year)) {
        const number = parts[2];
        this.setState({
          cveYear: year,
          cveNumber: number,
        });
      }
    } else {
      this.setState({
        cveNumber: value,
      });
    }
  };

  isFiltered(criteria) {
    return criteria && criteria.length > 0;
  }

  audit = (target) => {
    cveAudit("CVE-" + this.state.cveYear + "-" + this.state.cveNumber, target, this.state.statuses).then((data) => {
      if (data.success) {
        this.setState({
          results: data.data,
          selectedItems: data.data.filter((i) => i.selected).map((i) => i.id),
          resultType: target,
          messages: [],
          auditExecuted: true,
        });
      } else {
        this.setState({
          results: [],
          selectedItems: [],
          messages: data.messages,
          auditExecuted: true,
        });
      }
    });
  };

  render() {
    return (
      <span>
        <TopPanel title={t("CVE Audit")} icon="fa-search" helpUrl="reference/audit/audit-cve-audit.html">
          <Messages
            items={this.state.messages.map((msg) => {
              return { severity: "warning", text: msg };
            })}
          />
          <div className="input-group">
            <span className="input-group-addon">{t("CVE")}</span>
            <select
              id="cveIdentifierYear"
              value={this.state.cveYear}
              onChange={this.onCVEYearChange}
              className="form-control"
            >
              {YEARS.map((year) => (
                <option value={year}>{year}</option>
              ))}
            </select>
            <span className="input-group-addon">-</span>
            <input
              id="cveIdentifierId"
              className="form-control"
              value={this.state.cveNumber}
              onChange={this.onCVEChange}
            />
          </div>
          <div>
            {ALL.map((status) => {
              return (
                <div className="checkbox">
                  <label>
                    <input
                      type="checkbox"
                      checked={this.state.statuses.includes(status)}
                      onChange={(e) => {
                        if (this.state.statuses.includes(status)) {
                          this.setState({
                            statuses: this.state.statuses.filter((x) => x !== status),
                          });
                        } else {
                          this.setState({
                            statuses: this.state.statuses.concat([status]),
                          });
                        }
                      }}
                    />
                    <i
                      className={"fa fa-big " + PATCH_STATUS_LABEL[status].className}
                      title={PATCH_STATUS_LABEL[status].label}
                    />
                    <span>{" " + PATCH_STATUS_LABEL[status].label}</span>
                  </label>
                </div>
              );
            })}
          </div>
          <p>
            <div className="btn-group">
              <AsyncButton
                id="bootstrap-btn"
                defaultType="btn-default"
                icon="fa-desktop"
                text={t("Audit Servers")}
                action={() => this.audit(TARGET_SERVER)}
              />
              <AsyncButton
                id="bootstrap-btn"
                defaultType="btn-default"
                icon="fa-hdd-o"
                text={t("Audit Images")}
                action={() => this.audit(TARGET_IMAGE)}
              />
            </div>
          </p>
          {this.state.auditExecuted && (
            <div>
              <p>
                <a
                  className="btn btn-link"
                  target="_blank"
                  rel="noopener noreferrer"
                  href={
                    "https://cve.mitre.org/cgi-bin/cvename.cgi?name=" + this.state.cveYear + "-" + this.state.cveNumber
                  }
                  data-senna-off="true"
                >
                  <IconTag type="external-link" />
                  {t("MITRE CVE link")}
                </a>
                <a
                  className="btn btn-link"
                  target="_blank"
                  rel="noopener noreferrer"
                  href={
                    "https://www.suse.com/security/cve/CVE-" + this.state.cveYear + "-" + this.state.cveNumber + ".html"
                  }
                  data-senna-off="true"
                >
                  <IconTag type="external-link" />
                  {t("SUSE Security CVE link")}
                </a>
              </p>
            </div>
          )}
          <Table
            data={this.state.results}
            identifier={(row) => row.id}
            initialSortColumnKey="id"
            selectable={this.state.resultType === TARGET_SERVER && this.state.results.length > 0}
            onSelect={this.handleSelectItems}
            selectedItems={this.state.selectedItems}
            searchField={<SearchField filter={this.searchData} placeholder={t("Filter by name")} />}
          >
            <Column
              columnKey="patchStatus"
              width="10%"
              comparator={Utils.sortByText}
              header={t("Status")}
              cell={(row, criteria) => (
                <div>
                  <i
                    className={"fa fa-big " + PATCH_STATUS_LABEL[row.patchStatus].className}
                    title={PATCH_STATUS_LABEL[row.patchStatus].label}
                  />
                </div>
              )}
            />
            <Column
              columnKey="name"
              width="45%"
              comparator={Utils.sortByText}
              header={t("Name")}
              cell={(row, criteria) => {
                const url =
                  this.state.resultType === TARGET_SERVER
                    ? "/rhn/systems/details/Overview.do?sid=" + row.id
                    : "/rhn/manager/cm/images#/overview/" + row.id;
                return (
                  <a href={url}>
                    <Highlight enabled={this.isFiltered(criteria)} text={row.name} highlight={criteria} />
                  </a>
                );
              }}
            />
            <Column
              columnKey="action"
              width="45%"
              comparator={Utils.sortByText}
              header={t("Actions")}
              cell={(row, criteria) => {
                if (this.state.resultType === TARGET_SERVER) {
                  if (row.patchStatus === NOT_AFFECTED || row.patchStatus === PATCHED ||
                      row.patchStatus === AFFECTED_PATCH_UNAVAILABLE) {
                    return t("No action required");
                  } else if (row.patchStatus === AFFECTED_FULL_PATCH_APPLICABLE) {
                    return (
                      <div>
                        <div>
                          <a href={"/rhn/systems/details/ErrataList.do?sid=" + row.id}>
                            {t("Install a new patch on this system.")}
                          </a>
                        </div>
                        {row.erratas.map((errata) => {
                          return (
                            <div>
                              <a href={"/rhn/errata/details/SystemsAffected.do?eid=" + errata.id}>{errata.advisory}</a>
                            </div>
                          );
                        })}
                      </div>
                    );
                  } else if (row.patchStatus === AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT) {
                    return (
                      <div>
                        <div>
                          <a href={"/rhn/systems/details/SPMigration.do?sid=" + row.id}>
                            {t("Patch available, but system needs to be migrated to a newer Product.")}
                          </a>
                        </div>
                        <div>{"Channel: " + row.channels[0].name}</div>
                        <div>{"Patch: " + row.erratas[0].advisory}</div>
                      </div>
                    );
                  } else if (row.patchStatus === AFFECTED_PATCH_INAPPLICABLE) {
                    return (
                      <div>
                        <div>
                          <a href="/rhn/channels/manage/Manage.do">
                            {t("Patch available, but needs to be cloned into Channel.")}
                          </a>
                        </div>
                        <div>{"Channel: " + row.channels[0].name}</div>
                        <div>{"Patch: " + row.erratas[0].advisory}</div>
                      </div>
                    );
                  } else {
                    return t("If you see this report a bug.");
                  }
                } else if (this.state.resultType === TARGET_IMAGE) {
                  if (row.patchStatus === NOT_AFFECTED || row.patchStatus === PATCHED ||
                      row.patchStatus === AFFECTED_PATCH_UNAVAILABLE) {
                    return t("No action required");
                  } else if (row.patchStatus === AFFECTED_FULL_PATCH_APPLICABLE) {
                    return (
                      <LinkButton
                        icon="fa-cogs"
                        href={"/rhn/manager/cm/rebuild/" + row.id}
                        className="btn-xs btn-default pull-right"
                        text={t("Rebuild")}
                      />
                    );
                  } else if (row.patchStatus === AFFECTED_PATCH_INAPPLICABLE) {
                    return (
                      <div>
                        <div>
                          <a href="/rhn/channels/manage/Manage.do">
                            {t("Patch available but needs to be cloned into Channel.")}
                          </a>
                        </div>
                        <div>{t("Channel") + ": " + row.channels[0].name}</div>
                        <div>{t("Errata") + ": " + row.erratas[0].advisory}</div>
                      </div>
                    );
                  } else {
                    return t("If you see this report a bug.");
                  }
                } else {
                  return t("If you see this report a bug.");
                }
              }}
            />
          </Table>
          <a
            href={
              "/rhn/manager/api/audit/cve.csv?cveIdentifier=CVE-" +
              this.state.cveYear +
              "-" +
              this.state.cveNumber +
              "&target=" +
              this.state.resultType +
              "&statuses=" +
              this.state.statuses
            }
            data-senna-off="true"
          >
            <IconTag type="item-download-csv" />
            {t("Download CSV")}
          </a>
        </TopPanel>
        Please note that underlying data needed for this audit is updated nightly. If systems were registered very
        recently or channel subscriptions have been changed in the last 24 hours it is recommended that an{" "}
        <a href="/rhn/admin/BunchDetail.do?label=cve-server-channels-bunch">extra CVE data update</a> is scheduled in
        order to ensure consistent results.
      </span>
    );
  }
}

export const renderer = () => SpaRenderer.renderNavigationReact(<CVEAudit />, document.getElementById("cveaudit"));
