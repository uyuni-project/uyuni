// @flow
import { hot } from 'react-hot-loader';
import React from 'react';
import {Panel} from 'components/panels/Panel';
import {Button, AsyncButton} from 'components/buttons';
import Network from 'utils/network';
import {Messages, Utils as MessagesUtils} from 'components/messages';
import {Utils} from 'utils/functions';
import {IconTag as Icon} from 'components/icontag';

import type JsonResult from "../../../utils/network";

const {capitalize} = Utils;

type Props = {

}

type ExportersStatus = {
  exporters: {[string]: boolean}
}

type State = {
  loading: boolean,
  exportersStatus: {[string]: ?boolean},
  monitoringEnabled: boolean,
  messages: Array<Object>,
  error: boolean,
  loadingButtonText: string
}

const msgMap = {
  "internal_error": t("An internal error has occured. See the server logs for details."),
  "enabling_failed": t("Enabling monitoring failed. See the server logs for details."),
  "enabling_failed_partially": t("Failed to enable all monitoring services. Some services are still disabled. See the server logs for details."),
  "disabling_failed": t("Disabling monitoring failed. See the server logs for details."),
  "disabling_failed_partially": t("Failed to disable all monitoring services. Some services are still enabled. See the server logs for details."),
  "enabling_succeeded": t("Monitoring enabled successfully."),
  "disabling_succeeded": t("Monitoring disabled successfully."),
  "no_change": t("Monitoring status hasn't changed."),
  "unknown_status": t("Monitoring status unknown.")
};

const exporterMap = {
  "node": t("System"), 
  "tomcat": t("Tomcat (Java JMX)"),
  "taskomatic": t("Taskomatic (Java JMX)"),
  "postgres": t("PostgreSQL database")
}

function nullify(exporters: {[string]: ?boolean}) : {[string]: ?boolean} {
  Object.keys(exporters).forEach(key => {
    exporters[key] = null;
  })
  return exporters;
}

class MonitoringAdmin extends React.Component<Props, State> {
  
  constructor(props: Props) {
    super(props);
    this.state = {
      messages: [],
      loading: false,
      exportersStatus: {
        node: null,
        tomcat: null,
        taskomatic: null,
        postgres: null
      },
      monitoringEnabled: false,
      error: false,
      loadingButtonText: t("Checking monitoring services...")
    };
  }

  componentDidMount() {
    this.getMonitoringStatus();
  }

  getMonitoringStatus = () => {
    this.setState({loading: true, loadingButtonText : t("Checking monitoring services...")});

    Network.get("/rhn/manager/api/admin/config/monitoring").promise.then((data : JsonResult<ExportersStatus>) => {
        const monitoringEnabled = Object.keys(data.data.exporters).every(key => data.data.exporters[key] === true);
        this.setState({exportersStatus: data.data.exporters, monitoringEnabled: monitoringEnabled, loading: false, error: false});
    })
    .catch(this.handleResponseError);
  }

  onChange = () => {
    if (this.state.exportersStatus) {
      let toChange = !this.state.monitoringEnabled;
      this.setState({messages: [], loading: true, loadingButtonText: toChange ? t("Enabling monitoring services...") : t("Disable monitoring services...")});
      Network.post("/rhn/manager/api/admin/config/monitoring", JSON.stringify({"enable": toChange}), "application/json").promise
      .then((data : JsonResult<ExportersStatus>) => {
        let msg;
        if (data.data.exporters) {
          let newStatus : boolean = this.state.monitoringEnabled;
          if (this.state.monitoringEnabled && !toChange) { // enabled -> disabled
            const allSuccess : boolean = Object.keys(data.data.exporters).every(key => data.data.exporters[key] === true);
            const someFailed : boolean = Object.keys(data.data.exporters).some(key => data.data.exporters[key] === false);
            if (allSuccess) {
              newStatus = false;              
              msg = MessagesUtils.success(msgMap["disabling_succeeded"]);
            } else if (someFailed) {
              msg = MessagesUtils.error(msgMap["disabling_failed_partially"]);
            } else {
              msg = MessagesUtils.error(msgMap["disabling_failed"]);
            }
          } else if (!this.state.monitoringEnabled && toChange) { // disabled -> enabled
            const allSuccess : boolean = Object.keys(data.data.exporters).every(key => data.data.exporters[key] === true);
            const someFailed : boolean = Object.keys(data.data.exporters).some(key => data.data.exporters[key] === false);
            if (allSuccess) {
              newStatus = true;
              msg = MessagesUtils.success(msgMap["enabling_succeeded"]);
            } else if (someFailed) {
              msg = MessagesUtils.error(msgMap["enabling_failed_partially"]);
            } else {
              msg = MessagesUtils.error(msgMap["enabling_failed"]);
            }
          } else { // disabled -> disabled, enabled -> enabled
            msg = MessagesUtils.info(msgMap["no_change"]);
          }
          this.setState({
            messages: msg,
            exportersStatus: nullify(this.state.exportersStatus),
            monitoringEnabled: newStatus,
            loading: false,
            error: false
          });

        } else {
          this.setState({
            messages: MessagesUtils.error(msgMap["unknown_status"]),
            exportersStatus: nullify(this.state.exportersStatus),
            loading: false,
            error: false
          });          
        }
        this.getMonitoringStatus();
      })
      .catch(this.handleResponseError);      
    }
  }

  handleResponseError = (jqXHR: Object, arg: string = '') => {
    const msg = Network.responseErrorMessage(jqXHR,
      (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null);
    this.setState({messages: msg, loading: false, error: true});
  }

  iconType = (value) => {
    if (value === true) {
      return "item-enabled";
    } else if (value === false){
      return "item-error";
    } else {
      return "item-disabled";
    }
  }
  
  render() {
      const keys = Object.keys(this.state.exportersStatus).sort();

      const exporters = <ul style={{listStyle: 'none', paddingLeft: '0px'}}>{
        keys.map(key => 
          this.state.exportersStatus ? 
          (<li>
            <Icon type={this.iconType(this.state.exportersStatus[key])} className="fa-1-5x"/>
            { key in exporterMap ? exporterMap[key] : capitalize(key)}
            </li>)
          : null
          )
      }
      </ul>

      return (
      <div className="responsive-wizard">
        <Messages items={this.state.messages}/>
        <div className="spacewalk-toolbar-h1">
          <div className="spacewalk-toolbar"></div>
          <h1>
            <i className="fa fa-info-circle"></i>
            SUSE Manager Configuration - Monitoring
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
                { this.state.loading ? 
                  <Button id="loading-btn" disabled={true} 
                    className="btn-default" icon="fa-circle-o-notch fa-spin" text={this.state.loadingButtonText}/> :
                  <AsyncButton id="monitoring-btn" defaultType="btn-success"
                    text={ this.state.monitoringEnabled ? t("Disable monitoring services") : t("Enable monitoring services")}
                    action={this.onChange} initialValue={this.state.error ? "failure" : null }
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
                  { exporters }
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

export default hot(module)(MonitoringAdmin);
