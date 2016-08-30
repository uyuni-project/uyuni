'use strict';

var React = require("react");
const ReactDOM = require("react-dom");

var Network = require("../utils/network");
var FormulaFormModule = require("../components/FormulaForm");
var FormulaForm = FormulaFormModule.FormulaForm;
var toTitle = FormulaFormModule.toTitle;


function saveFormula(component) {
    var formData = {};
    formData.type = "SERVER";
    formData.id = serverId;
    formData.formula_name = component.state.serverData.formula_name;
    formData.content = component.serializeValues();

    Network.post(
        "/rhn/manager/api/formulas/save",
        JSON.stringify(formData),
        "application/json"
    ).promise.then(
    (data) => {
        component.setState({
            messages: data
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

function addFormulaNavBar(formulaList, activeId) {
    $("#formula-nav-bar").remove();

    var navBar = "<ul class='nav nav-tabs nav-tabs-pf' id='formula-nav-bar'>\n"
    for (var i in formulaList)
        navBar += "<li" + (i == activeId ? " class='active'>" : ">") + "<a href='/rhn/manager/systems/details/formula/" + i + "?sid=" + serverId + "'>" + toTitle(formulaList[i]) + "</a></li>\n";
    navBar += "</ul>"
    $(".spacewalk-content-nav").append(navBar);
}

ReactDOM.render(
    <FormulaForm
          dataUrl={"/rhn/manager/api/formulas/form/SERVER/" + serverId + "/" + formulaId}
          noFormulaText="No formulas found for this server! Add a formula to a group this server is part of."
          addFormulaNavBar={addFormulaNavBar}
          formulaId={formulaId}
          getFormulaUrl={function(id) {return "/rhn/manager/systems/details/formula/" + id + "?sid=" + serverId;}}
          saveFormula={saveFormula}
          currentScope="system" />,
    document.getElementById('formula')
);

