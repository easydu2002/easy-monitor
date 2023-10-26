package top.easydu.easymonitor.sdk.platform.hik.callback;

import com.sun.jna.Pointer;
import top.easydu.easymonitor.model.AlarmEventInfo;
import top.easydu.easymonitor.sdk.events.EventDispatcher;
import top.easydu.easymonitor.sdk.events.EventType;
import top.easydu.easymonitor.sdk.platform.hik.HCNetSDK;

import java.io.UnsupportedEncodingException;

public class FMSGCallBack_V31 implements HCNetSDK.FMSGCallBack_V31{
    @Override
    public boolean invoke(int lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) {
        switch (lCommand) {
            case HCNetSDK.COMM_ALARM_V30:  //移动侦测、视频丢失、遮挡、IO信号量等报警信息(V3.0以上版本支持的设备)

                // 报警信息
                HCNetSDK.NET_DVR_ALARMINFO_V30 struAlarmInfo = new HCNetSDK.NET_DVR_ALARMINFO_V30();
                struAlarmInfo.write();
                Pointer pAlarmInfo_V30 = struAlarmInfo.getPointer();
                pAlarmInfo_V30.write(0, pAlarmInfo.getByteArray(0, struAlarmInfo.size()), 0, struAlarmInfo.size());
                struAlarmInfo.read();

                // 报警扩展信息
                HCNetSDK.NET_DVR_ALARMINFO_EX alarminfoEx = new HCNetSDK.NET_DVR_ALARMINFO_EX();
                alarminfoEx.write();
                Pointer pAarminfoEx = alarminfoEx.getPointer();
                pAarminfoEx.write(0, pAlarmInfo.getByteArray(0, alarminfoEx.size()), 0, alarminfoEx.size());
                alarminfoEx.read();


                int type = alarminfoEx.dwAlarmType;
                int channelNumber = alarminfoEx.dwChannel[0];
                AlarmEventInfo info = new AlarmEventInfo(channelNumber, type);
                info.deviceIp = new String(pAlarmer.sDeviceIP).trim();

                EventDispatcher.dispatchEvent(EventType.ALARM, info);
                break;
        }
        return false;
    }
}
