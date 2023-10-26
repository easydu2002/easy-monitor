package top.easydu.easymonitor.hik;

import com.sun.media.sound.ModelOscillator;
import org.junit.Before;
import org.junit.Test;
import top.easydu.easymonitor.common.enums.MonitorBrand;
import top.easydu.easymonitor.domain.dto.LoginDTO;
import top.easydu.easymonitor.sdk.MonitorFactory;
import top.easydu.easymonitor.sdk.MonitorSdk;
import top.easydu.easymonitor.sdk.platform.hik.HIKSdk;

import static org.junit.Assert.assertTrue;

public class SdkTest {

    private MonitorSdk hikSdk = MonitorFactory.createMonitorySdk(MonitorBrand.HIK);

    @Before
    public void testSDKInit() {

        assertTrue( "SDK 初始异常！", hikSdk.init());
    }

    @Test
    public void testLogin() {

        LoginDTO dto = new LoginDTO("192.168.0.65", 8000, "admin", "xadc123@");

        assertTrue( "登录异常！", hikSdk.login(dto));

    }

    @Test
    public void listenAlarm() {

        assertTrue( "报警布防失败！", hikSdk.startListenAlarm("192.168.0.65"));

    }


    @Test
    public void testLogout() {

        assertTrue( "退出登录异常！", hikSdk.logout("192.168.0.65"));
    }

}
