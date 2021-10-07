import * as React from "react";

import FormulaForm from "components/FormulaForm";
import { Utils } from "utils/functions";
import SpaRenderer from "core/spa/spa-renderer";
import { DEPRECATED_unsafeEquals } from "utils/legacy";

const capitalize = Utils.capitalize;

export const renderer = (renderId, { serverId, formulaId }) => {
  const msgMap = {
    formula_saved: (
      <p>
        {t("Formula saved. Apply the ")}
        <a href={"/rhn/manager/systems/details/highstate?sid=" + serverId}>{t("Highstate")}</a>
        {t(" for the changes to take effect.")}
      </p>
    ),
    error_invalid_target: t("Invalid target type."),
  };

  function addFormulaNavBar(formulaList, activeId) {
    jQuery("#formula-nav-bar").remove();

    var navBar = "<ul class='nav nav-tabs nav-tabs-pf' id='formula-nav-bar'>\n";
    navBar +=
      "<li><a href='/rhn/manager/systems/details/formulas?sid=" +
      serverId +
      "'><i class='fa fa-pencil-square-o'></i>" +
      t("Configuration") +
      "</a></li>\n";
    for (var i in formulaList)
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
      messageTexts={msgMap}
    />,
    document.getElementById(renderId)
  );
};
