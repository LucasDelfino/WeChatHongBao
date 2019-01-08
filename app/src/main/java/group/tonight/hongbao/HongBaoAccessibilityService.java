package group.tonight.hongbao;

import android.accessibilityservice.AccessibilityService;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.socks.library.KLog;

import java.util.List;

public class HongBaoAccessibilityService extends AccessibilityService {
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private static final String TAG = HongBaoAccessibilityService.class.getSimpleName();
    //聊天列表
    private static final String LAYOUT_CHAT_LIST = "com.tencent.mm.ui.LauncherUI";
    //拆红包界面
    private static final String LAYOUT_OPEN_RED_BAG = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI";
    //红包详情
    private static final String LAYOUT_RED_BAG_DETAIL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";

    private static final String LAYOUT_UNKNOW = "com.tencent.mm.ui.base.p";

    private String mCurrentActivityName;
    private boolean mOpenningRedBag;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        CharSequence className = event.getClassName();
        int itemCount = event.getItemCount();
        if (className != null) {
            if (className.toString().contains("com.tencent.mm")) {
                KLog.e("onAccessibilityEvent: " + eventType + " " + className);
                mCurrentActivityName = className.toString();
            }
        }

        boolean hasRedBag = hasRedBag();
        KLog.e("onAccessibilityEvent: 有红包--->" + hasRedBag);
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                KLog.e("onAccessibilityEvent: TYPE_WINDOW_STATE_CHANGED");

                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
//                KLog.e( "onAccessibilityEvent: TYPE_WINDOW_CONTENT_CHANGED");
                KLog.e("onAccessibilityEvent: 有红包--->" + hasRedBag);

                if (mCurrentActivityName != null) {
                    final AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
                    switch (mCurrentActivityName) {
                        case LAYOUT_CHAT_LIST:
                            if (mOpenningRedBag) {
                                return;
                            }
                            if (hasRedBag()) {
                                List<AccessibilityNodeInfo> nodeInfosByViewId = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ao4");//点开红包弹窗
                                if (!nodeInfosByViewId.isEmpty()) {
                                    KLog.e("点击最后一个未拆开过的红包");
                                    nodeInfosByViewId.get(nodeInfosByViewId.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    mOpenningRedBag = true;
                                }
                            }
                            break;
                        case LAYOUT_OPEN_RED_BAG:
                            for (AccessibilityNodeInfo nodeInfo : rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/cv0")) {//拆开红包
                                if (nodeInfo.isClickable() && nodeInfo.getClassName().equals("android.widget.Button")) {
                                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    KLog.e("onAccessibilityEvent: 点击拆开红包按钮");
                                    break;
                                }
                            }
                            break;
                        case LAYOUT_RED_BAG_DETAIL:
                            mOpenningRedBag = false;
                            for (AccessibilityNodeInfo nodeInfo : rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/k4")) {//返回
                                if (nodeInfo.isClickable()) {
                                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    break;
                                }
                            }
                            break;
                        default:
                            break;
                    }
                }
                break;
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
//                KLog.e( "onAccessibilityEvent: TYPE_NOTIFICATION_STATE_CHANGED");
                break;
            default:
                break;
        }

    }

    private boolean hasRedBag() {
        AccessibilityNodeInfo rootInActiveWindow = getRootInActiveWindow();
        if (rootInActiveWindow == null) {
            return false;
        }
        //每个聊天联系人item
        //红包布局，包含红包图标、恭喜发财、已被领完
        List<AccessibilityNodeInfo> nodeInfoList = rootInActiveWindow.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ao4");
        if (!nodeInfoList.isEmpty()) {
            AccessibilityNodeInfo nodeInfo = nodeInfoList.get(nodeInfoList.size() - 1);//最后一个item

            List<AccessibilityNodeInfo> infoList = nodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mm:id/ape");//已被领完
            return infoList.isEmpty();
        }
        return false;
    }

    @Override
    public void onInterrupt() {

    }
}