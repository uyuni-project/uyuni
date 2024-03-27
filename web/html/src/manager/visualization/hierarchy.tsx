import * as React from "react";

import * as d3 from "d3";

import SpaRenderer from "core/spa/spa-renderer";

import { DateTimePicker } from "components/datetime";
import { Form, Select } from "components/input";
import { TopPanel } from "components/panels/TopPanel";

import { LocalizedMoment, localizedMoment } from "utils";
import { DEPRECATED_unsafeEquals } from "utils/legacy";

import Network from "../../utils/network";
import * as Preprocessing from "./data-processing/preprocessing";
import * as DataTree from "./data-tree";
import * as UI from "./ui/components";
import * as Utils from "./utils";

// See java/code/src/com/suse/manager/webui/templates/visualization/hierarchy.jade
declare global {
  interface Window {
    endpoint?: any;
    title?: any;
    view?: any;
  }
}

function showFilterTab(tabIdToShow) {
  d3.selectAll(".filter-tab-selector").classed("active", false);
  d3.selectAll(".filter-tab").classed("active", false);
  d3.select("#" + tabIdToShow + "-selector").classed("active", true);
  d3.select("#" + tabIdToShow).classed("active", true);
  Utils.adjustSvgDimensions();
}

// NB! This is a magic constant
const NO_GROUP_LABEL = "** NO GROUP **";

type Props = {};

type State = {
  showFilters: boolean;
  hasGroupingFilter: boolean;
  tree: ReturnType<typeof DataTree.dataTree> | undefined;
  partitioningDateTime: LocalizedMoment;
  systemGroups: any[];
  selectedSystemGroups: string[][];
};

class Hierarchy extends React.Component<Props, State> {
  state: State = {
    showFilters: false,
    hasGroupingFilter: false,
    tree: undefined,
    partitioningDateTime: localizedMoment(),
    systemGroups: [NO_GROUP_LABEL],
    selectedSystemGroups: [],
  };

  componentDidMount() {
    // Get data & put everything together in the graph!
    Network.get(window.endpoint).then(
      (data) => jQuery(document).ready(() => this.displayHierarchy(data)),
      (xhr) => d3.select("#svg-wrapper").text(t("There was an error fetching data from the server."))
    );
  }

  componentWillUnmount() {
    d3.select("#visualization-filter-wrapper").exit().remove();
    d3.select("#svg-wrapper").exit().remove();
    // This was disabled in displayHierarchy
    registerSpacewalkContentObservers && registerSpacewalkContentObservers();
  }

  displayHierarchy = (data) => {
    // disable the #spacewalk-content observer:
    // drawing svg triggers it for every small changes,
    // and it is not the desired behaviour/what the observer stands for
    // note: leaving it connected slow down the svg usability
    spacewalkContentObserver.disconnect();

    const container = Utils.prepareDom();
    const tree = DataTree.dataTree(data, container);
    if (window.view === "grouping") {
      // hack - derive preprocessor from global variable
      tree.preprocessor(Preprocessing.grouping());
    }
    tree.refresh();

    this.initUI(tree);

    const treeSystemGroups = tree
      .data()
      .map((e) => e.managed_groups || [])
      .reduce((a, b) => a.concat(b));
    const systemGroups = [NO_GROUP_LABEL, ...treeSystemGroups];
    this.setState({ tree, systemGroups });

    d3.select(window).on("resize", function () {
      Utils.adjustSvgDimensions();
    });
  };

  initUI = (tree: ReturnType<typeof DataTree.dataTree>) => {
    // Patch count filter
    const patchCountsFilter = d3.select("#filtering-tab").append("div").attr("class", "filter");

    patchCountsFilter.append("div").attr("class", "filter-title").text(t("Show systems with:"));

    // state of the patch status checkboxes:
    // [bug fix adv. checked, prod. enhancements checked, security adv. checked]
    const patchCountFilterConfig = [false, false, false];
    // create a callback function that
    //  - updates patchCountFilterConfig at given index,
    //  - updates the filters based on patchCountFilterConfig
    //  - refreshes the tree
    function patchCountFilterCallback(idx) {
      return function (checked) {
        patchCountFilterConfig[idx] = checked;
        if (!patchCountFilterConfig.includes(true)) {
          tree.filters().remove("patch_count_filter");
        } else {
          tree.filters().put("patch_count_filter", (d) => {
            return (
              Utils.isSystemType(d) &&
              patchCountFilterConfig // based on the checkboxes state, take into account the patch count
                .map((value, index) => value && (d.data.patch_counts || [])[index] > 0)
                .reduce((a, b) => a || b, false)
            );
          });
        }
        tree.refresh();
      };
    }
    UI.addCheckbox(
      patchCountsFilter,
      t("security advisories"),
      "fa-shield",
      "security-patches",
      patchCountFilterCallback(2)
    );
    UI.addCheckbox(patchCountsFilter, t("bug fix advisories"), "fa-bug", "bug-patches", patchCountFilterCallback(0));
    UI.addCheckbox(
      patchCountsFilter,
      t("product enhancement advisories"),
      "spacewalk-icon-enhancement",
      "minor-patches",
      patchCountFilterCallback(1)
    );

    d3.select("#filtering-tab").append("div").attr("id", "filter-systems-box");
    // System name filter
    UI.addFilter(d3.select("#filter-systems-box"), t("Filter by system name"), t("e.g., client.nue.sles"), (input) => {
      tree.filters().put("name", (d) => d.data.name.toLowerCase().includes(input.toLowerCase()));
      tree.refresh();
    });

    // Base channel filter
    UI.addFilter(d3.select("#filter-systems-box"), t("Filter by system base channel"), t("e.g., SLE12"), (input) => {
      tree
        .filters()
        .put("base_channel", (d) => (d.data.base_channel || "").toLowerCase().includes(input.toLowerCase()));
      tree.refresh();
    });

    // Installed products filter
    UI.addFilter(
      d3.select("#filter-systems-box"),
      t("Filter by system installed products"),
      t("e.g., SLES"),
      (input) => {
        if (!input) {
          tree.filters().remove("installedProducts");
        } else {
          tree
            .filters()
            .put("installedProducts", (d) =>
              (d.data.installedProducts || [])
                .map((ip) => ip.toLowerCase().includes(input.toLowerCase()))
                .reduce((v1, v2) => v1 || v2, false)
            );
        }
        tree.refresh();
      }
    );

    this.setState({
      hasGroupingFilter: !!tree.preprocessor().groupingConfiguration,
    });
  };

  toggleFilters = () => {
    const filterBox = jQuery("#visualization-filter-wrapper");
    if (filterBox.hasClass("open")) {
      filterBox.removeClass("open").slideUp(Number.MIN_VALUE, () => {
        Utils.adjustSvgDimensions();
      });
      this.setState({ showFilters: false });
    } else {
      filterBox.addClass("open").slideDown(Number.MIN_VALUE, () => {
        Utils.adjustSvgDimensions();
      });
      this.setState({ showFilters: true });
    }
  };

  // Partitioning filters
  partitionByCheckin = () => {
    const tree = this.state.tree;
    if (!tree) {
      return;
    }

    tree.partitioning().get()["user-partitioning"] = (d) => {
      if (DEPRECATED_unsafeEquals(d.data.checkin, undefined)) {
        return "";
      }
      const referenceDateTime = this.state.partitioningDateTime.valueOf();
      const firstPartition = d.data.checkin < referenceDateTime;
      d.data.partition = firstPartition;
      return firstPartition ? "stroke-red non-checking-in" : "stroke-green checking-in";
    };
    tree.refresh();
  };

  applyPatchesPartitioning = () => {
    const tree = this.state.tree;
    if (!tree) {
      return;
    }

    tree.partitioning().get()["user-partitioning"] = (d) => {
      if (!Utils.isSystemType(d) || DEPRECATED_unsafeEquals(d.data.patch_counts, undefined)) {
        return "";
      }
      const firstPartition = d.data.patch_counts.filter((pc) => pc > 0).length > 0;
      d.data.partition = firstPartition;
      return firstPartition ? "stroke-red unpatched" : "stroke-green patched";
    };
    tree.refresh();
  };

  clearPartitioning = () => {
    const tree = this.state.tree;
    if (!tree) {
      return;
    }

    tree.partitioning().get()["user-partitioning"] = (d) => {
      return "";
    };
    tree.refresh();
  };

  render() {
    let hurl: string | undefined = undefined;
    if (window.title === "Virtualization Hierarchy") {
      hurl = "reference/systems/visualization-menu.html";
    } else if (window.title === "Proxy Hierarchy") {
      hurl = "reference/systems/visualization-menu.html";
    } else if (window.title === "Systems Grouping") {
      hurl = "reference/systems/visualization-menu.html";
    }

    const formModel = this.state.selectedSystemGroups.reduce((result, item, index) => {
      result[index] = item;
      return result;
    }, {});

    return (
      <TopPanel title={t(window.title)} helpUrl={hurl}>
        <button className="toggle-filter-button" onClick={() => this.toggleFilters()}>
          {this.state.showFilters ? t("Hide filters") : t("Show filters")}
          <i className={"fa fa-caret-" + (this.state.showFilters ? "up" : "down")} aria-hidden="true"></i>
        </button>
        <div id="visualization-filter-wrapper">
          <ul className="nav nav-tabs">
            <li id="filtering-tab-selector" className="filter-tab-selector active">
              <button onClick={() => showFilterTab("filtering-tab")}>{t("Filtering")}</button>
            </li>
            <li id="partitioning-tab-selector" className="filter-tab-selector">
              <button onClick={() => showFilterTab("partitioning-tab")}>{t("Partitioning")}</button>
            </li>
          </ul>
          <div id="filtering-tab" className="filter-tab active"></div>
          <div id="partitioning-tab" className="filter-tab">
            <div className="filter">
              <div className="filter-title">{t("Partition systems by given check-in time:")}</div>
              <div className="input-group">
                <DateTimePicker
                  value={this.state.partitioningDateTime}
                  onChange={(partitioningDateTime) => this.setState({ partitioningDateTime })}
                />
              </div>
              <div className="btn-group">
                <button type="button" className="btn btn-default btn-sm" onClick={() => this.partitionByCheckin()}>
                  {t("Apply")}
                </button>
                <button type="button" className="btn btn-default btn-sm" onClick={() => this.clearPartitioning()}>
                  {t("Clear")}
                </button>
              </div>
            </div>
            <div className="filter">
              <div className="filter-title">{t("Partition systems based on whether there are patches for them:")}</div>
              <div className="btn-group">
                <button
                  type="button"
                  className="btn btn-default btn-sm"
                  onClick={() => this.applyPatchesPartitioning()}
                >
                  {t("Apply")}
                </button>
                <button type="button" className="btn btn-default btn-sm" onClick={() => this.clearPartitioning()}>
                  {t("Clear")}
                </button>
              </div>
            </div>
            {this.state.hasGroupingFilter ? (
              <div className="filter">
                <div className="filter-title">{t("Split into groups")}</div>
                <button
                  className="toggle-grouping-level"
                  onClick={() => this.setState({ selectedSystemGroups: [...this.state.selectedSystemGroups, []] })}
                >
                  {t("Add a grouping level")} <i className="fa fa-plus"></i>
                </button>
                {this.state.selectedSystemGroups.map((item, index) => {
                  return (
                    <div className="combobox" key={`combobox-${index}`}>
                      <Form model={formModel}>
                        <Select
                          key={`combobox-${index}`}
                          name={index.toString()}
                          isMulti
                          placeholder={t("Select a system group")}
                          options={this.state.systemGroups}
                          onChange={(_, value) => {
                            const tree = this.state.tree;
                            if (!tree) {
                              return;
                            }

                            const selectedSystemGroups = [...this.state.selectedSystemGroups];
                            selectedSystemGroups[index] = value || [];
                            this.setState({ selectedSystemGroups });

                            tree.preprocessor().groupingConfiguration(selectedSystemGroups);
                            tree.refresh();
                          }}
                          //
                        />
                      </Form>
                      <button
                        className="toggle-grouping-level"
                        title={t("Remove this level")}
                        onClick={() => {
                          const tree = this.state.tree;
                          if (!tree) {
                            return;
                          }

                          const selectedSystemGroups = [...this.state.selectedSystemGroups];
                          selectedSystemGroups.splice(index, 1);
                          this.setState({ selectedSystemGroups });

                          tree.preprocessor().groupingConfiguration(selectedSystemGroups);
                          tree.refresh();
                        }}
                      >
                        <i className="fa fa-close"></i>
                      </button>
                    </div>
                  );
                })}
              </div>
            ) : null}
          </div>
        </div>
        <div id="svg-wrapper">
          <div className="detailBox"></div>
        </div>
      </TopPanel>
    );
  }
}

export const renderer = (id) => SpaRenderer.renderNavigationReact(<Hierarchy />, document.getElementById(id));
