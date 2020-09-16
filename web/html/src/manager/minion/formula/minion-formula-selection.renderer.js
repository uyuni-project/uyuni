/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");

const Network = require("utils/network");
const FormulaSelection = require("components/formula-selection").FormulaSelection;
const capitalize = require("utils/functions").Utils.capitalize;
const SpaRenderer  = require("core/spa/spa-renderer").default;

export const renderer = (renderId, {serverId, warningMessage}) => {


  const messageTexts = {
    "formulas_saved" : <p>{t("Formula saved. Edit configuration options " +
      "in the enabled formulas and apply the ")}<a
      href={'/rhn/manager/systems/details/highstate?sid=' +
        serverId}>{t("Highstate")}</a>{t(" for the changes to take effect.")}
    </p>,
    "error_invalid_target" : t("Invalid target type.")
  }

  function getMessageText(msg) {
    return messageTexts[msg] ? t(messageTexts[msg]) : msg;
  }

  function saveRequest(component, selectedFormulas) {
    const formData = {};
    formData.type = "SERVER";
    formData.id = serverId;
    formData.selected = selectedFormulas;

    return Network.post(
      "/rhn/manager/api/formulas/select",
      JSON.stringify(formData),
      "application/json"
    ).promise.then(data => {
        component.setState({
          messages: data.map(msg => getMessageText(msg))
        });
      },
      (xhr) => {
        try {
          component.setState({
            errors: [JSON.parse(xhr.responseText)]
          })
        } catch (err) {
          component.setState({
            errors: [Network.errorMessageByStatus(xhr.status)]
          })
        }
      });
  }

  function addFormulaNavBar(formulaList) {
    jQuery("#formula-nav-bar").remove();

    var navBar = "<ul class='nav nav-tabs nav-tabs-pf' id='formula-nav-bar'>\n"
    navBar += "<li class='active'><a href='/rhn/manager/systems/details/formulas?sid=" + serverId + "'><i class='fa fa-pencil-square-o'></i>" + t("Configuration") + "</a></li>\n";
    for (var i in formulaList)
      navBar += "<li><a href='/rhn/manager/systems/details/formula/" + i + "?sid=" + serverId + "'>" + capitalize(formulaList[i]) + "</a></li>\n";
    navBar += "</ul>"
    jQuery(".spacewalk-content-nav").append(navBar);
  }

  SpaRenderer.renderNavigationReact(
    <FormulaSelection
      dataUrl={"/rhn/manager/api/formulas/list/SERVER/" + serverId}
      saveRequest={saveRequest}
      systemId={serverId}
      addFormulaNavBar={addFormulaNavBar}
      warningMessage={warningMessage}
    />,
    document.getElementById(renderId)
  );
};
