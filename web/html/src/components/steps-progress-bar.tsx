import * as React from "react";

import { useState } from "react";
import { Button } from "./buttons";

type Step = {
  title: string;
  content: React.ReactNode;
  validate: Boolean;
};

type StepsProgressBarProps = {
  /** The css className for the 'step progress bar' div */
  className?: string;
  /** steps title and contents */
  steps: Step[];
  onCreate?: Function
};

const StepsProgressBar = ({ className, steps, onCreate }: StepsProgressBarProps) => {
  const [currentStep, setCurrentStep] = useState(0);
  const [validate, setValidate] = useState(steps[currentStep].validate);

  const nextStep = () => {
    if (validate && currentStep < steps.length - 1) {
      setCurrentStep(currentStep + 1);
    } else if (onCreate) {
      onCreate();
    }
  };

  const prevStep = () => {
    if (currentStep > 0) {
      setCurrentStep(currentStep - 1);
    }
  };

  return (
    <div className={`progress-bar-wrapper ${className}`}>
      <div className="progress-bar-container">
        <div className="steps-container">
          {steps.map((step, index) => (
            <div key={index} className={`steps ${index < currentStep
              ? "completed"
              : index === currentStep
                ? "active"
                : ""
              }`}>
              <div className="step-circle"></div>
              <div className="step-title">{step.title}</div>
              <div className="step-line"><span></span></div>
            </div>
          ))}
        </div>
        <div className="main-content">
          <div className="content-section">
            {steps.map((step, index) => (
              <div
                key={index}
                style={{ display: currentStep === index ? "block" : "none" }}
              >
                {step.content}
              </div>
            ))}
          </div>
        </div>
        {/* <div className="content-section">{steps[currentStep]?.content}</div> */}
      </div>
      <div className="progress-bar-footer">
        <Button className={`btn-default pull-left`} text="Cancel" />
        <div className="pull-right">
          <Button className={`btn-default me-3 ${currentStep === 0
            ? "d-none"
            : ""
            }`} text="Back" handler={prevStep} />
          <Button className={"btn-primary"}
            text={`${currentStep === steps.length - 1
              ? "Create"
              : "Continue"
              }`}
            handler={nextStep} />
        </div>

      </div>
    </div>
  );
};

export { StepsProgressBar };
