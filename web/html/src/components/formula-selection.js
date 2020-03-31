/* eslint-disable */
'use strict';

const React = require("react");

const Messages = require("../components/messages").Messages;
const Network = require("../utils/network");
const Buttons = require("../components/buttons");

const Button = Buttons.Button;
const AsyncButton = Buttons.AsyncButton;
const capitalize = require("../utils/functions").Utils.capitalize;
const {SectionToolbar} = require("components/section-toolbar/section-toolbar");

class FormulaSelection extends React.Component {
    constructor(props) {
        super(props);

        ["init", "saveRequest", "resetChanges", "removeAllFormulas", "getGroupItemState",
        "getListIcon", "getListStyle", "generateList", "onGroupItemClick", "onListItemClick"]
        .forEach(method => this[method] = this[method].bind(this));

        this.state = {
            formulas: {},
            groups: {groupless: []},
            activeFormulas: [],
            acviteSelectedFormulas: [],
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
                activeFormulas: get(data.active, data.selected),
                activeSelectedFormulas: data.selected,
                formulas: formulaDict,
                groups: groupDict
            });
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
        var selectedFormulas = this.state.activeSelectedFormulas;
        jQuery.each(this.state.formulas, function(name, formula) {
            formula.selected = selectedFormulas.indexOf(name) >= 0;
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
                <span key="groupless" className="list-group-item disabled">
                    <strong>
                        <i className="fa fa-lg fa-square-o" />
                        {t(" No group")}
                    </strong>
                </span>
            );
            groups.groupless.forEach(function (formula) {
                list.push(
                    <a href="#" onClick={this.onListItemClick} id={formula.name} key={formula.name} title={formula.description} className={this.getListStyle(formula.selected)}>
                        <i className={this.getListIcon(formula.selected)} />
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        {capitalize(formula.name)}
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
                        {" " + capitalize(group_name)}
                    </strong>
                </a>
            );
            group.forEach(function (formula) {
                list.push(
                    <a href="#" onClick={this.onListItemClick} id={formula.name} key={formula.name} title={formula.description} className={this.getListStyle(formula.selected)}>
                        <i className={this.getListIcon(formula.selected)} />
                        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                        {capitalize(formula.name)}
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
      var items = [];
        if(this.props.warningMessage){
          items.push({severity: "warning", text: this.props.warningMessage});
        }
        if (this.state.messages.length > 0) {
          items = items.concat(this.state.messages.map(function(msg) {
                return {severity: "info", text: msg};
            }));
        }

        if (this.state.errors && this.state.errors.length > 0) {
          items = items.concat(this.state.errors.map(function(e) {
                return {severity: "error", text: e};
            }));
        }

        this.props.addFormulaNavBar(this.state.activeFormulas);

        return (
            <div>
                <p>
                    {t('On this page you can select Salt Formulas for this group/system, which can then be configured on group and system level. This allows you to automatically install and configure software.')} 
                </p>
                <Messages items={items}/>
                <SectionToolbar>
                    <div className="action-button-wrapper">
                        <span className="btn-group pull-right">
                            <AsyncButton id="save-btn" icon="fa-floppy-o" action={this.saveRequest} text={t("Save")} />
                            <Button id="clear-btn" icon="fa-eraser" text="Remove all" className="btn btn-default" handler={this.removeAllFormulas} />
                            <Button id="reset-btn" icon="fa-undo" text="Reset Changes" className="btn btn-default" handler={this.resetChanges} />
                        </span>
                    </div>
                </SectionToolbar>
                <div className="panel panel-default">
                    <div className="panel-heading">
                        <h4>{t("Formulas")}</h4>
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
                        </form>
                    </div>
                </div>
            </div>
        );
    }
}

function get(value, def) {
    if (value == undefined)
        return def;
    return value;
}

module.exports = {
    FormulaSelection: FormulaSelection
}
