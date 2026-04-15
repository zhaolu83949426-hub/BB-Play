CREATE TABLE IF NOT EXISTS series_dict (
    id BIGINT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    alias VARCHAR(50),
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,
    sort_weight INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS media_resource (
    id BIGINT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    alias VARCHAR(100),
    series_id BIGINT NOT NULL,
    age_range VARCHAR(20) NOT NULL,
    media_type VARCHAR(20) NOT NULL,
    play_url VARCHAR(500) NOT NULL,
    cover_url VARCHAR(500) NOT NULL,
    description VARCHAR(500),
    source_type VARCHAR(30),
    source_platform VARCHAR(50),
    source_remark VARCHAR(200),
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    is_abnormal BOOLEAN NOT NULL DEFAULT FALSE,
    abnormal_remark VARCHAR(200),
    click_count BIGINT NOT NULL DEFAULT 0,
    rating_avg NUMERIC(4, 2) NOT NULL DEFAULT 0,
    rating_count INTEGER NOT NULL DEFAULT 0,
    sort_weight INTEGER NOT NULL DEFAULT 0,
    last_test_status VARCHAR(20),
    last_test_code INTEGER,
    last_test_latency_ms INTEGER,
    last_test_error VARCHAR(500),
    last_test_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS media_rating (
    id BIGINT PRIMARY KEY,
    media_id BIGINT NOT NULL,
    score SMALLINT NOT NULL,
    device_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted SMALLINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_media_resource_filter
    ON media_resource (is_published, is_abnormal, media_type, age_range, series_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_media_resource_click ON media_resource (click_count DESC);
CREATE INDEX IF NOT EXISTS idx_media_resource_rating ON media_resource (rating_avg DESC);
CREATE INDEX IF NOT EXISTS idx_media_rating_media_id ON media_rating (media_id);
CREATE INDEX IF NOT EXISTS idx_series_dict_enabled ON series_dict (is_enabled, sort_weight);
CREATE INDEX IF NOT EXISTS idx_sys_user_username ON sys_user (username);

-- 插入默认管理员账号 (密码: admin123)
INSERT INTO sys_user (id, username, password, role, created_at, updated_at, deleted)
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', 'ADMIN', NOW(), NOW(), 0)
ON CONFLICT (username) DO NOTHING;

-- 插入演示系列数据
INSERT INTO series_dict (id, name, alias, is_enabled, sort_weight, created_at, updated_at, deleted) VALUES
(1001, '托马斯和朋友', '小火车', TRUE, 100, NOW(), NOW(), 0),
(1002, '小猪佩奇', '佩奇', TRUE, 90, NOW(), NOW(), 0),
(1003, '汪汪队立大功', '汪汪队', TRUE, 85, NOW(), NOW(), 0),
(1004, '睡前故事', '晚安故事', TRUE, 80, NOW(), NOW(), 0),
(1005, '儿歌童谣', '儿歌', TRUE, 75, NOW(), NOW(), 0),
(1006, '动物世界', '动物', TRUE, 70, NOW(), NOW(), 0),
(1007, '数字启蒙', '数学', TRUE, 65, NOW(), NOW(), 0),
(1008, '英语启蒙', 'ABC', TRUE, 60, NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- 插入演示音频资源
INSERT INTO media_resource (id, title, nickname, alias, series_id, age_range, media_type, play_url, cover_url, description, source_type, source_platform, source_remark, is_published, is_abnormal, abnormal_remark, click_count, rating_avg, rating_count, sort_weight, created_at, updated_at, deleted) VALUES
(2001, '托马斯和朋友们第1集', '滴滴', '小火车滴滴', 1001, 'AGE_2_4', 'AUDIO', 'https://example.com/audio/thomas-01.mp3', 'https://picsum.photos/seed/thomas1/400/300', '托马斯是一辆蓝色的小火车，他和朋友们在多多岛上快乐地工作。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 1024, 4.8, 156, 100, NOW() - INTERVAL '10 days', NOW(), 0),
(2002, '小猪佩奇去游乐场', '佩奇', '佩奇游乐场', 1002, 'AGE_2_4', 'AUDIO', 'https://example.com/audio/peppa-01.mp3', 'https://picsum.photos/seed/peppa1/400/300', '佩奇和乔治一起去游乐场玩耍，度过了快乐的一天。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 856, 4.9, 142, 95, NOW() - INTERVAL '9 days', NOW(), 0),
(2003, '汪汪队救援行动', '汪汪队', '狗狗救援', 1003, 'AGE_2_4', 'AUDIO', 'https://example.com/audio/paw-01.mp3', 'https://picsum.photos/seed/paw1/400/300', '汪汪队接到紧急任务，小狗们齐心协力完成救援。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 732, 4.7, 98, 90, NOW() - INTERVAL '8 days', NOW(), 0),
(2004, '晚安摇篮曲', '晚安歌', '睡前音乐', 1004, 'AGE_0_2', 'AUDIO', 'https://example.com/audio/lullaby-01.mp3', 'https://picsum.photos/seed/lullaby1/400/300', '温柔的摇篮曲，帮助宝宝安静入睡。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 1456, 4.9, 234, 85, NOW() - INTERVAL '7 days', NOW(), 0),
(2005, '小星星儿歌', '小星星', '一闪一闪', 1005, 'AGE_0_2', 'AUDIO', 'https://example.com/audio/star-01.mp3', 'https://picsum.photos/seed/star1/400/300', '一闪一闪亮晶晶，满天都是小星星。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 2134, 5.0, 312, 80, NOW() - INTERVAL '6 days', NOW(), 0),
(2006, '认识小动物', '动物叫声', '动物世界', 1006, 'AGE_0_2', 'AUDIO', 'https://example.com/audio/animal-01.mp3', 'https://picsum.photos/seed/animal1/400/300', '通过声音认识各种小动物，学习动物叫声。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 645, 4.6, 87, 75, NOW() - INTERVAL '5 days', NOW(), 0),
(2007, '数字1到10', '数数歌', '学数字', 1007, 'AGE_2_4', 'AUDIO', 'https://example.com/audio/number-01.mp3', 'https://picsum.photos/seed/number1/400/300', '跟着音乐学习数字1到10，轻松掌握数数。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 523, 4.5, 76, 70, NOW() - INTERVAL '4 days', NOW(), 0),
(2008, 'ABC字母歌', '字母歌', '学英语', 1008, 'AGE_4_6', 'AUDIO', 'https://example.com/audio/abc-01.mp3', 'https://picsum.photos/seed/abc1/400/300', '欢快的字母歌，帮助宝宝记住26个英文字母。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 789, 4.7, 112, 65, NOW() - INTERVAL '3 days', NOW(), 0),
(2009, '三只小猪的故事', '小猪盖房子', '经典童话', 1004, 'AGE_2_4', 'AUDIO', 'https://example.com/audio/pigs-01.mp3', 'https://picsum.photos/seed/pigs1/400/300', '三只小猪各自盖房子，最后战胜了大灰狼。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 934, 4.8, 145, 60, NOW() - INTERVAL '2 days', NOW(), 0),
(2010, '小兔子乖乖', '兔子', '儿歌经典', 1005, 'AGE_0_2', 'AUDIO', 'https://example.com/audio/rabbit-01.mp3', 'https://picsum.photos/seed/rabbit1/400/300', '小兔子乖乖，把门儿开开，经典儿歌。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 1267, 4.9, 189, 55, NOW() - INTERVAL '1 day', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- 插入演示视频资源
INSERT INTO media_resource (id, title, nickname, alias, series_id, age_range, media_type, play_url, cover_url, description, source_type, source_platform, source_remark, is_published, is_abnormal, abnormal_remark, click_count, rating_avg, rating_count, sort_weight, created_at, updated_at, deleted) VALUES
(3001, '托马斯的冒险之旅', '托马斯视频', '小火车冒险', 1001, 'AGE_2_4', 'VIDEO', 'https://example.com/video/thomas-v01.mp4', 'https://picsum.photos/seed/thomasv1/400/300', '托马斯在多多岛上展开了一场精彩的冒险之旅。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 632, 4.7, 89, 100, NOW() - INTERVAL '10 days', NOW(), 0),
(3002, '佩奇的生日派对', '佩奇生日', '生日快乐', 1002, 'AGE_2_4', 'VIDEO', 'https://example.com/video/peppa-v01.mp4', 'https://picsum.photos/seed/peppav1/400/300', '今天是佩奇的生日，家人和朋友们一起庆祝。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 845, 4.9, 124, 95, NOW() - INTERVAL '9 days', NOW(), 0),
(3003, '汪汪队海上救援', '海上救援', '狗狗出动', 1003, 'AGE_2_4', 'VIDEO', 'https://example.com/video/paw-v01.mp4', 'https://picsum.photos/seed/pawv1/400/300', '汪汪队在海上展开紧急救援行动。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 567, 4.6, 78, 90, NOW() - INTERVAL '8 days', NOW(), 0),
(3004, '动物音乐会', '动物表演', '音乐会', 1006, 'AGE_2_4', 'VIDEO', 'https://example.com/video/animal-v01.mp4', 'https://picsum.photos/seed/animalv1/400/300', '森林里的小动物们举办了一场精彩的音乐会。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 423, 4.5, 65, 85, NOW() - INTERVAL '7 days', NOW(), 0),
(3005, '数字王国探险', '数字探险', '学数学', 1007, 'AGE_4_6', 'VIDEO', 'https://example.com/video/number-v01.mp4', 'https://picsum.photos/seed/numberv1/400/300', '跟随主角一起进入数字王国，学习数学知识。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 389, 4.4, 52, 80, NOW() - INTERVAL '6 days', NOW(), 0),
(3006, '英语字母大冒险', 'ABC冒险', '学字母', 1008, 'AGE_4_6', 'VIDEO', 'https://example.com/video/abc-v01.mp4', 'https://picsum.photos/seed/abcv1/400/300', '在冒险中学习英语字母，寓教于乐。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 512, 4.6, 71, 75, NOW() - INTERVAL '5 days', NOW(), 0),
(3007, '小红帽的故事', '小红帽', '经典童话', 1004, 'AGE_2_4', 'VIDEO', 'https://example.com/video/redhood-v01.mp4', 'https://picsum.photos/seed/redhoodv1/400/300', '小红帽去看望奶奶的经典童话故事。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 678, 4.8, 95, 70, NOW() - INTERVAL '4 days', NOW(), 0),
(3008, '两只老虎儿歌', '老虎儿歌', '儿歌视频', 1005, 'AGE_0_2', 'VIDEO', 'https://example.com/video/tiger-v01.mp4', 'https://picsum.photos/seed/tigerv1/400/300', '两只老虎跑得快，经典儿歌动画版。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 1123, 4.9, 167, 65, NOW() - INTERVAL '3 days', NOW(), 0),
(3009, '恐龙世界探秘', '恐龙', '史前动物', 1006, 'AGE_4_6', 'VIDEO', 'https://example.com/video/dino-v01.mp4', 'https://picsum.photos/seed/dinov1/400/300', '带宝宝认识各种恐龙，探索史前世界。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 756, 4.7, 103, 60, NOW() - INTERVAL '2 days', NOW(), 0),
(3010, '托马斯学颜色', '学颜色', '认识颜色', 1001, 'AGE_0_2', 'VIDEO', 'https://example.com/video/color-v01.mp4', 'https://picsum.photos/seed/colorv1/400/300', '跟着托马斯一起认识各种颜色。', 'MANUAL', '示例平台', '演示数据', TRUE, FALSE, NULL, 892, 4.8, 128, 55, NOW() - INTERVAL '1 day', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- 插入演示评分数据
INSERT INTO media_rating (id, media_id, score, device_id, created_at, deleted) VALUES
(4001, 2001, 5, 'demo-device-001', NOW() - INTERVAL '5 days', 0),
(4002, 2001, 5, 'demo-device-002', NOW() - INTERVAL '4 days', 0),
(4003, 2001, 4, 'demo-device-003', NOW() - INTERVAL '3 days', 0),
(4004, 2002, 5, 'demo-device-004', NOW() - INTERVAL '5 days', 0),
(4005, 2002, 5, 'demo-device-005', NOW() - INTERVAL '4 days', 0),
(4006, 2005, 5, 'demo-device-006', NOW() - INTERVAL '3 days', 0),
(4007, 2005, 5, 'demo-device-007', NOW() - INTERVAL '2 days', 0),
(4008, 2005, 5, 'demo-device-008', NOW() - INTERVAL '1 day', 0),
(4009, 3002, 5, 'demo-device-009', NOW() - INTERVAL '4 days', 0),
(4010, 3002, 5, 'demo-device-010', NOW() - INTERVAL '3 days', 0)
ON CONFLICT (id) DO NOTHING;

-- 三期：绘本资源表
CREATE TABLE IF NOT EXISTS picture_book (
    id BIGINT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    alias VARCHAR(100),
    series_id BIGINT NOT NULL,
    age_range VARCHAR(20) NOT NULL,
    cover_url VARCHAR(500) NOT NULL,
    description VARCHAR(500),
    page_count INTEGER NOT NULL DEFAULT 0,
    is_published BOOLEAN NOT NULL DEFAULT FALSE,
    is_abnormal BOOLEAN NOT NULL DEFAULT FALSE,
    abnormal_remark VARCHAR(200),
    click_count BIGINT NOT NULL DEFAULT 0,
    rating_avg NUMERIC(4, 2) NOT NULL DEFAULT 0,
    rating_count INTEGER NOT NULL DEFAULT 0,
    complete_read_count BIGINT NOT NULL DEFAULT 0,
    sort_weight INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted SMALLINT NOT NULL DEFAULT 0
);

-- 三期：绘本页面表
CREATE TABLE IF NOT EXISTS picture_book_page (
    id BIGINT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    page_number INTEGER NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    text_content TEXT,
    audio_url VARCHAR(500),
    duration_sec INTEGER,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    deleted SMALLINT NOT NULL DEFAULT 0
);

-- 三期：绘本相关索引
CREATE INDEX IF NOT EXISTS idx_picture_book_filter
    ON picture_book (is_published, is_abnormal, age_range, series_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_picture_book_click ON picture_book (click_count DESC);
CREATE INDEX IF NOT EXISTS idx_picture_book_rating ON picture_book (rating_avg DESC);
CREATE INDEX IF NOT EXISTS idx_picture_book_page_book_id ON picture_book_page (book_id, page_number);

-- 三期：插入演示绘本数据
INSERT INTO picture_book (id, title, nickname, alias, series_id, age_range, cover_url, description, page_count, is_published, is_abnormal, abnormal_remark, click_count, rating_avg, rating_count, complete_read_count, sort_weight, created_at, updated_at, deleted) VALUES
(5001, '小兔子找萝卜', '兔子找萝卜', '找萝卜', 1004, 'AGE_0_2', 'https://picsum.photos/seed/book1/400/300', '小兔子肚子饿了，它要去找萝卜吃。一路上遇到了很多小动物。', 5, TRUE, FALSE, NULL, 234, 4.8, 45, 156, 100, NOW() - INTERVAL '5 days', NOW(), 0),
(5002, '小猪佩奇的一天', '佩奇的一天', '佩奇日常', 1002, 'AGE_2_4', 'https://picsum.photos/seed/book2/400/300', '跟着佩奇一起度过快乐的一天，从早上起床到晚上睡觉。', 8, TRUE, FALSE, NULL, 567, 4.9, 89, 234, 95, NOW() - INTERVAL '4 days', NOW(), 0),
(5003, '认识颜色', '学颜色', '颜色启蒙', 1007, 'AGE_0_2', 'https://picsum.photos/seed/book3/400/300', '红色、黄色、蓝色...让宝宝认识各种美丽的颜色。', 6, TRUE, FALSE, NULL, 423, 4.7, 67, 178, 90, NOW() - INTERVAL '3 days', NOW(), 0),
(5004, '数字1到5', '学数字', '数字启蒙', 1007, 'AGE_2_4', 'https://picsum.photos/seed/book4/400/300', '通过可爱的图片，让宝宝认识数字1到5。', 5, TRUE, FALSE, NULL, 345, 4.6, 54, 123, 85, NOW() - INTERVAL '2 days', NOW(), 0),
(5005, '晚安月亮', '月亮晚安', '睡前绘本', 1004, 'AGE_0_2', 'https://picsum.photos/seed/book5/400/300', '温馨的睡前故事，和月亮说晚安，和星星说晚安。', 7, TRUE, FALSE, NULL, 789, 5.0, 123, 345, 80, NOW() - INTERVAL '1 day', NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- 三期：插入演示绘本页面数据（小兔子找萝卜）
INSERT INTO picture_book_page (id, book_id, page_number, image_url, text_content, audio_url, duration_sec, created_at, updated_at, deleted) VALUES
(6001, 5001, 1, 'https://picsum.photos/seed/book1p1/800/600', '小兔子肚子饿了，它想吃萝卜。', NULL, NULL, NOW(), NOW(), 0),
(6002, 5001, 2, 'https://picsum.photos/seed/book1p2/800/600', '小兔子出门了，它看到了小鸟。小鸟说：你好呀！', NULL, NULL, NOW(), NOW(), 0),
(6003, 5001, 3, 'https://picsum.photos/seed/book1p3/800/600', '小兔子继续走，它看到了小松鼠。小松鼠在树上吃松果。', NULL, NULL, NOW(), NOW(), 0),
(6004, 5001, 4, 'https://picsum.photos/seed/book1p4/800/600', '小兔子走到菜园，终于找到了大萝卜！', NULL, NULL, NOW(), NOW(), 0),
(6005, 5001, 5, 'https://picsum.photos/seed/book1p5/800/600', '小兔子开心地吃着萝卜，真好吃呀！', NULL, NULL, NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;

-- 三期：插入演示绘本页面数据（小猪佩奇的一天）
INSERT INTO picture_book_page (id, book_id, page_number, image_url, text_content, audio_url, duration_sec, created_at, updated_at, deleted) VALUES
(6006, 5002, 1, 'https://picsum.photos/seed/book2p1/800/600', '早上，太阳升起来了。佩奇醒来了。', NULL, NULL, NOW(), NOW(), 0),
(6007, 5002, 2, 'https://picsum.photos/seed/book2p2/800/600', '佩奇刷牙洗脸，准备吃早餐。', NULL, NULL, NOW(), NOW(), 0),
(6008, 5002, 3, 'https://picsum.photos/seed/book2p3/800/600', '吃完早餐，佩奇和乔治一起玩玩具。', NULL, NULL, NOW(), NOW(), 0),
(6009, 5002, 4, 'https://picsum.photos/seed/book2p4/800/600', '中午，佩奇一家人一起吃午饭。', NULL, NULL, NOW(), NOW(), 0),
(6010, 5002, 5, 'https://picsum.photos/seed/book2p5/800/600', '下午，佩奇去公园玩滑梯。', NULL, NULL, NOW(), NOW(), 0),
(6011, 5002, 6, 'https://picsum.photos/seed/book2p6/800/600', '傍晚，佩奇回家吃晚饭。', NULL, NULL, NOW(), NOW(), 0),
(6012, 5002, 7, 'https://picsum.photos/seed/book2p7/800/600', '晚上，佩奇洗澡，准备睡觉。', NULL, NULL, NOW(), NOW(), 0),
(6013, 5002, 8, 'https://picsum.photos/seed/book2p8/800/600', '佩奇躺在床上，说：晚安！', NULL, NULL, NOW(), NOW(), 0)
ON CONFLICT (id) DO NOTHING;
