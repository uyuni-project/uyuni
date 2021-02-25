import { mapFilterFormToRequest, mapResponseToFilterForm } from "./filter.utils";

describe("Testing filters form <> request mappers", () => {
  test("test filter package name", () => {
    const filterForm = {
      filter_name: "filter packageName",
      matcher: "contains",
      type: "name",
      name: "package_contains",
      rule: "deny",
    };
    expect(mapResponseToFilterForm([mapFilterFormToRequest(filterForm)])[0]).toEqual(
      expect.objectContaining(filterForm)
    );
  });

  test("test filter NEVRA", () => {
    let filterForm: any = {
      filter_name: "filter nevra name",
      matcher: "equals",
      type: "nevra",
      packageName: "asd",
      epoch: "123",
      version: "123",
      release: "123",
      architecture: "123",
      rule: "deny",
    };
    expect(mapResponseToFilterForm([mapFilterFormToRequest(filterForm)])[0]).toEqual(
      expect.objectContaining(filterForm)
    );
    expect(mapFilterFormToRequest(filterForm).criteriaKey).toEqual("nevra");
    expect(mapFilterFormToRequest(filterForm).criteriaValue).toEqual("asd-123:123-123.123");

    delete filterForm.architecture;
    delete filterForm.epoch;
    filterForm.rule = "allow";

    expect(mapResponseToFilterForm([mapFilterFormToRequest(filterForm)])[0]).toEqual(
      expect.objectContaining(filterForm)
    );
    expect(mapFilterFormToRequest(filterForm).criteriaKey).toEqual("nevr");
    expect(mapFilterFormToRequest(filterForm).criteriaValue).toEqual("asd-123-123");
  });

  test("test filter advisory name", () => {
    const filterForm = {
      filter_name: "filter advisory_name name",
      matcher: "equals",
      type: "advisory_name",
      advisory_name: "advisory_name_12",
      rule: "deny",
    };
    expect(mapResponseToFilterForm([mapFilterFormToRequest(filterForm)])[0]).toEqual(
      expect.objectContaining(filterForm)
    );
    expect(mapFilterFormToRequest(filterForm).criteriaKey).toEqual("advisory_name");
    expect(mapFilterFormToRequest(filterForm).criteriaValue).toEqual("advisory_name_12");
  });

  test("test filter advisory type", () => {
    const filterForm = {
      filter_name: "filter advisory_type name",
      matcher: "equals",
      type: "advisory_type",
      advisory_type: "Security Advisory",
      rule: "deny",
    };
    expect(mapResponseToFilterForm([mapFilterFormToRequest(filterForm)])[0]).toEqual(
      expect.objectContaining(filterForm)
    );
    expect(mapFilterFormToRequest(filterForm).criteriaKey).toEqual("advisory_type");
    expect(mapFilterFormToRequest(filterForm).criteriaValue).toEqual("Security Advisory");

    filterForm.rule = "allow";
    filterForm.advisory_type = "Product Enhancement Advisory";

    expect(mapResponseToFilterForm([mapFilterFormToRequest(filterForm)])[0]).toEqual(
      expect.objectContaining(filterForm)
    );
    expect(mapFilterFormToRequest(filterForm).criteriaKey).toEqual("advisory_type");
    expect(mapFilterFormToRequest(filterForm).criteriaValue).toEqual("Product Enhancement Advisory");
  });

  test("test filter synopsis", () => {
    const filterForm = {
      filter_name: "filter synopsis name",
      matcher: "equals",
      type: "synopsis",
      synopsis: "synopsis_name",
      rule: "deny",
    };
    expect(mapResponseToFilterForm([mapFilterFormToRequest(filterForm)])[0]).toEqual(
      expect.objectContaining(filterForm)
    );
    expect(mapFilterFormToRequest(filterForm).criteriaKey).toEqual("synopsis");
    expect(mapFilterFormToRequest(filterForm).criteriaValue).toEqual("synopsis_name");
    expect(mapFilterFormToRequest(filterForm).matcher).toEqual("equals");

    filterForm.rule = "allow";
    filterForm.matcher = "contains";

    expect(mapResponseToFilterForm([mapFilterFormToRequest(filterForm)])[0]).toEqual(
      expect.objectContaining(filterForm)
    );
    expect(mapFilterFormToRequest(filterForm).criteriaKey).toEqual("synopsis");
    expect(mapFilterFormToRequest(filterForm).criteriaValue).toEqual("synopsis_name");
    expect(mapFilterFormToRequest(filterForm).matcher).toEqual("contains");
    expect(mapFilterFormToRequest(filterForm).rule).toEqual("allow");
  });

  // test('test patch by date', () => {
  // TODO: add this test when moment isn't a global dependency and comes from NPM
  // });

  test("test Patch (contains Package) - package_nevr", () => {
    let filterForm: any = {
      filter_name: "filter contains package name",
      matcher: "contains_pkg_lt_evr",
      type: "package_nevr",
      packageName: "asd",
      epoch: "123",
      version: "123",
      release: "123",
      rule: "deny",
    };
    expect(mapResponseToFilterForm([mapFilterFormToRequest(filterForm)])[0]).toEqual(
      expect.objectContaining(filterForm)
    );
    expect(mapFilterFormToRequest(filterForm).criteriaKey).toEqual("package_nevr");
    expect(mapFilterFormToRequest(filterForm).criteriaValue).toEqual("asd 123:123-123");
    expect(mapFilterFormToRequest(filterForm).matcher).toEqual("contains_pkg_lt_evr");

    delete filterForm.epoch;
    filterForm.rule = "allow";
    filterForm.matcher = "contains_pkg_eq_evr";

    expect(mapResponseToFilterForm([mapFilterFormToRequest(filterForm)])[0]).toEqual(
      expect.objectContaining(filterForm)
    );
    expect(mapFilterFormToRequest(filterForm).criteriaKey).toEqual("package_nevr");
    expect(mapFilterFormToRequest(filterForm).criteriaValue).toEqual("asd 123-123");
    expect(mapFilterFormToRequest(filterForm).matcher).toEqual("contains_pkg_eq_evr");
  });

  test("test Patch (contains Package Name) - package_name", () => {
    const filterForm = {
      filter_name: "filter patch contains package name",
      matcher: "contains",
      type: "package_name",
      package_name: "package name",
      rule: "deny",
    };
    expect(mapResponseToFilterForm([mapFilterFormToRequest(filterForm)])[0]).toEqual(
      expect.objectContaining(filterForm)
    );
    expect(mapFilterFormToRequest(filterForm).criteriaKey).toEqual("package_name");
    expect(mapFilterFormToRequest(filterForm).criteriaValue).toEqual("package name");
    expect(mapFilterFormToRequest(filterForm).matcher).toEqual("contains");
    expect(mapFilterFormToRequest(filterForm).rule).toEqual("deny");

    filterForm.rule = "allow";

    expect(mapResponseToFilterForm([mapFilterFormToRequest(filterForm)])[0]).toEqual(
      expect.objectContaining(filterForm)
    );
    expect(mapFilterFormToRequest(filterForm).criteriaKey).toEqual("package_name");
    expect(mapFilterFormToRequest(filterForm).criteriaValue).toEqual("package name");
    expect(mapFilterFormToRequest(filterForm).matcher).toEqual("contains");
    expect(mapFilterFormToRequest(filterForm).rule).toEqual("allow");
  });

  test("test module stream filters", () => {
    const filterForm: any = {
      filter_name: "module stream filter",
      matcher: "equals",
      type: "module_stream",
      moduleName: "mymodule",
      moduleStream: "mystream",
    };

    // Filter value should be in 'module:stream' format
    expect(mapFilterFormToRequest(filterForm).criteriaKey).toEqual("module_stream");
    expect(mapFilterFormToRequest(filterForm).criteriaValue).toEqual("mymodule:mystream");
    expect(mapResponseToFilterForm([mapFilterFormToRequest(filterForm)])[0]).toEqual(
      expect.objectContaining(filterForm)
    );

    // Rule should be "allow" in any case
    filterForm.rule = "deny";

    expect(mapFilterFormToRequest(filterForm).rule).toEqual("allow");

    // Stream name can be empty
    filterForm.moduleStream = "";

    expect(mapFilterFormToRequest(filterForm).criteriaValue).toEqual("mymodule");
    expect(mapResponseToFilterForm([mapFilterFormToRequest(filterForm)])[0]).toEqual(
      expect.objectContaining({
        moduleName: "mymodule",
        moduleStream: undefined,
      })
    );

    // Stream name can be undefined
    filterForm.moduleStream = undefined;

    expect(mapFilterFormToRequest(filterForm).criteriaValue).toEqual("mymodule");
    expect(mapResponseToFilterForm([mapFilterFormToRequest(filterForm)])[0]).toEqual(
      expect.objectContaining({
        moduleName: "mymodule",
        moduleStream: undefined,
      })
    );

    // Module name must terminate after the first colon
    filterForm.moduleName = "my:module";
    filterForm.moduleStream = "my:stream";

    expect(mapFilterFormToRequest(filterForm).criteriaValue).toEqual("my:module:my:stream");
    expect(mapResponseToFilterForm([mapFilterFormToRequest(filterForm)])[0]).toEqual(
      expect.objectContaining({
        moduleName: "my",
        moduleStream: "module:my:stream",
      })
    );
  });
});
