// @flow
import { hot } from 'react-hot-loader';
import React, {useState, useEffect} from 'react';
import {Panel} from 'components/panels/Panel';
import {Button, AsyncButton} from 'components/buttons';
import Network from 'utils/network';
import {Messages, Utils as MessagesUtils} from 'components/messages';
import {Utils} from 'utils/functions';
import {IconTag as Icon} from 'components/icontag';
import withPageWrapper from 'components/general/with-page-wrapper';
import useMonitoringApi from './use-monitoring-api.js';

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

const HelpPanel = (props) => {
  return (
    <div className="col-sm-3 hidden-xs" id="wizard-faq">
      <h4>{t("Server Monitoring")}</h4>
      <p>
        {t("The server uses ")}<a href="https://prometheus.io" target="_blank" rel="noopener noreferrer">{t("Prometheus")}</a>{t(" exporters to expose metrics about your environment.")}
      </p>
      <p>
        {t("Refer to the ")}<a href="/docs/administration/pages/prometheus.html" target="_blank">{t("documentation")}</a>{t(" to learn how to to consume these metrics.")}
      </p>
  </div>);
}

const MonitoringAdmin = (props) => {
  
    const {loading, monitoringEnabled, fetchStatus, changeStatus, exportersStatus, messages, setMessages} = useMonitoringApi();

    const handleResponseError = (jqXHR: Object, arg: string = "") => {
      const msg = Network.responseErrorMessage(jqXHR,
        (status, msg) => msgMap[msg] ? t(msgMap[msg], arg) : null);
      setMessages(msg);
    }

    useEffect(() => {
      fetchStatus()
      .catch(handleResponseError);
    }, []);

    const changeMonitoringState = () => {
      if (exportersStatus === null) {
        return;
      }
      changeStatus(!monitoringEnabled)
        .then((result) => {
          if (result.success) {
            setMessages(MessagesUtils.success(msgMap[result.message]));
          } else {
            setMessages(MessagesUtils.error(msgMap[result.message]));
          }
        })
        .catch(handleResponseError);
    }  

    let buttonLoadingText = "";
    if (loading) {
      if (monitoringEnabled === null) {
        buttonLoadingText = t("Checking services...");
      } else if (monitoringEnabled === true) {
        buttonLoadingText = t("Disabling services...");
      } else if (monitoringEnabled === false) {
        buttonLoadingText = t("Enabling services...");
      }
    }
    return (
    <div className="responsive-wizard">
      <Messages items={messages}/>
      <div className="spacewalk-toolbar-h1">
        <div className="spacewalk-toolbar"></div>
        <h1>
          <i className="fa fa-info-circle"></i>
          {t("SUSE Manager Configuration - Monitoring")}
          <a href="/docs/reference/admin/general.html" target="_blank">
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
          <li><a href="/rhn/admin/config/GeneralConfig.do?">{t("General")}</a></li>
          <li><a href="/rhn/admin/config/BootstrapConfig.do?">{t("Bootstrap Script")}</a></li>
          <li><a href="/rhn/admin/config/Orgs.do?">{t("Organizations")}</a></li>
          <li><a href="/rhn/admin/config/Restart.do?">{t("Restart")}</a></li>
          <li><a href="/rhn/admin/config/Cobbler.do?">{t("Cobbler")}</a></li>
          <li><a href="/rhn/admin/config/BootstrapSystems.do?">{t("Bare-metal systems")}</a></li>
          <li className="active"><a href="/rhn/manager/admin/config/monitoring?">{t("Monitoring")}</a></li>
        </ul>
      </div>
      <Panel
        key="schedule"
        title={t('Monitoring')}
        headingLevel="h4"
        footer={
          <div className="row">
            <div className="col-md-offset-3 col-md-9">
              { loading ? 
                <Button id="loading-btn" disabled={true} 
                  className="btn-default" icon="fa-circle-o-notch fa-spin" text={buttonLoadingText}/> :
                <AsyncButton id="monitoring-btn" defaultType={ monitoringEnabled ? "btn-default" : "btn-success" }
                  icon={ monitoringEnabled ? "fa-pause": "fa-play"}
                  text={ monitoringEnabled ? t("Disable services") : t("Enable services")}
                  action={changeMonitoringState}
                />
              }
            </div>
          </div>
        }>
          <div className="row">
            <div className="col-sm-9">
              <div className="col-md-4 text-left">
                <label>{t("Monitoring")}</label>
              </div>
              <div className="col-md-8">
                { exportersStatus ? 
                  <ExportersList exporters={exportersStatus}/> :
                  <ListPlaceholder/>
                  }
              </div>
            </div>
            <HelpPanel/>
          </div>
      </Panel>  
    </div>);
}

export default hot(module)(withPageWrapper(MonitoringAdmin));
