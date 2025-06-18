export function initializeTooltips(): void {
  const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
  console.log(`Initializing ${tooltipTriggerList.length} tooltips`);
  [...tooltipTriggerList].forEach(tooltipTriggerEl => {
    new bootstrap.Tooltip(tooltipTriggerEl);
  });
}
