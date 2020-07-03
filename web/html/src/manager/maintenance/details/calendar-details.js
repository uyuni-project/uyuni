/* eslint-disable */
'use strict';

import React, {useState} from "react";
import {BootstrapPanel} from "components/panels/BootstrapPanel";
import {Table} from "components/table/Table";
import {Column} from "components/table/Column";
import {Check} from "components/input/Check";
import {Form} from "components/input/Form";
import {DeleteDialog} from "components/dialog/DeleteDialog";

const MaintenanceCalendarDetails = (props) => {
    const [strategy, setStrategy] = useState(false);

    const setCheck = (model) => {
        /* strategy gets initialized as empty string, but we want the initial value to be false.
         * Is equivalent to: if strategy is "" then set it to false */
        model.strategy === "" && (model.strategy = false);
        setStrategy(model.strategy);
    };

    return (
        <>
            <DeleteDialog id="delete-modal"
                          title={t("Delete maintenance calendar")}
                          content={
                              <Form model={{strategy}} onChange={setCheck}>
                                  <div>{t("Are you sure you want to delete the selected item?")}</div>
                                  <div>{t("This will remove the current schedule from all the systems assigned to it.")}</div>
                                  <Check name="strategy" label={<b>{t("Cancel affected actions?")}</b>} divClass="col-md-6" />
                              </Form>
                          }
                          onConfirm={() => props.onDelete({
                              calendarName: props.calendarName,
                              strategy: strategy ? "Cancel" : "Fail"
                          })}
            />
            <MaintenanceCalendarOverview
                calendarName={props.calendarName}
                scheduleNames={props.scheduleNames}
                calendarUrl={props.calendarUrl}
                calendarData={props.calendarData}
            />
        </>
    );
}

type OverviewProps = {
    calendarName: string,
    schedulesName: Array<string>,
    calendarUrl: string,
    calendarData: string,
}
const MaintenanceCalendarOverview = (props) => {
    const tableData = [
        {left: t("Calendar Name") + ":", right: props.calendarName},
        {left: t("Used by Schedule") + ":", right: props.scheduleNames.map(name => name.name).join(", ")},
    ];
    props.calendarUrl && tableData.push({left: t("Url") + ":", right: props.calendarUrl});

    return (
        <div>
            <BootstrapPanel title={t("Calendar Details")}>
                <Table
                    data={tableData}
                    identifier={row => tableData.indexOf(row)}
                    initialItemsPerPage={0}
                >
                    <Column columnKey="left" cell={(row) => row.left} />
                    <Column columnKey="right" cell={(row) => row.right} />
                </Table>
            </BootstrapPanel>
            {
                props.calendarData &&
                <div className="panel panel-default">
                    <div className="panel-heading">
                        <h4>
                            {props.calendarName}
                        </h4>
                    </div>
                    <div className="panel-body">
                        <pre>
                            {props.calendarData}
                        </pre>
                    </div>
                </div>
            }
        </div>
    );
}

export default MaintenanceCalendarDetails;
