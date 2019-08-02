/* eslint-disable */
'use strict';

const React = require("react");
const ReactDOM = require("react-dom");

const Network = require("../../../utils/network");
const FormulaSelection = require("../../../components/formula-selection").FormulaSelection;
const capitalize = require("../../../utils/functions").Utils.capitalize;

const messageTexts = {
  "formulas_saved" : <p>{t("Formula saved. Apply the ")}<a href={'/rhn/manager/groups/details/highstate?sgid=' + groupId}>{t("Highstate")}</a>{t(" for the changes to take effect.")}</p>,
  "error_invalid_target" : t("Invalid target type.")
}

function getMessageText(msg) {
  return messageTexts[msg] ? t(messageTexts[msg]) : msg;
}

function saveRequest(component, selectedFormulas) {
    const formData = {};
    formData.type = "GROUP";
    formData.id = groupId;
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
    $("#formula-nav-bar").remove();

    var navBar = "<ul class='nav nav-tabs nav-tabs-pf' id='formula-nav-bar'>\n"
    navBar += "<li class='active'><a href='/rhn/manager/groups/details/formulas?sgid=" + groupId + "'>" + t("Formulas") + "</a></li>\n";
    for (var i in formulaList)
        navBar += "<li><a href='/rhn/manager/groups/details/formula/" + i + "?sgid=" + groupId + "'>" + capitalize(formulaList[i]) + "</a></li>\n";
    navBar += "</ul>"
    $(".spacewalk-content-nav").append(navBar);
}

ReactDOM.render(
  <FormulaSelection
    dataUrl={"/rhn/manager/api/formulas/list/GROUP/" + groupId}
    saveRequest={saveRequest}
    systemGroupId={groupId}
    addFormulaNavBar={addFormulaNavBar} />,
  document.getElementById('formulas')
);
