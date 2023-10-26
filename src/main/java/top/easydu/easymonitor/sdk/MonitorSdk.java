package top.easydu.easymonitor.sdk;

import top.easydu.easymonitor.domain.dto.LoginDTO;

public interface MonitorSdk {

    /**
     * SDK 初始化
     */
    boolean init();


    /**
     * 登录
     * @param dto
     * @return
     */
    boolean login(LoginDTO dto);

    /**
     * 退出登录
     * @param ip
     * @return
     */
    boolean logout(String ip);

    /**
     * 报警监听
     * @param ip
     * @return
     */
    boolean startListenAlarm(String ip);

    /**
     * 取消报警监听
     * @param ip
     * @return
     */
    boolean stopListenAlarm(String ip);
}
