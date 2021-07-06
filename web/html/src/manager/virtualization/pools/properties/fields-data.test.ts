import { computeSourceAdapterSelection } from "./fields-data";

describe("source_adapter_selection computing helper", () => {
  test("automatic case", () => {
    expect(computeSourceAdapterSelection({})).toEqual(undefined);
  });

  test("scsi_host name case", () => {
    const model = {
      source_adapter_type: "scsi_host",
      source_adapter_name: "testname",
    };
    expect(computeSourceAdapterSelection(model)).toEqual("name");
  });

  test("scsi_host parent_address case", () => {
    const model = {
      source_adapter_parentAddress: "someparentaddress",
      source_adapter_parentAddressUid: "someUid",
    };
    expect(computeSourceAdapterSelection(model)).toEqual("parent_address");
    expect(computeSourceAdapterSelection({ source_adapter_parentAddress: "foo" })).toEqual("parent_address");
    expect(computeSourceAdapterSelection({ source_adapter_parentAddressUid: "foo" })).toEqual("parent_address");
  });

  test("fc_host name case", () => {
    expect(
      computeSourceAdapterSelection({
        source_adapter_type: "fc_host",
        source_adapter_parent: "foo",
      })
    ).toEqual("name");
  });

  test("fc_host wwnn_wwpn case", () => {
    expect(
      computeSourceAdapterSelection({
        source_adapter_type: "fc_host",
        source_adapter_parentWwnn: "foo",
        source_adapter_parentWwpn: "bar",
      })
    ).toEqual("wwnn_wwpn");

    expect(
      computeSourceAdapterSelection({
        source_adapter_type: "fc_host",
        source_adapter_parentWwnn: "foo",
      })
    ).toEqual("wwnn_wwpn");

    expect(
      computeSourceAdapterSelection({
        source_adapter_type: "fc_host",
        source_adapter_parentWwpn: "foo",
      })
    ).toEqual("wwnn_wwpn");
  });

  test("fc_host fabric_wwn case", () => {
    expect(
      computeSourceAdapterSelection({
        source_adapter_type: "fc_host",
        source_adapter_parentFabricWwn: "foo",
      })
    ).toEqual("fabric_wwn");
  });
});
