package top.easydu.easymonitor.sdk.platform.hik.common.constants;

import top.easydu.easymonitor.common.enums.BaseEnum;

public enum AlarmType implements BaseEnum {

    SignalLossAlarm(0, "信号量报警"),
    DiskFull(1, "硬盘满"),
    SignalLoss(2, "信号丢失"),
    MotionDetection(3, "移动侦测"),
    UnformattedDisk(4, "硬盘未格式化"),
    ReadWriteDiskError(5, "读写硬盘出错"),
    CoverAlarm(6, "遮挡报警"),
    FormatMismatch(7, "制式不匹配"),
    UnauthorizedAccess(8, "非法访问");

    private int code;
    private String desc;

    AlarmType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getDesc() {
        return desc;
    }
}
