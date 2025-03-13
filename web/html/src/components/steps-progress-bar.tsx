import * as React from "react";
import { useState } from "react";

import { Button } from "./buttons";

type Step = {
  title: string;
  content: React.ReactNode;
  validate: boolean | null;
};

type StepsProgressBarProps = {
  /** The css className for the 'step progress bar' div */
  className?: string;
  /** steps title and contents */
  steps: Step[];
  onCreate?: Function;
  onCancel?: Function;
};

const StepsProgressBar = ({ className, steps, onCreate, onCancel }: StepsProgressBarProps) => {
  const [currentStep, setCurrentStep] = useState(0);

  const nextStep = () => {
    const isLastStep = currentStep === steps.length - 1;
    const currentValidate = steps[currentStep].validate;

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

  const cancel = () => {
    if (onCancel) {
      onCancel();
    }
  };

  return (
    <div className={`progress-bar-wrapper ${className}`}>
      <div className="progress-bar-container">
        <div className="steps-container">
          {steps.map((step, index) => (
            <div
              key={step.title}
              className={`steps ${index < currentStep ? "completed" : index === currentStep ? "active" : ""}`}
            >
              <div className="step-circle"></div>
              <div className="step-title">{step.title}</div>
              <div className="step-line">
                <span></span>
              </div>
            </div>
          ))}
        </div>
        <div className="main-content">
          <div className="content-section">
            {steps.map((step, index) => (
              <div key={step.title} style={{ display: currentStep === index ? "block" : "none" }}>
                {step.content}
              </div>
            ))}
          </div>
        </div>
      </div>
      <div className="progress-bar-footer">
        <Button className="btn-default btn-sm pull-left" text="Cancel" handler={cancel} />
        <div className="pull-right">
          <Button
            className={`btn-default btn-sm me-3 ${currentStep === 0 ? "d-none" : ""}`}
            text="Back"
            handler={prevStep}
          />
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
