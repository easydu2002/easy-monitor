package top.easydu.easymonitor.hik;

import top.easydu.easymonitor.common.enums.MonitorBrand;
import top.easydu.easymonitor.domain.dto.LoginDTO;
import top.easydu.easymonitor.model.AlarmEventInfo;
import top.easydu.easymonitor.sdk.MonitorFactory;
import top.easydu.easymonitor.sdk.MonitorSdk;
import top.easydu.easymonitor.sdk.events.Event;
import top.easydu.easymonitor.sdk.events.EventDispatcher;
import top.easydu.easymonitor.sdk.events.EventType;
import top.easydu.easymonitor.sdk.events.listener.AlarmListener;
import top.easydu.easymonitor.sdk.events.listener.EventListener;
import top.easydu.easymonitor.sdk.platform.hik.common.constants.AlarmType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import static org.junit.Assert.assertTrue;

public class MainTest {

    public static void main(String[] args) throws IOException {

        MonitorSdk hikSdk = MonitorFactory.createMonitorySdk(MonitorBrand.HIK);
        hikSdk.init();

        String testIp = "192.168.0.65";

        LoginDTO dto = new LoginDTO(testIp, 8000, "admin", "xadc123@");

        hikSdk.login(dto);

        hikSdk.startListenAlarm(testIp);

        hikSdk.logout(dto.getIp());
        hikSdk.login(dto);

        hikSdk.login(new LoginDTO("192.168.0.63", 8000, "admin", "xadc123@"));

        hikSdk.startListenAlarm(testIp);
        new Thread(() -> {
            while (true) {

            }
        }).start();
        EventDispatcher.addEventListener(EventType.ALARM, new AlarmListener() {
            @Override
            public void on(Event<AlarmEventInfo> event) {
                System.out.println("告警："+ event.getEventData());
            }
        });
    }
}
