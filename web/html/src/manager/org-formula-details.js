'use strict';

var React = require("react");
const ReactDOM = require("react-dom");

var Panel = require("../components/panel").Panel;

var FormulaDetail = React.createClass({

    getInitialState: function() {
        if (this.props.formula.errors) {
            return {
                errors: this.props.formula.errors
            };
        }
        return {};
    },

    render: function() {
        return (
        <Panel title={"View Formula: " + this.props.formula.name} icon="spacewalk-icon-salt-add">
            <form className="form-horizontal">
                <div className="form-group">
                    <label className="col-md-3 control-label">Name:</label>
                    <div className="col-md-6">
                        <input className="form-control" type="text" name="name" ref="formulaName"
                            defaultValue={this.props.formula.name} disabled />
                    </div>
                </div>
            </form>
        </Panel>
        )
    }
});

ReactDOM.render(
  <FormulaDetail formula={formulaData()}/>,
  document.getElementById('formula-details')
);

