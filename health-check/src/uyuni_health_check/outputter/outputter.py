from rich.panel import Panel
from rich.markdown import Markdown
from rich.panel import Panel
from rich.table import Table
from rich.text import Text
from uyuni_health_check.utils import console

def show(data):
    if data:
        console.print(
            Panel(
                Text(
                    f"Ooops! Errors found!",
                    justify="center",
                )
            ),
            style="italic red blink",
        )
        table = Table(show_header=True, header_style="bold magenta", expand=True)
        table.add_column("File")
        table.add_column("Errors")

        for metric in data:
            table.add_row(metric["metric"]["filename"], metric["values"][-1][1])

        console.print(table)
    else:
        console.print(
            Panel(
                Text(
                    f"Good news! No errors detected in logs.",
                    justify="center",
                )
            ),
            style="italic green",
        )  