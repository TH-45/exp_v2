export interface StartCenterEntry {
  busType: string;
  businessName: string;
  createPath: string;
  description: string;
}

export const START_CENTER_REGISTRY: StartCenterEntry[] = [
  {
    busType: 'tender',
    businessName: '招标项目',
    createPath: '/bidding/project',
    description: '创建招标项目并提交审批',
  },
  {
    busType: 'contract',
    businessName: '合同台账',
    createPath: '/contracts/contract',
    description: '创建合同单据并提交审批',
  },
  {
    busType: 'change',
    businessName: '合同变更',
    createPath: '/contracts/change',
    description: '创建合同变更单并提交审批',
  },
  {
    busType: 'payment',
    businessName: '收付款台账',
    createPath: '/contracts/payment',
    description: '创建收付款单并提交审批',
  },
];

export function findStartEntry(busType: string) {
  return START_CENTER_REGISTRY.find((x) => x.busType === busType);
}
