import { ref } from 'vue';
import { listDictOptions, type DictOption } from '@/api/system/dict';

interface UseDictLabelResolverOptions {
  dictCodes: string[];
  fallbackOptions?: DictOption[];
  fallbackLabelMap?: Record<string, string>;
}

function isSameAsCode(label: string, code: string) {
  return label.trim().toUpperCase() === code.trim().toUpperCase();
}

export function useDictLabelResolver(options: UseDictLabelResolverOptions) {
  const dictOptions = ref<DictOption[]>([]);

  async function loadOptions() {
    for (const dictCode of options.dictCodes) {
      try {
        const res = await listDictOptions(dictCode);
        const list = Array.isArray(res) ? res : (res as { data?: DictOption[] })?.data ?? [];
        if (list.length > 0) {
          dictOptions.value = list;
          return;
        }
      } catch {
        // ignore and try next dict code
      }
    }
    dictOptions.value = options.fallbackOptions ?? [];
  }

  function getLabel(value?: string) {
    const raw = String(value || '').trim();
    if (!raw) return '-';
    const upperRaw = raw.toUpperCase();
    const matched = dictOptions.value.find((item) => String(item.value || '').trim().toUpperCase() === upperRaw);
    const dictLabel = String(matched?.label || '').trim();
    const fallbackLabel = options.fallbackLabelMap?.[upperRaw];
    // 字典命中且标签不是编码本身，优先使用字典标签
    if (dictLabel && !isSameAsCode(dictLabel, raw)) {
      return dictLabel;
    }
    // 字典无命中或标签仍是编码时，使用中文兜底映射
    if (fallbackLabel) {
      return fallbackLabel;
    }
    return dictLabel || raw;
  }

  return {
    dictOptions,
    loadOptions,
    getLabel,
  };
}
