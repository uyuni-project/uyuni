/* eslint-disable */
'use strict';

var React = require("react");
const ReactDOM = require("react-dom");

var Network = require("../utils/network");
var FormulaFormModule = require("../components/FormulaForm");
var FormulaForm = FormulaFormModule.FormulaForm;
const capitalize = require("../utils/functions").Utils.capitalize;

const msgMap = {
  "formula_saved" : <p>{t("Formula saved. Apply the ")}<a href={'/rhn/manager/systems/details/highstate?sid=' + serverId}>{t("Highstate")}</a>{t(" for the changes to take effect.")}</p>,
  "error_invalid_target" : t("Invalid target type.")
}

function addFormulaNavBar(formulaList, activeId) {
    $("#formula-nav-bar").remove();

    var navBar = "<ul class='nav nav-tabs nav-tabs-pf' id='formula-nav-bar'>\n"
    navBar += "<li><a href='/rhn/manager/systems/details/formulas?sid=" + serverId + "'>Formulas</a></li>\n";
    for (var i in formulaList)
        navBar += "<li" + (i == activeId ? " class='active'>" : ">") + "<a href='/rhn/manager/systems/details/formula/" + i + "?sid=" + serverId + "'>" + capitalize(formulaList[i]) + "</a></li>\n";
    navBar += "</ul>"
    $(".spacewalk-content-nav").append(navBar);
}

ReactDOM.render(
    <FormulaForm
          dataUrl={"/rhn/manager/api/formulas/form/SERVER/" + serverId + "/" + formulaId}
          saveUrl="/rhn/manager/api/formulas/save"
          addFormulaNavBar={addFormulaNavBar}
          formulaId={formulaId}
          systemId={serverId}
          getFormulaUrl={function(id) {return "/rhn/manager/systems/details/formula/" + id + "?sid=" + serverId;}}
          scope="system"
          messageTexts={msgMap} />,
    document.getElementById('formula')
);

