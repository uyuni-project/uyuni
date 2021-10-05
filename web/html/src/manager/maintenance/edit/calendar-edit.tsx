import * as React from "react";
import validator from "validator";
import { useState, useEffect, forwardRef, useImperativeHandle } from "react";
import { Check } from "components/input/Check";
import { Form } from "components/input/Form";
import { Text } from "components/input/Text";
import { Button } from "components/buttons";
import { ModalButton } from "components/dialog/ModalButton";
import { DangerDialog } from "components/dialog/LegacyDangerDialog";

import { MessageType, Utils as MessagesUtils } from "components/messages";

type CalendarEditProps = {
  messages: (messages: MessageType[]) => any;
  isEdit: boolean;
  calendar?: {
    id: number;
    name: string;
    url?: string;
    data: string;
  };
  onRefresh: (...args: any[]) => any;
  onEdit: (...args: any[]) => any;
  isLoading: (...args: any[]) => any;
};

const MaintenanceCalendarEdit = forwardRef((props: CalendarEditProps, ref) => {
  const [model, setModel] = useState<{ name?: string; strategy: boolean }>({ name: "", strategy: false });
  const [data, setData] = useState<string | undefined>();
  const [dataText, setDataText] = useState("");
  const [icalLoading, setIcalLoading] = useState(false);

  useEffect(() => {
    if (props.isEdit) {
      setModel({ ...model, name: props.calendar?.name });
      setData(props.calendar?.data);
      setDataText(props.calendar?.url || "");
    }
  }, [props.calendar]);

  const onFormChanged = (newModel) => {
    /* strategy gets initialized as empty string, but we want the initial value to be false.
     * Is equivalent to: if strategy is "" then set it to false */
    newModel.strategy === "" && (newModel.strategy = false);
    setModel({ name: newModel.name, strategy: newModel.strategy });
  };

  const onDataTextChanged = (event) => {
    setDataText(event.target.value);
  };

  const onIcalFileAttach = (event) => {
    props.isLoading(true);
    const reader = new FileReader();
    reader.onload = (e) => icalFileLoaded(e.target?.result);
    reader.readAsText(event.target.files[0]);
    !props.isEdit && setDataText(event.target.files[0].name);
  };

  const onIcalFileRemove = () => {
    const fileUpload = document.getElementById("ical-data-upload") as HTMLInputElement | null;
    if (fileUpload) {
      fileUpload.value = "";
    }
    setData(undefined);
    setDataText("");
  };

  const handleFileAttach = () => {
    const fileUpload = document.getElementById("ical-data-upload") as HTMLInputElement | null;
    if (fileUpload) {
      fileUpload.click();
    }
  };

  const icalFileLoaded = (fileString) => {
    setData(fileString);
    props.isLoading(false);
  };

  const onConfirmRefresh = () => {
    setIcalLoading(true);
    props
      .onRefresh({
        id: props.calendar?.id,
        name: props.calendar?.name,
        url: props.calendar?.url,
        strategy: model.strategy ? "Cancel" : "Fail",
      })
      .then(() => setIcalLoading(false));
  };

  const validateUrl = (urlIn) => {
    if (urlIn.trim() === "") {
      return true;
    }
    return validator.isURL(urlIn, { protocols: ["http", "https"] });
  };

  useImperativeHandle(ref, () => ({
    onEdit() {
      const params: any = {
        name: model.name,
        data: data,
        url: !props.isEdit && data ? "" : dataText,
      };

      if (props.isEdit) {
        params.id = props.calendar?.id;
        params.strategy = model.strategy ? "Cancel" : "Fail";
      }
      validateUrl(params.url)
        ? props.onEdit(params)
        : props.messages(MessagesUtils.error(t("Url '{0}' is invalid", params.url)));
    },
  }));

  return (
    <Form onChange={(model) => onFormChanged(model)} model={model}>
      <Text
        name="name"
        required
        type="text"
        label={t("Calendar Name")}
        labelClass="col-md-3"
        divClass="col-md-6"
        disabled={props.isEdit}
      />
      {props.isEdit && !props.calendar?.url && (
        <Check name="strategy" label={<b>{t("Cancel affected actions")}</b>} divClass="col-md-6 col-md-offset-3" />
      )}
      <div className="form-horizontal">
        <div className="form-group">
          <label className="col-md-3 control-label">{t("Calendar data")}:</label>
          {(!props.isEdit || props.calendar?.url) && (
            <div className={"align-middle col-md-" + (props.isEdit ? "5" : "4")}>
              <input
                type="text"
                className="form-control text-truncate"
                id="calendar-data-text"
                name="calendar-data-text"
                placeholder={t("Enter Url to ical file")}
                value={dataText}
                disabled={Boolean(!props.isEdit && data)}
                onChange={onDataTextChanged}
              />
              {!data && <b className="pl-4">or</b>}
            </div>
          )}
          {!(props.isEdit && props.calendar?.url) ? (
            !data ? (
              <div className="pl-0 col-md-1">
                <Button
                  id="ical-upload-btn"
                  className="btn-default"
                  text={t("Attach file")}
                  handler={handleFileAttach}
                />
              </div>
            ) : (
              <div className="col-md-1">
                <Button id="ical-rm-btn" className="btn-default" text={t("Remove file")} handler={onIcalFileRemove} />
              </div>
            )
          ) : (
            <div className="col-md-1">
              <ModalButton
                id="url-refresh-btn"
                className="btn-default btn-sm"
                icon={icalLoading ? "fa fa-circle-o-notch fa-spin" : "fa-refresh"}
                target="confirm-modal"
                title={t("Refresh data from url")}
                disabled={props.calendar.url !== dataText}
              />
              <DangerDialog
                id="confirm-modal"
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
          )}
        </div>
        <input className="hidden" type="file" id="ical-data-upload" onChange={onIcalFileAttach} />
      </div>
      {props.isEdit && data && (
        <div className="panel panel-default">
          <div className="panel-heading">
            <h4>{props.calendar?.name}</h4>
          </div>
          <div className="panel-body">
            <pre>{data}</pre>
          </div>
        </div>
      )}
    </Form>
  );
});

export default MaintenanceCalendarEdit;
