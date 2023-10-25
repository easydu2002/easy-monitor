package top.easydu.easymonitor;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for simple App.
 */
public class LoginModuleTest {

    @Before
    public void testSDKInit() {
        assertTrue( "SDK 初始异常！", MonitorSdk.init());
    }

    @Test
    public void testLogin() {
        assertTrue("登录异常", MonitorSdk.login("192.168.0.121", 37777, "admin", "dahua123@dc"));

    }

    @Test
    public void testLogout() {
        assertTrue("退出登录错误", MonitorSdk.logout("192.168.0.121"));
    }
}
