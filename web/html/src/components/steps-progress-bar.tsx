import { useState } from "react";

import { Button, LinkButton } from "./buttons";

type Step = {
  title: string;
  content: React.ReactNode;
  validate?: (() => boolean) | (() => Promise<boolean>) | null;
};

type StepsProgressBarProps = {
  /** steps title and contents */
  steps: Step[];
  onCreate?: () => void;
  onCancel?: string;
};

const StepsProgressBar = ({ steps, onCreate, onCancel }: StepsProgressBarProps) => {
  const [currentStep, setCurrentStep] = useState(0);

  const nextStep = async () => {
    const isLastStep = currentStep === steps.length - 1;
    const currentValidate = steps[currentStep].validate ? await steps[currentStep].validate() : true;

    if ((currentValidate || currentValidate === null) && !isLastStep) {
      setCurrentStep((prevStep) => prevStep + 1);
    } else if (isLastStep && onCreate) {
      onCreate();
    }
  };

  const prevStep = () => {
    if (currentStep > 0) {
      setCurrentStep(currentStep - 1);
    }
  };

  return (
    <div className={`progress-bar-wrapper`}>
      <div className="progress-bar-container">
        <div className="steps-container">
          {steps.map((step, index) => (
            <div
              key={step.title}
              className={`steps ${index < currentStep ? "completed" : index === currentStep ? "active" : ""}`}
            >
              <div className="step-circle">{index < currentStep ? <i className="fa fa-check"> </i> : null}</div>
              <div className="step-title">{step.title}</div>
              <div className="step-line">
                <span></span>
              </div>
            </div>
          ))}
        </div>
        <div className="main-content">
          <div className="content-section">
            {steps.map((step, index) => (currentStep === index ? <div key={step.title}>{step.content}</div> : null))}
          </div>
        </div>
      </div>
      <div className="progress-bar-footer">
        <LinkButton className="btn-default btn-sm pull-left" text={t("Cancel")} href={onCancel} />
        <div className="pull-right">
          {currentStep !== 0 ? <Button className={`btn-default btn-sm me-3`} text="Back" handler={prevStep} /> : null}
          <Button
            className="btn-primary btn-sm"
            text={`${currentStep === steps.length - 1 ? "Create" : "Continue"}`}
            handler={nextStep}
          />
        </div>
      </div>
    </div>
  );
};

export { StepsProgressBar };
