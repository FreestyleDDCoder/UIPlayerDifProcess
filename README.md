# UIPlayerDifProcess
* ![image](https://github.com/FreestyleDDCoder/UIPlayerDifProcess/blob/master/resources/relationship.png)
* 用于不同进程之间，播放器和界面关联的验证；在mediaServerProcess当中随便找两个视频丢到assets\movie路径下打包即可。
* videoUIProcess
  UI进程，用于播放界面显示，创建SurfaceView
* mediaServerProcess
  媒体进程，用于播放逻辑控制，与接收来自UI进程的Surface
  主要用于剥离界面和播放逻辑之间的关联性。
* ![image](https://github.com/FreestyleDDCoder/UIPlayerDifProcess/blob/master/resources/player.gif)
