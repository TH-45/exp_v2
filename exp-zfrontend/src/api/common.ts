/**
 * 分页查询结果接口
 * @template T - 列表项的类型
 */
export interface PageResult<T> {
  /** 总记录数 */
  total?: number;
  /** 当前页码 */
  page?: number;
  /** 每页大小 */
  size?: number;
  /** 数据列表 */
  list?: T[];
}

/**
 * 分页查询请求参数接口
 * @template TQuery - 查询参数的类型，默认为 Record<string, unknown>
 */
export interface PageQuery<TQuery = Record<string, unknown>> {
  /** 页码，从1开始 */
  pageNum: number;
  /** 每页大小 */
  pageSize: number;
  /** 查询参数 */
  queryParam?: TQuery;
}

/**
 * 分页查询输入类型，支持部分字段和额外查询字段
 * @template TQuery - 查询参数的类型，必须是对象类型
 */
export type PageQueryInput<TQuery extends object = Record<string, unknown>> = Partial<PageQuery<TQuery> & TQuery>;

/**
 * 构建分页查询参数
 * @template TQuery - 查询参数的类型
 * @param input - 输入的查询参数，可以包含分页信息和额外查询字段
 * @param defaults - 默认的分页参数
 * @returns 构建好的分页查询对象
 */
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

/**
 * 解析分页查询结果
 * @template T - 列表项的类型
 * @param res - 分页查询响应结果
 * @returns 解析后的分页数据，包含列表、总数、页码和每页大小
 */
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
