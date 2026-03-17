import { defineAsyncComponent, type Component } from 'vue';
import type { ApprovalDetail } from '@/api/approval';

export interface BusinessPanelProps {
  busId: number | string;
  busType: string;
  detail: ApprovalDetail | null;
}

interface BusinessPanelEntry {
  busType: string;
  component: Component;
}

const BUSINESS_PANEL_REGISTRY: BusinessPanelEntry[] = [
  {
    busType: 'CONTRACT',
    component: defineAsyncComponent(() => import('./panels/ContractPanel.vue')),
  },
];

export function resolveBusinessPanel(busType: string) {
  const target = String(busType || '').trim().toUpperCase();
  return BUSINESS_PANEL_REGISTRY.find((x) => String(x.busType || '').trim().toUpperCase() === target)?.component || null;
}
