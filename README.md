# UIPlayerDifProcess
* 用于不同进程之间，播放器和界面关联的验证；在mediaServerProcess当中随便找两个视频丢到assets\movie路径下打包即可。
* ![image](https://github.com/FreestyleDDCoder/UIPlayerDifProcess/blob/master/resources/relationship.png)
* videoUIProcess
  UI进程，用于播放界面显示，创建SurfaceView
* mediaServerProcess
  媒体进程，用于播放逻辑控制，与接收来自UI进程的Surface
  主要用于剥离界面和播放逻辑之间的关联性。
* ![image](https://github.com/FreestyleDDCoder/UIPlayerDifProcess/blob/master/resources/player.gif)
# LICENSE
Copyright [2021] [Zhan Liang]

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
