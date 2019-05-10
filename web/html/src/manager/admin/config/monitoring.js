// @flow
import { hot } from 'react-hot-loader';
import React, {useState} from 'react';
import {Panel} from 'components/panels/Panel';
import {Button, AsyncButton} from 'components/buttons';
import Network from 'utils/network';
import {Messages, Utils as MessagesUtils} from 'components/messages';
import {Utils} from 'utils/functions';
import {IconTag as Icon} from 'components/icontag';
import withPageWrapper from 'components/general/with-page-wrapper';

import type JsonResult from "../../../utils/network";

const {capitalize} = Utils;

const msgMap = {
  "internal_error": t("An internal error has occured. See the server logs for details."),
  "enabling_failed": t("Enabling monitoring failed. See the server logs for details."),
  "enabling_failed_partially": t("Failed to enable all monitoring services. Some services are still disabled. See the server logs for details."),
  "disabling_failed": t("Disabling monitoring failed. See the server logs for details."),
  "disabling_failed_partially": t("Failed to disable all monitoring services. Some services are still enabled. See the server logs for details."),
  "enabling_succeeded": t("Monitoring enabled successfully."),
  "disabling_succeeded": t("Monitoring disabled successfully."),
  "no_change": t("Monitoring status hasn't changed."),
  "unknown_status": t("An error occured. Monitoring status unknown. Refresh the page.")
};

const exporterMap = {
  "node": t("System"), 
  "tomcat": t("Tomcat (Java JMX)"),
  "taskomatic": t("Taskomatic (Java JMX)"),
  "postgres": t("PostgreSQL database")
}

type ExportersStatusType = {
  [string]: boolean
};

type ExportersResultType = {
  exporters: ExportersStatusType
};

type MonitoringApiType = {
  fetchStatus: () => Promise<ExportersStatusType>,
  changeStatus: (boolean, boolean) => Promise<{newStatus: ?boolean, outcome: String, exporters: ExportersStatusType}>,
};

const MonitoringApi = (setLoading: (boolean) => void) : MonitoringApiType => {

  const fetchStatus = (): Promise<ExportersStatusType> => {
    setLoading(true);
    return Network.get("/rhn/manager/api/admin/config/monitoring").promise
    .then((data: JsonResult<ExportersResultType>) => {
      return data.data.exporters;
    })
    .finally(() => {
      setLoading(false);
    });
  }

  const changeStatus = (isEnabled: boolean, toEnable: boolean): Promise<{newStatus: ?boolean, outcome: String, exporters: ExportersStatusType}> => {
    setLoading(true);
    return Network.post("/rhn/manager/api/admin/config/monitoring", JSON.stringify({"enable": toEnable}), "application/json").promise
    .then((data : JsonResult<ExportersResultType>) => {
        if (data.data.exporters) {
          if (isEnabled && !toEnable) { // enabled -> disabled
            const allDisabled : boolean = Object.keys(data.data.exporters).every(key => data.data.exporters[key] === false);
            const someEnabled : boolean = Object.keys(data.data.exporters).some(key => data.data.exporters[key] === true);
            if (allDisabled) {
              return {newStatus: false, outcome: "success", exporters: data.data.exporters};
            } else if (someEnabled) {
              return {newStatus: false, outcome: "partial", exporters: data.data.exporters};
            } else {
              return {newStatus: false, outcome: "failed", exporters: data.data.exporters};
            }
          } else if (!isEnabled && toEnable) { // disabled -> enabled
            const allEnabled : boolean = Object.keys(data.data.exporters).every(key => data.data.exporters[key] === true);
            const someDisabled : boolean = Object.keys(data.data.exporters).some(key => data.data.exporters[key] === false);
            if (allEnabled) {
              return {newStatus: true, outcome: "success", exporters: data.data.exporters};
            } else if (someDisabled) {
              return {newStatus: true, outcome: "partial", exporters: data.data.exporters};
            } else {
              return {newStatus: true, outcome: "failed", exporters: data.data.exporters};
            }
          } else { // disabled -> disabled, enabled -> enabled
              return {newStatus: null, outcome: "no_change", exporters: data.data.exporters};
          }
        } else {
            return {newStatus: null, outcome: "unknown", exporters: []};
        }
      })
      .finally(() => {
        setLoading(false);
      });
  }

  return {
    fetchStatus,
    changeStatus,
  };
}

type MonitoringContainerStatus = {
  messages: Array<Object>,
  loading: boolean,
  exportersStatus: ?ExportersStatusType
}

class MonitoringContainer extends React.Component<{}, MonitoringContainerStatus> {
  
  constructor(props) {
    super(props);
    this.state = {
      messages: [],
      loading: false,
      exportersStatus: null
    }
  }

  componentDidMount() {
    this.getMonitoringStatus();
  }

  setLoading = (loading: boolean) => {
    this.setState({loading: loading});
  }

  getMonitoringStatus = () => {
    const {fetchStatus} = MonitoringApi(this.setLoading);
    fetchStatus()
    .then((exporters) => {
      this.setState({exportersStatus: exporters});
    })
    .catch(this.handleResponseError);
  }

  handleResponseError = (jqXHR: Object, arg: string = '') => {
    const msg = Network.responseErrorMessage(jqXHR,
      (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null);
    this.setState({messages: msg});
  }

  onChange = () => {
    const {changeStatus} = MonitoringApi(this.setLoading);
    if (!this.state.exportersStatus) {
      return;
    }
    const isEnabled = Object.keys(this.state.exportersStatus).every(key => this.state.exportersStatus[key] === true);
    this.setState({messages: []});
    changeStatus(isEnabled, !isEnabled)
      .then((result) => {
        let msg;
        if (result.newStatus === true) { //enabled
          switch (result.outcome) {
            case "success":
              msg = MessagesUtils.success(msgMap["enabling_succeeded"]);
              break;
            case "partial":
              msg = MessagesUtils.error(msgMap["enabling_failed_partially"]);
              break;
            default:
              msg = MessagesUtils.error(msgMap["enabling_failed"]);  
          }

        } else if (result.newStatus === false) { // disabled
          switch (result.outcome) {
            case "success":
              msg = MessagesUtils.success(msgMap["disabling_succeeded"]);
              break;
            case "partial":
              msg = MessagesUtils.error(msgMap["disabling_failed_partially"]);
              break;
            default:
              msg = MessagesUtils.error(msgMap["disabling_failed"]);  
          }
        } else {
          msg = MessagesUtils.error(msgMap["unknown_status"]);
        }
        this.setState({
          messages: msg,
          exportersStatus: result.exporters
        });
      })
  }  

  render() {
    const isEnabled = this.state.exportersStatus ? 
      Object.keys(this.state.exportersStatus).every(key => this.state.exportersStatus[key] === true) :
      null;

    return (<MonitoringAdmin 
      loading={this.state.loading}
      monitoringEnabled={isEnabled}
      exportersStatus={this.state.exportersStatus}
      messages={this.state.messages}
      onChange={this.onChange}
      />
    );
  }
}

const ExporterIcon = (props : {status: ?boolean}) => {
    let type;
    if (props.status === true) {
      type = "item-enabled";
    } else if (props.status === false){
      type = "item-error";
    } else {
      type = "item-disabled";
    }
    return <Icon type={type} className="fa-1-5x"/>;
}

const ExporterItem = (props: {name: string, status: boolean}) => {
  return <li>
    <ExporterIcon status={props.status}/>
    { props.name in exporterMap ? exporterMap[props.name] : capitalize(props.name)}
    </li>;
}

const ExportersList = (props : {exporters: {[string]: boolean}}) => {
    const keys = Object.keys(props.exporters).sort();

    return <ul style={{listStyle: 'none', paddingLeft: '0px'}}>
      { keys.map(key => <ExporterItem name={key} 
            status={props.exporters[key]}/>)}
      </ul>;
}

const ListPlaceholderItem = (props) => {
  return <li style={{margin: "5px 0px 5px 0px"}}>
    <ExporterIcon status={null}/>
    <div style={{display: 'inline-block', width: 200, height: '1em', backgroundColor: '#dddddd'}}></div>
    </li>;
}

const ListPlaceholder = (props) => {
  return <ul style={{listStyle: 'none', paddingLeft: '0px', color: "#dddddd", filter: "blur(2px)"}}>
    {}
    <ListPlaceholderItem/>
    <ListPlaceholderItem/>
    <ListPlaceholderItem/>
    <ListPlaceholderItem/>   
  </ul>
}

type MonitoringAdminProps = {
  loading: boolean,
  monitoringEnabled: ?boolean,
  exportersStatus: ?{[string]: boolean},
  messages: Array<Object>,
  onChange: () => void
}

class MonitoringAdmin extends React.Component<MonitoringAdminProps> {
  
  render() {
      let buttonLoadingText : string = "";
      if (this.props.loading) {
        if (this.props.monitoringEnabled === null) {
          buttonLoadingText = t("Checking monitoring services...");
        } else if (this.props.monitoringEnabled === true) {
          buttonLoadingText = t("Disabling monitoring services...");
        } else if (this.props.monitoringEnabled === false) {
          buttonLoadingText = t("Enabling monitoring services...");
        }
      }
      return (
      <div className="responsive-wizard">
        <Messages items={this.props.messages}/>
        <div className="spacewalk-toolbar-h1">
          <div className="spacewalk-toolbar"></div>
          <h1>
            <i className="fa fa-info-circle"></i>
            {t("SUSE Manager Configuration - Monitoring")}
            <a href="/rhn/help/reference/en-US/ref.webui.admin.config.jsp#s3-sattools-config-gen" target="_blank">
              <i className="fa fa-question-circle spacewalk-help-link"></i>
            </a>
          </h1>
        </div>
        <div className="page-summary">
          <p>
            {t("Setup your SUSE Manager server monitoring.")}
          </p>
        </div>
        <div className='spacewalk-content-nav'>
          <ul className="nav nav-tabs">
            <li><a href="/rhn/admin/config/GeneralConfig.do?">General</a></li>
            <li><a href="/rhn/admin/config/BootstrapConfig.do?">Bootstrap Script</a></li>
            <li><a href="/rhn/admin/config/Orgs.do?">Organizations</a></li>
            <li><a href="/rhn/admin/config/Restart.do?">Restart</a></li>
            <li><a href="/rhn/admin/config/Cobbler.do?">Cobbler</a></li>
            <li><a href="/rhn/admin/config/BootstrapSystems.do?">Bare-metal systems</a></li>
            <li className="active"><a href="/rhn/manager/admin/config/monitoring?">Monitoring</a></li>
          </ul>
        </div>
        <Panel
          key="schedule"
          title={t('Monitoring')}
          headingLevel="h4"
          footer={
            <div className="row">
              <div className="col-md-offset-3 col-md-9">
                { this.props.loading ? 
                  <Button id="loading-btn" disabled={true} 
                    className="btn-default" icon="fa-circle-o-notch fa-spin" text={buttonLoadingText}/> :
                  <AsyncButton id="monitoring-btn" defaultType="btn-success"
                    text={ this.props.monitoringEnabled ? t("Disable services") : t("Enable services")}
                    action={this.props.onChange}
                  />
                }
              </div>
            </div>
          }>
            <div className="row">
              <div className="col-sm-9">
                <div className="col-md-4 text-left">
                  <label>Monitoring</label>
                </div>
                <div className="col-md-8">
                  { this.props.exportersStatus ? 
                    <ExportersList exporters={this.props.exportersStatus}/> :
                    <ListPlaceholder/>
                   }
                </div>
              </div>
              <div className="col-sm-3 hidden-xs" id="wizard-faq">
              <h4>Server Monitoring</h4>
              <p>
              The server uses <a href="https://prometheus.io" target="_blank" rel="noopener noreferrer">Prometheus</a> exporters to expose metrics about your environment.
              </p>
              <p>
              Refer to the <a href="/docs/index.html" target="_blank">documentation</a> to learn how to to consume these metrics.
              </p>
              </div>
            </div>
        </Panel>  
      </div>);
  }
}

export default hot(module)(withPageWrapper(MonitoringContainer));
