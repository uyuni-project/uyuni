import * as d3 from "d3";
import { DEPRECATED_unsafeEquals } from "utils/legacy";
import * as UI from "./ui/components";

function computeSvgDimensions() {
  const width = (d3.select("#svg-wrapper").node() as Element).getBoundingClientRect().width - 2;
  const height =
    window.innerHeight -
    (d3.select("header").node() as Element).getBoundingClientRect().height -
    (d3.select("section .spacewalk-toolbar-h1").node() as Element).getBoundingClientRect().height -
    (d3.select("#visualization-filter-wrapper").node() as Element).getBoundingClientRect().height -
    100;

  return [width, height];
}

function adjustSvgDimensions() {
  const dimensions = computeSvgDimensions();
  // try to find the object via d3
  d3.select("#svg-wrapper svg").attr("width", dimensions[0]).attr("height", dimensions[1]);
}

function prepareDom() {
  const dimensions = computeSvgDimensions();
  const svg = d3.select("#svg-wrapper").append("svg").attr("width", dimensions[0]).attr("height", dimensions[1]);

  UI.svgTextStyle(svg);

  const container = svg.append("g").attr("class", "container");

  // Zoom handling
  svg
    .call(
      d3
        .zoom()
        .scaleExtent([1 / 8, 16])
        .on("zoom", zoomed) as any
    )
    .on("dblclick.zoom", null);
  function zoomed(d) {
    // @ts-expect-error: `d3.event.transform` doesn't seem to be correctly typed in the matching version of types
    container.attr("transform", d3.event.transform);
  }

  return container;
}

function isSystemType(d) {
  return (
    d.data.type &&
    (d.data.type === "system" || d.data.type === "proxy") &&
    !DEPRECATED_unsafeEquals(d.data.rawId, "") &&
    d.data.id !== "root"
  );
}

function isCompliantToSSM(d) {
  return (
    isSystemType(d) && ["enterprise_entitled", "salt_entitled", "bootstrap_entitled"].includes(d.data.base_entitlement)
  );
}

export { computeSvgDimensions, adjustSvgDimensions, prepareDom, isSystemType, isCompliantToSSM };
