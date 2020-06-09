/* eslint-disable */
'use strict';

const React = require("react");

const ReactDOM = require("react-dom");
const AsyncButton = require("components/buttons").AsyncButton;
const Button = require("components/buttons").Button;
const ModalButton = require("components/dialog/ModalButton").ModalButton;
const DangerDialog = require("components/dialog/DangerDialog").DangerDialog;
const { Combobox } = require("components/combobox");
const { InnerPanel } = require("components/panels/InnerPanel");
const { Form } = require('components/input/Form');
const { Text } = require('components/input/Text');
const { Radio } = require('components/input/Radio');
const { Check } = require('components/input/Check');

class MaintenanceWindowsEdit extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            type: props.type,
            calendarDataText: "",
            selectedCalendar: 0
        };

        if(this.isEdit()) {
            this.setSchedule(this.props.schedule);
        }
    }

    setSchedule = (schedule) => {
        Object.assign(this.state, schedule);
        this.state.calendarDataText = schedule.calendarUrl;
        Object.assign(this.state, {
            calendarAdded: schedule.calendarName ? true : false
        })
        if (this.state.type === "schedule" && this.props.calendarNames.length > 1 && this.props.schedule.calendarName) {
            Object.assign(this.state, {
                selectedCalendar: this.props.calendarNames.filter(name => name.text === this.props.schedule.calendarName)[0].id
            });
        }
    };

    setCalendarName = () => {
        return this.state.type === "schedule" ?
            ((this.state.calendarAdded && this.state.calendarName) ? this.state.calendarName : "<None>")
            : this.state.calendarName;
    };

    isEdit = () => {
        return this.props.schedule ? true : false;
    };

    onEdit = () => {
        return this.props.onEdit({
            scheduleId: this.state.scheduleId,
            scheduleName: this.state.scheduleName,
            scheduleType: this.state.scheduleType,
            calendarId: this.state.calendarId,
            calendarName: this.setCalendarName(),
            calendarData: this.state.calendarData,
            calendarUrl: (!this.isEdit() && this.state.calendarData) ? "" : this.state.calendarDataText,
            /* Reschedule strategy. false == 'FAIL', true == 'CANCEL' */
            strategy: this.state.strategy ? "Cancel" : "Fail"
        });
    };

    onFormChanged = (model) => {
        model.strategy === "" && (model.strategy = false);
        this.setState(model);
    };

    onCalendarDataTextChanged = (event) => {
        this.setState({
            calendarDataText: event.target.value
        });
    };

    onIcalFileAttach = (event) => {
        this.setState({
            icalLoading: true
        });
        const reader = new FileReader();
        reader.onload = (e) => this.icalFileLoaded(e.target.result);
        reader.readAsText(event.target.files[0]);
        this.setState({
            calendarDataText: event.target.files[0].name
        });
    };

    onIcalFileRemove = () => {
        document.getElementById("ical-data-upload").value = "";
        this.setState({
            calendarData: undefined,
            calendarDataText: "",
        });
    };

    handleFileAttach = () => {
        document.getElementById("ical-data-upload").click();
    };

    icalFileLoaded = (fileString) => {
        this.setState({
            calendarData: fileString,
            icalLoading: false
        });
    };

    onSelectCalendar = (item) => {
        this.setState({
            selectedCalendar: item.id,
            calendarName: item.text
        })
    };

    renderScheduleEdit() {
        const model = {
            scheduleName: this.state.scheduleName,
            scheduleType: this.state.scheduleType,
            calendarName: this.state.calendarName,
            strategy: this.state.strategy,
        }
        return (
            <Form onChange={this.onFormChanged} model={model}>
                <Text name="scheduleName" required type="text" label={t("Schedule Name")}
                      labelClass="col-sm-3" divClass="col-sm-6"
                      disabled={this.isEdit()} />
                <Radio defaultValue="SINGLE" name="scheduleType" inline={true} label={t('Type')} labelClass="col-md-3" divClass="col-md-6"
                       items={[
                           {label: <b>{t('Single')}</b>, value: 'SINGLE'},
                           {label: <b>{t('Multi')}</b>, value: 'MULTI'},
                       ]}
                />
                {this.isEdit() &&
                <Check name="strategy" label={<b>{t("Cancel affected actions")}</b>} divClass="col-md-6 col-md-offset-3" />
                }
                <div className="form-group">
                    <div className="col-md-6 col-md-offset-3">
                        {!this.state.calendarAdded ?
                            <Button className="btn-default" text="Add Calendar" icon="fa-chevron-down "
                                    handler={() => this.setState({calendarAdded: !this.state.calendarAdded})} />
                            :
                            <div className="panel panel-default">
                                <div className="panel-heading" style={{padding: "0px"}}>
                                    <Button text="Add Calendar" icon="fa-chevron-up"
                                            handler={() => this.setState({calendarAdded: !this.state.calendarAdded})} />
                                </div>
                                <div className="panel-body">
                                    <div className="form-horizontal">
                                        <div className="form-group">
                                            <label className="col-md-3 control-label">{t("Calendar")}:</label>
                                            <div className="col-md-7">
                                                <Combobox id="calendarSelect" name="calendarSelect"
                                                          data={this.props.calendarNames}
                                                          selectedId={this.state.selectedCalendar}
                                                          onSelect={this.onSelectCalendar}
                                                />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        }
                    </div>
                </div>
            </Form>
        )
    }

    renderCalendarEdit() {
        const model = {
            calendarName: this.state.calendarName,
            strategy: this.state.strategy
        }
        return (
            <Form onChange={model => this.onFormChanged(model)} model={model}>
                <Text name="calendarName" required type="text" label={t("Calendar Name")}
                      labelClass="col-md-3" divClass="col-md-6" disabled={this.isEdit()}/>
                {(this.isEdit() && !this.state.calendarUrl) &&
                <Check name="strategy" label={<b>{t("Cancel affected actions")}</b>} divClass="col-md-6 col-md-offset-3" />
                }
                <div className="form-horizontal">
                    <div className="form-group">
                        <label className="col-md-3 control-label">{t("Calendar data")}:</label>
                        {(!this.isEdit() || this.state.calendarUrl) &&
                        <div className={"align-center col-md-" + (this.isEdit() ? "5" : "4")} >
                            <input type="text" className="form-control"
                                   style={{textOverflow: "ellipsis"}}
                                   placeholder={t("Enter Url to ical file")}
                                   value={this.state.calendarDataText}
                                   disabled={this.state.calendarData && !this.state.calendarUrl}
                                   onChange={this.onCalendarDataTextChanged}/>
                            {!this.state.calendarData && <b style={{paddingLeft: "25px"}}>or</b>}
                        </div>
                        }
                        {!(this.isEdit() && this.state.calendarUrl) ? (
                                !this.state.calendarData ?
                                    <div className="col-md-1">
                                        <Button id="ical-upload-btn" className="btn-default"
                                                text={t("Attach file")}
                                                handler={this.handleFileAttach}
                                        />
                                    </div>
                                    :
                                    <div className="col-md-1">
                                        <Button id="ical-rm-btn" className="btn-default" text={t("Remove file")}
                                                handler={this.onIcalFileRemove}/>
                                    </div>
                            ) :
                            <div className="col-md-1">
                                <ModalButton id="url-refresh-btn"
                                             className="btn-default btn-sm"
                                             icon="fa-refresh"
                                             target="confirm-modal"
                                             title={t("Refresh data from url")}
                                             disabled={this.state.calendarUrl !== this.state.calendarDataText}
                                />
                                <DangerDialog id="confirm-modal"
                                              title="Confirm calendar refresh"
                                              content={
                                                  <div>
                                                      <div>{t("Refreshing the calendar causes affected actions to be rescheduled.")}</div>
                                                      <div>{t("Confirm if you want to proceed.")}</div>
                                                      <Check name="strategy" label={<b>{t("Cancel affected actions?")}</b>} divClass="col-md-6" />
                                                  </div>
                                              }
                                              onConfirm={() => this.props.onRefresh({
                                                  calendarId: this.state.calendarId,
                                                  calendarName: this.state.calendarName,
                                                  strategy: this.state.strategy ? "Cancel" : "Fail",
                                                  calendarUrl: this.state.calendarUrl
                                                  })}
                                              submitText="Confirm"
                                              submitIcon="fa-check"
                                />
                            </div>
                        }
                    </div>
                </div>
                {(this.isEdit() && this.state.calendarData) &&
                <div className="panel panel-default">
                    <div className="panel-heading">
                        <h4>
                            {this.state.calendarName}
                        </h4>
                    </div>
                    <div className="panel-body">
                        <pre>
                            {this.state.calendarData}
                        </pre>
                    </div>
                </div>
                }
            </Form>
        )
    }

    render() {
        const buttons = [
            <div className="btn-group pull-right">
                <AsyncButton action={this.onEdit} defaultType="btn-success"
                             disabled={this.state.icalLoading}
                             text={(this.isEdit() ? t("Update ") : t("Create ")) +
                             (this.state.type === "schedule" ? t("Schedule") : t("Calendar"))}
                />
            </div>
        ];
        const buttonsLeft = [
            <div className="btn-group pull-left">
                <Button id="back-btn" className="btn-default" icon="fa-chevron-left" text={t("Back")}  handler={() => this.props.onActionChanged("back")}/>
            </div>
        ];
        return (
            <InnerPanel title={t("Schedule Maintenance Window")} icon="spacewalk-icon-salt" buttonsLeft={buttonsLeft} buttons={buttons} >
                {this.state.type === "schedule" ? this.renderScheduleEdit() : this.renderCalendarEdit()}
                <input type="file" id="ical-data-upload" style={{display: "none"}} onChange={this.onIcalFileAttach}/>
            </InnerPanel>
        );
    }
}

module.exports = {
    MaintenanceWindowsEdit: MaintenanceWindowsEdit
};
