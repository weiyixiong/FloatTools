# FloatTools
  一个不依赖电脑不需要root的手机端界面调试工具，类似于iOS的FLEX插件。
#功能
1.在任意一个Activity内进行View级拖动，并且查看且View的各属性。

![image](https://github.com/weiyixiong/FloatTools/blob/master/gif/view_debug.gif)

2.捕捉当前App的Logcat

![image](https://github.com/weiyixiong/FloatTools/blob/master/gif/logcat.gif)

3.为插件设置TriggerEvent。 点击插件上按钮触发预置事件

![image](https://github.com/weiyixiong/FloatTools/blob/master/gif/trigger_event.gif)


4.录制点击和编辑事件，可以回放，并且设置成每次App启动时自动播放，解决每次修改代码后需要重新跳转到开发界面的情况。
录制
![image](https://github.com/weiyixiong/FloatTools/blob/master/gif/record2.gif)

回放
![image](https://github.com/weiyixiong/FloatTools/blob/master/gif/replay2.gif)


### Download
***
Gradle:
``` groovy
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

	dependencies {
	    compile 'com.github.weiyixiong:FloatTools:1.4.3'
	}
```

### 使用方法
```
 FloatTools.init(app);
```
####配置
```
 FloatTools.setConfig(new FloatConfig.Builder().setShowLogCatWindow(false).setTriggerEnabled(false).....create());
```
### License
***
```
Copyright 2016 Blankj

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

	http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.