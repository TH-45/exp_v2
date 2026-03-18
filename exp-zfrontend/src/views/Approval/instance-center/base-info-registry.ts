import { defineAsyncComponent, type Component } from 'vue';

interface BaseInfoPanelEntry {
  busType?: string;
  procCode?: string;
  component: Component;
}

const DEFAULT_BASE_INFO_PANEL = defineAsyncComponent(() => import('./base-info/panels/DefaultWorkOrderBaseInfo.vue'));

const BASE_INFO_PANEL_REGISTRY: BaseInfoPanelEntry[] = [
  {
    busType: 'CONTRACT',
    procCode: 'CONTRACT_FUND_OUT',
    component: DEFAULT_BASE_INFO_PANEL,
  },
  {
    busType: 'CONTRACT',
    procCode: 'CONTRACT_FUND_IN',
    component: DEFAULT_BASE_INFO_PANEL,
  },
  {
    component: DEFAULT_BASE_INFO_PANEL,
  },
];

export function resolveBaseInfoPanel(busType: string, procCode?: string) {
  const normalizedBusType = String(busType || '').trim().toUpperCase();
  const normalizedProcCode = String(procCode || '').trim().toUpperCase();

  const exactMatched = BASE_INFO_PANEL_REGISTRY.find(
    (entry) =>
      String(entry.busType || '').trim().toUpperCase() === normalizedBusType &&
      String(entry.procCode || '').trim().toUpperCase() === normalizedProcCode
  );
  if (exactMatched) {
    return exactMatched.component;
  }

  const busOnlyMatched = BASE_INFO_PANEL_REGISTRY.find(
    (entry) => entry.busType && !entry.procCode && String(entry.busType || '').trim().toUpperCase() === normalizedBusType
  );
  if (busOnlyMatched) {
    return busOnlyMatched.component;
  }

  return BASE_INFO_PANEL_REGISTRY.find((entry) => !entry.busType && !entry.procCode)?.component || DEFAULT_BASE_INFO_PANEL;
}
