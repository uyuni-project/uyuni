import * as React from "react";

import { useState } from "react";
import { Button } from "./buttons";

type Step = {
  title: string;
  content: React.ReactNode; // Allows JSX, strings, or any valid React content
};

export type StepsProgressBarProps = {
  id?: string;
  /** The css className for the 'modal-dialog' div */
  className?: string;
  /** steps title and contents */
  steps: Step[];
};


const StepsProgressBar = ({ steps, classNames = {} }) => {
  const [currentStep, setCurrentStep] = useState(0);
  const [validate, setValidate] = useState(steps[currentStep].validate);

  const nextStep = () => {

    if (validate && currentStep < steps.length - 1) {
      setCurrentStep(currentStep + 1);
    }
  };

  const prevStep = () => {
    if (currentStep > 0) {
      setCurrentStep(currentStep - 1);
    }
  };

  return (
    <div className="progress-bar-wrapper">
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
          <div className="section pueple">{steps[currentStep]?.content}</div>
        </div>
      </div>
      <div className="progress-bar-footer">
        <Button className={`btn-default pull-left ${currentStep === 0
          ? "d-none"
          : ""
          }`} text="Back" handler={prevStep} />
        <Button className={`pull-right ${currentStep === steps.length - 1
          ? "btn-primary"
          : "btn-default"
          }`} text={`${currentStep === steps.length - 1
            ? "Create"
            : "Next"
            }`} handler={nextStep} />
      </div>
    </div>
  );
};

export { StepsProgressBar };
