export interface PageResult<T> {
  /** 分页查询响应字段 */
  total?: number;
  page?: number;
  size?: number;
  list?: T[];
}

export interface PageQuery<TQuery = Record<string, unknown>> {
  /** 分页查询请求字段 */
  pageNum: number;
  pageSize: number;
  queryParam?: TQuery;
}

export type PageQueryInput<TQuery extends object = Record<string, unknown>> = Partial<PageQuery<TQuery> & TQuery>;

export function buildPageQuery<TQuery extends object = Record<string, unknown>>(
  input: PageQueryInput<TQuery>,
  defaults: { pageNum?: number; pageSize?: number } = {},
): PageQuery<TQuery> {
  const { pageNum, pageSize, queryParam, ...queryFields } = input;
  return {
    pageNum: pageNum ?? defaults.pageNum ?? 1,
    pageSize: pageSize ?? defaults.pageSize ?? 10,
    queryParam: {
      ...(queryParam || {}),
      ...(queryFields as TQuery),
    },
  };
}

export function parsePageResult<T>(
  res?: PageResult<T>,
): { list: T[]; total: number; page?: number; size?: number } {
  const list = Array.isArray(res?.list) ? res!.list! : [];
  return {
    list,
    total: Number(res?.total ?? list.length) || 0,
    page: res?.page,
    size: res?.size,
  };
}
