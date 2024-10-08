import { NetworkProperties } from "./network-properties";

export default () => {
  const onSubmit = (event) => {
    console.log(event);
  };

  return (
    <NetworkProperties
      serverId={"fakeserverid"}
      submitText={t("Submit")}
      submit={onSubmit}
      initialModel={undefined}
      messages={[]}
      timezone="CET"
      localTime="2021-02-23T14:42+01:00"
      actionChains={[]}
    />
  );
};
