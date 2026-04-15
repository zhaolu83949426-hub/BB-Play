# 二期功能详细设计（Redis版）

## 1. 文档定位

本文档用于定义低龄宝宝视听绘本系统二期功能的详细设计，重点解决老人和家长在重复找内容、回看历史、排查失效资源、连续播放音频时的实际使用问题。

本次设计是在一期能力基础上的增量扩展，默认沿用当前项目的技术结构与分层方式：

- 前端：Vue 3 + Vant 移动端页面
- 后端：Spring Boot + Spring Security + MyBatis Plus
- 数据库：PostgreSQL
- 二期新增状态存储：Redis

二期仅提供一套 Redis 版实现，不提供本地存储、多端同步或账号体系并行方案。

## 2. 二期目标与范围

### 2.1 目标

- 提升老人快速找回常看内容的效率
- 降低重复搜索和记忆资源名称的成本
- 在管理侧提前发现失效资源链接
- 让音频播放从“单条播放”升级为“可连续播放”

### 2.2 范围

- 收藏功能
- 最近播放
- 管理页资源链接测试
- 音频播放列表与播放模式

### 2.3 非范围

- 视频播放列表
- 视频单曲循环、列表循环、随机播放
- 用户注册登录体系改造
- 跨设备同步
- 推荐算法与个性化推荐
- 绘本阅读能力

## 3. 与一期现状的衔接

### 3.1 一期已具备能力

- 前台首页支持音频、视频资源浏览、搜索、筛选、排序
- 音频支持底部抽屉播放器，当前仅支持单资源播放
- 视频支持独立播放页
- 资源支持点击统计与匿名评分
- 管理侧支持资源列表、资源新增编辑、上下架、异常标记

### 3.2 二期扩展点

- 在前台资源卡片、音频抽屉、视频播放页增加收藏能力
- 在首页增加“最近播放”和“我的收藏”入口或模块
- 在管理页资源列表增加“测试链接”按钮与结果展示
- 在现有音频抽屉播放器中增加播放列表、上一首、下一首、播放模式切换

### 3.3 二期技术取舍

- 收藏、最近播放、音频播放器状态全部存 Redis
- 资源链接测试结果落数据库，保证管理页刷新后仍可查看
- 前台仍保持匿名访问，不引入注册登录
- 管理页仍沿用管理员登录鉴权，不放开匿名测试链接能力

## 4. 总体方案设计

### 4.1 统一身份方案

二期前台新增用户态数据，但系统仍不引入正式用户体系，因此采用匿名轻量身份识别。

- 首次访问前台时，由后端生成匿名 `uid`
- `uid` 使用 UUID 字符串即可
- 后端通过 Cookie 下发 `BBPLAY_UID`
- 后续前台请求自动携带 Cookie，无需前端手动拼装身份参数
- 同一浏览器在 Cookie 未过期前视为同一匿名用户

### 4.2 Cookie 策略

- Cookie 名称：`BBPLAY_UID`
- 生效路径：`/`
- 有效期：180 天
- `HttpOnly`：开启，避免前端脚本直接操作
- `SameSite`：`Lax`
- `Secure`：生产环境开启，开发环境可关闭

### 4.3 Redis 使用边界

- Redis 只承担前台匿名用户状态数据存储
- 不把资源主数据迁移到 Redis
- 收藏列表、最近播放列表采用“索引 + 快照”结构，避免每次列表都全量查数据库
- 若资源主数据被后台修改，前台列表显示以 Redis 快照为主，点击进入播放或详情时以后端实时数据为准

### 4.4 Redis Key 设计

- `bbplay:uid:{uid}`：用户访问信息 Hash
- `bbplay:fav:zset:{uid}`：收藏排序索引 ZSet，score 为收藏时间戳
- `bbplay:fav:meta:{uid}`：收藏快照 Hash，field 为 `resourceId`
- `bbplay:recent:zset:{uid}`：最近播放排序索引 ZSet，score 为最近播放时间戳
- `bbplay:recent:meta:{uid}`：最近播放快照 Hash，field 为 `resourceId`
- `bbplay:audio:state:{uid}`：音频播放器状态 String，值为 JSON

### 4.5 TTL 规则

- `bbplay:uid:{uid}`：180 天，访问时续期
- 收藏相关键：180 天，写入和读取时续期
- 最近播放相关键：180 天，写入和读取时续期
- 音频播放器状态：30 天，状态变更时续期

### 4.6 页面信息架构调整

前台页面：

- 首页
  - 搜索区
  - 筛选区
  - 最近播放模块
  - 我的收藏模块
  - 音频/视频资源列表
  - 音频播放器抽屉
- 视频播放页
  - 视频播放器
  - 收藏按钮
  - 评分入口

管理页面：

- 管理资源列表页
  - 资源基础信息
  - 上下架、异常标记
  - 测试链接按钮
  - 最近测试结果展示

## 5. 收藏功能详细设计

### 5.1 业务目标

宝宝通常会反复听同一批音频、反复看同一批视频。家长和老人不希望每次重新搜索，因此需要把常用内容固定下来，形成一个稳定、易找的个人常看区。

### 5.2 交互设计

- 首页资源卡片右上角增加收藏图标
- 音频抽屉增加当前音频收藏按钮
- 视频播放页增加收藏按钮
- 首页增加“我的收藏”模块，默认展示最近收藏的前 10 条
- 点击“我的收藏”中的资源，直接进入对应播放行为

### 5.3 页面表现规则

- 未收藏：显示空心收藏图标
- 已收藏：显示高亮收藏图标
- 点击收藏后立即更新界面状态，不要求用户手动刷新
- 收藏列表为空时显示“暂无收藏”

### 5.4 数据结构

收藏项快照 `FavoriteItem`：

- `resourceId`：资源 ID
- `mediaType`：资源类型，取值 `AUDIO` 或 `VIDEO`
- `title`：正式标题
- `nickname`：昵称
- `coverUrl`：封面地址
- `seriesName`：系列名称
- `ageRange`：年龄段
- `playUrl`：播放地址快照
- `favoritedAt`：收藏时间

### 5.5 Redis 存储规则

- 排序索引：`bbplay:fav:zset:{uid}`
- 快照存储：`bbplay:fav:meta:{uid}`
- `ZSet member` 直接使用 `resourceId`
- `Hash field` 直接使用 `resourceId`
- `Hash value` 存储 `FavoriteItem` JSON 字符串

### 5.6 写入规则

收藏时：

- 校验资源存在且可用
- 生成最新快照
- 执行 `ZADD bbplay:fav:zset:{uid} score resourceId`
- 执行 `HSET bbplay:fav:meta:{uid} resourceId snapshot`
- 收藏重复提交时不报错，仅更新时间并置顶

取消收藏时：

- 执行 `ZREM bbplay:fav:zset:{uid} resourceId`
- 执行 `HDEL bbplay:fav:meta:{uid} resourceId`

### 5.7 读取规则

- 收藏列表按收藏时间倒序读取
- 首页“我的收藏”模块读取前 10 条
- 收藏页接口支持分页，默认每页 30 条
- 如果 Redis 中存在资源快照，但数据库中该资源已删除或下架，则列表读取时直接过滤掉该条，并异步清理 Redis 脏数据

### 5.8 前后端接口设计

`POST /api/user/favorites/{resourceId}`

- 说明：新增收藏或刷新收藏时间
- 入参：无
- 出参：`favorited=true`

`DELETE /api/user/favorites/{resourceId}`

- 说明：取消收藏
- 入参：无
- 出参：`favorited=false`

`GET /api/user/favorites`

- 参数：
  - `page`
  - `pageSize`
- 返回字段：
  - `records`
  - `total`
  - `current`
  - `size`

`GET /api/user/favorites/ids`

- 说明：批量返回当前用户收藏的资源 ID 集合，供首页快速渲染收藏状态
- 参数：`resourceIds`，逗号分隔
- 返回：`ids: number[]`

### 5.9 与现有接口的衔接

- `GET /api/media/list` 返回项新增 `favorited` 字段
- `GET /api/media/{id}` 返回详情新增 `favorited` 字段
- `favorited` 的判断来源为当前匿名用户在 Redis 中的收藏状态
- 这样前端首页与视频播放页无需额外逐条查收藏状态

### 5.10 关键流程

收藏流程：

1. 前端点击收藏按钮
2. 后端从 Cookie 解析 `uid`
3. 若无 `uid`，则创建并下发
4. 查询资源主数据并构建快照
5. 写入 Redis 索引与快照
6. 返回收藏成功与最新状态

### 5.11 验收标准

- 刷新页面后收藏状态保持一致
- 重复收藏不会产生重复条目
- 取消收藏后首页和详情页状态立即变化
- 收藏列表点击后可正常进入音频或视频播放

## 6. 最近播放详细设计

### 6.1 业务目标

老人经常记不住资源名称，但能记住“刚才播过”。最近播放的核心价值是帮助用户从记忆模糊状态中快速回到最近使用内容。

### 6.2 交互设计

- 首页新增“最近播放”模块
- 模块默认显示最近 10 条
- 点击任一条记录，直接进入对应播放行为
- 音频播放时，最近播放可带上最新进度
- 视频不扩展播放模式，但保留最近播放记录

### 6.3 数据结构

最近播放快照 `RecentPlayItem`：

- `resourceId`
- `mediaType`
- `title`
- `nickname`
- `coverUrl`
- `seriesName`
- `ageRange`
- `playUrl`
- `durationSec`
- `positionSec`
- `playedAt`

### 6.4 记录上限

- 固定保留最近 30 条
- 不做用户可配置化
- 超过 30 条后自动淘汰最旧记录

### 6.5 记录时机

音频：

- 播放满 10 秒时写一次
- 手动暂停时写一次
- 切歌时写一次当前曲目进度
- 播放结束时写一次

视频：

- 播放满 10 秒时写一次
- 手动离开播放页时写一次
- 播放结束时写一次

### 6.6 去重与覆盖规则

- 同一 `resourceId` 重复播放时，不新增重复记录
- 仅更新 `playedAt`、`durationSec`、`positionSec` 并移动到首位

### 6.7 Redis 存储规则

- 排序索引：`bbplay:recent:zset:{uid}`
- 快照存储：`bbplay:recent:meta:{uid}`
- `score` 使用最近播放时间戳
- `member` 使用 `resourceId`

### 6.8 裁剪规则

- 写入完成后读取 `ZCARD`
- 当数量大于 30 时，执行 `ZREMRANGEBYRANK` 删除最旧超限项
- 获取被删除的 `resourceId`
- 同步执行 `HDEL bbplay:recent:meta:{uid}` 删除快照

### 6.9 前后端接口设计

`POST /api/user/recent-play`

- 说明：写入最近播放记录
- 请求体：
  - `resourceId`
  - `durationSec`
  - `positionSec`

`GET /api/user/recent-play`

- 参数：`limit`，默认 10，最大 30
- 返回：最近播放列表，按 `playedAt` 倒序

### 6.10 首页展示规则

- 默认显示最近 10 条
- 资源被删除、下架或异常后，前台不展示该条，并在后台读取时顺带清理 Redis 数据
- 音频和视频混合展示，保持真实使用顺序

### 6.11 与现有播放流程的衔接

- `HomeView.vue` 中当前音频播放器在 `onPlay`、`togglePlay`、`onEnded`、切换曲目时补充最近播放上报
- `VideoPlayerView.vue` 在进入播放、离开页面、播放结束时补充最近播放上报
- 该记录能力不替代现有点击统计，二者并行存在

### 6.12 验收标准

- 最近播放固定上限 30 条
- 重复播放不会产生重复条目
- 首页刷新后最近播放仍可见
- 点击最近播放条目可正确恢复对应资源播放

## 7. 资源链接测试详细设计

### 7.1 业务目标

当前资源来自路由器插件映射 URL，稳定性存在不确定性。管理页需要在资源失效前提前发现问题，减少前台播放失败。

### 7.2 适用范围

- 仅管理页可用
- 音频与视频资源都支持测试
- 默认测试 `playUrl`
- `coverUrl` 不纳入本期测试按钮范围

### 7.3 前端交互设计

管理资源列表页新增：

- “测试链接”按钮
- 测试状态展示区
  - 最近状态
  - HTTP 状态码
  - 耗时
  - 最近测试时间
  - 错误信息

### 7.4 按钮行为规则

- 点击后按钮进入加载态
- 同一资源测试未完成前不可重复点击
- 测试成功后提示“链接可访问”
- 测试失败后提示“链接不可访问”，并展示失败原因

### 7.5 后端检测流程

1. 根据资源 ID 读取 `playUrl`
2. 发起 `HEAD` 请求
3. 若目标服务返回 `405`、`501` 或明确不支持 `HEAD`，则回退为 `GET + Range: bytes=0-0`
4. 记录总耗时
5. 根据响应结果判断可用性
6. 落库最新测试结果
7. 返回前端最新结果

### 7.6 判定规则

- `2xx`：可访问
- `3xx`：可访问，记录最终状态码
- `4xx`：不可访问
- `5xx`：不可访问
- 连接超时、读取超时、域名解析失败、SSL 异常：不可访问

### 7.7 超时与重定向规则

- 连接超时：3 秒
- 读取超时：5 秒
- 允许有限次重定向，最多 3 次
- 整个请求处理目标控制在 10 秒内返回

### 7.8 数据库字段设计

二期需要在 `media_resource` 表增加以下字段：

- `last_test_status`：最近测试状态，建议值 `SUCCESS`、`FAILED`
- `last_test_code`：最近测试状态码
- `last_test_latency_ms`：最近测试耗时
- `last_test_error`：最近错误信息
- `last_test_at`：最近测试时间

### 7.9 实体扩展建议

`MediaResource` 增加字段：

- `lastTestStatus`
- `lastTestCode`
- `lastTestLatencyMs`
- `lastTestError`
- `lastTestAt`

`MediaAdminItemVO` 增加字段：

- `lastTestStatus`
- `lastTestCode`
- `lastTestLatencyMs`
- `lastTestError`
- `lastTestAt`

### 7.10 接口设计

`POST /api/admin/media/{id}/test-link`

- 说明：测试资源播放链接
- 入参：无
- 返回：
  - `status`
  - `statusCode`
  - `latencyMs`
  - `checkedAt`
  - `errorMessage`

### 7.11 与现有管理接口的衔接

- 当前管理控制器为 `AdminMediaController`
- 二期在该控制器中新增 `testLink` 接口即可
- 服务层在 `MediaService` 中新增 `testLink(Long id)` 方法
- 列表页 `AdminMediaListView.vue` 增加按钮和测试结果展示区域

### 7.12 验收标准

- 点击测试按钮后 10 秒内返回结果
- 失败时能明确区分状态码失败、超时、域名不可达等原因
- 刷新管理页列表后仍可看到最近测试结果

## 8. 音频播放列表与播放模式详细设计

### 8.1 业务目标

一期音频只能单条播放，切换下一条必须重新返回列表点击。二期需要让音频具备连续播放能力，并且支持老人可理解的简单播放模式。

### 8.2 功能边界

- 仅对音频生效
- 视频播放页不增加播放列表
- 仅在现有首页音频抽屉播放器上扩展
- 不增加独立音频播放页

### 8.3 播放列表来源

- 用户在首页当前筛选条件下看到的音频列表，即当前播放列表来源
- 点击某个音频开始播放时，将当前列表中的全部音频资源写入播放器状态
- `currentIndex` 指向当前点击的音频项

### 8.4 播放器新增控件

- 上一首按钮
- 下一首按钮
- 播放模式切换按钮
- 播放列表入口
- 当前播放序号提示

### 8.5 播放模式定义

- `SINGLE_LOOP`：当前曲目播放结束后继续播放当前曲目
- `LIST_LOOP`：按列表顺序播放，到最后一首后回到第一首
- `LIST_SHUFFLE`：按随机顺序播放当前列表全部歌曲，单轮不重复

### 8.6 状态模型

音频播放器状态 `AudioPlayerState`：

- `playlist`：音频资源快照数组
- `currentIndex`：当前播放索引
- `currentResourceId`：当前播放资源 ID
- `playMode`：播放模式
- `currentTimeSec`：当前播放进度
- `durationSec`：总时长
- `shuffleBag`：随机模式剩余待播放索引列表
- `updatedAt`：状态更新时间

音频资源快照 `AudioPlaylistItem`：

- `resourceId`
- `title`
- `nickname`
- `coverUrl`
- `playUrl`
- `seriesName`

### 8.7 Redis 存储

- Key：`bbplay:audio:state:{uid}`
- 类型：String
- 值：`AudioPlayerState` JSON

### 8.8 写入时机

- 点击音频开始播放时写入完整状态
- 切换上一首、下一首时更新状态
- 切换播放模式时更新状态
- 手动拖动进度时更新状态
- 页面刷新前或播放器收起时更新当前进度

### 8.9 页面刷新恢复规则

- 首页加载完成后读取 `GET /api/user/audio-player-state`
- 若存在有效状态且资源仍可用，则恢复当前音频、播放模式、进度、播放列表
- 若当前资源已失效，则丢弃整个播放器状态，不做兼容 fallback

### 8.10 上一首与下一首规则

`SINGLE_LOOP`：

- 点击上一首：按列表上一首处理
- 点击下一首：按列表下一首处理
- 自动播完：继续当前曲目

`LIST_LOOP`：

- 点击上一首：当前索引减一，首项回绕到末项
- 点击下一首：当前索引加一，末项回绕到首项
- 自动播完：播放下一首

`LIST_SHUFFLE`：

- 自动播完：从 `shuffleBag` 取下一首
- 点击下一首：同样从 `shuffleBag` 取下一首
- 点击上一首：按最近播放轨迹回退到上一首，不重新随机

### 8.11 随机袋算法

- 进入 `LIST_SHUFFLE` 时，基于当前列表生成一个不含当前索引的随机序列，作为 `shuffleBag`
- 每次自动播完或点击下一首时，从 `shuffleBag` 头部取一个索引作为下一首
- 当 `shuffleBag` 为空时，表示本轮播放完成
- 本轮完成后，重新根据全列表生成新的随机袋，继续下一轮
- 每一轮中同一首音频只出现一次

### 8.12 列表变化处理规则

- 用户在首页重新筛选后再次点击音频，新的可见音频列表覆盖旧播放列表
- 若只是刷新页面，不重建列表，直接恢复 Redis 中最后一次状态
- 若恢复时发现列表为空或当前索引非法，则丢弃播放器状态

### 8.13 前后端接口设计

`GET /api/user/audio-player-state`

- 说明：读取当前匿名用户最近一次音频播放器状态

`PUT /api/user/audio-player-state`

- 请求体：
  - `playlist`
  - `currentIndex`
  - `currentResourceId`
  - `playMode`
  - `currentTimeSec`
  - `durationSec`
  - `shuffleBag`

### 8.14 与现有前端实现的衔接

- `frontend/src/views/front/HomeView.vue` 当前已有 `currentAudio`、`audioRef`、`progress`、`onEnded` 等基础能力
- 二期在该页面基础上扩展，不新增第二套播放器实现
- `VideoPlayerView.vue` 不引入播放模式控件，保持现有结构

### 8.15 验收标准

- 点击任一音频后，当前可见音频列表进入播放序列
- 三种播放模式切换后立即生效
- 随机模式单轮内不重复播放
- 页面刷新后恢复当前曲目、模式与进度
- 视频播放页不出现音频专属控件

## 9. 前后端接口汇总

### 9.1 前台新增接口

- `POST /api/user/favorites/{resourceId}`
- `DELETE /api/user/favorites/{resourceId}`
- `GET /api/user/favorites`
- `GET /api/user/favorites/ids`
- `POST /api/user/recent-play`
- `GET /api/user/recent-play`
- `GET /api/user/audio-player-state`
- `PUT /api/user/audio-player-state`

### 9.2 前台既有接口扩展

- `GET /api/media/list`：返回 `favorited`
- `GET /api/media/{id}`：返回 `favorited`

### 9.3 管理新增接口

- `POST /api/admin/media/{id}/test-link`

## 10. 后端实现设计

### 10.1 控制层拆分

- 收藏、最近播放、播放器状态建议新增 `UserMediaController`
- 资源链接测试继续放在 `AdminMediaController`
- 前台资源列表、详情、点击、评分仍保留在 `FrontMediaController`

### 10.2 服务层拆分建议

- `MediaService`：继续负责资源主数据、点击、评分、管理操作
- 新增 `UserMediaStateService`：负责匿名用户收藏、最近播放、音频状态
- 新增 `MediaLinkTestService`：负责资源链接测试

### 10.3 Redis 接入要求

- 新增 Redis 依赖与连接配置
- 使用 `StringRedisTemplate` 即可满足本期需要
- Redis JSON 统一使用应用现有 JSON 序列化工具
- 关键 Redis 写入使用同一服务内封装，避免控制器直接操作 Redis

### 10.4 匿名用户初始化

- 在前台匿名接口访问时统一解析 `BBPLAY_UID`
- 若不存在则创建新 `uid`
- 创建时同时写入 `bbplay:uid:{uid}`
- 每次访问匿名接口时刷新 `lastSeenAt`

### 10.5 数据一致性要求

- 收藏和最近播放以 Redis 为准
- 资源详情、播放地址、上下架状态以数据库为准
- 读取 Redis 快照时发现数据库状态失效，直接清理 Redis 数据

### 10.6 Mapper 与 SQL 约束

- 数据库更新放在 Mapper 与 Service 中完成
- SQL 仍写在 Mapper 层，按 MyBatis Plus 方式实现
- 不使用 XML SQL，不使用 `@Sql`

## 11. 前端实现设计

### 11.1 首页改造点

- 资源卡片增加收藏按钮
- 搜索筛选区下方增加最近播放模块
- 最近播放模块下方增加我的收藏模块
- 音频抽屉增加播放列表控制栏
- 音频抽屉增加收藏按钮、上一首、下一首、模式切换

### 11.2 视频播放页改造点

- 增加收藏按钮
- 增加最近播放上报
- 保持现有评分交互不变

### 11.3 管理列表页改造点

- 每条资源新增测试链接按钮
- 每条资源展示最近测试状态和时间
- 测试中按钮 loading，避免重复提交

### 11.4 前端校验要求

新增或编辑资源时：

- 标题：必填，最大长度 100
- 资源 URL：必填，最大长度 500
- 封面 URL：非必填，最大长度 500
- 错误表现与一期表单风格保持一致
- 必填项继续显示 `*`
- 超长禁止继续输入

## 12. 数据模型调整

### 12.1 `media_resource` 新增字段

- `last_test_status`
- `last_test_code`
- `last_test_latency_ms`
- `last_test_error`
- `last_test_at`

### 12.2 VO / DTO 调整建议

前台 VO：

- `MediaFrontItemVO` 新增 `favorited`
- `MediaDetailVO` 新增 `favorited`

管理 VO：

- `MediaAdminItemVO` 新增测试结果字段

前台 DTO：

- 新增 `RecentPlayReportRequest`
- 新增 `AudioPlayerStateSaveRequest`

### 12.3 状态枚举建议

- 收藏状态不单独建枚举，直接使用布尔值
- 播放模式枚举：`SINGLE_LOOP`、`LIST_LOOP`、`LIST_SHUFFLE`
- 链接测试状态枚举：`SUCCESS`、`FAILED`

## 13. 安全与权限设计

### 13.1 前台匿名能力

- 收藏、最近播放、播放器状态均属于匿名轻量用户态
- 不要求用户登录
- 不暴露可伪造的显式 `uid` 请求参数
- 身份仅通过 Cookie 识别

### 13.2 管理侧能力

- 资源链接测试仍需管理员登录
- 不开放前台匿名调用
- 测试接口归属 `/api/admin/**`，继续受现有 Spring Security 规则保护

## 14. 测试与验证

### 14.1 后端验证

- 收藏新增、取消、重复收藏、分页查询
- 最近播放新增、去重、裁剪、无效资源清理
- 音频状态保存、读取、无效状态丢弃
- 链接测试成功、4xx、5xx、超时、域名不可达、重定向

### 14.2 前端验证

- 首页资源卡片收藏状态渲染
- 收藏后立即更新 UI
- 最近播放模块排序正确
- 音频三种模式切换后行为正确
- 刷新页面后恢复当前曲目与进度
- 视频页只显示收藏与评分，不显示播放模式控件

### 14.3 集成验证

- 首页点击音频，进入完整播放列表
- 切换下一首后最近播放正确更新
- 管理页点击测试链接后状态回显并可刷新保留

## 15. 开发顺序建议

1. 接入 Redis 与匿名 `uid` 识别能力
2. 实现收藏接口与前台收藏状态返回
3. 实现最近播放接口与首页展示
4. 实现音频播放器状态保存、恢复与三种播放模式
5. 实现资源链接测试接口与管理页展示
6. 完成前后端联调与真机验证

## 16. 二期结论

二期采用 Redis 版设计后，系统仍保持“前台匿名可用、管理页轻量维护”的整体定位，但用户体验会明显提升：

- 常看内容可通过收藏快速进入
- 刚播放过的内容可通过最近播放快速找回
- 管理员能在前台故障前发现失效资源
- 音频具备真正可用的连续播放能力

该方案与当前项目代码结构兼容，新增范围明确，适合直接进入开发实施。
