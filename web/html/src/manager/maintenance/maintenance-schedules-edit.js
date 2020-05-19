/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const AsyncButton = require("components/buttons").AsyncButton;
const Button = require("components/buttons").Button;
const { Combobox } = require("components/combobox");
const { InnerPanel } = require("components/panels/InnerPanel");
const { Form } = require('components/input/Form');
const { Text } = require('components/input/Text');
const { Radio } = require('components/input/Radio');
const { Check } = require('components/input/Check');

class MaintenanceSchedulesEdit extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            /* For testing only */
            calendar: [
                {id: 0, text: "Test-calendar-name1"},
                {id: 1, text: "Test-calendar-name2"},
                {id: 2, text: "Test-calendar-name3"}
            ],
            /* TODO: States to add */
            scheduleName: "",
            calendarName: "",
            scheduleType: "single",
            calendarData: undefined,
            calendarAdded: false,
            fileName: "",
            view: "existing",
            hover: false
        };

        if(this.isEdit()) {
            this.setSchedule(this.props.schedule);
        } else {
            /* TODO */
        }
    }

    setSchedule = (schedule) => {
        Object.assign(this.state, schedule);
    };

    isEdit = () => {
        return this.props.schedule ? true : false;
    };

    onEdit = () => {
        return this.props.onEdit({
            scheduleId: this.state.scheduleId,
            scheduleName: this.state.scheduleName,
            scheduleType: this.state.scheduleType,
            calendarAdded: this.state.calendarAdded,
            calendarType: this.state.view,
            calendarId: this.state.calendarId,
            calendarName: this.state.calendarName,
            calendarData: this.state.calendarData,
            calendarUrl: this.state.calendarUrl
        });
    };

    onFormChanged = (model) => {
        this.setState({
            scheduleName: model.scheduleName,
            scheduleType: model.scheduleType,
            calendarName: model.calendarName || this.state.calendarName,
        });
    };

    setModel = () => {
        return {
            scheduleName: this.state.scheduleName,
            scheduleType: this.state.scheduleType,
            calendarName: this.state.calendarName,
        }
    }

    onFileDestChanged = (event) => {
        this.setState({
            fileName: event.target.value
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
            fileName: event.target.files[0].name
        });
    };

    onIcalFileRemove = () => {
        this.setState({
            calendarData: undefined,
            calendarUrl: undefined,
            fileName: ""
        });
    };

    handleFileAttach = () => {
        this.state.fileName === "" ?
            document.getElementById("ical-data-upload").click()
            : this.setState({calendarUrl: this.state.fileName}) //this.getDataFromURL(this.state.fileName);
    };

    // getDataFromURL = (url) => {
    //     fetch(url).then(response => {
    //         response.text().then(data => {
    //             this.setState({
    //                 calendarData: data
    //             })
    //         });
    //     });
    // };

    icalFileLoaded = (fileString) => {
        this.setState({
            calendarData: fileString,
            icalLoading: false
        });
    };

    setView = (view) => {
        this.setState({
            view: view
        });
    };

    onToggleHover = () => {
        this.setState({
            hover: !this.state.hover
        })
    };

    onSelectCalendar = (item) => {
        this.setState({
            calendarName: item.text
        })
    };

    render() {
        const buttons = [
            <div className="btn-group pull-right">
                <AsyncButton action={this.onEdit} defaultType="btn-success" text={(this.isEdit() ? t("Update ") : t("Create ")) + t("Schedule")} />
            </div>
        ];
        const buttonsLeft = [
            <div className="btn-group pull-left">
                <Button id="back-btn" className="btn-default" icon="fa-chevron-left" text={t("Back")}  handler={() => this.props.onActionChanged("back")}/>
            </div>
        ];
        const hover = this.state.hover ? {cursor: "pointer"} : {};
        return (
            <InnerPanel title={t("Schedule Maintenance Window")} icon="spacewalk-icon-salt" buttonsLeft={buttonsLeft} buttons={buttons} >
                <Form onChange={this.onFormChanged} model={this.setModel()}>
                    <Text name="scheduleName" required type="text" label={t("Schedule Name")} labelClass="col-sm-3" divClass="col-sm-6" />
                    <Radio name="scheduleType" inline={true} label={t('Type')} labelClass="col-md-3" divClass="col-md-6"
                           items={[
                               {label: <b>{t('Single')}</b>, value: 'single'},
                               {label: <b>{t('Multi')}</b>, value: 'multi'},
                           ]}
                    />
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
                                        <div className="spacewalk-content-nav" id="config-channels-tabs">
                                            <ul className="nav nav-tabs">
                                                <li className={this.state.view === 'existing' || this.state.view === '' ? 'active' : ''}>
                                                    <a onClick={() => this.setView('existing')} style={hover}
                                                       onMouseEnter={this.onToggleHover}
                                                       onMouseLeave={this.onToggleHover}>{t("Select existing")}</a>
                                                </li>
                                                <li className={this.state.view === 'new' ? 'active' : ''}>
                                                    <a onClick={() => this.setView('new')} style={hover}
                                                       onMouseEnter={this.onToggleHover}
                                                       onMouseLeave={this.onToggleHover}>{t("Create new")}</a>
                                                </li>
                                            </ul>
                                        </div>
                                        {this.state.view === "new" ?
                                            <div className="form-horizontal">
                                                <Text name="calendarName" required type="text" label={t("Calendar Name")}
                                                      labelClass="col-md-3" divClass="col-md-8"/>
                                                <div className="form-group">
                                                    <label className="col-md-3 control-label">{t("Calendar data")}:</label>
                                                    <div className="col-md-6" style={{alignItems: "center", display: "flex"}}>
                                                        <input type="text" className="form-control"
                                                               placeholder={t("Enter Url to ical file")}
                                                               value={this.state.fileName}
                                                               disabled={this.state.calendarData || this.state.calendarUrl}
                                                               onChange={this.onFileDestChanged}/>
                                                        {!this.state.calendarData && <b style={{paddingLeft: "25px"}}>or</b>}
                                                    </div>
                                                    {!this.state.calendarData ?
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
                                                    }
                                                </div>
                                            </div>
                                            :
                                            <div className="form-horizontal">
                                                <div className="form-group">
                                                    <label className="col-md-3 control-label">{t("Calendar")}:</label>
                                                    <div className="col-md-7">
                                                        <Combobox id="calendarSelect" name="calendarSelect"
                                                                  data={this.props.calendarNames}
                                                                  onSelect={this.onSelectCalendar}
                                                        />
                                                    </div>
                                                </div>
                                            </div>
                                        }
                                    </div>
                                </div>
                            }
                        </div>
                    </div>
                    {/* TODO: Render reschedule strategy picker */}
                </Form>
                <input type="file" id="ical-data-upload" style={{display: "none"}} onChange={this.onIcalFileAttach}/>
            </InnerPanel>
        );
    }
}

module.exports = {
    MaintenanceSchedulesEdit: MaintenanceSchedulesEdit
};
