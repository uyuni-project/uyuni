/* eslint-disable */
import * as React from 'react';
import * as Network from 'utils/network';
import {Utils, Formulas} from 'utils/functions';
import {default as Jexl} from 'jexl';
import {Button} from 'components/buttons';
import {Messages} from 'components/messages';
import {FormulaFormContext, FormulaFormContextProvider, FormulaFormRenderer, text, get} from './formulas/FormulaComponentGenerator';
import {SectionToolbar} from 'components/section-toolbar/section-toolbar';

const getEditGroupSubtype = Formulas.getEditGroupSubtype;
const EditGroupSubtype = Formulas.EditGroupSubtype;
const deepCopy = Utils.deepCopy;
const capitalize = Utils.capitalize;

const defaultMessageTexts = {
    "pillar_only_formula_saved": <p>{t("Formula saved. Applying the highstate is not needed for this formula.")}</p>
}

//props:
//dataUrl = url to get the server data
//saveUrl = url to save the data, data is sent as post request
//addFormulaNavBar = function(formulaList, activeFormulaId) to add the formula nav bar
//formulaId = the id of the formula to be shown
//getFormulaUrl = function(formulaId) that returns the url to a formula page by id (used for prev/next buttons)
//scope = current active scope (system or group)
class FormulaForm extends React.Component {
    constructor(props) {
        super(props);

        const previewMessage = <p>On this page you can configure <a href="https://docs.saltstack.com/en/latest/topics/development/conventions/formulas.html" target="_blank" rel="noopener noreferrer">Salt Formulas</a> to automatically install and configure software.</p>;

        this.state = {
            formulaName: "",
            formulaList: [],
            formulaRawLayout: {},
            systemData: {},
            groupData: {},
            formulaChanged: false,
            messages: [previewMessage],
            errors: []
        };

        window.addEventListener("beforeunload", function (e) {
            if (!this.state.formulaChanged)
                return null;

            let confirmationMessage = 'You have unsaved changes. '
                + 'If you leave before saving, your changes will be lost.';

            get(e, window.event).returnValue = confirmationMessage; //Gecko + IE
            return confirmationMessage; //Gecko + Webkit, Safari, Chrome etc.
        }.bind(this));

        this.init();
    }

    init = () => {
        var dataPromise;
        if (this.props.getDataPromise) {
            dataPromise = this.props.getDataPromise()
        } else {
            dataPromise = Network.get(this.props.dataUrl).promise;
        }

        dataPromise.then(data => {
            if (data === null)
                this.setState({
                    formulaName: "",
                    formulaList: [],
                    formulaRawLayout: {},
                    systemData: {},
                    groupData: {},
                    formulaChanged: false,
                    metadata: {}
                });
            else {
                const rawLayout = data.layout;
                this.setState({
                    formulaName: data.formula_name,
                    formulaList: data.formula_list,
                    formulaRawLayout: rawLayout,
                    systemData: get(data.system_data, {}),
                    groupData: get(data.group_data, {}),
                    formulaChanged: false,
                    formulaMetadata: data.metadata
                });
            }
        });
    }

    saveFormula = (data) => {
        this.setState({ formulaChanged: false });
        let scope = this.props.scope;
        let formType = scope.toUpperCase();
        if (formType === 'SYSTEM') {
            formType = 'SERVER';
        }

        if(data.errors) {
            const messages = [];
            if (data.errors.required && data.errors.required.length > 0) {
                messages.push(t("Please input required fields: {0}", data.errors.required.join(', ')));
            }
            if (data.errors.invalid && data.errors.invalid.length > 0) {
                messages.push(t("Invalid format of fields: {0}", data.errors.invalid.join(', ')));
            }
            this.setState({
                    messages: [],
                    errors: messages
            });
        } else {
          let formData = {
                type: formType,
                id: this.props.systemId,
                formula_name: this.state.formulaName,
                content: data.values
          };

          Network.post(
            this.props.saveUrl,
            JSON.stringify(formData),
            "application/json"
          ).promise.then(function (data) {
            if (data instanceof Array) {
              this.setState({ messages: data.map(msg => this.getMessageText(msg)), errors: [] });
            }
          }.bind(this),
            function (error) {
                try {
                    this.setState({
                        errors: [JSON.parse(error.responseText)]
                    });
                } catch (e) {
                    this.setState({
                        errors: [Network.errorMessageByStatus(error.status)]
                    });
                }
            }.bind(this));
            window.scrollTo(0, 0);
        }

    }

    getMessageText = (msg) => {
      if (!this.props.messageTexts[msg] && defaultMessageTexts[msg]) {
          return t(defaultMessageTexts[msg]);
      }
      return this.props.messageTexts[msg] ? t(this.props.messageTexts[msg]) : msg;
    }

    render() {
        let messageItems = this.state.messages.map((msg) => {
            return { severity: "info", text: msg };
        });
        messageItems = messageItems.concat(this.state.errors.map((msg) => {
            return { severity: "error", text: msg };
        }));
        const messages = <Messages items={messageItems} />;

        if (this.state.formulaRawLayout === undefined || this.state.formulaRawLayout === null || $.isEmptyObject(this.state.formulaRawLayout)) {
            if (this.props.addFormulaNavBar !== undefined)
                this.props.addFormulaNavBar(get(this.state.formulaList, ["Not found"]), this.props.formulaId);
            return (
                <div>
                    {messages}
                    <div className="panel panel-default">
                        <div className="panel-heading">
                            <h4>Error while loading form!</h4>
                        </div>
                        <div className="panel-body">
                            The requested form could not get loaded! The corresponding formula either does not exist or has no valid layout file.
                        </div>
                    </div>
                </div>
            );
        }
        else {
            if (this.props.addFormulaNavBar !== undefined) {
                this.props.addFormulaNavBar(this.state.formulaList, this.props.formulaId);
            }
            const nextHref = this.props.getFormulaUrl(this.props.formulaId + 1);
            const prevHref = this.props.getFormulaUrl(this.props.formulaId - 1);
            return (
                <FormulaFormContextProvider layout={this.state.formulaRawLayout}
                    systemData={this.state.systemData}
                    groupData={this.state.groupData}
                    scope={this.props.scope}>
                        <div>
                            {messages}
                            <div className="form-horizontal">
                                <SectionToolbar>
                                    <div className="btn-group">
                                        <button id="prev-btn" type="button" onClick={() => window.location.href = prevHref} disabled={this.props.formulaId === 0} className="btn btn-default"><i className="fa fa-arrow-left" /> Prev</button>
                                        <button id="next-btn" type="button" onClick={() => window.location.href = nextHref} disabled={this.props.formulaId >= this.state.formulaList.length - 1} className="btn btn-default">Next <i className="fa fa-arrow-right fa-right" /></button>
                                    </div>
                                    <div className="action-button-wrapper">
                                            <FormulaFormContext.Consumer>
                                                {({validate, clearValues}) =>
                                                    <div className="btn-group">
                                                        <Button id="save-btn" icon="fa-floppy-o" text="Save Formula" className={"btn btn-success"} handler={() => this.saveFormula(validate())} />
                                                        <Button id="reset-btn" icon="fa-eraser" text="Clear values" className="btn btn-default" handler={() => clearValues(() => window.confirm("Are you sure you want to clear all values?"))} />
                                                    </div>
                                                }
                                            </FormulaFormContext.Consumer>
                                    </div>
                                </SectionToolbar>
                                <div className="panel panel-default">
                                    <div className="panel-heading">
                                        <h3>{capitalize(get(this.state.formulaName, "Unnamed"))}</h3>
                                    </div>
                                    <div className="panel-body">
                                        <div className="formula-content">
                                        <p>{text(this.state.formulaMetadata.description)}</p>
                                        <hr/>
                                            <FormulaFormRenderer />
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                </FormulaFormContextProvider>
            );
        }
    }
}



export default FormulaForm;
