import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import { FormulaSelection } from "components/formula-selection";

import { Utils } from "utils/functions";
import Network from "utils/network";

const capitalize = Utils.capitalize;

export const renderer = (renderId, { groupId, warningMessage }) => {
  const messageMap = {
    formulas_saved: t(
      "Formula saved. Edit configuration options in the enabled formulas and apply the <link>Highstate</link> for the changes to take effect.",
      {
        link: (str) => (
          <a href={"/rhn/manager/groups/details/highstate?sgid=" + groupId} key="link">
            {str}
          </a>
        ),
      }
    ),
    error_invalid_target: t("Invalid target type."),
  };

  function getMessageText(msg: string) {
    return messageMap[msg] || msg;
  }

  function saveRequest(component, selectedFormulas) {
    const formData: any = {};
    formData.type = "GROUP";
    formData.id = groupId;
    formData.selected = selectedFormulas;

    return Network.post("/rhn/manager/api/formulas/select", formData).then(
      (data) => {
        component.setState({
          messages: data.map((msg) => getMessageText(msg)),
        });
      },
      (xhr) => {
        try {
          component.setState({
            errors: [JSON.parse(xhr.responseText)],
          });
        } catch (err) {
          component.setState({
            errors: [Network.errorMessageByStatus(xhr.status)],
          });
        }
      }
    );
  }

  function addFormulaNavBar(formulaList) {
    jQuery("#formula-nav-bar").remove();

    let navBar = "<ul class='nav nav-tabs nav-tabs-pf' id='formula-nav-bar'>\n";
    navBar +=
      "<li class='active'><a href='/rhn/manager/groups/details/formulas?sgid=" +
      groupId +
      "'>" +
      t("Formulas") +
      "</a></li>\n";
    for (const i in formulaList)
      navBar +=
        "<li><a href='/rhn/manager/groups/details/formula/" +
        i +
        "?sgid=" +
        groupId +
        "'>" +
        capitalize(formulaList[i]) +
        "</a></li>\n";
    navBar += "</ul>";
    jQuery(".spacewalk-content-nav").append(navBar);
  }

  SpaRenderer.renderNavigationReact(
    <FormulaSelection
      dataUrl={"/rhn/manager/api/formulas/list/GROUP/" + groupId}
      saveRequest={saveRequest}
      addFormulaNavBar={addFormulaNavBar}
      warningMessage={warningMessage}
    />,
    document.getElementById(renderId)
  );
};
