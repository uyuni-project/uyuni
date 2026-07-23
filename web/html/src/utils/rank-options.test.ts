import { afterEach, describe, expect, jest, test } from "@jest/globals";
import { readFileSync } from "fs";
import { resolve } from "path";

type RankingFunctions = {
  handle_ranking: (rankingWidgetName: string, storerName: string) => boolean;
  handle_ranking_dispatch: (rankingWidgetName: string, storerName: string) => boolean;
};

const rankingSource = readFileSync(resolve(__dirname, "../../javascript/rank_options.js"), "utf8");
const loadRankingFunctions = new Function(
  `${rankingSource}\nreturn { handle_ranking, handle_ranking_dispatch };`
) as () => RankingFunctions;

const { handle_ranking, handle_ranking_dispatch } = loadRankingFunctions();

afterEach(() => {
  document.body.innerHTML = "";
  jest.restoreAllMocks();
});

describe("rank options form submission", () => {
  test("populates a ranking value and lets the form submit natively", () => {
    document.body.innerHTML = `
      <form>
        <select id="ranksWidget">
          <option value="first">First</option>
          <option value="second">Second</option>
        </select>
        <input id="rankedValues">
        <button type="submit">Submit</button>
      </form>
    `;

    const form = document.querySelector("form") as HTMLFormElement;
    const button = document.querySelector("button") as HTMLButtonElement;
    const programmaticSubmit = jest.spyOn(HTMLFormElement.prototype, "submit").mockImplementation(() => undefined);
    let nativeSubmits = 0;

    button.addEventListener("click", () => handle_ranking_dispatch("ranksWidget", "rankedValues"));
    form.addEventListener("submit", (event) => {
      nativeSubmits += 1;
      event.preventDefault();
    });

    button.click();

    expect(programmaticSubmit).not.toHaveBeenCalled();
    expect(nativeSubmits).toBe(1);
    expect((document.getElementById("rankedValues") as HTMLInputElement).value).toBe("first,second");
  });

  test("populates both kickstart rankings before one native submission", () => {
    document.body.innerHTML = `
      <form>
        <select id="preRanksWidget">
          <option value="pre-first">Pre first</option>
          <option value="pre-second">Pre second</option>
        </select>
        <input id="rankedPreValues">
        <select id="postRanksWidget">
          <option value="post-first">Post first</option>
          <option value="post-second">Post second</option>
        </select>
        <input id="rankedPostValues">
        <button type="submit">Submit</button>
      </form>
    `;

    const form = document.querySelector("form") as HTMLFormElement;
    const button = document.querySelector("button") as HTMLButtonElement;
    const programmaticSubmit = jest.spyOn(HTMLFormElement.prototype, "submit").mockImplementation(() => undefined);
    let nativeSubmits = 0;

    button.addEventListener("click", () => {
      handle_ranking("preRanksWidget", "rankedPreValues");
      handle_ranking_dispatch("postRanksWidget", "rankedPostValues");
    });
    form.addEventListener("submit", (event) => {
      nativeSubmits += 1;
      event.preventDefault();
    });

    button.click();

    expect(programmaticSubmit).not.toHaveBeenCalled();
    expect(nativeSubmits).toBe(1);
    expect((document.getElementById("rankedPreValues") as HTMLInputElement).value).toBe("pre-first,pre-second");
    expect((document.getElementById("rankedPostValues") as HTMLInputElement).value).toBe("post-first,post-second");
  });
});
