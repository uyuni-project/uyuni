export const ConfirmChanges = ({ toEnable, toDisable }) => {
  const numberOfChanges = toEnable.length + toDisable.length;

  if (numberOfChanges < 1) {
    return <h6>{t("Please apply any changes before submitting.")}</h6>;
  }

  return (
    <>
      <h6>Changes Summary:</h6>
      <div>
        <p>
          {t("Streams to be enabled:")} {toEnable.length === 0 && t("No changes.")}
        </p>
        <ul>
          {toEnable.map((it) => (
            <li key={it}>{it}</li>
          ))}
        </ul>
      </div>

      <div>
        <p>
          {t("Streams to be disabled:")} {toDisable.length === 0 && t("No changes.")}
        </p>
        <ul>
          {toDisable.map((it) => (
            <li key={it}>{it}</li>
          ))}
        </ul>
      </div>
    </>
  );
};
