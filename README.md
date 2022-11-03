# Simple Audio Classification App

## 前言

我的目标是实现一套可增量学习的音频识别系统，包括服务器端和客户端两部分，此项目是安卓客户端的简单实现。

鉴于这是看了半个月《第一行代码》就开始做的项目，有问题全靠Google，中间还因为别的事情中断了一阵子，所以整体代码结构比较混乱，UI也很朴素，可能还有一些未知的bug，现在也大致是一个半成品的状态，距离预想的功能还有一定距离。接下来做一个简单的介绍。

## 功能介绍

这是一个简单的声音识别安卓客户端，目前主要实现的功能包括：

* 录制音频

* 发送到服务器端进行识别，返回结果并进行显示
* 查看以往的识别记录，以及删除播放音频
* 深色模式和浅色模式

部分截图如下：

| <img src="https://my-bed.oss-cn-shanghai.aliyuncs.com/img/202211031549272.jpg" alt="90c6d1c43e5b28b2f99a56a4ec26934" style="zoom:33%;" /> | <img src="https://my-bed.oss-cn-shanghai.aliyuncs.com/img/202211031549162.jpg" alt="c4b5d83df400fa573970db0c8e1f861" style="zoom:33%;" /> | <img src="https://my-bed.oss-cn-shanghai.aliyuncs.com/img/202211031550672.jpg" alt="da5f95c0ec5ffb9b8b8185ca02cb924" style="zoom:33%;" /> |
| ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| <img src="https://my-bed.oss-cn-shanghai.aliyuncs.com/img/202211031555311.png" alt="image-20221103155534221" style="zoom:33%;" /> | <img src="https://my-bed.oss-cn-shanghai.aliyuncs.com/img/202211031555019.png" alt="image-20221103155550918" style="zoom:33%;" /> | <img src="https://my-bed.oss-cn-shanghai.aliyuncs.com/img/202211031556529.png" alt="image-20221103155603426" style="zoom:33%;" /> |

接下来要实现的功能包括

* 完成服务器端的代码（图中出现网络异常是因为服务器端口还未开放）
* 更丰富的音频信息展示
* 音频录制时长、采样率等的设置
* 定时检测
* 等等

## 代码结构

项目总体是按照MVVM架构实现的，采用的是一个Activity多个Fragment的设计思路。

从截图也可以看到，主要就是两个Fragment，`HomeFragment`和`HistoryFragment`。

### HomeFragment

`HomeFragment`位于`HistoryFragment.kt`，主要实现的就是音频的录制，上传和结果呈现。

音频录制部分主要使用的是位于`RecordService.kt`的`RecordService`。

上传主要使用的`Retrofit`。因为使用的是MVVM模式，所以相关的代码在`AudioViewModel.kt`、`Repository.kt`、`AudioClassNetwork.kt`、`ServiceCreator`等多处。

结果呈现包括了数据库的存储，数据库方面使用了Room，相关的代码在`AudioViewModel.kt`、`Repository.kt`、`ClassDao.kt`等处。参考教程：https://www.jianshu.com/p/243a862c5cfe

### HistoryFragment

`HistoryFragment`位于`HistoryFragment.kt`主要实现的就是音频信息和波形的展示。使用的是`RecyclerView`配合自己写的Adapter。

Adapter位于`AudioAdapter.kt`，里面涉及了点击事件、动画等等的操作。

数据库方面涉及的代码上面差不多，在`AudioViewModel.kt`、`Repository.kt`、`ClassDao.kt`等处。