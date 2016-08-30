'use strict';

const React = require("react");
const ReactDOM = require("react-dom");

const Messages = require("../components/messages").Messages;
const Network = require("../utils/network");
const Buttons = require("../components/buttons")

const Button = Buttons.Button;
const AsyncButton = Buttons.AsyncButton;

const toTitle = require("../components/FormulaForm").toTitle;

var GroupFormulas = React.createClass({

    requestServerData() {
        Network.get("/rhn/manager/api/formulas/list/GROUP/" + groupId).promise.then(data => {
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
    },

    getInitialState: function() {
        const st = {
            formulas: {},
            groups: {groupless: []},
            messages: []
        };
        this.requestServerData();
        return st;
    },

    applyRequest: function() {
        if (this.state.serverData.added.length > 0 || this.state.serverData.removed.length > 0) {
            const response = confirm(t("There are unsaved changes. Do you want to proceed ?"))
            if (response == false) {
                return null;
            }
        }
        return Network.post(
            "/rhn/manager/api/states/apply",
            JSON.stringify({
                id: groupId,
                type: "GROUP",
                states: []
            }),
            "application/json"
            )
            .promise.then( data => {
              console.log("apply action queued:" + data)
              this.setState({
                  messages: [t("Applying the highstated has been scheduled for each minion server in this group")]
              });
            });
    },

    saveRequest: function() {
        const formData = {};
        formData.type = "GROUP";
        formData.id = groupId;
        formData.selected = [];
        jQuery.each(this.state.formulas, function(name, formula) {
            if (formula.selected)
                formData.selected.push(name);
        });

        return Network.post(
            "/rhn/manager/api/formulas/select",
            JSON.stringify(formData),
            "application/json"
        ).promise.then(data => {
            this.state.messages = data;
            this.requestServerData();
        },
        (xhr) => {
            try {
                this.setState({
                    errors: [JSON.parse(xhr.responseText)]
                })
            } catch (err) {
                this.setState({
                    errors: [Network.errorMessageByStatus(xhr.status)]
                })
            }
        });
    },

    resetChanges: function() {
        jQuery.each(this.state.formulas, function(name, formula) {
            formula.selected = (this.state.activeFormulas.indexOf(e.name) >= 0);
        });
        this.forceUpdate();
    },

    getGroupItemState: function(group) {
        const selectedCount = 0;
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
    },

    getListIcon: function(state) {
        if (!state)
            return "fa fa-square-o";
        else if (state == 2)
            return "fa fa-dot-circle-o";
        else
            return "fa fa-chack-square-o";
    },

    getListClass: function(state) {
        if (state)
            return "list-group-item list-group-item-info";
        else
            return "list-group-item";
    },

    generateList: function() {
        var list = [];
        const groups = this.state.groups;

        if (groups.groupless.length > 0) {
            groups.groupless.forEach(function (formula) {
                list.push(
                    <a href="#" onClick={this.onListItemClick} id={formula.name} key={formula.name} title={formula.description} className={this.getListClass(formula.selected)}>
                        <i className={this.getListIcon(formula.selected)} />
                        {" " + toTitle(formula.name)}
                    </a>
                );
            }, this);
        }
        for (var group_name in groups) {
            if (group_name == "groupless") continue;
            const group = groups[group_name];
            const group_state = this.getGroupItemState(group);
            list.push(
                <a href="#" onClick={this.onGroupItemClick} id={"group_" + group_name} key={"group_" + group_name} className={this.getListClass(group_state)}>
                    <strong>
                        <i className={this.getListIcon(group_state)} />
                        {" " + toTitle(group_name)}
                    </strong>
                </a>
                ); // this blocks broken syntax highlighting //
            group.forEach(function (formula) {
                list.push(
                    <a href="#" onClick={this.onListItemClick} id={formula.name} key={formula.name} title={formula.description} className={this.getListClass(formula.selected)}>
                        <i className={this.getListIcon(formula.selected)} />
                        <i className="fa fa-circle" />
                        {" " + toTitle(formula.name)}
                    </a>//
                );
            }, this);
        }
        return list;
    },

    onListItemClick: function(e) {
        e.preventDefault();

        const formula = this.state.formulas[(e.target.href == undefined ? e.target.parentElement.id : e.target.id)];
        formula.selected = !formula.selected;
        this.forceUpdate();
    },

    onGroupItemClick: function(e) {
        e.preventDefault();

        const group = this.state.groups[(e.target.href == undefined ? e.target.parentElement.parentElement.id : e.target.id).slice(6)];
        const state = this.getGroupItemState(group);
        switch (state) {
            case 0:
                group.forEach(function(formula) {
                    formula.selected = true;
                });
                break;
            case 1:
                group.forEach(function(formula) {
                    formula.selected = false;
                });
                break;
            case 2:
                group.forEach(function(formula) {
                    formula.selected = (this.state.activeFormulas.indexOf(e.name) >= 0);
                }, this);
                break;
        }
        this.forceUpdate();
    },

    render: function() {
        var messages = <Messages items={[{severity: "info", text:
            <p><strong>{t('This is a feature preview')}</strong>: On this page you can select <a href="https://docs.saltstack.com/en/latest/topics/development/conventions/formulas.html">Salt formulas</a> for this group, which can then be configured on group and system level. This allows you to automatically install and configure software. We would be glad to receive your feedback via the <a href="https://forums.suse.com/forumdisplay.php?22-SUSE-Manager" target="_blank">{t('forum')}</a>.</p>
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

        addFormulaNavBar(this.state.activeFormulas);

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
                                    <Button id="reset-btn" icon="fa-undo" text="Reset Changes" className="btn btn-default pull-right" handler={this.resetChanges} />
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        );
    }
});

function addFormulaNavBar(formulaList) {
    $("#formula-nav-bar").remove();

    var navBar = "<ul class='nav nav-tabs nav-tabs-pf' id='formula-nav-bar'>\n"
    navBar += "<li class='active'><a href='/rhn/manager/groups/details/formulas?sgid=" + groupId + "'>Formulas</a></li>\n";
    for (var i in formulaList)
        navBar += "<li><a href='/rhn/manager/groups/details/formula/" + i + "?sgid=" + groupId + "'>" + toTitle(formulaList[i]) + "</a></li>\n";
    navBar += "</ul>"
    $(".spacewalk-content-nav").append(navBar);
}

ReactDOM.render(
  <GroupFormulas />,
  document.getElementById('formulas')
);
