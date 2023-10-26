package top.easydu.easymonitor.sdk;

import top.easydu.easymonitor.common.enums.MonitorBrand;
import top.easydu.easymonitor.sdk.platform.hik.HIKSdk;

public class MonitorFactory {

    /**
     * 创建 sdk 实例
     * @param monitorBrand
     * @return
     */
    public static MonitorSdk createMonitorySdk(MonitorBrand monitorBrand) {

        if (monitorBrand == MonitorBrand.DaHua) {

            return null;
        } else if (monitorBrand == MonitorBrand.HIK){
            return HIKSdk.getInstance();
        } else {
            return null;
        }
    }

}
