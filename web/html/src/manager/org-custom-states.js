'use strict';

const React = require("react");
const CustomStates = require("../components/custom-states").CustomStates;


function matchUrl(target) {
    return "/rhn/manager/api/states/match?oid=" + orgId
             + (target ? "&target=" + target : "");
}

function applyRequest() {
    return $.ajax({
        type: "POST",
        url: "/rhn/manager/api/states/apply",
        data: JSON.stringify({
            oid: orgId,
            states: ["custom"]
        }),
        contentType: "application/json",
        dataType: "json"
    });
}

function saveRequest(states) {
    return $.ajax({
        type: "POST",
        url: "/rhn/manager/api/states/save",
        data: JSON.stringify({
            oid: orgId,
            saltStates: states
        }),
        contentType: "application/json"
    })
}


//class Page extends React.Component {
//
//  constructor(props) {
//    super(props);
//
//    ["matchUrl"]
//    .forEach(method => this[method] = this[method].bind(this));
//  }
//
//  matchUrl() {
//      return "/rhn/manager/api/states/match?oid=" + orgId;
//  }
//
//  render() {
//    return (<CustomStates matchUrl={this.matchUrl}/>);
//  }
//
//}

React.render(
  <CustomStates matchUrl={matchUrl} saveRequest={saveRequest} applyRequest={applyRequest}/>,
  document.getElementById('custom-states')
);
