package top.easydu.easymonitor.sdk.platform.hik;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.easydu.easymonitor.domain.dto.LoginDTO;
import top.easydu.easymonitor.module.AlarmListenModule;
import top.easydu.easymonitor.sdk.MonitorSdk;
import top.easydu.easymonitor.sdk.platform.hik.callback.FExceptionCallBack;
import top.easydu.easymonitor.sdk.platform.hik.callback.FMSGCallBack_V31;
import top.easydu.easymonitor.utils.OsSelect;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HIKSdk implements MonitorSdk {

    private final static Logger log = LoggerFactory.getLogger(HIKSdk.class);

    private static HIKSdk instance = null;
    private static HCNetSDK hCNetSDK = null;

    /**
     * 设备 IP -> 用户句柄
     */
    private static Map<String, Integer> loginMap = new ConcurrentHashMap<>();

    /**
     * 设备 IP -> 报警句柄
     */
    private static Map<String, Integer> alarmMap = new ConcurrentHashMap<>();

    private static final HCNetSDK.FMSGCallBack_V31 fmsgCallBack_v31 = new FMSGCallBack_V31();

    private static final HCNetSDK.FExceptionCallBack fExceptionCallBack = new FExceptionCallBack();


    /**
     * 动态库加载
     *
     * @return
     */
    private static boolean createSDKInstance() {
        if (hCNetSDK == null) {
            synchronized (HCNetSDK.class) {
                String strDllPath = "";
                try {
                    if (OsSelect.isWindows())
                        //win系统加载库路径
                        strDllPath = ".\\lib\\hik\\HCNetSDK.dll";
                    else if (OsSelect.isLinux())
                        //Linux系统加载库路径
                        strDllPath = ".\\lib\\libhcnetsdk.so";

                    log.info("strDllPath", strDllPath);
                    hCNetSDK = (HCNetSDK) Native.load(strDllPath, HCNetSDK.class);
                } catch (Exception ex) {
                    log.warn("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    public synchronized static MonitorSdk getInstance() {
        if (instance == null) {
            return (instance = new HIKSdk());
        }
        return instance;
    }

    public boolean init() {
        if (hCNetSDK == null) {
            if (!createSDKInstance()) {
                log.warn("Load SDK fail");
                return false;
            }
        }
        //linux系统建议调用以下接口加载组件库
        if (OsSelect.isLinux()) {
            HCNetSDK.BYTE_ARRAY ptrByteArray1 = new HCNetSDK.BYTE_ARRAY(256);
            HCNetSDK.BYTE_ARRAY ptrByteArray2 = new HCNetSDK.BYTE_ARRAY(256);
            //这里是库的绝对路径，请根据实际情况修改，注意改路径必须有访问权限
            String strPath1 = "./lib/hik/libcrypto.so.1.1";
            String strPath2 = "./lib/hik//libssl.so.1.1";

            System.arraycopy(strPath1.getBytes(), 0, ptrByteArray1.byValue, 0, strPath1.length());
            ptrByteArray1.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArray1.getPointer());

            System.arraycopy(strPath2.getBytes(), 0, ptrByteArray2.byValue, 0, strPath2.length());
            ptrByteArray2.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(4, ptrByteArray2.getPointer());

            String strPathCom = "./lib/hik/";
            HCNetSDK.NET_DVR_LOCAL_SDK_PATH struComPath = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
            System.arraycopy(strPathCom.getBytes(), 0, struComPath.sPath, 0, strPathCom.length());
            struComPath.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(2, struComPath.getPointer());
        }

        boolean result = hCNetSDK.NET_DVR_Init();

        this.initCallback();
        
        if (!result) {
            log.error("海康 SDK 初始化错误，错误码: " + hCNetSDK.NET_DVR_GetLastError());
        }

        log.info("海康 SDK 初始化成功!");

        return result;
    }

    private void initCallback() {

        /**
         * 异常回调
         */
        boolean errDbResult = hCNetSDK.NET_DVR_SetExceptionCallBack_V30(0, 0, fExceptionCallBack, Pointer.NULL);
        if (!errDbResult) {
            log.warn("异常回调设置失败! 错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }

        // 报警回调
        boolean alarmCbResult = hCNetSDK.NET_DVR_SetDVRMessageCallBack_V50(0, fmsgCallBack_v31, Pointer.NULL);
        if (!alarmCbResult) {
            log.warn("设置布防回调失败! 错误码：" + hCNetSDK.NET_DVR_GetLastError());
        }

//        hCNetSDK.NET_DVR_SetDVRMessage()
    }

    @Override
    public boolean login(LoginDTO dto) {
        Integer _lUser = loginMap.get(dto.getIp());
        if (_lUser != null && _lUser != -1) {
            log.warn("设备已登录");
            return true;
        }
        //注册
        HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();//设备登录信息
        HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();//设备信息

        String m_sDeviceIP = dto.getIp();//设备ip地址
        m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
        System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, m_sDeviceIP.length());

        String m_sUsername = dto.getUsername();//设备用户名
        m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
        System.arraycopy(m_sUsername.getBytes(), 0, m_strLoginInfo.sUserName, 0, m_sUsername.length());

        String m_sPassword = dto.getPassword();//设备密码
        m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
        System.arraycopy(m_sPassword.getBytes(), 0, m_strLoginInfo.sPassword, 0, m_sPassword.length());

        m_strLoginInfo.wPort = (short) dto.getPort().intValue();
        m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
        m_strLoginInfo.byLoginMode = 0;  //ISAPI登录
        m_strLoginInfo.write();

        Integer lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
        loginMap.put(dto.getIp(), lUserID);
        if (lUserID == -1) {
            log.warn("登录失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return false;
        } else {
            log.info(String.format("设备 %s 登录成功！", dto.getIp()));
            return true;
        }
    }

    @Override
    public boolean logout(String ip) {
        Integer lUserId = loginMap.get(ip);
        if (lUserId == null) {
            log.info(String.format("设备 %s 未登录", ip));
            return true;
        }

        stopListenAlarm(ip);

        boolean result = hCNetSDK.NET_DVR_Logout(lUserId);
        loginMap.remove(ip);
//        log.info(String.format("设备 %s 退出登录成功！", ip));
        return result;
    }

    @Override
    public boolean startListenAlarm(String ip) {
        Integer lUserID = loginMap.get(ip);
        if (lUserID == null) {
            log.warn("未登录");
            return false;
        }
        Integer handle = alarmMap.get(ip);
        if (handle != null) {
            log.warn("设备已经布防");
            return true;
        }

        //报警布防参数设置
        HCNetSDK.NET_DVR_SETUPALARM_PARAM_V50 m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM_V50();
        m_strAlarmInfo.dwSize = m_strAlarmInfo.size();
        m_strAlarmInfo.byLevel = 1;  //布防等级
        m_strAlarmInfo.byAlarmInfoType = 1;   // 智能交通报警信息上传类型：0- 老报警信息（NET_DVR_PLATE_RESULT），1- 新报警信息(NET_ITS_PLATE_RESULT)
        m_strAlarmInfo.byDeployType = 1;   //布防类型 0：客户端布防 1：实时布防
        m_strAlarmInfo.write();

        int lAlarmHandle = hCNetSDK.NET_DVR_SetupAlarmChan_V50(lUserID, m_strAlarmInfo, Pointer.NULL, 0);
        if (lAlarmHandle == -1) {
            log.warn("布防失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
            return false;
        }

        alarmMap.put(ip, lAlarmHandle);
        return true;
    }

    @Override
    public boolean stopListenAlarm(String ip) {
        Integer lAlarmHandle = alarmMap.get(ip);
        if (lAlarmHandle == null) {
            return true;
        }
        alarmMap.remove(ip);
        return hCNetSDK.NET_DVR_CloseAlarmChan(lAlarmHandle);
    }
}
