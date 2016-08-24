'use strict';

var React = require("react");
const ReactDOM = require("react-dom");

var MessagesUtils = require("../components/messages").Utils;
var Network = require("../utils/network");
var FormulaFormModule = require("../components/FormulaForm");
var FormulaForm = FormulaFormModule.FormulaForm;
var toTitle = FormulaFormModule.toTitle;


function updateFormula(component) {
	var formData = {};
	formData.serverId = serverId;
	formData.formula_name = component.state.serverData.formula_name;
	formData.content = component.serializeValues();
	
    Network.post("/rhn/manager/systems/details/formula/form/save", JSON.stringify(formData), "application/json").promise.then(
	(data) => {
		component.setState({
			messages: MessagesUtils.info(t("Formula saved!"))
		});
	},
    (xhr) => {
       if (xhr.status == 400) {
           // validation err
           var errs = JSON.parse(xhr.responseText);
           this.setState({errors: errs});
       } else {
           this.setState({errors: [t("An internal server error occurred")]});
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
	  	dataUrl={"/rhn/manager/systems/details/formula/form/" + serverId + "/" + formulaId}
	  	noFormulaText="No formulas found for this server! Add a formula to a group this server is part of."
	  	addFormulaNavBar={addFormulaNavBar}
	  	formulaId={formulaId}
	  	getFormulaUrl={function(id) {return "/rhn/manager/systems/details/formula/" + id + "?sid=" + serverId;}}
	  	updateFormula={updateFormula}
	  	currentScope="system"
	  	flashMessages={flashMessage()} />,
	document.getElementById('formula')
);

