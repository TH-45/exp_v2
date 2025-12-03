# 前端路由（history 模式）与后端 SPA 对接说明

> 适用范围：当前前端项目 `exp-zfrontend` 使用 Vue Router 的 `createWebHistory`（history 模式）。  
> 目标：说明后端 / 网关需要提供哪些接口与路由配置，才能正确支持单页应用（SPA）。

---

## 一、前端路由模式与部署要求

### 1. 路由模式

- 前端使用：`createWebHistory()`  
- URL 示例：
  - 登录页：`/login`
  - 仪表盘：首页：`/`
  - 用户管理：`/system/user`

### 2. history 模式的后端要求

history 模式下，**前端路由完全由浏览器和前端应用控制**，后端在收到这些路径（如 `/system/user`）时，如果不是 API 请求，就应该**统一返回前端的 `index.html` 文件**，否则刷新页面会出现 404。

典型规则：

- **API 请求**：以 `/api/exp/` 开头（例如 `/api/exp/auth/login`），由后端微服务处理并返回 JSON。  
- **前端路由请求**：不以 `/api/` 开头的路径（例如 `/`、`/login`、`/system/user`），统一返回 `index.html`。

---

## 二、前端对后端的接口约定（首批必须实现）

### 1. 鉴权相关接口

前端在 `src/api/auth.ts` 中约定了一个登录接口和一个“获取当前登录用户信息”的接口，均遵循 `接口约定.md` 中的统一响应结构 `ApiResponse<T>`。

- **登录接口**  
  - **URL**：`POST /api/exp/auth/login`
  - **请求体（JSON）**：
    ```json
    {
      "username": "admin",
      "password": "123456"
    }
    ```
  - **统一响应结构示例**：
    ```json
    {
      "success": true,
      "code": "0",
      "message": "OK",
      "data": {
        "token": "jwt-token-string",
        "userId": "u_001",
        "username": "管理员",
        "roles": ["ADMIN"],
        "permissions": ["system:user:view", "system:user:edit"]
      }
    }
    ```
  - 说明：
    - `token`：JWT 或其他形式的访问令牌，前端会存入 `localStorage` 并在后续请求头中带上：
      - `Authorization: Bearer {token}`

- **获取当前登录用户信息（profile）**  
  - **URL**：`GET /api/exp/auth/profile`
  - **请求头**：`Authorization: Bearer {token}`
  - **统一响应结构中的 `data` 示例**：
    ```json
    {
      "userId": "u_001",
      "username": "管理员",
      "deptId": "d_001",
      "deptName": "市场部",
      "roles": ["ADMIN"],
      "permissions": ["system:user:view", "system:user:edit"],
      "menus": ["dashboard", "system:user", "bidding:project", "contracts:list"]
    }
    ```

- **可选：退出登录（后端记录黑名单或会话）**  
  - **URL**：`POST /api/exp/auth/logout`
  - **请求头**：`Authorization: Bearer {token}`
  - **作用**：可用于标记 token 失效（如写入 Redis 黑名单）。

### 2. 用户菜单与权限接口（后续扩展）

前端可以在后期改为**动态根据后端返回的菜单生成路由**，推荐预留接口：

- **获取当前用户菜单树**  
  - **URL**：`GET /api/exp/system/menu/user`
  - **请求头**：`Authorization: Bearer {token}`
  - **返回体（示例）**：
    ```json
    [
      {
        "id": 1,
        "name": "Dashboard",
        "path": "/",
        "icon": "House",
        "children": []
      },
      {
        "id": 2,
        "name": "System",
        "path": "/system",
        "icon": "Setting",
        "children": [
          {
            "id": 3,
            "name": "用户管理",
            "path": "/system/user",
            "icon": "User",
            "children": []
          }
        ]
      }
    ]
    ```

前端可根据此菜单树渲染侧边栏，并结合权限码（`perms`）控制按钮显示。

---

## 三、Spring Cloud Gateway 示例配置（建议）

当前后端采用 Spring Cloud Gateway 作为统一网关，可参考以下思路：

### 1. 区分 API 与前端静态资源

推荐部署方式：

- **前端静态资源**（`index.html`、`assets/**` 等）可以：
  - 部署在 Nginx / 静态资源服务器上；或
  - 由 Gateway 通过路由转发到前端静态资源服务。

- **后端 API**：
  - 统一使用 `/api/exp/**` 前缀，并转发到具体微服务，如：
    - `/api/exp/auth/**` → `exp-auth` 服务
    - `/api/exp/system/**` → `auth-system-service`（示例）

### 2. history 模式下的前端路由回退

如果前端部署在 Nginx 上，可以使用如下规则（伪代码，仅示意）：

```nginx
location / {
    try_files $uri $uri/ /index.html;
}

location /api/ {
    proxy_pass http://gateway-service;
}
```

含义：

- 当访问 `/`、`/login`、`/system/user` 等路径时，如果没有找到对应的物理文件，则返回 `/index.html` 交给前端路由处理。  
- 当访问 `/api/**` 时，反向代理到后端网关。

如果直接由 Spring Cloud Gateway 提供前端静态资源，可以在网关或后端服务中配置：

- 对于非 `/api/**` 的请求，如果不是静态资源（js/css 等），统一返回 `index.html`。

---

## 四、前后端约定总结（方便论文与实现）

1. **前端路由**：  
   - 使用 Vue Router 的 history 模式，所有业务页面的 URL 都是“看起来像正常多页”的路径。  
2. **后端路由**：  
   - `/api/exp/**`：REST 接口，由各微服务（auth、system、bid-contract、project、corp 等）处理。  
   - 其他路径：由静态资源服务器或网关统一返回 `index.html`，由前端 SPA 进行路由匹配。  
3. **鉴权协议**：  
   - 登录接口：`POST /api/exp/auth/login`，返回统一结构 `ApiResponse<LoginResult>`，其中 `data.token` 为访问令牌。  
   - 前端在请求时通过 `Authorization: Bearer {token}` 传递认证信息。  
   - 网关或后端服务负责解析 JWT，进行权限校验和数据权限控制。  

后续如果你需要，我可以基于实际的 `exp-gateway` `application.yml`，帮你写出一份更贴近你当前环境的 Gateway 配置示例（包括路由到各个微服务、前端静态资源的代理等）。



