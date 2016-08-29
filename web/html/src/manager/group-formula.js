'use strict';

var React = require("react");
const ReactDOM = require("react-dom");

var Network = require("../utils/network");
var FormulaFormModule = require("../components/FormulaForm");
var FormulaForm = FormulaFormModule.FormulaForm;
var toTitle = FormulaFormModule.toTitle;

function updateFormula(component) {
    var formData = {};
    formData.type = "GROUP";
    formData.id = groupId;
    formData.formula_name = component.state.serverData.formula_name;
    formData.content = component.serializeValues();
    
    Network.post("/rhn/manager/api/formulas/save", JSON.stringify(formData), "application/json").promise.then(
    (data) => {
        component.setState({
            messages: [t("Formula saved!")]
        });
    },
    (xhr) => {
       if (xhr.status == 400 || xhr.status == 403) {
           // validation err or permission denied
           var errs = JSON.parse(xhr.responseText);
           component.setState({errors: errs});
       } else {
           component.setState({errors: [t("An internal server error occurred")]});
       }
    });
}

function addFormulaNavBar(formulaList, activeId) {
    $("#formula-nav-bar").remove();
    
    var navBar = "<ul class='nav nav-tabs nav-tabs-pf' id='formula-nav-bar'>\n"
    navBar += "<li><a href='/rhn/manager/groups/details/formulas?sgid=" + groupId + "'>Formulas</a></li>\n";
    for (var i in formulaList)
        navBar += "<li" + (i == activeId ? " class='active'>" : ">") + "<a href='/rhn/manager/groups/details/formula/" + i + "?sgid=" + groupId + "'>" + toTitle(formulaList[i]) + "</a></li>\n";
    navBar += "</ul>"
    $(".spacewalk-content-nav").append(navBar);
}

const noFormulaText = (
    <div>
        Click <a href={"/rhn/manager/groups/details/formulas?sgid=" + groupId}>here</a> to manage the formulas of this server group.
    </div>
);
ReactDOM.render(
    <FormulaForm
          dataUrl={"/rhn/manager/api/formulas/form/GROUP/" + groupId + "/" + formulaId}
          noFormulaText={noFormulaText}
          addFormulaNavBar={addFormulaNavBar}
          formulaId={formulaId}
          getFormulaUrl={function(id) {return "/rhn/manager/groups/details/formula/" + id + "?sgid=" + groupId;}}
          updateFormula={updateFormula}
          currentScope="group" />,
    document.getElementById('formula')
);

