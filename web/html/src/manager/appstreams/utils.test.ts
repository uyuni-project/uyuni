import { AppStreamModule, Channel, ChannelAppStream } from "./appstreams.type";
import { getStreamName, handleModuleEnableDisable, numberOfChanges } from "./utils";

const nodejs = (stream: string, enabled: boolean = true): AppStreamModule => ({
  name: "nodejs",
  stream,
  version: "1234-test-version",
  context: "context-test",
  arch: "x86_64",
  enabled,
});

const redis = (stream: string, enabled: boolean = true): AppStreamModule => ({
  name: "redis",
  stream,
  version: "4321-test-version",
  context: "context",
  arch: "x86_64",
  enabled,
});

const CHANNEL: Channel = { id: 1, label: "stream-channel-1", name: "Stream Channel 1" };

describe("Testing appstreams module functions", () => {
  test("getStreamName should return correct stream name", () => {
    const module = nodejs("18");
    expect(getStreamName(module)).toBe("nodejs:18");
  });

  test("numberOfChanges should return correct number of changes", () => {
    const toEnable = new Map([
      [1, ["nodejs:18", "postgresql:15"]],
      [2, ["redis:5"]],
    ]);
    const toDisable = new Map([
      [1, ["nodejs:20"]],
      [2, ["redis:4", "mod:test"]],
    ]);
    expect(numberOfChanges(toEnable, toDisable)).toBe(6);
  });

  test("handleModuleEnableDisable should handle appStream disablement correctly", () => {
    /**
     * Given there is a channel with 2 appStreams combined in 1 module:
     *   - module redis - appStreams redis:4 and redis:5
     * And redis:5 is enabled in the channel
     */
    const redis4 = redis("4", false);
    const redis5 = redis("5", true);
    const appStreamsMap = {
      redis: [redis4, redis5],
    };
    const appStreams: ChannelAppStream[] = [
      {
        channel: CHANNEL,
        appStreams: appStreamsMap,
      },
    ];

    // And there are no changes in the current state.
    const toEnable = new Map();
    const toDisable = new Map();
    const setToEnable = jest.fn();
    const setToDisable = jest.fn();

    // When redis:5 is passed to the handleEnableDisable function
    handleModuleEnableDisable(CHANNEL, redis5, appStreams, toEnable, toDisable, setToEnable, setToDisable);

    // Then redis:5 should be included in the toDisable Map of the channel
    expect(setToEnable).not.toHaveBeenCalled();
    expect(setToDisable).toHaveBeenCalledTimes(1);
    const toDisableUpdateFn = setToDisable.mock.calls[0][0];
    const toDisableNewState = toDisableUpdateFn(new Map());
    expect(toDisableNewState.get(CHANNEL.id)).toContain(getStreamName(redis5));
  });

  test("handleModuleEnableDisable should handle appStream enablement correctly", () => {
    /**
     * Given there is a channel with 4 appStreams combined in 2 modules:
     *   - module nodejs - appStreams nodejs:18 and nodejs:20
     *   - module redis - appStreams redis:4 and redis:5
     * And all appStreams are disabled in the channel
     */
    const nodejs18 = nodejs("18", false);
    const nodejs20 = nodejs("20", false);
    const redis4 = redis("4", false);
    const redis5 = redis("5", false);
    const appStreamsMap = {
      nodejs: [nodejs18, nodejs20],
      redis: [redis4, redis5],
    };
    const appStreams: ChannelAppStream[] = [
      {
        channel: CHANNEL,
        appStreams: appStreamsMap,
      },
    ];

    // And there are no changes in the current state.
    const toEnable = new Map();
    const toDisable = new Map();
    const setToEnable = jest.fn();
    const setToDisable = jest.fn();

    //When nodejs:20 is passed to the handleEnableDisable function
    handleModuleEnableDisable(CHANNEL, nodejs20, appStreams, toEnable, toDisable, setToEnable, setToDisable);

    // Then nodejs:20 appStream should be included in the toEnable Map of the channel.
    expect(setToEnable).toHaveBeenCalledTimes(1);
    expect(setToDisable).not.toHaveBeenCalled();
    const updateFunction = setToEnable.mock.calls[0][0];
    const prevState = new Map();
    const newState = updateFunction(prevState);
    expect(newState.get(CHANNEL.id)).toContain(getStreamName(nodejs20));

    // Once the nodejs:20 appStream is in the toEnable Map of the channel.
    toEnable.set(CHANNEL.id, [getStreamName(nodejs20)]);

    // When the nodejs:18 is passed to the handleEnableDisable function
    handleModuleEnableDisable(CHANNEL, nodejs18, appStreams, toEnable, toDisable, setToEnable, setToDisable);

    // Then it should replace nodejs:20 with nodejs:18 since they are part of the same 'nodejs' module.

    // There is the initial call to enable nodejs:20, then when we pass nodejs:18 it will be called
    // twice more, one to enable nodejs:18 and another to disable nodejs:20.
    expect(setToEnable).toHaveBeenCalledTimes(3);
    const secondCallUpdateFunction = setToEnable.mock.calls[1][0];
    const thirdCallUpdateFunction = setToEnable.mock.calls[2][0];

    const secondCallNewState = secondCallUpdateFunction(newState);
    expect(secondCallNewState.get(CHANNEL.id)).toContain(getStreamName(nodejs18));
    expect(secondCallNewState.get(CHANNEL.id)).toContain(getStreamName(nodejs20));

    const thirdCallNewState = thirdCallUpdateFunction(secondCallNewState);

    // After reproducing the update function calls, nodejs:18 should be enabled and nodejs:20 disabled.
    expect(thirdCallNewState.get(CHANNEL.id)).toContain(getStreamName(nodejs18));
    expect(thirdCallNewState.get(CHANNEL.id)).not.toContain(getStreamName(nodejs20));
  });

  test("handleModuleEnableDisable should handle appStream replacement correctly", () => {
    /**
     * Given there is a channel with 2 appStreams combined in 1 module:
     *   - module nodejs - appStreams nodejs:18 and nodejs:20
     * And nodejs:18 is enabled in the channel
     */
    const nodejs18 = nodejs("18", true);
    const nodejs20 = nodejs("20", false);
    const appStreamsMap = {
      nodejs: [nodejs18, nodejs20],
    };
    const appStreams: ChannelAppStream[] = [
      {
        channel: CHANNEL,
        appStreams: appStreamsMap,
      },
    ];

    // And there are no changes in the current state.
    const toEnable = new Map();
    const toDisable = new Map();
    const setToEnable = jest.fn();
    const setToDisable = jest.fn();

    // When nodejs:20 is passed to the handleEnableDisable function
    handleModuleEnableDisable(CHANNEL, nodejs20, appStreams, toEnable, toDisable, setToEnable, setToDisable);

    // Then nodejs:20 appStream should be included in the toEnable Map of the channel.
    // And nodejs:18 should be included in the toDisable Map of the channel
    expect(setToEnable).toHaveBeenCalledTimes(1);
    expect(setToDisable).toHaveBeenCalledTimes(1);

    const toEnableUpdateFn = setToEnable.mock.calls[0][0];
    const toEnableNewState = toEnableUpdateFn(new Map());
    expect(toEnableNewState.get(CHANNEL.id)).toContain(getStreamName(nodejs20));

    const toDisableUpdateFn = setToDisable.mock.calls[0][0];
    const toDisableNewState = toDisableUpdateFn(new Map());
    expect(toDisableNewState.get(CHANNEL.id)).toContain(getStreamName(nodejs18));
  });
});
