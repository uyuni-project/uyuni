'use strict';

const React = require("react");
const CustomStates = require("../components/custom-states").CustomStates;


function matchUrl(target) {
    return "/rhn/manager/api/states/match?id=" + groupId + "&type=group"
             + (target ? "&target=" + target : "");
}

function applyRequest() {
    return $.ajax({
        type: "POST",
        url: "/rhn/manager/api/states/apply",
        data: JSON.stringify({
            id: groupId,
            type: "group",
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
            id: groupId,
            type: "group",
            saltStates: states
        }),
        contentType: "application/json"
    })
}

React.render(
  <CustomStates matchUrl={matchUrl} saveRequest={saveRequest} applyRequest={applyRequest}/>,
  document.getElementById('custom-states')
);
