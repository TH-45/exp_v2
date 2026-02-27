/**
 * 公共编码生成工具
 */

/* ================================
   基础方法
================================ */

// 年份后两位
const getYearShort = (): string =>
    new Date().getFullYear().toString().slice(2)

// 日期 YYMMDD
const getDateShort = (): string => {
    const now = new Date()
    const y = now.getFullYear().toString().slice(2)
    const m = String(now.getMonth() + 1).padStart(2, '0')
    const d = String(now.getDate()).padStart(2, '0')
    return `${y}${m}${d}`
}

// 数字随机
const randomNumber = (len: number): string =>
    Array.from({ length: len }, () =>
        Math.floor(Math.random() * 10)
    ).join('')

// 字母+数字随机
const randomStr = (len: number): string => {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
    return Array.from({ length: len }, () =>
        chars[Math.floor(Math.random() * chars.length)]
    ).join('')
}

/* ================================
   项目编码 PJ+YY+jb+lsh+rand{3}
================================ */
export function generateProjectCode(
    tenderMode: string,
    lsh: number
): string {
    const tenderMap: Record<string, string> = {
        OPEN: 'GK',
        INVITE: 'YQ',
        NEGOTIATION: 'TP'
    }

    const year = getYearShort()
    const jb = tenderMap[tenderMode] || 'UN'
    const lshStr = lsh.toString().padStart(3, '0')
    const rand = randomStr(3)

    return `PJ${year}${jb}${lshStr}${rand}`
}

/* ================================
   岗位编码 POS+YYMMDD+rand[3]
================================ */
export function generatePostCode(): string {
    return `POS${getDateShort()}${randomNumber(3)}`
}

/* ================================
   角色编码 ROL+YYMMDD+rand[3]
================================ */
export function generateRoleCode(): string {
    return `ROL${getDateShort()}${randomNumber(3)}`
}

/* ================================
   菜单编码 MU+YYMMDD+rand[4]
================================ */
export function generateMenuCode(): string {
    return `MU${getDateShort()}${randomNumber(4)}`
}

/* ================================
   合同编码 CT+YY+lx+lsh+rand[4]
================================ */
export function generateContractCode(
    lx: string,   // 2位类型
    lsh: number   // 3位流水号
): string {
    const year = getYearShort()
    const lshStr = lsh.toString().padStart(3, '0')
    const rand = randomNumber(4)

    return `CT${year}${lx}${lshStr}${rand}`
}