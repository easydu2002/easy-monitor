package top.easydu.easymonitor.sdk.platform.hik.callback;

import com.sun.jna.Pointer;
import top.easydu.easymonitor.sdk.platform.hik.HCNetSDK;

public class FExceptionCallBack implements HCNetSDK.FExceptionCallBack {

    @Override
    public void invoke(int dwType, int lUserID, int lHandle, Pointer pUser) {

        System.out.println("dwType: " + dwType);
    }
}
