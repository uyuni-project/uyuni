import { searchCriteriaInExtension } from "./products.utils";

const extension = {
  label: "suse base 1 2 asd",
  channels: [{ summary: "suse base 1 channel 1  " }, { summary: "suse base 1 channel 2  " }],
  extensions: [
    {
      label: "suse extension 1 child 1",
      channels: [{ summary: "suse extension 1 child 1 channel 1" }, { summary: "suse extension 1 child 1 channel 2" }],
      extensions: [],
    },
    {
      label: "suse extension 1 child 2",
      channels: [{ summary: "suse extension 1 child 2 channel 1" }, { summary: "suse extension 1 child 2 channel 2" }],
      extensions: [
        {
          label: "suse extension 2 child 1",
          channels: [
            { summary: "suse extension 2 child 1 channel 1" },
            { summary: "suse extension 2 child 1 channel 2" },
          ],
          extensions: [
            {
              label: "suse extension 3 child 1",
              channels: [
                { summary: "suse extension 3 child 1 channel 1" },
                { summary: "suse extension 3 child 1 channel 2" },
              ],
              extensions: [],
            },
          ],
        },
      ],
    },
  ],
};

describe("Testing searchCriteriaInExtension", () => {
  test("check criteria in base extension label and channels", () => {
    const criteriaLabel = "base 1";
    const criteriaChannel1 = "base 1 channel 1";
    const criteriaChannel2 = "base 1 channel 2";
    const criteriaWrong = "redhat";

    expect(searchCriteriaInExtension(extension, criteriaLabel)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaChannel1)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaChannel2)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaWrong)).toBeFalsy();
  });

  test("check criteria in first level extension label and channels", () => {
    const criteriaLabel = "extension 1 child 1";
    const criteriaChannel1 = "extension 1 child 1 channel 1";
    const criteriaChannel2 = "extension 1 child 1 channel 1";
    const criteriaWrong = "redhat";

    expect(searchCriteriaInExtension(extension, criteriaLabel)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaChannel1)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaChannel2)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaWrong)).toBeFalsy();
  });

  test("check criteria in second level extension label and channels", () => {
    const criteriaLabel = "suse extension 2 child 1";
    const criteriaChannel1 = "extension 2 child 1 channel 1";
    const criteriaChannel2 = "suse extension 2 child 1 channel 2";
    const criteriaWrong = "redhat";

    expect(searchCriteriaInExtension(extension, criteriaLabel)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaChannel1)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaChannel2)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaWrong)).toBeFalsy();
  });

  test("check criteria in third level extension label and channels", () => {
    const criteriaLabel = "extension 3";
    const criteriaChannel1 = "extension 3";
    const criteriaChannel2 = "extension 3";
    const criteriaWrong = "redhat 3 extension";

    expect(searchCriteriaInExtension(extension, criteriaLabel)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaChannel1)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaChannel2)).toBeTruthy();
    expect(searchCriteriaInExtension(extension, criteriaWrong)).toBeFalsy();
  });
});
