/* eslint-disable */
'use strict';

import React, {useState, useEffect, forwardRef, useImperativeHandle} from "react";
import {Check} from "components/input/Check";
import {Form} from "components/input/Form";
import {Text} from "components/input/Text";
import {Button} from "components/buttons";
import {ModalButton} from "components/dialog/ModalButton";
import {DangerDialog} from "components/dialog/DangerDialog";

const MessagesUtils = require("components/messages").Utils;

const MaintenanceCalendarEdit = forwardRef((props, ref) => {
    const [calendarName, setCalendarName] = useState("");
    const [calendarData, setCalendarData] = useState();
    const [calendarDataText, setCalendarDataText] = useState("");
    const [icalLoading, setIcalLoading] = useState(false);
    const [strategy, setStrategy] = useState(false);

    useEffect(() => {
        if (props.isEdit) {
            setCalendarName(props.calendar.calendarName);
            setCalendarData(props.calendar.calendarData);
            setCalendarDataText(props.calendar.calendarUrl || "");
        }
    }, [props.calendar]);

    const model = {
        calendarName: calendarName,
        strategy: strategy
    };

    const onFormChanged = (model) => {
        /* strategy gets initialized as empty string, but we want the initial value to be false.
         * Is equivalent to: if strategy is "" then set it to false */
        model.strategy === "" && (model.strategy = false);
        setCalendarName(model.calendarName);
        setStrategy(model.strategy);
    };

    const onCalendarDataTextChanged = (event) => {
        setCalendarDataText(event.target.value);
    };

    const onIcalFileAttach = (event) => {
        props.isLoading(true);
        const reader = new FileReader();
        reader.onload = (e) => icalFileLoaded(e.target.result);
        reader.readAsText(event.target.files[0]);
        !props.isEdit && setCalendarDataText(event.target.files[0].name);
    };

    const onIcalFileRemove = () => {
        document.getElementById("ical-data-upload").value = "";
        setCalendarData(undefined);
        setCalendarDataText("");
    };

    const handleFileAttach = () => {
        document.getElementById("ical-data-upload").click();
    };

    const icalFileLoaded = (fileString) => {
        setCalendarData(fileString);
        props.isLoading(false);
    };

    const onConfirmRefresh = () => {
        setIcalLoading(true);
        props.onRefresh({
            calendarId: props.calendar.calendarId,
            calendarName: props.calendar.calendarName,
            calendarUrl: props.calendar.calendarUrl,
            strategy: strategy ? "Cancel" : "Fail"
        });
    };

    const validateUrl = (url) => {
        if (url.trim() === "") {
            return true;
        }
        try {
            const URL = new window.URL(url);
            return URL.protocol === "https:" || URL.protocol === "http:";
        }
        catch (_) {
            return false;
        }
    }

    useImperativeHandle(ref, () => ({
        onEdit() {
            const params = {
                calendarName: calendarName,
                calendarData: calendarData,
                calendarUrl:  (!props.isEdit && calendarData) ? "" : calendarDataText
            };

            if (props.isEdit) {
                params.calendarId = props.calendar.calendarId;
                params.strategy = strategy ? "Cancel" : "Fail";
            }
            validateUrl(params.calendarUrl)
                ? props.onEdit(params)
                : props.messages(MessagesUtils.error(t("Url '{0}' is invalid", params.calendarUrl)));
        }
    }));

    return (
        <Form onChange={model => onFormChanged(model)} model={model}>
            <Text name="calendarName" required type="text" label={t("Calendar Name")}
                  labelClass="col-md-3" divClass="col-md-6" disabled={props.isEdit}/>
            {(props.isEdit && !props.calendar.calendarUrl) &&
            <Check name="strategy" label={<b>{t("Cancel affected actions")}</b>} divClass="col-md-6 col-md-offset-3" />
            }
            <div className="form-horizontal">
                <div className="form-group">
                    <label className="col-md-3 control-label">{t("Calendar data")}:</label>
                    {(!props.isEdit || props.calendar.calendarUrl) &&
                    <div className={"align-middle col-md-" + (props.isEdit ? "5" : "4")} >
                        <input type="text" className="form-control text-truncate"
                               placeholder={t("Enter Url to ical file")}
                               value={calendarDataText}
                               disabled={!props.isEdit && calendarData}
                               onChange={onCalendarDataTextChanged}/>
                        {!calendarData && <b className="pl-4">or</b>}
                    </div>
                    }
                    {!(props.isEdit && props.calendar.calendarUrl) ? (
                            !calendarData ?
                                <div className="pl-0 col-md-1">
                                    <Button id="ical-upload-btn" className="btn-default"
                                            text={t("Attach file")}
                                            handler={handleFileAttach}
                                    />
                                </div>
                                :
                                <div className="col-md-1">
                                    <Button id="ical-rm-btn" className="btn-default" text={t("Remove file")}
                                            handler={onIcalFileRemove}/>
                                </div>
                        ) :
                        <div className="col-md-1">
                            <ModalButton id="url-refresh-btn"
                                         className="btn-default btn-sm"
                                         icon={icalLoading ? "fa fa-circle-o-notch fa-spin" : "fa-refresh"}
                                         target="confirm-modal"
                                         title={t("Refresh data from url")}
                                         disabled={props.calendar.calendarUrl !== calendarDataText}
                            />
                            <DangerDialog id="confirm-modal"
                                          title={t("Confirm calendar refresh")}
                                          content={
                                              <div>
                                                  <div>{t("Refreshing the calendar will reschedule all affected actions.")}</div>
                                                  <div>{t("Confirm if you want to proceed.")}</div>
                                                  <Check name="strategy" label={<b>{t("Cancel affected actions?")}</b>} divClass="col-md-6" />
                                              </div>
                                          }
                                          onConfirm={() => onConfirmRefresh()}
                                          submitText={t("Confirm")}
                                          submitIcon="fa-check"
                            />
                        </div>
                    }
                </div>
                <input className="hidden" type="file" id="ical-data-upload" onChange={onIcalFileAttach}/>
            </div>
            {(props.isEdit && calendarData) &&
            <div className="panel panel-default">
                <div className="panel-heading">
                    <h4>
                        {props.calendar.calendarName}
                    </h4>
                </div>
                <div className="panel-body">
                        <pre>
                            {calendarData}
                        </pre>
                </div>
            </div>
            }
        </Form>
    );
});

export default MaintenanceCalendarEdit;
