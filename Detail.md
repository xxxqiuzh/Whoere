Descriptions:
Whoere面向用户是一款android App，用户通过注册、登录即可使用。通过设置范围和兴趣标签，就可以和范围内共同兴趣的好友一起聊天。

项目采用CS架构：
基于socket实现信息传输
Client端：负责向Server发送消息，包含用户信息和消息内容。
Server端：负责消息接收，同时根据接收到的消息完成筛选功能，最后完成转发。
筛选功能包括：基于范围筛选和基于兴趣标签筛选。
Client端：还负责处理异常情况，同时实现了简单的用户提示功能，优化用户体验。

项目规划及部署：
Server端部署在阿里云linux云服务器上，用Java实现，所以只要求安装java运行环境环境。
Client端是一款android App，实现了简单美观的用户界面，方便用户使用。

开发环境及工具：
本地开发环境及工具：jre-1.8.0 eclipse-neon Android-Studio
云端环境：linux 14.04 jre-1.8.0
