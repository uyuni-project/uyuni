'use strict';

const React = require("react");

var Messages = require("../components/messages").Messages
var Button = require("../components/buttons").Button;
const Network = require("../utils/network");


var GroupFormulas = React.createClass({

    getInitialState: function() {
        var st = {
            "serverData": [],
        };
        Network.get("/rhn/manager/groups/details/formulas/data/" + groupId).promise.then(data => {
          	this.setState({"serverData" : data});
        });
        return st;
    },

	updateFormula: function(e) {
		e.preventDefault(); // block redirect from the form
		var formData = {};
		formData["groupId"] = groupId;
		formData["url"] = window.location.href;
		var select = document.getElementById("chooseFormula");
		formData["selectedFormula"] = select.options[select.selectedIndex].value;

        Network.post("/rhn/manager/groups/details/formulas/apply", JSON.stringify(formData), "application/json").promise.then(
		data => {
                console.log(data);
                window.location.href = data.url;
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
	},
	
	handleChange: function(event) {
		event.target.parent.value = event.target.value;
	},

    render: function() {
		var choose_formula_form = createForm(this.state.serverData);
		var msg = null;

        if(typeof this.props.flashMessages !== "undefined") {
            msg = <Messages items={this.props.flashMessages}/>;
        }
        
        var errs = null;
        if (this.state.errors) {
            errs = <Messages items={this.state.errors.map(function(e) {
                return {severity: "error", text: e};
            })}/>;
        }
        
        return (
        	<div>
				{errs}{msg}
				<div className="panel panel-default">
					<div className="panel-heading">
						<h4>{"Formula"}</h4>
					</div>
					<div className="panel-body">
				    	<form id="chooseFormulaForm" className="form-horizontal" onSubmit={this.updateFormula}>
				        	<div className="form-group">
								<label htmlFor="chooseFormula" className="col-lg-3 control-label">
									Choose formula:
								</label>
								<div className="col-lg-6">
									<select className="form-control" name="chooseFormula" id="chooseFormula" value={this.state.serverData["selected"]} onChange={this.handleChange}>
										{choose_formula_form}
									</select>
								</div>
							</div>
									
				        	<div className="form-group">
				        		<div className="col-lg-offset-3 col-lg-6">
				        			<Button id="save-btn" icon="fa-floppy-o" text="Apply" className="btn btn-success" handler={this.updateFormula} />
				        		</div>
				        	</div>
				    	</form>
				    </div>
				</div>
		    </div>
        );

    }
});

function createForm(server_data) {
	var form = [];
	var formulas = server_data["formulas"];

	form.push(<option value="none">No formula</option>);
	for (var key in formulas) {
		form.push(
			<option value={formulas[key]}>
				{formulas[key]}
			</option>
		);
	}
	return form;
}

React.render(
  <GroupFormulas flashMessages={flashMessage()} />,
  document.getElementById('formulas')
);
