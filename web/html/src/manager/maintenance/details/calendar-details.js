/* eslint-disable */
'use strict';

import React, {useState} from "react";
import {BootstrapPanel} from "components/panels/BootstrapPanel";
import {Table} from "components/table/Table";
import {Column} from "components/table/Column";
import {Check} from "components/input/Check";
import {Form} from "components/input/Form";
import {DeleteDialog} from "components/dialog/DeleteDialog";

type CalendarDetailsProps = {
    name: string,
    scheduleNames: Array<Map<string, string>>,
    url: string,
    data: string,
    onDelete: () => void
};

const MaintenanceCalendarDetails = (props: CalendarDetailsProps) => {
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
                              name: props.name,
                              strategy: strategy ? "Cancel" : "Fail"
                          })}
            />
            <MaintenanceCalendarOverview
                name={props.name}
                scheduleNames={props.scheduleNames}
                url={props.url}
                data={props.data}
            />
        </>
    );
}

type OverviewProps = {
    name: string,
    scheduleNames: Array<Map<string, string>>,
    url: string,
    data: string,
}
const MaintenanceCalendarOverview = (props: OverviewProps) => {
    const tableData = [
        {left: t("Calendar Name") + ":", right: props.name},
        {left: t("Used by Schedule") + ":", right: props.scheduleNames.map(name => name.name).join(", ")},
    ];
    props.url && tableData.push({left: t("Url") + ":", right: props.url});

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
                props.data &&
                <div className="panel panel-default">
                    <div className="panel-heading">
                        <h4>
                            {props.name}
                        </h4>
                    </div>
                    <div className="panel-body">
                        <pre>
                            {props.data}
                        </pre>
                    </div>
                </div>
            }
        </div>
    );
}

export default MaintenanceCalendarDetails;
