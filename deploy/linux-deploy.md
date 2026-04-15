# Linux 单机部署步骤

## 1. 安装环境
```bash
sudo apt update
sudo apt install -y openjdk-17-jdk nginx postgresql postgresql-contrib nodejs npm
```

## 2. 初始化数据库
```bash
sudo -u postgres psql -c "CREATE DATABASE bb_play;"
sudo -u postgres psql -d bb_play -f /opt/bb-play/backend/sql/init.sql
```

## 3. 构建后端
```bash
mvn -f /opt/bb-play/backend/pom.xml -DskipTests package
```

## 4. 构建前端
```bash
npm --prefix /opt/bb-play/frontend install
npm --prefix /opt/bb-play/frontend run build
```

## 5. 配置 Nginx
```bash
sudo cp /opt/bb-play/deploy/nginx-bb-play.conf /etc/nginx/sites-available/bb-play.conf
sudo ln -sf /etc/nginx/sites-available/bb-play.conf /etc/nginx/sites-enabled/bb-play.conf
sudo nginx -t
sudo systemctl restart nginx
```

## 6. 配置后端服务
```bash
sudo cp /opt/bb-play/deploy/bb-play-backend.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable bb-play-backend
sudo systemctl restart bb-play-backend
```
