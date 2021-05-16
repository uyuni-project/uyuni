import * as utils from "./utils";

describe("allOrNone tests", () => {
  expect(utils.allOrNone(["one", "two"])).toBeTruthy();
  expect(utils.allOrNone([null, null])).toBeTruthy();
  expect(utils.allOrNone(["one", null])).toBeFalsy();
});

describe("Regex tests", () => {
  test("IPv4", () => {
    expect("192.168.128.5".match(utils.ipv4Pattern)).toContain("192.168.128.5");
    expect("192.168.128.274".match(utils.ipv4Pattern)).toBeNull();
    expect("8.8.8.8.8".match(utils.ipv4Pattern)).toBeNull();
    expect("8.8.8".match(utils.ipv4Pattern)).toBeNull();
    expect("8..8.8".match(utils.ipv4Pattern)).toBeNull();
  });

  test("IPv6", () => {
    expect("FE80:0000:0000:0000:0202:B3FF:FE1E:8329".match(utils.ipv6Pattern)).toContain(
      "FE80:0000:0000:0000:0202:B3FF:FE1E:8329"
    );
    expect("FE80::0202:B3FF:FE1E:8329".match(utils.ipv6Pattern)).toContain("FE80::0202:B3FF:FE1E:8329");
    expect("fe80::0202:b3ff:fe1e:8329".match(utils.ipv6Pattern)).toContain("fe80::0202:b3ff:fe1e:8329");
    expect("::1".match(utils.ipv6Pattern)).toContain("::1");
    expect("::".match(utils.ipv6Pattern)).toContain("::");
    expect("2001:db8::".match(utils.ipv6Pattern)).toContain("2001:db8::");
  });

  test("IP", () => {
    expect("192.168.128.5".match(utils.ipPattern)).toContain("192.168.128.5");
    expect("fe80::0202:b3ff:fe1e:8329".match(utils.ipPattern)).toContain("fe80::0202:b3ff:fe1e:8329");
    expect("2001:db8::".match(utils.ipPattern)).toContain("2001:db8::");
  });

  test("MAC address", () => {
    expect("DE:AD:BE:EF:01:02".match(utils.macPattern)).toContain("DE:AD:BE:EF:01:02");
    expect("de:ad:be:ef:01:02".match(utils.macPattern)).toContain("de:ad:be:ef:01:02");
    expect("de:ad:be:ef:1:2".match(utils.macPattern)).toBeNull();
    expect("de:ad:be:ef".match(utils.macPattern)).toBeNull();
    expect("01:02:03:de:ad:be:ef".match(utils.macPattern)).toBeNull();
    expect("01 02 03 ad be ef".match(utils.macPattern)).toBeNull();
  });

  test("DNS name", () => {
    expect("demo-lab.ACME-corp.com".match(utils.dnsNamePattern)).toContain("demo-lab.ACME-corp.com");
    expect("boggus_name.acme.com".match(utils.dnsNamePattern)).toBeNull();
  });

  test("UUID", () => {
    expect("EEBB13ab-bb9f-4665-b587-06e337808ad4".match(utils.uuidPattern)).toContain(
      "EEBB13ab-bb9f-4665-b587-06e337808ad4"
    );
    expect("EEBB13abbb9f4665b58706e337808ad4".match(utils.uuidPattern)).toContain("EEBB13abbb9f4665b58706e337808ad4");
  });
});
