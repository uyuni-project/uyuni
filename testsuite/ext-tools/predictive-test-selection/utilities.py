"""
Shared utility functions for the predictive test selection scripts.
"""
import logging

def setup_logging(level, log_file):
    """
    Set up logging to both console and file.

    Args:
        level (int): Logging level (e.g., logging.DEBUG, logging.INFO).
        log_file (str): Path to the file where logs will be written.

    Returns:
        logging.Logger: Configured logger instance.
    """
    if level == logging.DEBUG:
        fmt = "%(levelname)s - %(filename)s:%(lineno)d - %(message)s"
    else:
        fmt = "%(levelname)s - %(message)s"
    formatter = logging.Formatter(fmt)
    my_logger = logging.getLogger(__name__)
    my_logger.setLevel(level)

    # Clear any existing handlers to avoid conflicts
    for handler in my_logger.handlers[:]:
        my_logger.removeHandler(handler)

    console_handler = logging.StreamHandler()
    console_handler.setFormatter(formatter)
    console_handler.setLevel(level)
    my_logger.addHandler(console_handler)

    file_handler = logging.FileHandler(log_file)
    file_handler.setFormatter(formatter)
    file_handler.setLevel(level)
    my_logger.addHandler(file_handler)
    return my_logger
