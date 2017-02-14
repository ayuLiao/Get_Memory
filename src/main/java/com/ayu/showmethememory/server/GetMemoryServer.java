package com.ayu.showmethememory.server;

import android.accessibilityservice.AccessibilityService;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.ayu.showmethememory.utils.SharedPerferenceUtil;

import java.util.ArrayList;
import java.util.List;

public class GetMemoryServer extends AccessibilityService {
    //微信聊天界面中，item的总个数
    private static final String WECHAT_UI_LIST_COUNT = "WECHAT_UI_LIST_COUNT";
    //窗口节点信息，放置在AccessibilityNodeInfo中，本质是个树型结构
    private List<AccessibilityNodeInfo> parents;
    /**
     * 键盘锁的对象,用于红包软件在手机锁屏（没有密码的情况下）时，点亮屏幕进行解锁
     */
    private KeyguardManager.KeyguardLock kl;

    private boolean getMemory = false;//是否模拟点击进行抢红包
    private boolean isFromNotification  = false;//是否从Notification进入微信聊天界面
    private PowerManager.WakeLock wl;
    private Context context;

    private SharedPerferenceUtil sharedPerferenceUtil;
    private String IS_RUNNING_SERVICE = "is_running_service";
    //是不是第一次进入
    private boolean firstIn = false;
    private boolean isFromChatList  = false;
    private int hongbaoNum = 0;//红包总数
    private String HONBBO_SHU = "hongbao_shu";
    private boolean isnomenory = false;//默认是有钱的

    public GetMemoryServer() {
    }

    /**
     *  当系统连接上你的服务时被调用
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        parents = new ArrayList<>();
        if (context == null) {
            context = getApplicationContext();
        }
        if (sharedPerferenceUtil == null) {
            sharedPerferenceUtil = SharedPerferenceUtil.getInstance(context);
        }
    }

    /**
     *  必须重写的方法：此方法用了接受系统发来的event。
     *  在你注册的event发生是被调用。在整个生命周期会被调用多次。
     */
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (context == null) {
            context = getApplicationContext();
        }
        if (sharedPerferenceUtil == null) {
            sharedPerferenceUtil = SharedPerferenceUtil.getInstance(context);
        }
        //服务是否在运行
        boolean isOpen = sharedPerferenceUtil.getBoolean(IS_RUNNING_SERVICE, true);
        if (!isOpen) {
            return;
        }

        //getEventType()：事件类型
        int eventType = event.getEventType();
        //根据事件回调类型进行处理
        switch (eventType) {
            /**
             * 当通知栏发生改变时，查看通知中是否有微信红包的字样，有就通过通知栏
             * 跳转到相应的界面
             */
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                Log.e("ayu_change", "TYPE_NOTIFICATION_STATE_CHANGED");
                /**
                 * CharSequence就是字符序列,String 继承于CharSequence
                 */
                List<CharSequence> texts = event.getText();
                if (!texts.isEmpty()) {
                    for (CharSequence text : texts) {
                        String content = text.toString();
                        if (content.contains("[微信红包]")) {
                            //如果此时屏幕是锁屏时，没有密码的锁屏
                            boolean isLock = isScreenLocked2();
                            if (isLock) {
                                //解锁
                                Log.e("ayu_u", "解锁444444444444444444444444444444");
                                AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
                                Log.e("auy_u",nodeInfo+"------------"+"不为空ongoing");
                                List<AccessibilityNodeInfo> infos = nodeInfo.findAccessibilityNodeInfosByViewId("com.android.keyguard:id/notification_title_view");
                                if(infos.size()==0||infos==null){
                                    return;
                                }
                                AccessibilityNodeInfo info = infos.get(0);
                                AccessibilityNodeInfo parent = info.getParent();
                                Log.e("auy_u",parent+"------------"+"不为空ongoing");
                                while (parent != null) {
                                    //如父元素也可点击
                                    if (parent.isClickable()) {
                                        Log.e("ayu_u", "5555555555555555555");
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
                                /**
                                 * event.getParcelableData()会得到Notification，通知栏
                                 * instanceof用于判断就是Notification
                                 */
                                if (event.getParcelableData() != null &&
                                        event.getParcelableData() instanceof Notification) {
                                    Notification notification = (Notification) event.getParcelableData();
                                    /**
                                     * PendingIntent 是一种特殊的 Intent ，字面意思可以解释为延迟的 Intent ，
                                     * 用于在某个事件结束后执行特定的 Action 。
                                     * 从上面带 Action 的通知也能验证这一点，当用户点击通知时，才会执行。
                                     *
                                     * 这里就是获得Notification中的PendingIntent，用于跳转到相应的界面
                                     */
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
            //当窗口的状态发生改变时，如出现了微信红包，就可以进行抢红包了
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
        }
    }
    //得到聊天界面中ListView中的最后一项
    private void getListViewLastMemory(AccessibilityEvent event,AccessibilityNodeInfo root){
        List<AccessibilityNodeInfo> wechatList = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a22");
        if (wechatList != null && wechatList.size() > 0) {
            //获得第一个
            AccessibilityNodeInfo accessibilityNodeInfo = wechatList.get(0);
            //获取AccessibilityEvent中Item的数目
            int itemCount = event.getItemCount();

            //获得sharedPerference中旧的数目
            int oldCount = sharedPerferenceUtil.getInteger(WECHAT_UI_LIST_COUNT, itemCount);
            //存入新的数目
            sharedPerferenceUtil.putInteger(WECHAT_UI_LIST_COUNT, itemCount);

            int i = itemCount - oldCount;
            //大于0表示有新信息来
            if (i <= 0) {
                isnomenory = false;
                return;
            }
            //判断最新的数据是否有子元素，这样可以判断是否是红包，因为文字、语言、图片都没有子元素
            AccessibilityNodeInfo nodeInfo = accessibilityNodeInfo.getChild(accessibilityNodeInfo.getChildCount()-1);
            if (nodeInfo == null) {
                return;
            }
            //获取聊天界面的红包id，如果是红包的id，获得领取红包或者查看红包的文字
            List<AccessibilityNodeInfo> getRedMoneyList = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a5u");
            if (getRedMoneyList != null && getRedMoneyList.size() > 0) {
                try {
                    getMemory = true;
                    getLastPacket();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void getListViewLastMemory2(AccessibilityEvent event,AccessibilityNodeInfo root){
        List<AccessibilityNodeInfo> wechatList = root.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a22");
        if (wechatList != null && wechatList.size() > 0) {
            //获得第一个
            AccessibilityNodeInfo accessibilityNodeInfo = wechatList.get(0);
            //判断最新的数据是否有子元素，这样可以判断是否是红包，因为文字、语言、图片都没有子元素
            AccessibilityNodeInfo nodeInfo = accessibilityNodeInfo.getChild(accessibilityNodeInfo.getChildCount()-1);
            if (nodeInfo == null) {
                return;
            }
            //获取聊天界面的红包id，如果是红包的id，获得领取红包或者查看红包的文字
            List<AccessibilityNodeInfo> getRedMoneyList = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/a5u");
            if (getRedMoneyList != null && getRedMoneyList.size() > 0) {
                try {
                    getMemory = true;
                    getLastPacket();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    /**
     * 判断手机屏幕是否锁屏
     * @return
     */
    private boolean isScreenLocked() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isLocked = pm.isScreenOn();
        return isLocked;
    }

    private boolean isScreenLocked2() {
        KeyguardManager mKeyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean flag = mKeyguardManager.inKeyguardRestrictedInputMode();
        return flag;
    }
    /**
     * 通过ID获取控件，并进行模拟点击
     * @param clickId
     */
    private void inputClick(String clickId) {
        /**
         * 如果配置能够获取窗口内容,则会返回当前活动窗口的根结点
         */
        AccessibilityNodeInfo nodeInfo = getRootInActiveWindow();
        if (nodeInfo != null) {
            //找到id对应的控件
            List<AccessibilityNodeInfo> list = nodeInfo.findAccessibilityNodeInfosByViewId(clickId);
            for (AccessibilityNodeInfo item : list) {
                //模拟点击
                item.performAction(AccessibilityNodeInfo.ACTION_CLICK);

            }
        }
    }

    /**
     * 获取List中最后一个红包，并进行模拟点击
     */
    private void getLastPacket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        //传入根节点
        recycle(rootNode,0);
        //点击最后一个红包，进入红包打开界面
        if(parents.size()>0){
            parents.get(parents.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
        }
    }

    /**
     * 回归函数遍历每一个节点，并将含有"领取红包"存进List中
     *
     * @param info
     */
    public void recycle(AccessibilityNodeInfo info,int j) {
        //只有一个子元素
        if (info.getChildCount() == 0) {
            if (info.getText() != null) {
                /**
                 * 查看红包：领取自己发的，领取红包：领取他人发的
                 */
                if ("查看红包".equals(info.getText().toString())||"领取红包".equals(info.getText().toString())) {
                    if (info.isClickable()) {
                        //模拟点击
                        info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                    Log.e("ayu_siez",j+"");
                    //得到父元素
                    AccessibilityNodeInfo parent = info.getParent();
                    while (parent != null) {
                        //如父元素也可点击
                        if (parent.isClickable()) {
                            //就添加到parents中
                            parents.add(parent);
                            break;
                        }
                        //再上一parent
                        parent = parent.getParent();
                    }

                }
            }
        } else {
            // 遍历所有子元素
            for (int i = 0; i < info.getChildCount(); i++) {
                if (info.getChild(i) != null) {
                    recycle(info.getChild(i),i);
                }
            }
        }
    }

    /**
     *  必须重写的方法：系统要中断此service返回的响应时会调用。
     *  在整个生命周期会被调用多次。
     */
    @Override
    public void onInterrupt() {
        if (context == null) {
            context = getApplicationContext();
        }
        if (sharedPerferenceUtil == null) {
            sharedPerferenceUtil = SharedPerferenceUtil.getInstance(context);
        }
    }


    /**
     *  在系统要关闭此service时调用。
     */
    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        startService(new Intent(this, GetMemoryServer.class));
    }
}
