'use strict';

const React = require("react");

const Messages = require("../components/messages").Messages;
const Network = require("../utils/network");
const Buttons = require("../components/buttons")

const Button = Buttons.Button;
const AsyncButton = Buttons.AsyncButton;

const toTitle = require("../components/FormulaForm").toTitle;

class FormulaSelection extends React.Component {
    constructor(props) {
        super(props);

        ["init", "applyRequest", "saveRequest", "resetChanges", "removeAllFormulas", "getGroupItemState",
        "getListIcon", "getListStyle", "generateList", "onGroupItemClick", "onListItemClick"]
        .forEach(method => this[method] = this[method].bind(this));

        this.state = {
            formulas: {},
            groups: {groupless: []},
            showDescription: false,
            messages: []
        };
        this.init();
    }

    init() {
        Network.get(this.props.dataUrl).promise.then(data => {
            const groupDict = {groupless: []};
            const formulaDict = {};
            data.formulas.forEach(function(e) {
                e.selected = (data.selected.indexOf(e.name) >= 0);
                const group = e.group || "groupless";
                if (groupDict[group] == undefined)
                    groupDict[group] = [];
                groupDict[group].push(e);
                formulaDict[e.name] = e;
            });
            this.setState({
                activeFormulas: data.selected,
                formulas: formulaDict,
                groups: groupDict
            });
        });
    }

    applyRequest() {
        var unsavedChanges = false;
        for (var name in this.state.formulas) {
            if ((this.state.activeFormulas.indexOf(name) >= 0) != this.state.formulas[name].selected) {
                unsavedChanges = true;
                break;
            }
        }
        if (unsavedChanges) {
            const response = confirm(t("There are unsaved changes. Do you want to proceed ?"))
            if (response == false) {
                return null;
            }
        }
        return this.props.applyRequest(this)
            .then(data => {
                window.scrollTo(0, 0);
            });
    }

    saveRequest() {
        var selectedFormulas = [];
        jQuery.each(this.state.formulas, function(name, formula) {
            if (formula.selected)
                selectedFormulas.push(name);
        });
        this.state.activeFormulas = selectedFormulas;
        return this.props.saveRequest(this, selectedFormulas)
            .then(data => {
                this.init();
                window.scrollTo(0, 0);
            });
    }

    resetChanges() {
        const activeFormulas = this.state.activeFormulas;
        jQuery.each(this.state.formulas, function(name, formula) {
            formula.selected = (activeFormulas.indexOf(name) >= 0);
        });
        this.forceUpdate();
    }

    removeAllFormulas() {
        jQuery.each(this.state.formulas, function(name, formula) {
            formula.selected = false;
        });
        this.forceUpdate();
    }

    getGroupItemState(group) {
        var selectedCount = 0;
        group.forEach(function (formula) {
            if (formula.selected)
                selectedCount++;
        });
        if (selectedCount == 0)
            return 0;
        else if (selectedCount == group.length)
            return 1;
        else
            return 2;
    }

    getListIcon(state) {
        if (!state)
            return "fa fa-lg fa-square-o";
        else if (state == 1)
            return "fa fa-lg fa-check-square-o";
        else
            return "fa fa-lg fa-minus-square-o";
    }

    getListStyle(state) {
        if (state)
            return "list-group-item list-group-item-info";
        else
            return "list-group-item";
    }

    getDescription(formula) {
        if (this.state.showDescription == formula.name)
            return <p className="list-group-item-text formula-description" >{formula.description}</p>;
        else
            return null;
    }
    
    generateList() {
        var list = [];
        const groups = this.state.groups;

        if (groups.groupless.length > 0) {
            list.push(
                <a href="#" onClick={(e) => e.preventDefault()} key={"groupless"} className="list-group-item disabled">
                    <strong>
                        <i className="fa fa-lg fa-square-o" />
                        {t(" No group")}
                    </strong>
                </a>
            );
            groups.groupless.forEach(function (formula) {
                list.push(
                    <a href="#" onClick={this.onListItemClick} id={formula.name} key={formula.name} title={formula.description} className={this.getListStyle(formula.selected)}>
                        <i className={this.getListIcon(formula.selected)} />
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        {toTitle(formula.name)}
                        { formula.description ? (<i id={"info_button_" + formula.name} className="fa fa-lg fa-info-circle pull-right" />) : null }
                        {this.getDescription(formula)}
                    </a>
                );
            }, this);
        }
        for (var group_name in groups) {
            if (group_name == "groupless") continue;
            const group = groups[group_name];
            const group_state = this.getGroupItemState(group);
            list.push(
                <a href="#" onClick={this.onGroupItemClick} id={"group_" + group_name} key={"group_" + group_name} className={this.getListStyle(group_state)}>
                    <strong>
                        <i className={this.getListIcon(group_state)} />
                        {" " + toTitle(group_name)}
                    </strong>
                </a>
            );
            group.forEach(function (formula) {
                list.push(
                    <a href="#" onClick={this.onListItemClick} id={formula.name} key={formula.name} title={formula.description} className={this.getListStyle(formula.selected)}>
                        <i className={this.getListIcon(formula.selected)} />
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        {toTitle(formula.name)}
                        { formula.description ? (<i id={"info_button_" + formula.name} className="fa fa-lg fa-info-circle pull-right" />) : null }
                        {this.getDescription(formula)}
                    </a>
                );
            }, this);
        }
        return list;
    }

    onListItemClick(e) {
        e.preventDefault();
        const formula = this.state.formulas[(e.target.href == undefined ? e.target.parentElement.id : e.target.id)];

        if (e.target.id.startsWith("info_button_"))
            this.state.showDescription = (this.state.showDescription == formula.name ? false : formula.name);
        else
            formula.selected = !formula.selected;
        this.forceUpdate();
    }

    onGroupItemClick(e) {
        e.preventDefault();

        var group = e.target;
        while (!group.id.startsWith("group_"))
            group = group.parentElement;
        group = this.state.groups[group.id.slice(6)];
        const state = this.getGroupItemState(group);
        switch (state) {
            case 0:
            case 2:
                group.forEach(function(formula) {
                    formula.selected = true;
                });
                break;
            case 1:
                group.forEach(function(formula) {
                    formula.selected = false;
                });
                break;
        }
        this.forceUpdate();
    }

    render() {
        var messages = <Messages items={[{severity: "info", text:
            <p><strong>{t('This is a feature preview')}</strong>: On this page you can
            select <a href="https://docs.saltstack.com/en/latest/topics/development/conventions/formulas.html">Salt formulas</a> for
            this group/system, which can then be configured on group and system level. This allows you to automatically install and configure software. 
            We would be glad to receive your feedback via the <a href="https://forums.suse.com/forumdisplay.php?22-SUSE-Manager" target="_blank">{t('forum')}</a>.</p>
        }]}/>;
        if (this.state.messages.length > 0) {
            messages = <Messages items={this.state.messages.map(function(msg) {
                return {severity: "info", text: msg};
            })}/>;
        }

        var errors = null;
        if (this.state.errors && this.state.errors.length > 0) {
            errors = <Messages items={this.state.errors.map(function(e) {
                return {severity: "error", text: e};
            })}/>;
        }

        this.props.addFormulaNavBar(this.state.activeFormulas);

        return (
            <div>
                {errors}{messages}
                <div className="panel panel-default">
                    <div className="panel-heading">
                        <h4>Formulas</h4>
                    </div>
                    <div className="panel-body">
                        <form id="chooseFormulaForm" className="form-horizontal" onSubmit={function (e) {e.preventDefault();}}>
                            <div className="form-group">
                                <label htmlFor="chooseFormulas" className="col-lg-3 control-label">
                                    Choose formulas:
                                </label>
                                <div className="col-lg-6">
                                    <div id="chooseFormulas" className="list-group">
                                      {this.generateList()}
                                    </div>
                                </div>
                            </div>
                            <div className="form-group">
                                <div className="col-lg-offset-3 col-lg-6">
                                    <span className="btn-group">
                                        <AsyncButton id="save-btn" icon="floppy-o" action={this.saveRequest} name={t("Save")} />
                                        <AsyncButton id="apply-btn" defaultType="btn-success" action={this.applyRequest} name={t("Apply Highstate")} />
                                    </span>
                                    <span className="btn-group pull-right">
                                        <Button id="clear-btn" icon="fa-eraser" text="Remove all" className="btn btn-default" handler={this.removeAllFormulas} />
                                        <Button id="reset-btn" icon="fa-undo" text="Reset Changes" className="btn btn-default" handler={this.resetChanges} />
                                    </span>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        );
    }
}

module.exports = {
    FormulaSelection: FormulaSelection
}
