# small-car

> 智能小车控制app的cordova工程, 用来打包html页面到app

## 构建

``` bash

首先要安装nodejs

# 安装cordova
npm install -g cordova

# 添加依赖
npm install

# 添加 android平台
cordova platform add android

# 安装插件
cordova plugin add ../my-plugin/bluetoothSerial
cordova plugin add ../my-plugin/wifiManage

# 导入android studio
将生成的 platforms/android 目录导入到android studio中



```
