type SortType = {
    direction: number;
    column: string;
};

export default class PageControl {
    page: number;
    pageSize: number;
    query: string | null | undefined;
    sort: SortType | null | undefined;

    constructor(
        page: number,
        pageSize: number,
        query?: string | null | undefined,
        sortColumn?: string | null | undefined,
        sortDirection: number = 1
    ) {
        this.page = page;
        this.pageSize = pageSize;
        this.query = query;
        if (sortColumn) {
            this.sort = {
                direction: sortDirection,
                column: sortColumn,
            };
        }
    }
}
