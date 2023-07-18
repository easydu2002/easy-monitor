package top.easydu.easymonitor.module;

import com.netsdk.demo.module.LoginModule;
import com.netsdk.lib.NetSDKLib;
import com.netsdk.lib.ToolKits;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.easydu.easymonitor.MonitorSdk;
import top.easydu.easymonitor.common.enums.AlarmStatus;
import top.easydu.easymonitor.model.AlarmEventInfo;

import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * \if ENGLISH_LANG
 * alarm listen interface
 * contains: start and stop alarm listen
 * \else
 * 报警接口实现
 * 包含 ：向设备订阅报警和停止订阅报警
 * \endif
 */
public class AlarmListenModule {

	private final static Logger logger = LoggerFactory.getLogger(AlarmListenModule.class);

	/**
	 * ip -> 回调函数
	 */
	private final static Map<String, Consumer<AlarmEventInfo>> alarmMap = new ConcurrentHashMap();

	private final static List<AlarmEventInfo> alarmList = new Vector<>();

	/**
	 * 只能这么写了，lambda 表达式要被回收
	 */
	private static class fAlarmDataCB implements NetSDKLib.fMessCallBack{
		@Override
		public boolean invoke(int lCommand, NetSDKLib.LLong lLoginID,
							  Pointer pStuEvent, int dwBufLen, String strDeviceIP,
							  NativeLong nDevicePort, Pointer dwUser) {

			Consumer<AlarmEventInfo> consumer = alarmMap.get(strDeviceIP);
			switch (lCommand) {
				case NetSDKLib.NET_ALARM_ALARM_EX:
				case NetSDKLib.NET_MOTION_ALARM_EX:
				case NetSDKLib.NET_VIDEOLOST_ALARM_EX:
				case NetSDKLib.NET_SHELTER_ALARM_EX:
				case NetSDKLib.NET_DISKFULL_ALARM_EX:
				case NetSDKLib.NET_DISKERROR_ALARM_EX: {
					byte []alarm = new byte[dwBufLen];
					pStuEvent.read(0, alarm, 0, dwBufLen);
					for (int i = 0; i < dwBufLen; i++) {
						if (alarm[i] == 1) {
							AlarmEventInfo alarmEventInfo = new AlarmEventInfo(i, lCommand, AlarmStatus.ALARM_START);
							alarmEventInfo.deviceIp = strDeviceIP;
							if (!alarmList.contains(alarmEventInfo)) {
								alarmList.add(alarmEventInfo);
							}
							if (consumer != null) {
								consumer.accept(alarmEventInfo);
							}
							logger.warn(String.format("监听告警: %s", alarmEventInfo));
						}else {
							AlarmEventInfo alarmEventInfo = new AlarmEventInfo(i, lCommand, AlarmStatus.ALARM_STOP);
							alarmEventInfo.deviceIp = strDeviceIP;
							if (alarmList.contains(alarmEventInfo)) {
								alarmList.remove(alarmEventInfo);
								if (consumer != null) {
									consumer.accept(alarmEventInfo);
								}
								logger.warn(String.format("移除告警: %s", alarmEventInfo));
							}

						}
					}
					break;
				}
				default:
					break;

			}

			return true;
		}

	}

	private final static fAlarmDataCB cbMessage = new fAlarmDataCB();

	/**
	 * \if ENGLISH_LANG
	 * start alarm listen
	 * \else
	 * 向设备订阅报警
	 * \endif
	 */
	public static boolean startListen(String deviceIp, Consumer<AlarmEventInfo> callback) {

		if (alarmMap.get(deviceIp) != null) {
			logger.warn(String.format("device %s Had Subscribe Alarm.", deviceIp));
			return true;
		}

		NetSDKLib.LLong handle = MonitorSdk.getLoginHandle(deviceIp);

		if (handle == null || handle.longValue() == 0) {
			logger.warn(String.format("设备 %s 未登录!", deviceIp));
			return false;
		}

		LoginModule.netsdk.CLIENT_SetDVRMessCallBack(cbMessage, null); // set alarm listen callback


		if (!LoginModule.netsdk.CLIENT_StartListenEx(handle)) {
			logger.warn("CLIENT_StartListenEx Failed!" + ToolKits.getErrorCodePrint());
			return false;
		} else { 
			logger.warn("CLIENT_StartListenEx success.");
		}
		
		alarmMap.put(deviceIp, callback);
		return true;
	}


	/**
	 * \if ENGLISH_LANG
	 * stop alarm listen
	 * \else
	 * 停止订阅报警
	 * \endif
	 */
	public static boolean stopListen(String deviceIp) {

		if (alarmMap.get(deviceIp) == null) {
			return true;
		}

		if (!LoginModule.netsdk.CLIENT_StopListen(MonitorSdk.getLoginHandle(deviceIp))) {
			logger.warn("CLIENT_StopListen Failed!" + ToolKits.getErrorCodePrint());
			return false;
		} else {
			logger.warn("CLIENT_StopListen success.");
		}

		alarmMap.remove(deviceIp);
		return true;
	}



}	
	



