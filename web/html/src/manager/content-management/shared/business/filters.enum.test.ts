import { getClmFilterDescription } from "./filters.enum";

describe("Testing filters enum and descriptions", () => {
  test("test filter package name description", () => {
    const filter = {
      name: "filter by package name",
      criteriaKey: "name",
      criteriaValue: "package name",
      entityType: "package",
      rule: "deny",
      matcher: "contains",
    };

    expect(getClmFilterDescription(filter)).toEqual(
      "filter by package name: deny package containing package name (name)"
    );
  });

  test("test filter NEVRA description", () => {
    const filter = {
      name: "filter by nevra name",
      criteriaKey: "nevra",
      criteriaValue: "asd-123:123-123.123",
      entityType: "package",
      rule: "allow",
      matcher: "equals",
    };

    expect(getClmFilterDescription(filter)).toEqual(
      "filter by nevra name: allow package equal asd-123:123-123.123 (nevra)"
    );
    filter.criteriaKey = "nevr";
    filter.criteriaValue = "asd-123-123";
    filter.rule = "deny";
    expect(getClmFilterDescription(filter)).toEqual("filter by nevra name: deny package equal asd-123-123 (nevr)");

    filter.matcher = "lower";
    expect(getClmFilterDescription(filter)).toEqual("filter by nevra name: deny package lower than asd-123-123 (nevr)");

    filter.matcher = "lowereq";
    expect(getClmFilterDescription(filter)).toEqual(
      "filter by nevra name: deny package lower or equal than asd-123-123 (nevr)"
    );

    filter.matcher = "greater";
    expect(getClmFilterDescription(filter)).toEqual(
      "filter by nevra name: deny package greater than asd-123-123 (nevr)"
    );

    filter.matcher = "greatereq";
    expect(getClmFilterDescription(filter)).toEqual(
      "filter by nevra name: deny package greater or equal than asd-123-123 (nevr)"
    );
  });

  test("test filter package provides description", () => {
    const filter = {
      name: "filter by provides name",
      criteriaKey: "provides_name",
      criteriaValue: "installhint(reboot-needed)",
      entityType: "package",
      rule: "deny",
      matcher: "provides_name",
    };

    expect(getClmFilterDescription(filter)).toEqual(
      "filter by provides name: deny package provides name equal installhint(reboot-needed) (provides_name)"
    );
    filter.criteriaKey = "provides_name";
    filter.criteriaValue = "installhint(reboot-needed)";
    filter.rule = "allow";
    expect(getClmFilterDescription(filter)).toEqual(
      "filter by provides name: allow package provides name equal installhint(reboot-needed) (provides_name)"
    );
  });

  test("test filter advisory name description", () => {
    const filter = {
      name: "filter by advisory name",
      criteriaKey: "advisory_name",
      criteriaValue: "advisory_name_12",
      entityType: "patch",
      rule: "allow",
      matcher: "equals",
    };

    expect(getClmFilterDescription(filter)).toEqual(
      "filter by advisory name: allow patch equal advisory_name_12 (advisory_name)"
    );
  });

  test("test filter advisory type description", () => {
    const filter = {
      name: "filter by advisory type",
      criteriaKey: "advisory_type",
      criteriaValue: "Security Advisory",
      entityType: "patch",
      rule: "allow",
      matcher: "equals",
    };

    expect(getClmFilterDescription(filter)).toEqual(
      "filter by advisory type: allow patch equal Security Advisory (advisory_type)"
    );
    filter.criteriaValue = "Product Enhancement Advisory";
    filter.rule = "deny";
    expect(getClmFilterDescription(filter)).toEqual(
      "filter by advisory type: deny patch equal Product Enhancement Advisory (advisory_type)"
    );
  });

  test("test filter synopsis description", () => {
    const filter = {
      name: "filter by synopsis type",
      criteriaKey: "synopsis",
      criteriaValue: "namesynopsis_123",
      entityType: "patch",
      rule: "allow",
      matcher: "equals",
    };

    expect(getClmFilterDescription(filter)).toEqual(
      "filter by synopsis type: allow patch equal namesynopsis_123 (synopsis)"
    );
    filter.criteriaValue = "namesynopsis_1234";
    filter.matcher = "contains";
    filter.rule = "deny";
    expect(getClmFilterDescription(filter)).toEqual(
      "filter by synopsis type: deny patch containing namesynopsis_1234 (synopsis)"
    );
  });

  test("test patch by date description", () => {
    const filter = {
      name: "filter by date",
      criteriaKey: "issue_date",
      criteriaValue: "2018-09-10T00:00+01:00",
      entityType: "patch",
      rule: "deny",
      matcher: "greatereq",
    };

    expect(getClmFilterDescription(filter)).toEqual(
      "filter by date: deny patch greater or equal than 2018-09-10T00:00+01:00 (issue_date)"
    );
  });

  test("test Patch (contains Package) - package_nevr - description", () => {
    const filter = {
      name: "filter contains package name",
      criteriaKey: "package_nevr",
      criteriaValue: "asd 123:123-123",
      entityType: "patch",
      rule: "allow",
      matcher: "contains_pkg_lt_evr",
    };

    expect(getClmFilterDescription(filter)).toEqual(
      "filter contains package name: allow patch contains package with epoch/version/release lower than asd 123:123-123 (package_nevr)"
    );
    filter.matcher = "contains_pkg_le_evr";
    filter.rule = "deny";
    expect(getClmFilterDescription(filter)).toEqual(
      "filter contains package name: deny patch contains package with epoch/version/release lower or equal than asd 123:123-123 (package_nevr)"
    );
    filter.matcher = "contains_pkg_eq_evr";
    expect(getClmFilterDescription(filter)).toEqual(
      "filter contains package name: deny patch contains package with epoch/version/release equal than asd 123:123-123 (package_nevr)"
    );
    filter.matcher = "contains_pkg_ge_evr";
    expect(getClmFilterDescription(filter)).toEqual(
      "filter contains package name: deny patch contains package with epoch/version/release greater or equal than asd 123:123-123 (package_nevr)"
    );
    filter.matcher = "contains_pkg_gt_evr";
    expect(getClmFilterDescription(filter)).toEqual(
      "filter contains package name: deny patch contains package with epoch/version/release greater than asd 123:123-123 (package_nevr)"
    );
  });

  test("test Patch (contains Package Name) - package_name", () => {
    const filter = {
      name: "filter patch contains package name",
      criteriaKey: "package_name",
      criteriaValue: "package name",
      entityType: "patch",
      rule: "allow",
      matcher: "contains",
    };

    expect(getClmFilterDescription(filter)).toEqual(
      "filter patch contains package name: allow patch containing package name (package_name)"
    );
  });

  test("test Patch (contains Package) - package_provides_name - description", () => {
    const filter = {
      name: "filter contains package provides name",
      criteriaKey: "package_provides_name",
      criteriaValue: "installhint(reboot-needed)",
      entityType: "patch",
      rule: "deny",
      matcher: "contains_provides_name",
    };

    expect(getClmFilterDescription(filter)).toEqual(
      "filter contains package provides name: deny patch contains package which provides name equal installhint(reboot-needed) (package_provides_name)"
    );
  });
});
