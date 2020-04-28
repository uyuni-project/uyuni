/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");
const AsyncButton = require("components/buttons").AsyncButton;
const Button = require("components/buttons").Button;
const {Combobox} = require("components/combobox");
const { Form } = require('components/input/Form');
const { InnerPanel } = require("components/panels/InnerPanel");
const { Text } = require('components/input/Text');
const { Radio } = require('components/input/Radio');

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
            type: "single",
            hover: false,
            icalData: undefined,
            icalFileName: "",
            view: "existing"
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
            scheduleName: this.state.scheduleName,
            calendarName: this.state.calendarName,
            type: this.state.type,
            view: this.state.view,
            icalData: this.state.icalData,
            url: this.state.icalFileName
        });
    };

    onFormChanged = (model) => {
        this.setState({
            scheduleName: model.scheduleName,
            calendarName: model.calendarName || this.state.calendarName,
            type: model.type || this.state.type
        });
    };

    onFileDestChanged = (event) => {
        this.setState({
            icalFileName: event.target.value
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
            icalFileName: event.target.files[0].name
        });
    };

    onIcalFileRemove = () => {
        this.setState({
            icalData: undefined,
            icalFileName: ""
        });
    };

    handleFileAttach = () => {
        this.state.icalFileName === "" ?
            document.getElementById("ical-data-upload").click()
            : this.getDataFromURL(this.state.icalFileName);
    }

    getDataFromURL = (url) => {
        fetch(url).then(response => {
            response.text().then(data => {
                this.setState({
                    icalData: data
                })
            });
        });
    };

    icalFileLoaded = (fileString) => {
        this.setState({
            icalData: fileString,
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
                <Form onChange={this.onFormChanged}
                      model={{scheduleName: this.state.scheduleName, calendarName: this.state.calendarName, type: this.state.type}}>
                    <Text name="scheduleName" required type="text" label={t("Schedule Name")} labelClass="col-sm-3" divClass="col-sm-6" />
                    <div className="panel panel-default">
                        <div className="panel-heading">
                            <h3>{t("Select calendar")}</h3>
                        </div>
                        <div className="panel-body">
                            <div className="spacewalk-content-nav" id="config-channels-tabs">
                                <ul className="nav nav-tabs">
                                    <li className={this.state.view === 'existing' || this.state.view === '' ? 'active' : ''}>
                                        <a onClick={() => this.setView('existing')} style={hover} onMouseEnter={this.onToggleHover} onMouseLeave={this.onToggleHover}>{t("Select existing")}</a>
                                    </li>
                                    <li className={this.state.view === 'new' ? 'active' : ''}>
                                        <a onClick={() => this.setView('new')} style={hover} onMouseEnter={this.onToggleHover} onMouseLeave={this.onToggleHover}>{t("Create new")}</a>
                                    </li>
                                </ul>
                            </div>
                            {this.state.view === "new" ?
                                <div className="form-horizontal">
                                    <Text name="calendarName" required type="text" label={t("Calendar Name")} labelClass="col-md-3" divClass="col-md-6" />
                                    <div className="form-group">
                                        <label className="col-md-3 control-label">{t("Calendar data")}:</label>
                                        <div className="col-md-4">
                                            <input type="text" className="form-control" value={this.state.icalFileName} disabled={this.state.icalData} onChange={this.onFileDestChanged} />
                                        </div>
                                        <div className="col-md-1">
                                            <Button id="ical-upload-btn" className="btn-default" text={t("Attach file")}
                                                    handler={this.handleFileAttach}
                                            />
                                        </div>
                                        <div className="col-md-1">
                                            <Button id="ical-rm-btn" className="btn-default" text={t("Remove file")}
                                                    handler={this.onIcalFileRemove}/>
                                        </div>
                                    </div>
                                </div>
                                :
                                <div className="form-horizontal">
                                    <div className="form-group">
                                        <label className="col-md-3 control-label">{t("Calendar")}:</label>
                                        <div className="col-md-6">
                                            <Combobox id="calendarSelect" name="calendarSelect"
                                                      data={this.state.calendar}
                                                      onSelect={this.onSelectCalendar}
                                            />
                                        </div>
                                    </div>
                                    <Radio name="type" inline={true} label={t('Type')} labelClass="col-md-3" divClass="col-md-6"
                                           items={[
                                               {label: <b>{t('Single')}</b>, value: 'single'},
                                               {label: <b>{t('Multi')}</b>, value: 'multi'},
                                           ]}
                                    />
                                </div>
                            }
                        </div>
                    </div>
                </Form>
                <input type="file" id="ical-data-upload" style={{display: "none"}} onChange={this.onIcalFileAttach}/>
            </InnerPanel>
        );
    }
}

module.exports = {
    MaintenanceSchedulesEdit: MaintenanceSchedulesEdit
};
