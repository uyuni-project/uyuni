import * as React from "react";

import SpaRenderer from "core/spa/spa-renderer";

import FormulaForm from "components/FormulaForm";

import { Utils } from "utils/functions";
import { DEPRECATED_unsafeEquals } from "utils/legacy";

const capitalize = Utils.capitalize;

export const renderer = (renderId, { serverId, formulaId }) => {
  const messageMap = {
    formula_saved: t("Formula saved. Apply the <link>Highstate</link> for the changes to take effect.", {
      link: (str) => (
        <a href={"/rhn/manager/systems/details/highstate?sid=" + serverId} key="link">
          {str}
        </a>
      ),
    }),
    error_invalid_target: t("Invalid target type."),
  };

  function addFormulaNavBar(formulaList, activeId) {
    jQuery("#formula-nav-bar").remove();

    let navBar = "<ul class='nav nav-tabs nav-tabs-pf' id='formula-nav-bar'>\n";
    navBar +=
      "<li><a href='/rhn/manager/systems/details/formulas?sid=" +
      serverId +
      "'><i class='fa fa-pencil-square-o'></i>" +
      t("Configuration") +
      "</a></li>\n";
    for (const i in formulaList)
      navBar +=
        "<li" +
        (DEPRECATED_unsafeEquals(i, activeId) ? " class='active'>" : ">") +
        "<a href='/rhn/manager/systems/details/formula/" +
        i +
        "?sid=" +
        serverId +
        "'>" +
        capitalize(formulaList[i]) +
        "</a></li>\n";
    navBar += "</ul>";
    jQuery(".spacewalk-content-nav").append(navBar);
  }

  SpaRenderer.renderNavigationReact(
    <FormulaForm
      dataUrl={"/rhn/manager/api/formulas/form/SERVER/" + serverId + "/" + formulaId}
      saveUrl="/rhn/manager/api/formulas/save"
      addFormulaNavBar={addFormulaNavBar}
      formulaId={formulaId}
      systemId={serverId}
      getFormulaUrl={function (id) {
        return "/rhn/manager/systems/details/formula/" + id + "?sid=" + serverId;
      }}
      scope="system"
      messageTexts={messageMap}
    />,
    document.getElementById(renderId)
  );
};
