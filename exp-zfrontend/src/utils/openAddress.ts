/**
 * 开标地点 openAddress 工具
 * 存储格式：浙江省, 杭州市, 西湖区, 详细地址（逗号连接，第二段为城市）
 */

/**
 * 从 openAddress 解析出列表展示用的城市（第二段）
 * 空或未定义视为「线上」
 */
export function parseOpenAddressCity(openAddress?: string | null): string {
  if (!openAddress || !String(openAddress).trim()) return '线上';
  const parts = String(openAddress).split(',').map((s) => s.trim()).filter(Boolean);
  if (parts.length >= 2) return parts[1];
  return '线下';
}

/**
 * 解析 openAddress 为省、市、区、详细地址
 */
export function parseOpenAddress(openAddress?: string | null): {
  province: string;
  city: string;
  district: string;
  detail: string;
} {
  const empty = { province: '', city: '', district: '', detail: '' };
  if (!openAddress || !String(openAddress).trim()) return empty;
  const parts = String(openAddress).split(',').map((s) => s.trim()).filter(Boolean);
  return {
    province: parts[0] ?? '',
    city: parts[1] ?? '',
    district: parts[2] ?? '',
    detail: parts.length > 3 ? parts.slice(3).join(',').trim() : '',
  };
}

/**
 * 将省、市、区、详细地址拼接为 openAddress 存储格式
 */
export function buildOpenAddress(province: string, city: string, district: string, detail: string): string {
  const arr = [province, city, district, detail].filter(Boolean);
  return arr.join(', ');
}

/** 级联节点（与 element-china-area-data 的 regionData 项结构一致） */
export interface RegionNode {
  value: string;
  label: string;
  children?: RegionNode[];
}

/**
 * 根据省、市、区名称在级联数据中查找对应的 value 数组 [省code, 市code, 区code]
 * 用于编辑时回显级联选择器
 */
export function findRegionCodesByLabels(
  regionData: RegionNode[],
  provinceLabel: string,
  cityLabel: string,
  districtLabel: string
): string[] | null {
  const province = regionData.find((p) => p.label === provinceLabel);
  if (!province) return null;
  const city = province.children?.find((c) => c.label === cityLabel);
  if (!city) return [province.value];
  const district = city.children?.find((d) => d.label === districtLabel);
  if (!district) return [province.value, city.value];
  return [province.value, city.value, district.value];
}
