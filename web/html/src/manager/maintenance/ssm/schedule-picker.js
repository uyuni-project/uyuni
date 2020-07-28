// @flow

import React, {useState, useEffect, useContext} from "react";

import {AsyncButton} from "components/buttons";
import {ModalButton} from "components/dialog/ModalButton";
import {Form, FormContext} from "components/input/Form";
import {Select} from "components/input/Select";
import {Check} from "components/input/Check";
import {Utils as MessagesUtils} from "components/messages";

import CancelActionsDialog from "../shared/cancel-actions-dialog";

import type {Node} from 'react';
import type {MessageType} from "components/messages";

const Network = require("utils/network");

type ScheduleType = {
  scheduleId: number,
  scheduleName: string
};

type WithMaintenanceSchedulesProps = {
  systems: string[],
  onMessage: (messages: MessageType[]) => void,
  children: (
    schedules: ScheduleType[],
    onAssign: (scheduleId: number, cancelActions: boolean) => Promise<any>
  ) => Node
};

export function WithMaintenanceSchedules(props: WithMaintenanceSchedulesProps) {
  const [schedules, setSchedules] = useState([]);

  const onAssign = (scheduleId: number, cancelActions: boolean): Promise<any> => {
    let uri;
    let data;
    let successMsg;
    if (scheduleId === 0) {
      uri = "/rhn/manager/api/maintenance/schedule/unassign";
      successMsg = t("Maintenance schedule has been cleared");
      data = props.systems;
    }
    else {
      uri = `/rhn/manager/api/maintenance/schedule/${scheduleId}/assign`;
      successMsg = t("Maintenance schedule has been assigned");
      data = {
        systemIds: props.systems,
        cancelActions: cancelActions
      };
    }

    return Network.post(uri, JSON.stringify(data), "application/json", false).promise
      .then(() => props.onMessage(MessagesUtils.success(successMsg)))
      .catch(xhr => props.onMessage(
        MessagesUtils.error(Network.errorMessageByStatus(xhr.status))));
  };

  useEffect(() => {
    Network.get("/rhn/manager/api/maintenance/schedule/list").promise
      .then(setSchedules);
  }, []);

  return props.children(schedules, onAssign);
}

type SchedulePickerFormProps = {
  schedules: ScheduleType[],
  onAssign: (scheduleId: number, cancelActions: boolean) => Promise<any>
};

export function SchedulePickerForm(props: SchedulePickerFormProps) {
  const [model, setModel] = useState({});
  const [isValid, setValid] = useState(false);
  const onSubmit = () => props.onAssign(parseInt(model.scheduleId), model.cancelActions);
  const onChange = (model) => setModel(Object.assign({}, model));

  return (
    <>
      <Form model={model} onChange={onChange} onValidate={setValid}>
        <SchedulePicker schedules={props.schedules}/>
        <div className="form-group">
          <div className="col-md-offset-3 col-md-6">
            {
              model.scheduleId === "0" ?
                <AsyncButton
                  id="submit-btn"
                  text={t("Clear All")}
                  icon={t("fa-times")}
                  action={onSubmit}
                  defaultType="btn-danger"
                  className="pull-right"
                  disabled={!isValid}
                /> :
                ( model.cancelActions ?
                  <ModalButton
                    id="submit-btn"
                    target="cancel-confirm"
                    text={t("Assign All")}
                    icon={t("fa-edit")}
                    className="btn-success pull-right"
                    disabled={!isValid}
                  /> :
                  <AsyncButton
                    id="submit-btn"
                    text={t("Assign All")}
                    icon={t("fa-edit")}
                    action={onSubmit}
                    defaultType="btn-success"
                    className="pull-right"
                    disabled={!isValid}
                  />
                )
            }
          </div>
        </div>
      </Form>
      <CancelActionsDialog id="cancel-confirm" onConfirmAsync={onSubmit}/>
    </>
  );
}

export function SchedulePicker(props: {schedules: ScheduleType[]}) {
  const context = useContext(FormContext);
  return (
    <>
      <Select
        name="scheduleId"
        label={t("Schedule")}
        labelClass="col-md-3"
        divClass="col-md-6"
        required
        defaultValue=""
      >
        <option key="-1" value="" disabled>Select a schedule</option>
        <option key="0" value="0">None - clear schedule</option>
        {props.schedules.map(s => <option key={s.scheduleId} value={s.scheduleId}>{s.scheduleName}</option>)}
      </Select>
      { context.model.scheduleId !== "0" &&
        <Check
          name="cancelActions"
          label={t("Cancel affected actions")}
          divClass="col-md-6 col-md-offset-3"
        />
      }
    </>
  );
}
