package top.easydu.easymonitor;

import com.netsdk.demo.module.LoginModule;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import com.netsdk.lib.NetSDKLib.LLong;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Hello world!
 *
 */
public class MonitorSdk {

    private final static Logger logger = LoggerFactory.getLogger(MonitorSdk.class);

    private final static Map<String, LLong> loginHandleMap = new ConcurrentHashMap();

    // 设备信息
    private final static Map<String, NetSDKLib.NET_DEVICEINFO_Ex> deviceInfoExMap = new ConcurrentHashMap<>();

    public static LLong getLoginHandle(String deviceIp) {
        return loginHandleMap.get(deviceIp);
    }

    private static Map<String, LLong> getLoginHandleAll() {
        return loginHandleMap;
    }
    private static NetSDKLib.NET_DEVICEINFO_Ex getDeviceInfo(String deviceIp) {
        return deviceInfoExMap.get(deviceIp);
    }

    /**
     * 初始化 sdk
     */
    public static boolean init() {

        boolean success = LoginModule.init(

                (lLong, ip, port, pointer) -> {
                    logger.warn(String.format("Device[%s] Port[%d] DisConnect!", ip, port));
                },

                (lLong, ip, port, pointer) -> {

                    logger.warn(String.format("ReConnect Device[%s] Port[%d]", ip, port));
                }
        );

        if (success) {
            logger.info("SDK initialization success!");
        } else {
            logger.info("SDK initialization failure! %s", LoginModule.netsdk.CLIENT_GetLastError());
        }

        return success;
    }

    /**
     * 登录设备
     * @param m_strIp 设备IP
     * @param m_nPort 设备 tcp 端口
     * @param m_strUser 用户名
     * @param m_strPassword 密码
     * @return
     */
    public static boolean login(String m_strIp, int m_nPort, String m_strUser, String m_strPassword) {

        LLong lLong = loginHandleMap.get(m_strIp);

        if (lLong != null && lLong.longValue() != 0) {
            logger.warn(String.format("设备 %s 重复登录!", m_strIp));
            return true;
        }

        NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY pstInParam = new NetSDKLib.NET_IN_LOGIN_WITH_HIGHLEVEL_SECURITY();
        pstInParam.nPort = m_nPort;
        pstInParam.szIP = m_strIp.getBytes();
        pstInParam.szPassword = m_strPassword.getBytes();
        pstInParam.szUserName = m_strUser.getBytes();

        //出参
        NetSDKLib.NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY pstOutParam = new NetSDKLib.NET_OUT_LOGIN_WITH_HIGHLEVEL_SECURITY();

        // 设备信息
        pstOutParam.stuDeviceInfo = new NetSDKLib.NET_DEVICEINFO_Ex();

        LLong m_hLoginHandle = LoginModule.netsdk.CLIENT_LoginWithHighLevelSecurity(pstInParam, pstOutParam);
        if(m_hLoginHandle.longValue() == 0) {
            logger.warn(String.format("Login Device[%s] Port[%d]Failed. %s", m_strIp, m_nPort, ToolKits.getErrorCodePrint()));
        } else {
            loginHandleMap.put(m_strIp, m_hLoginHandle);
            deviceInfoExMap.put(m_strIp, pstOutParam.stuDeviceInfo);
            logger.warn(String.format("Login Success [ " + m_strIp + " ]"));
        }

        return m_hLoginHandle.longValue() == 0 ? false : true;

    }

    /**
     * 退出登录
     * @param ip 设备IP
     * @return
     */
    public static boolean logout(String ip) {

        LLong lLong = loginHandleMap.get(ip);
        if(lLong == null || lLong.longValue() == 0) {
            logger.warn(String.format("设备%s 未登录！", ip));
            return false;
        }

        boolean bRet = LoginModule.netsdk.CLIENT_Logout(lLong);

        if(bRet) {
            lLong.setValue(0);
            logger.warn(String.format("设备%s 退出登录！", ip));
        }
        return bRet;
    }

    /**
     * 登出所有
     */
    public static void logoutAll() {
        loginHandleMap.keySet().forEach(ip -> MonitorSdk.logout(ip));
    }

}
