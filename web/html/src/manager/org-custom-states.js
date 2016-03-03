'use strict';

const React = require("react");
const ReactDOM = require('react-dom');
const CustomStates = require("../components/custom-states").CustomStates;


function matchUrl(target) {
    return "/rhn/manager/api/states/match?id=" + orgId + "&type=org"
             + (target ? "&target=" + target : "");
}

function applyRequest() {
    return $.ajax({
        type: "POST",
        url: "/rhn/manager/api/states/apply",
        data: JSON.stringify({
            id: orgId,
            type: "org",
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
            id: orgId,
            type: "org",
            saltStates: states
        }),
        contentType: "application/json"
    })
}

ReactDOM.render(
  <CustomStates matchUrl={matchUrl} saveRequest={saveRequest} applyRequest={applyRequest}/>,
  document.getElementById('custom-states')
);
