import { promises as fs } from "fs";
import { tmpdir } from "os";
import path from "path";

import {
  findExampleFiles,
  generateLegacyStories,
  shouldRegenerateOnChange,
  storyGroupName,
} from "./generate-legacy-stories-lib";
import { storyExportName, storyTemplate } from "./story-template";

async function writeFile(filePath: string, content = "export default () => null;\n"): Promise<void> {
  await fs.mkdir(path.dirname(filePath), { recursive: true });
  await fs.writeFile(filePath, content, "utf8");
}

describe("generate legacy Storybook stories", () => {
  let workspace: string;
  let inputDir: string;
  let outputDir: string;

  beforeEach(async () => {
    workspace = await fs.mkdtemp(path.join(tmpdir(), "uyuni-storybook-"));
    inputDir = path.join(workspace, "html/src");
    outputDir = path.join(workspace, "storybook/generated");
  });

  afterEach(async () => {
    await fs.rm(workspace, { recursive: true, force: true });
  });

  test("finds example files and ignores generated or vendored directories", async () => {
    await writeFile(path.join(inputDir, "components/buttons/variants.example.tsx"));
    await writeFile(path.join(inputDir, "components/buttons/icons.example.ts"));
    await writeFile(path.join(inputDir, "components/buttons/not-a-story.tsx"));
    await writeFile(path.join(inputDir, "node_modules/package/ignored.example.tsx"));
    await writeFile(path.join(inputDir, "storybook/generated/ignored.example.tsx"));
    await writeFile(path.join(inputDir, "vendors/ignored.example.tsx"));
    await writeFile(path.join(inputDir, "dist/ignored.example.tsx"));

    await expect(findExampleFiles(inputDir)).resolves.toEqual([
      "components/buttons/icons.example.ts",
      "components/buttons/variants.example.tsx",
    ]);
  });

  test("renders a legacy CSF story wrapper", () => {
    expect(storyTemplate("components/buttons/usage-guidelines.example.tsx")).toContain(
      'title: "Legacy Example Stories/buttons"'
    );
    expect(storyTemplate("components/buttons/usage-guidelines.example.tsx")).toContain(
      'import Component from "components/buttons/usage-guidelines.example";'
    );
    expect(storyTemplate("components/buttons/usage-guidelines.example.tsx")).toContain('tags: ["!autodocs"]');
    expect(storyTemplate("components/buttons/usage-guidelines.example.tsx")).toContain('name: "usage-guidelines"');
  });

  test("matches the legacy internal storybook grouping", () => {
    expect(storyGroupName("components/input/check/form.example.tsx")).toBe("check");
    expect(storyTemplate("components/input/check/form.example.tsx")).toContain('title: "Legacy Example Stories/check"');
    expect(storyTemplate("components/utils/loading/text.example.tsx")).toContain(
      'title: "Legacy Example Stories/loading"'
    );
    expect(storyTemplate("core/intl/index.example.tsx")).toContain('title: "Legacy Example Stories/intl"');
  });

  test("creates valid export names from file names", () => {
    expect(storyExportName("usage-guidelines")).toBe("Usage_guidelines");
    expect(storyExportName("123-start")).toBe("Story_123_start");
  });

  test("detects source changes that require regenerating wrappers", () => {
    expect(shouldRegenerateOnChange("components/buttons/variants.example.tsx")).toBe(true);
    expect(shouldRegenerateOnChange("components/buttons/variants.tsx")).toBe(false);
    expect(shouldRegenerateOnChange("storybook/generated/components/buttons/variants.example.stories.tsx")).toBe(false);
    expect(shouldRegenerateOnChange("vendors/ignored.example.tsx")).toBe(false);
    expect(shouldRegenerateOnChange(undefined)).toBe(true);
  });

  test("generates wrapper files and clears stale output", async () => {
    await writeFile(path.join(inputDir, "components/buttons/variants.example.tsx"));
    await writeFile(path.join(inputDir, "core/intl/index.example.tsx"));
    await writeFile(path.join(outputDir, "stale.stories.tsx"), "stale");

    await expect(generateLegacyStories({ inputDir, outputDir })).resolves.toMatchObject({
      count: 2,
      exampleFiles: ["components/buttons/variants.example.tsx", "core/intl/index.example.tsx"],
      outputDir,
    });

    await expect(fs.readFile(path.join(outputDir, "stale.stories.tsx"), "utf8")).rejects.toMatchObject({
      code: "ENOENT",
    });
    await expect(
      fs.readFile(path.join(outputDir, "components/buttons/variants.example.stories.tsx"), "utf8")
    ).resolves.toContain("Legacy story generated from components/buttons/variants.example.tsx.");
    await expect(fs.readFile(path.join(outputDir, "core/intl/index.example.stories.tsx"), "utf8")).resolves.toContain(
      'import Component from "core/intl/index.example";'
    );
  });

  test("updates wrappers without removing directories in watch mode", async () => {
    const staleDirectory = path.join(outputDir, "core");

    await writeFile(path.join(inputDir, "components/buttons/icons.example.tsx"));
    await writeFile(path.join(staleDirectory, "stale.example.stories.tsx"), "stale");

    await expect(generateLegacyStories({ inputDir, outputDir, cleanOutput: false })).resolves.toMatchObject({
      count: 1,
      exampleFiles: ["components/buttons/icons.example.tsx"],
      outputDir,
    });

    await expect(fs.readFile(path.join(staleDirectory, "stale.example.stories.tsx"), "utf8")).rejects.toMatchObject({
      code: "ENOENT",
    });
    await expect(fs.stat(staleDirectory).then((stats) => stats.isDirectory())).resolves.toBe(true);
    await expect(
      fs.readFile(path.join(outputDir, "components/buttons/icons.example.stories.tsx"), "utf8")
    ).resolves.toContain('import Component from "components/buttons/icons.example";');
  });
});
