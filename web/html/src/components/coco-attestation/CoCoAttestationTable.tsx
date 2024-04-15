import * as React from "react";

import { pageSize } from "core/user-preferences";

import { SystemLink } from "components/links";

import { Button } from "../buttons";
import { BootstrapPanel } from "../panels/BootstrapPanel";
import { Column } from "../table/Column";
import { Table } from "../table/Table";
import { AttestationReport, renderStatus, renderTime } from "./Utils";

type Props = {
  dataUrl: string;
  showSystem?: boolean;
  onReportDetails: (report: AttestationReport) => void;
};

class CoCoAttestationTable extends React.Component<Props> {
  public static readonly defaultProps: Partial<Props> = {
    showSystem: true,
  };

  render = () => {
    return (
      <BootstrapPanel>
        <div className="panel panel-default">
          <div className="panel-heading">
            <div>
              <h3>{t("Attestations")}</h3>
            </div>
          </div>
          <div>
            <Table
              selectable={false}
              data={this.props.dataUrl}
              identifier={(row) => row.id}
              initialItemsPerPage={pageSize}
              emptyText={t(`No attestation report available. Schedule an execution to create one.`)}
              initialSortColumnKey="executionTime"
            >
              {this.props.showSystem && (
                <Column
                  columnClass="text-center"
                  headerClass="text-center"
                  columnKey="system"
                  header={t("System")}
                  cell={(row) => <SystemLink id={row.systemId}>{row.systemName}</SystemLink>}
                />
              )}
              <Column
                columnClass="text-center"
                headerClass="text-center"
                columnKey="creationTime"
                header={t("Created")}
                cell={(row) => renderTime(row.creationTime)}
              />
              <Column
                columnClass="text-center"
                headerClass="text-center"
                columnKey="modificationTime"
                header={t("Last modified")}
                cell={(row) => renderTime(row.modificationTime)}
              />
              <Column
                columnClass="text-center"
                headerClass="text-center"
                columnKey="status"
                header={t("Status")}
                cell={(row) => renderStatus(row.status, row.statusDescription)}
              />
              <Column
                columnClass="text-center"
                headerClass="text-center"
                columnKey="attestationTime"
                header={t("Attestation time")}
                cell={(row) => renderTime(row.attestationTime)}
              />
              <Column
                columnClass="text-right"
                headerClass="text-right"
                header={t("Actions")}
                cell={(row) => (
                  <div className="btn-group">
                    <Button
                      className="btn-default btn-sm"
                      title={t("Details")}
                      icon="fa-list"
                      handler={() => this.props.onReportDetails(row)}
                    />
                  </div>
                )}
              />
            </Table>
          </div>
        </div>
      </BootstrapPanel>
    );
  };
}

export default CoCoAttestationTable;
