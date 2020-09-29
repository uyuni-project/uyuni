// @flow
import { hot } from 'react-hot-loader';
import React, {useEffect} from 'react';
import {Panel} from 'components/panels/Panel';
import {HelpLink} from 'components/utils/HelpLink';
import {Button, AsyncButton} from 'components/buttons';
import * as Network from 'utils/network';
import {Messages, Utils as MessagesUtils} from 'components/messages';
import {Utils} from 'utils/functions';
import {IconTag as Icon} from 'components/icontag';
import withPageWrapper from 'components/general/with-page-wrapper';
import useMonitoringApi from './use-monitoring-api.js';
import './monitoring-admin.css';

const {capitalize} = Utils;

const msgRestart = t("Restart is needed for the configuration changes to take effect.");

const msgMap = {
  "internal_error": t("An internal error has occured. See the server logs for details."),
  "enabling_failed": t("Enabling monitoring failed. See the server logs for details."),
  "enabling_failed_partially": t("Failed to enable all monitoring services. Some services are still disabled. See the server logs for details."),
  "disabling_failed": t("Disabling monitoring failed. See the server logs for details."),
  "disabling_failed_partially": t("Failed to disable all monitoring services. Some services are still enabled. See the server logs for details."),
  "enabling_succeeded": t("Monitoring enabled successfully."),
  "disabling_succeeded": t("Monitoring disabled successfully."),
  "tomcat_msg_enable": t("The Tomcat Prometheus exporter is up but the JMX configuration is disabled. " +
    "Click the Enable button to enable the JMX configuration or click Disable to stop the Prometheus exporter."),
  "tomcat_msg_disable":t("The Tomcat Prometheus exporter is down but the JMX configuration is enabled. " +
    "Click the Disable button to disable the JMX configuration or click Enable to start the Prometheus exporter."),
  "taskomatic_msg_enable": t("The Taskomatic Prometheus exporter is up but the JMX configuration is disabled. " +
    "Click the Enable button to enable the JMX configuration of click Disable to stop the Prometheus exporter."),
  "taskomatic_msg_disable": t("The Taskomatic Prometheus exporter is down but the JMX configuration is enabled. " +
    "Click the Disable button to disable the JMX configuration of click Enable to start the Prometheus exporter."),
  "tomcat_msg_restart": msgRestart,
  "taskomatic_msg_restart": msgRestart,
  "self_monitoring_msg_restart": msgRestart,
  "no_change": t("Monitoring status hasn't changed."),
  "unknown_status": t("An error occured. Monitoring status unknown. Refresh the page.")
};

const exporterMap = {
  "node": t("System"), 
  "tomcat": t("Tomcat (Java JMX)"),
  "taskomatic": t("Taskomatic (Java JMX)"),
  "postgres": t("PostgreSQL database"),
  "self_monitoring": t("Server self monitoring")
}

const ExporterIcon = (props : {status: ?boolean, name: string, message: ?string}) => {
    let type;
    let tooltip;
    if (props.status === true) {
      type = props.message === "restart" || props.message === "enable" || props.message === "disable" ? "item-enabled-pending" : "item-enabled";
      if (props.message) { 
        tooltip = t("Enabled") + ". " + msgMap[props.name + "_msg_" + (props.message ? props.message : "")]; // double check props.message to keep flow happy
      } else {
        tooltip = t("Enabled");
      }
    } else if (props.status === false) {
      console.log("disabled " + props.name + " " + (props.message ? props.message : "null"));
      type = props.message === "restart" || props.message === "enable" || props.message === "disable" ? "item-error-pending" : "item-error";
      if (props.message) {
        tooltip = t("Disabled") + ". " + msgMap[props.name + "_msg_" + (props.message ? props.message : "")]; // double check props.message to keep flow happy
      } else {
        tooltip = t("Disabled");
      }
    } else {
      type = "item-disabled";
      tooltip = null;
    }
    return <Icon type={type} className="fa-1-5x" title={ tooltip }/>;
}

const ExporterItem = (props: {name: string, status: boolean, message: ?string}) => {
  return <li key={props.name}>
    <ExporterIcon status={props.status} message={props.message} name={props.name}/>
    { props.name in exporterMap ? exporterMap[props.name] : capitalize(props.name)}
    </li>;
}

const ExportersList = (props : {exporters: {[string]: boolean}, messages: {[string]: string}}) => {
    const keys = Object.keys(props.exporters).sort();

    return <ul style={{listStyle: 'none', paddingLeft: '0px'}}>
      { keys.map(key => <ExporterItem name={key} 
            status={props.exporters[key]} message={props.messages[key]}/>)}
      </ul>;
}

const ListPlaceholderItem = (props) => {
  return <li className="placeholder-item">
    <Icon type="item-disabled" className="fa-1-5x"/>
    <div/>
    </li>;
}

const ListPlaceholder = (props) => {
  return <ul className="placeholder">
    { Object.keys(exporterMap).map(e => <ListPlaceholderItem/>) }
  </ul>
}

const HelpPanel = (props) => {
  return (
    <div className="col-sm-3 hidden-xs" id="wizard-faq">
      <h4>{t("Server Monitoring")}</h4>
      <p>
        {t("The server uses ")}<a href="https://prometheus.io" target="_blank" rel="noopener noreferrer">{t("Prometheus")}</a>{t(" exporters to expose metrics about your environment.")}
      </p>
      <p>
        {t("Refer to the ")}<a href="/docs/suse-manager/administration/monitoring.html" target="_blank">{t("documentation")}</a>{t(" to learn how to to consume these metrics.")}
      </p>
  </div>);
}

const ExportersMessages = (props: {messages: {[string]: string}}) => {
    if (props.messages) {
      const keys = Object.keys(props.messages).sort();

      return (<ul style={{listStyle: 'none', paddingLeft: '0px'}}>
        { keys.filter(key => props.messages[key] !== "restart").map(key => 
          <li key={key}>
            <Icon type="system-warn" className="fa-1-5x"/>{ msgMap[key + "_msg_" + props.messages[key]] }
          </li>)  
        }
        </ul>);
    } else {
      return null;
    }
}

const MonitoringAdmin = (props) => {
  
    const {action, fetchStatus, changeStatus, exportersStatus, exportersMessages, restartNeeded, messages, setMessages} = useMonitoringApi();

    const handleResponseError = (jqXHR: Object, arg: string = "") => {
      const msg = Network.responseErrorMessage(jqXHR,
        (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null);
      setMessages(msg);
    }

    useEffect(() => {
      fetchStatus()
      .catch(handleResponseError);
    }, []);

    const changeMonitoringStatus = (enable: boolean) => {
      if (exportersStatus === null) {
        return;
      }
      changeStatus(enable)
        .then((result: any) => {
          if (result.success) {
            setMessages(MessagesUtils.success(msgMap[result.message]));
          } else {
            setMessages(MessagesUtils.error(result.message in msgMap ? msgMap[result.message] : result.message));
          }
        })
        .catch(handleResponseError);
    }  

    let buttons;
    if (action) {
      switch(action) {
        case "checking":
          buttons = 
                <React.Fragment>
                  <Button id="enable-monitoring-btn" disabled={true}
                    className="btn-default gap-right" icon="fa-play" text={ t("Enable") }/> 
                  <Button id="disable-monitoring-btn" disabled={true} 
                    className="btn-default" icon="fa-stop" text={ t("Disable") }/>               
                </React.Fragment>;          
          break;
        case "enabling":
          buttons = 
                <React.Fragment>
                  <Button id="enable-monitoring-btn" disabled={true} 
                    className="btn-default gap-right" icon="fa-circle-o-notch fa-spin" text={ t("Enable") }/> 
                  <Button id="disable-monitoring-btn" disabled={true} 
                    className="btn-default" icon="fa-pause" text={ t("Disable") }/>
                </React.Fragment>;
          break;
        case "disabling":
          buttons = 
                <React.Fragment>
                  <Button id="enable-monitoring-btn" disabled={true} 
                    className="btn-default gap-right" icon="fa-play" text={ t("Enable") }/> 
                  <Button id="disable-monitoring-btn" disabled={true} 
                    className="btn-default" icon="fa-circle-o-notch fa-spin" text={ t("Disable") }/>
                </React.Fragment>;
          break;
        default:
          buttons = null
      }
    } else {
      buttons = 
                <React.Fragment>
                  <AsyncButton id="enable-monitoring-btn" defaultType="btn-success"
                    icon="fa-play"
                    text={ t("Enable") }
                    className="gap-right"
                    action={() => changeMonitoringStatus(true) }/>
                  <AsyncButton id="disable-monitoring-btn" defaultType="btn-danger"
                    icon="fa-stop"
                    text={ t("Disable") }
                    action={() => changeMonitoringStatus(false) } 
                  />                                       
                </React.Fragment>
    }
    return (
    <div className="responsive-wizard">
      {messages && <Messages items={messages}/>}
      <div className="spacewalk-toolbar-h1">
        <div className="spacewalk-toolbar"></div>
        <h1>
          <i className="fa fa-info-circle"></i>
          {t("SUSE Manager Configuration - Monitoring")}
          <HelpLink url="/docs/suse-manager/administration/monitoring.html"/>
        </h1>
      </div>
      <div className="page-summary">
        <p>
          {t("Setup your SUSE Manager server monitoring.")}
        </p>
      </div>
      <div className='spacewalk-content-nav'>
        <ul className="nav nav-tabs">
          <li><a className="js-spa" href="/rhn/admin/config/GeneralConfig.do?">{t("General")}</a></li>
          <li><a className="js-spa" href="/rhn/admin/config/BootstrapConfig.do?">{t("Bootstrap Script")}</a></li>
          <li><a className="js-spa" href="/rhn/admin/config/Orgs.do?">{t("Organizations")}</a></li>
          <li><a className="js-spa" href="/rhn/admin/config/Restart.do?">{t("Restart")}</a></li>
          <li><a className="js-spa" href="/rhn/admin/config/Cobbler.do?">{t("Cobbler")}</a></li>
          <li><a className="js-spa" href="/rhn/admin/config/BootstrapSystems.do?">{t("Bare-metal systems")}</a></li>
          <li className="active js-spa"><a href="/rhn/manager/admin/config/monitoring?">{t("Monitoring")}</a></li>
        </ul>
      </div>
      <Panel
        key="schedule"
        title={t('Monitoring')}
        headingLevel="h4"
        footer={
          <div className="row">
            <div className="col-md-offset-3 col-md-9">
              { buttons }
            </div>
          </div>
        }>
          <div className="row" style={{"display": "flex"}}>
            <div className="col-sm-9">
              <div className="col-md-4 text-left">
                <label>{t("Monitoring")}</label>
              </div>
              <div className="col-md-8">
                { exportersStatus ? 
                  <ExportersList exporters={exportersStatus} messages={exportersMessages}/> :
                  <ListPlaceholder/>
                }
                {
                  restartNeeded ? 
                    <div><Icon type="system-reboot" className="text-warning fa-1-5x"/>
                      <a href="/rhn/admin/config/Restart.do?">{ t("Restarting") }</a>
                      { t(" Tomcat and Taskomatic is needed for the configuration changes to take effect.") }
                    </div> :
                    null
                }
                <ExportersMessages messages={exportersMessages}/>
              </div>
            </div>
            <HelpPanel/>
          </div>
      </Panel>  
    </div>);
}

export default hot(module)(withPageWrapper(MonitoringAdmin));
