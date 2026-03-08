import { computed, ref } from 'vue';
import { listDictOptions, type DictOption } from '@/api/system/dict';
import type { TagProps } from 'element-plus';

const TENDER_STATUS_DICT_CODE = 'TenderStatus';

function normalizeDictOptions(res: DictOption[] | { data?: DictOption[] }) {
  if (Array.isArray(res)) return res;
  return Array.isArray(res?.data) ? res.data : [];
}

function normalizeStatusText(value?: string) {
  return String(value ?? '').trim().toLowerCase();
}

function resolveStatusSemantic(status?: string, label?: string) {
  const text = normalizeStatusText(label || status);
  if (!text) return '';

  if (
    text.includes('未开始') ||
    text.includes('准备') ||
    text.includes('draft') ||
    text.includes('pending') ||
    text.includes('prepare')
  ) {
    return 'pending';
  }

  if (
    text.includes('进行') ||
    text.includes('公告') ||
    text.includes('投标中') ||
    text.includes('开标中') ||
    text.includes('评标中') ||
    text.includes('processing') ||
    text.includes('active') ||
    text.includes('opening') ||
    text.includes('evaluating')
  ) {
    return 'processing';
  }

  if (
    text.includes('已结束') ||
    text.includes('完成') ||
    text.includes('closed') ||
    text.includes('finished') ||
    text.includes('completed')
  ) {
    return 'completed';
  }

  if (
    text.includes('废标') ||
    text.includes('终止') ||
    text.includes('cancel') ||
    text.includes('abort')
  ) {
    return 'cancelled';
  }

  return '';
}

function semanticToTagType(semantic: string): TagProps['type'] {
  if (semantic === 'pending') return 'info';
  if (semantic === 'processing') return 'warning';
  if (semantic === 'completed') return 'success';
  if (semantic === 'cancelled') return 'danger';
  return 'info';
}

export function useTenderStatusDict() {
  const tenderStatusOptions = ref<DictOption[]>([]);

  const tenderStatusMap = computed(
    () => new Map(tenderStatusOptions.value.map((item) => [String(item.value), item.label])),
  );

  function getTenderStatusText(status?: string) {
    if (!status) return '';
    return tenderStatusMap.value.get(String(status)) ?? status;
  }

  function getTenderStatusTagType(status?: string): TagProps['type'] {
    const label = getTenderStatusText(status);
    return semanticToTagType(resolveStatusSemantic(status, label));
  }

  async function fetchTenderStatusOptions() {
    try {
      const res = await listDictOptions(TENDER_STATUS_DICT_CODE);
      tenderStatusOptions.value = normalizeDictOptions(res);
    } catch {
      tenderStatusOptions.value = [];
    }
  }

  return {
    tenderStatusOptions,
    fetchTenderStatusOptions,
    getTenderStatusText,
    getTenderStatusTagType,
  };
}
