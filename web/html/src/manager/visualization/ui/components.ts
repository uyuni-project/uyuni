// D3 UI "components" used for visualization

// Add a filter to given selection
// params:
// - targetSelection - selection where to append the element to
// - caption - text label
// - placeholder - placeholder ("the example input text")a
// - onInputCallback - callback receiving the new value of input when input
// changes
function addFilter(targetSelection, caption, placeholder, onInputCallback) {
  const filterDiv = targetSelection.append("div").attr("class", "filter");
  filterDiv.append("span").attr("class", "filter-title").text(caption);
  filterDiv
    .append("input")
    .attr("type", "text")
    .attr("placeholder", placeholder)
    .on("input", function (this: HTMLInputElement) {
      onInputCallback(this.value);
    });
}

// Add a checkbox to given selection
//
// params:
// - targetSelection - selection where to append the element to
// - caption - text label
// - icon - the fontawesome icon that is prepended to the caption
// - classes - classes of the caption (css styling)
// - callback - callback receiving the new value of checkbox when checkbox
// changes
function addCheckbox(targetSelection, caption, icon, classes, callback) {
  const parentDiv = targetSelection.append("div");
  const id = "visualization-checkbox-" + caption.trim();

  parentDiv
    .append("input")
    .attr("type", "checkbox")
    .attr("id", id)
    .on("change", function (this: HTMLInputElement) {
      callback(this.checked);
    });

  parentDiv
    .append("label")
    .attr("for", id)
    .classed(classes, true)
    .html((icon ? '<i class="fa ' + icon + '"></i>' : "") + caption);
}

// Add a button with a caption
//
// params:
// - targetSelection - selection where to append the element to
// - caption
// - callback - called after the button is clicked
function addButton(targetSelection, caption, callback) {
  targetSelection
    .append("button")
    .attr("type", "button")
    .attr("class", "btn btn-default btn-sm")
    .on("click", callback)
    .text(caption);
}

function svgTextStyle(container) {
  const textStyle = container
    .append("defs")
    .append("filter")
    .attr("x", "0")
    .attr("y", "0")
    .attr("width", "1")
    .attr("height", "1")
    .attr("id", "textStyle");
  textStyle.append("feFlood").attr("flood-color", "rgba(220, 220, 220, 0.8)");
  textStyle.append("feComposite").attr("in", "SourceGraphic");
}

export { addFilter, addCheckbox, addButton, svgTextStyle };
