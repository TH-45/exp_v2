export interface FlowRouteMatchInput {
  busType?: string;
  procCode?: string;
}

export interface FlowRouteTarget {
  path: string;
  query?: Record<string, string>;
}

interface FlowRouteConfig {
  busType: string;
  procCode: string;
  targetPath: string;
}

const FLOW_ROUTE_CONFIGS: FlowRouteConfig[] = [
  {
    busType: 'CONTRACT',
    procCode: 'CONTRACT_FUND_OUT',
    targetPath: '/contracts/contract/create',
  },
  {
    busType: 'CONTRACT',
    procCode: 'CONTRACT_FUND_IN',
    targetPath: '/contracts/contract/create',
  },
];

export function resolveFlowRouteTarget(input: FlowRouteMatchInput): FlowRouteTarget | null {
  const busType = String(input.busType || '').trim().toUpperCase();
  const procCode = String(input.procCode || '').trim().toUpperCase();
  if (!busType || !procCode) return null;

  const matched = FLOW_ROUTE_CONFIGS.find((item) => item.busType === busType && item.procCode === procCode);
  if (!matched) return null;

  return {
    path: matched.targetPath,
    query: {
      startBusType: busType,
      startProcCode: procCode,
      startMode: 'process-start',
    },
  };
}
