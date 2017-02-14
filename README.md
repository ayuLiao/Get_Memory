#Get_Meory说明
## 简介
自从微信推出抢红包功能后，市面上相应出现了微信抢红包软件，而且红极一时，你可以在Google上搜索到很多讲解微信红包软件的文章，但是这些文章都如蜻蜓点水，只讲解了最简单的情况下抢红包的实现思路，本篇文章建立在其他微信抢红包软件的文章只上，讲讲比较复杂的场景下快速抢到红包，如手机息屏时抢到红包、屏蔽微信群消失时抢到红包等等

## 效果
首先来看一下实现后的效果，项目名字叫做红包鸟

首先开启红包鸟的辅助功能选项

![开启红包鸟的辅助服务.gif](http://obfs4iize.bkt.clouddn.com/%E5%BC%80%E5%90%AF%E7%BA%A2%E5%8C%85%E9%B8%9F%E7%9A%84%E8%BE%85%E5%8A%A9%E6%9C%8D%E5%8A%A1.gif)

四种情况下的抢红包：
1.不在微信聊天界面收到微信的状态栏提示时，抢红包
![抢通知栏红包.gif](http://obfs4iize.bkt.clouddn.com/%E6%8A%A2%E9%80%9A%E7%9F%A5%E6%A0%8F%E7%BA%A2%E5%8C%85.gif)

2.在当前聊天界面时，抢红包

![抢正在聊天界面中出现的红包.gif](http://obfs4iize.bkt.clouddn.com/%E6%8A%A2%E6%AD%A3%E5%9C%A8%E8%81%8A%E5%A4%A9%E7%95%8C%E9%9D%A2%E4%B8%AD%E5%87%BA%E7%8E%B0%E7%9A%84%E7%BA%A2%E5%8C%85.gif)

3.微信群消息被屏蔽时，抢红包

![抢设置了免打扰的红包.gif](http://obfs4iize.bkt.clouddn.com/%E6%8A%A2%E8%AE%BE%E7%BD%AE%E4%BA%86%E5%85%8D%E6%89%93%E6%89%B0%E7%9A%84%E7%BA%A2%E5%8C%85.gif)

4.没有设置锁屏密码，手机息屏时抢红包

![抢无密码锁屏的红包.gif](http://obfs4iize.bkt.clouddn.com/%E6%8A%A2%E6%97%A0%E5%AF%86%E7%A0%81%E9%94%81%E5%B1%8F%E7%9A%84%E7%BA%A2%E5%8C%85.gif)

## 思路
实现红包鸟，一开始有两个大致的思路，一就是监听微信的网络请求数据，将接受到红包的请求数据辨别出来，然后在进行相应的抢红包操作，后面通过抓包软件抓包发现数据都是加密的，这样是意料之中的事情，毕竟一个国民级应用，在网络数据安全性上还是会有一定的考虑的，不然自己的聊天消息随随便便就被人监听就太欠缺了，所以就有了第二个思路，模拟用户抢红包，就像PC端的按键精灵将用户的鼠标点击都记录下来，生成对应的脚本，然后就可以重复用户点击，我的第一个反应是Monkey（一个用于Android测试的工具），但是Android上有AccessibilityService这个更优雅的解决方式，市面上几乎所有的微信抢红包神器都是使用这个类来实现的

## 讲讲AccessibilityService
AccessibilityService是Google为了让Android系统更加易用，而为用户提供的无障碍辅助服务，该服务主要用于帮助身体有缺陷的用户（残疾、失明）来使用Android系统，它可以监听界面变化的事件，模拟用户的操作

AccessibilityService运行在后台，需要用户手动开启（因为这个功能太吊了），界面中的任何变化都会产生一个事件然后由系统发送给AccessibilityService，我们通过对不同事件的判断，构建AccessibilityNodeInfo类对象来模拟用户的操作，如点击、长按、滑动等等

本篇文章不会讲太多基础知识，大家可以看[你真的理解AccessibilityService吗](http://www.jianshu.com/p/4cd8c109cdfb)这篇文章，或者自己Google更多的相关文章，其实并不难懂

## 模拟抢红包
在抢红包时，用户是怎么抢，红包鸟就怎么抢，它有多种情况，如下

情况一：用户不在微信的聊天界面，而在其他界面，这时微信会有通知栏通知
我所说的微信聊天界面
![微信聊天界面.jpg](http://obfs4iize.bkt.clouddn.com/%E5%BE%AE%E4%BF%A1%E8%81%8A%E5%A4%A9%E7%95%8C%E9%9D%A2.jpg)

那么红包鸟抢红包的步骤就是：
1.通过AccessibilityService服务获知Android通知栏发生了变化
2.获得通知栏中的内容
3.判断是否有“[微信红包]”这四个字
4.如果有就通过状态栏进入微信中相应的聊天界面
5.通过查找界面中“领取红包”或者“查看红包”这几个字来找到红包
6.模拟点击红包，进行领取

情况二：用户在微信聊天界面，这是有这个界面的消息，微信是不会通过通知栏提示的
那么红包鸟抢红包的步骤就是：
1.通过AccessibilityService服务获知微信界面滚动（新消息来了，界面会滚动）
2.判断是不是红包
3.是红包就模拟点击红包，进行领取

情况三：用户在微信列表，并将该群设为消息免扰，那么微信同样不会通过通知栏提示
我所说的微信列表

![微信列表](http://obfs4iize.bkt.clouddn.com/%E5%BE%AE%E4%BF%A1%E5%88%97%E8%A1%A8.png)

那么红包鸟抢红包的步骤就是：
1.通过AccessibilityService服务获知微信界面内容变化
2.判断界面中是否有“[微信红包]”这四个字
3.有则点击微信列表中的这个Item，进入聊天界面
4.通过查找界面中“领取红包”或者“查看红包”这几个字来找到红包
5.模拟点击红包，进行领取

如果你屏蔽了该群的消息，有不在微信列表，红包鸟就抢不到红包

情况四：用户没有设置消息免扰，手机息屏，此时微信通过通知栏来点亮屏幕，提醒用户有新信息

那么红包鸟抢红包的步骤就是：
1.通过AccessibilityService服务获知Android通知栏发生了变化
2.判断通知栏内容是否包含“[微信红包]”这四个字
3.如果有，则双击通知栏，进入微信聊天界面
4.模拟点击，进行抢红包

如果你屏蔽了该群，那么手机息屏时，微信就不会通过通知栏点亮屏幕对用户进行通知，这时红包鸟是抢不到红包的

你可以将红包鸟想象成另外一个你，你能抢的红包，它就能抢而且手速比你快，但是你不能抢的，比如因为屏蔽了群，你在玩其他的东西，你都不知道有红包发过来了，红包鸟也抢不了

## 具体实现步骤

### 准备AccessibilityService类
首先在AndroidManifest文件中声明一个继承AccessibilityService类的服务，如下
```xml
<service
    android:name=".server.GetMemoryServer"
    android:enabled="true"
    android:exported="true"
    android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE">
    <intent-filter>
        <action android:name="android.accessibilityservice.AccessibilityService" />
    </intent-filter>
    <meta-data
        android:name="android.accessibilityservice"
        android:resource="@xml/get_memory_config" />
</service>
```
这个service需要android.permission.BIND_ACCESSIBILITY_SERVICE权限才可以使用，而且还需要将intent-filter中的action设置为android.accessibilityservice.AccessibilityService，对系统意图进行过滤，接着配置meta-data标签，通过xml文件来实现对GetMemoryServer类的配置

上面的代码，处理meta-data标签中的的resource属性可以更改，其他都不能改动，错一个字母，自动的AccessibilityService服务就无法生效

接着看到xml文件中的get_memory_config.xml配置文件
```xml
<?xml version="1.0" encoding="utf-8"?>
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeAllMask"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags=""
    android:canRetrieveWindowContent="true"
    android:description="@string/description"
    android:notificationTimeout="100"
    android:packageNames="com.tencent.mm" />
```
accessibilityEventTypes：表示无障碍服务监听界面中的哪些变化，如窗口打开、滑动、控件焦点变化等
accessibilityFeedbackType：表示反馈方式
canRetrieveWindowContent：无障碍服务是否可以获得窗口中的内容
notificationTimeout：接受事件的间隔时间
packageNames：指定该服务需要监听那个包产生的事件

接着创建一个类来继承AccessibilityService类，实现其中onAccessibilityEvent()方法和onInterrupt()方法，代码如下
```java
public class GetMemoryServer extends AccessibilityService {
	 /**
     *  必须重写的方法：此方法用了接受系统发来的event。
     *  在你注册的event发生是被调用。在整个生命周期会被调用多次。
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

     /**
     *  必须重写的方法：系统要中断此service返回的响应时会调用。
     *  在整个生命周期会被调用多次。
     */
    @Override
    public void onInterrupt() {}
}
```
onAccessibilityEvent()方法用于接收在配置文件中（get_memory_config.xml文件中）设置该服务可以监听的事件，红包鸟大部分逻辑都是在该方法中实现的

更多AccessibilityService的内容可以查阅官方文档[点这里](https://developer.android.com/reference/android/accessibilityservice/AccessibilityServiceInfo.html)

### 情况一和情况四的实现代码
首先在onAccessibilityEvent()方法中获得事件类型
```java
int eventType = event.getEventType();
```

然后通过switch来区分不同的事件，对不同的事件实现不同逻辑

情况一和情况四实现的具体代码如下
```java
case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
    Log.e("ayu_change", "TYPE_NOTIFICATION_STATE_CHANGED");
    List<CharSequence> texts = event.getText();
    if (!texts.isEmpty()) {
        for (CharSequence text : texts) {
            String content = text.toString();
            if (content.contains("[微信红包]")) {
                //如果此时屏幕是锁屏时，没有密码的锁屏
                boolean isLock = isScreenLocked2();
                if (isLock) {
                    //解锁
                    AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                    List<AccessibilityNodeInfo> infos = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.keyguard:id/notification_title_view");
                    if(infos.size()==0||infos==null){
                        return;
                    }
                    AccessibilityNodeInfo info = infos.get(0);
                    AccessibilityNodeInfo parent = info.getParent();
                    while (parent != null) {
                        //如父元素也可点击
                        if (parent.isClickable()) {
                            //就添加到parents中
                            firstIn = true;
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            break;
                        }
                            //再上一parent
                            parent = parent.getParent();
                        }
                        return;
                    }else
                    {
                    if (isFromChatList) {
                        isFromChatList = false;
                        return;
                    }
                    //模拟打开通知栏消息，即打开微信
                              
                    if (event.getParcelableData() != null &&
                            event.getParcelableData() instanceof Notification) {
                        Notification notification = (Notification) event.getParcelableData();
                        
                        PendingIntent pendingIntent = notification.contentIntent;
               			try {
                            getMemory = true;
                            isFromNotification = true;
                            pendingIntent.send();
                            Log.d("ayuLiao", "进入微信");
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.d("ayuLiao", "从Notification跳转失败");
                            }
                        }
                    }
                }
            }
        }
        break;
```
事件类型为TYPE_NOTIFICATION_STATE_CHANGED，表示手机的通知栏有变化，这样就可以通过AccessibilityEvent类的getText()方法获得CharSequence类的数据集，CharSequence就是字符序列,String 继承于CharSequence，也就是获得通知栏中的内容，如果有[微信红包]这几个字，判断通过isScreenLocked2()方法判断手机是否在息屏状态，在没有设置微信无法使用通知栏的情况下，如果手机息屏，微信将会把屏幕点亮，此事需要通过AccessibilityNodeInfo类对象获得这个通知栏，然后调用performAction()方法模拟点击两次，就可以解开首先息屏，进入微信了，如果手机没有息屏，则直接获得Notification类对象，然后同Notification类的contentIntent进入对应的界面

可以通过uiautomatorviewer工具来获得手机界面的布局，这个工具可以从Android Studio的DDMS中开启

![红包DDMS.png](http://obfs4iize.bkt.clouddn.com/%E7%BA%A2%E5%8C%85DDMS.png)

![uiautomatorviewer.png](http://obfs4iize.bkt.clouddn.com/uiautomatorviewer.png)

然后分析手机界面，通过分析获得布局中关键控件的id

![手机息屏获得id.png](http://obfs4iize.bkt.clouddn.com/%E6%89%8B%E6%9C%BA%E6%81%AF%E5%B1%8F%E8%8E%B7%E5%BE%97id.png)

然后使用AccessibilityNodeInfo类的findAccessibilityNodeInfosByViewId()方法通过id来找到相应的控件，这里我们找标题的id，因为每次微信通知栏提醒都会有标题，但是标题不可点击，所以获得其可以点击的父元素，进行双击，破除手机息屏，进入微信

来看isScreenLocked2()方法，使用该方法来判断手机是否息屏
```java
private boolean isScreenLocked2() {
        KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean flag = mKeyguardManager.inKeyguardRestrictedInputMode();
        return flag;
    }
```
如果返回的值为true，表示有有两种状态：a、屏幕是黑的  b、目前正处于解锁状态
如果返回的值为false，表示目前未锁屏

还可以通过PowerManager类或者接收系统锁屏广播的形式来判断手机是否息屏，具体可以参考[Android判断屏幕锁屏的方法总结](http://blog.csdn.net/heroxuetao/article/details/24639203)

当我们通过通知栏进入微信相应的聊天界面时，onAccessibilityEvent()方法就会接收到TYPE_WINDOW_STATE_CHANGED类型的事件，表示窗口类型发生变化，来看一下该类型下的具体代码
```java
			/**
             * 直接中聊天列表界面进入聊天详情界面，不会触发TYPE_WINDOW_STATE_CHANGED
             */
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                Log.e("ayu_change","TYPE_WINDOW_STATE_CHANGED");
                String className = event.getClassName().toString();
                //进入聊天界面
                if (className.equals("com.tencent.mm.ui.LauncherUI")) {
                    //点击最后一个红包
                    Log.e("demo","点击红包");
                    //从Notification进入该界面
                    if(isFromNotification){
                        isFromNotification = false;
                        /**
                         * 如果从Notification进入，就获得界面的中红包的节点，打开最后一个红包则可，
                         * 因为Notification提醒了，就一定有红包
                         */
                        getLastPacket();
                    }
                    if(firstIn){
                        firstIn = false;
                        AccessibilityNodeInfo root = getRootInActiveWindow();
                        getListViewLastMemory2(event, root);
                    }
                } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI")) {
                    //开红包
                    Log.e("demo","开红包");
                    //com.tencent.mm:id/bi3点击红包对应的id，不同版本的微信有所不同
                    AccessibilityNodeInfo Receiveroot = getRootInActiveWindow();

                    List<AccessibilityNodeInfo> infos = Receiveroot.findAccessibilityNodeInfosByText("手慢了，红包派完了");
                    if(infos.size()==0||infos==null){//红包中还有前，有开字
                        if(getMemory) {
                            inputClick("com.tencent.mm:id/bi3");
                            isnomenory = false;
                            hongbaoNum = sharedPerferenceUtil.getInteger(HONBBO_SHU, hongbaoNum);
                            hongbaoNum++;
                            sharedPerferenceUtil.putInteger(HONBBO_SHU,hongbaoNum);
                            getMemory = false;
                        }
                    }else{
                        inputClick("com.tencent.mm:id/bfu");
                        isnomenory = true;
                    }


                } else if (className.equals("com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI")) {
                    //退出红包
                    Log.e("demo","退出红包");
                        inputClick("com.tencent.mm:id/gv");
                }
                break;
```
在该case下，我们判断了3个不同的界面对应的类的名称，以此来区分在哪个界面
我们可以通过一些开发者APP来实现类的查看，这里我使用的是“开发者助手”这款良心APP

com.tencent.mm.ui.LauncherUI：表示聊天窗口，其实聊天列表窗口也是这个类

![得到微信中的类.jpg](http://obfs4iize.bkt.clouddn.com/%E5%BE%97%E5%88%B0%E5%BE%AE%E4%BF%A1%E4%B8%AD%E7%9A%84%E7%B1%BB.jpg)

com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI：表示点击开红包的界面

![得到微信中的类2](http://obfs4iize.bkt.clouddn.com/%E5%BE%97%E5%88%B0%E5%BE%AE%E4%BF%A1%E4%B8%AD%E7%9A%84%E7%B1%BB2.jpg)

com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI：表示开完红包后的详情界面

![得到微信中的类1](http://obfs4iize.bkt.clouddn.com/%E5%BE%97%E5%88%B0%E5%BE%AE%E4%BF%A1%E4%B8%AD%E7%9A%84%E7%B1%BB1.jpg)

对应不同的界面，要做不同的逻辑处理，因为每次界面变换都会触发TYPE_WINDOW_STATE_CHANGED类型的事件，所以将这些逻辑都写在这个case内

这里简单来讲讲其中的逻辑
如果类名为com.tencent.mm.ui.LauncherUI，那么就有两种可能，一是从Notification通知栏进入该界面的，二是从聊天列表界面进入的，所以需要分开来处理，从通知栏直接进入的，说明一定是有新红包了，所以就调用getLastPacket()方法模拟点击该界面中最后也就是最新的红包，如果是从聊天列表中进入的，就通过firstIn来判断是否是红包鸟模拟点击进入的还是用户自己进入的，如果红包鸟模拟点击进入的就调用getListViewLastMemory2()方法，该方法会判断聊天界面中最新的内容是否为红包，如果是，再次调用getLastPacket()方法模拟点击红包

模拟点击完红包，就会进入的界面的类名就为com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyReceiveUI，进入该界面，同样会有两种情况，一是红包抢完了，可以通过查找界面中是否拥有“手慢了，红包派完了”的字眼，如果有就说明红包已经抢完了，调用inputClick()方法，传入关闭控件的id，模拟点击该控件，如果有红包，就通过id找到开红包的控件，然后将红包的数量添加到sharedPerference中，用于显示红包鸟总共帮用户抢到多少个红包，这里获得控件id的方式更上面一样

![手慢了](http://obfs4iize.bkt.clouddn.com/%E6%89%8B%E6%85%A2%E4%BA%86.png)

![分析红包布局.png](http://obfs4iize.bkt.clouddn.com/%E5%88%86%E6%9E%90%E7%BA%A2%E5%8C%85%E5%B8%83%E5%B1%80.png)

如果有钱，那么抢完红包就会进入类名就为出com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI的界面，同样通过id找到退出该界面的控件模拟点击则可

### 情况二对应的代码
因为本身就在聊天界面中，微信是不会再通过通知栏来提示用户有新消息，所以我就需要直接通过GetMemoryServer服务来监听界面滚动事件，因为聊天时，每次有新的内容出现，都会有滚动事件的发生，先看具体的代码

```java
//窗口滚动时
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                Log.e("ayu_change","TYPE_VIEW_SCROLLED");
                AccessibilityNodeInfo root = getRootInActiveWindow();
                if(root==null){
                    return;
                }
                //获得聊天界面的返回按钮
                List<AccessibilityNodeInfo> exitNodeInfos = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gf");
                //确认该界面是聊天界面
                if (exitNodeInfos != null && exitNodeInfos.size() > 0) {
                    getListViewLastMemory(event, root);
                    if(firstIn){
                        firstIn = false;
                        getListViewLastMemory2(event, root);
                    }
                }
                break;
```
在onAccessibilityEvent()方法监听到界面滚动事件时，首先判断是否在微信聊天界面，这里不可以通过上面类名的方式来判断，因为聊天列表和聊天界面的类名是相同的，这里通过判断该界面中是否存在返回键来判断此界面是微信聊天界面

![判断在聊天界面.png](http://obfs4iize.bkt.clouddn.com/%E5%88%A4%E6%96%AD%E5%9C%A8%E8%81%8A%E5%A4%A9%E7%95%8C%E9%9D%A2.png)

如果是聊天界面，就调用getListViewLastMemory()方法，在该方法中，会判断屏幕滑动事件中的Item数，如果Item数增大了，说明有新消息来了，判断该消息是否为红包，如果是就调用getLastPacket()方法进行抢红包，如果没变，可能就是用户在翻看前面的聊天信息，此时不做处理

这样还通过firstIn变量来判断是否是红包鸟模拟点击聊天列表进来的，因为用户可能屏蔽了群消息，这样就只能判断聊天列表中是否有相应的提示来进入聊天界面，此时屏幕滑动事件中的Item数可能不会改变了，所以调用getListViewLastMemory2()方法来获得红包，点击红包后就会进入TYPE_WINDOW_STATE_CHANGED，然后跟上面代码一样，点击开红包和退出

### 情况三对应的代码
如果用户设置该群为免打扰，那么就只能判断聊天列表中是否有“微信红包”的字眼来判断了，每次界面内容改变时，都对界面的内容搜索一次，以此来判断，具体代码如下

```java
case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                Log.e("ayu_change","TYPE_WINDOW_CONTENT_CHANGED");
                AccessibilityNodeInfo Listroot = getRootInActiveWindow();
                if(Listroot==null){
                    return;
                }
                if(isnomenory){
                    return;
                }
                //获得聊天界面的返回按钮
                List<AccessibilityNodeInfo> back = Listroot.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/gf");
                //确认该界面是聊天界面
                if (back != null && back.size() > 0) {
                    return;
                }

                //获得聊天列表中的ListView
                List<AccessibilityNodeInfo> chatListInfos = Listroot.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/bjx");
                //判断是否在聊天列表
                if (chatListInfos != null && chatListInfos.size() > 0) {
                    //获得第一个
                    AccessibilityNodeInfo nodeInfo = chatListInfos.get(0);
                    //获得聊天列表中详细内容的信息
                    List<AccessibilityNodeInfo> getRedMoneyList = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/afc");
                    for(AccessibilityNodeInfo info : getRedMoneyList){
                        if(info.toString().contains("[微信红包]")){
                            AccessibilityNodeInfo parent = info.getParent();
                            while (parent != null) {
                                //如父元素也可点击
                                if (parent.isClickable()) {
                                    //就添加到parents中
                                    firstIn = true;
                                    isFromChatList = true;
//                                    SystemClock.sleep(200);
                                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    break;
                                }
                                //再上一parent
                                parent = parent.getParent();
                            }
                        }
                    }
                }
                break;
```
界面内容改变会触发TYPE_WINDOW_CONTENT_CHANGED类型的事件，这里只监听聊天列表中的内容是否改变，因为就算用户屏蔽了该群，相应的信息还是会显示聊天列表中，但是这里还需要考虑，就是用户没有屏蔽该群，这里依旧会监听到内容中有“微信红包”而进行模拟点击，但是因为用户没有屏蔽，所以还会有通知栏通知，通过打印可知，内容改变的事件先被接受，但是为了避免最新的红包被抢两次，所以要通过一些变量进行判断

上段代码的核心逻辑其实非常简单，先判断界面是否是聊天界面，通过该界面是否包含特定的ListView控件来判断

![判断聊天列表中的ListView.png](http://obfs4iize.bkt.clouddn.com/%E5%88%A4%E6%96%AD%E8%81%8A%E5%A4%A9%E5%88%97%E8%A1%A8%E4%B8%AD%E7%9A%84ListView.png)

接着通过ID获得聊天列表中不同Item中的内容

![聊天列表的内容.png](http://obfs4iize.bkt.clouddn.com/%E8%81%8A%E5%A4%A9%E5%88%97%E8%A1%A8%E7%9A%84%E5%86%85%E5%AE%B9.png)

判断内容中是否包含了“[微信红包]”，如果包含，就找到该内容可以点击的父元素，进行模拟点击，这样就可以进入聊天界面中了，新的内容会让界面滚动，而触发TYPE_VIEW_SCROLLED类型的事件，所以后面的逻辑又回到TYPE_VIEW_SCROLLED这个case中

## 总结
到这里核心的逻辑已经讲解完了，其实这个项目不难，就是需要耐心分析微信的界面和微信对用户不同的操作会有什么做法

对于AccessibilityService产生的不同事件最好都写上相应的Log提示，这样在编写项目时会清楚这些监听事件的触发顺序
