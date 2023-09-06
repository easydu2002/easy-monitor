package top.easydu.easymonitor.model;

import top.easydu.easymonitor.common.enums.AlarmStatus;

import java.util.Date;

public class AlarmEventInfo {
    public static long index = 0;
    public long id;
    /**
     * 报警通道
     */
    public int chn;
    /**
     * 报警类型
     */
    public int type;
    /**
     * 报警日期
     */
    public Date date;
    /**
     * 报警状态
     */
    public AlarmStatus status;

    /**
     * 报警设备IP
     */
    public String deviceIp;

    public AlarmEventInfo(int chn, int type, AlarmStatus status) {
        this.chn = chn;
        this.type = type;
        this.status = status;
        this.date = new Date();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlarmEventInfo showInfo = (AlarmEventInfo) o;
        return chn == showInfo.chn && type == showInfo.type;
    }

    @Override
    public String toString() {
        return "AlarmEventInfo{" +
                "id=" + id +
                ", chn=" + chn +
                ", type=" + type +
                ", date=" + date +
                ", status=" + status +
                ", deviceIp=" + deviceIp +
                '}';
    }
}
