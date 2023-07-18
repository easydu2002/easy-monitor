package top.easydu.easymonitor.common.enums;

import java.util.Arrays;
import java.util.Optional;

public enum AlarmType {


    NET_ALARM_ALARM(0x2101, "外部报警"),
    NET_MOTION_ALARM(0x2102, "动态检测报警"),
    NET_VIDEOLOST_ALARM(0x2103, "视频丢失报警"),
    NET_SHELTER_ALARM(0x2104, "视频遮挡报警"),
    NET_DISKFULL_ALARM(0x2106, "硬盘满报警"),
    NET_DISKERROR_ALARM(0x2107, "坏硬盘报警");

    private int type;

    private String description;

    AlarmType(int type, String description) {
        this.type = type;
        this.description = description;
    }

    public int getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public static AlarmType getByType(Integer type) {
        Optional<AlarmType> alarmCondition = Arrays.stream(AlarmType.values()).filter(_item -> _item.getType() == type).findAny();

        if (alarmCondition.isPresent()) {

            return alarmCondition.get();
        }

        return null;
    }
}
