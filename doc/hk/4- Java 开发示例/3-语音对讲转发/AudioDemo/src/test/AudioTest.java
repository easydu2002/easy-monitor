package test;

import Common.osSelect;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import test.HCNetSDK.FVoiceDataCallback_MR_V30;
import test.HCNetSDK.FVoiceDataCallBack_V30;

import java.io.*;
import java.nio.ByteBuffer;

public class AudioTest {
    static int lUserID = -1;
    static HCNetSDK hCNetSDK = null;
    static int lVoiceComHandle = -1; //语音对讲句柄
    static int lVoiceHandle = -1; //语音转发句柄
    static File file = null;
    static File filePcm = null;
    static FileOutputStream outputStream = null;
    static FileOutputStream outputStreamPcm = null;

    static File fileEncode = null;
    static FileOutputStream outputStreamG711 = null;
    static Pointer pDecHandle = null;
    static cbVoiceDataCallBack_MR_V30 cbVoiceDataCallBack = null;
    static VoiceDataCallBack voiceDatacallback = null;



    /**
     * 根据不同操作系统选择不同的库文件和库路径
     *
     * @return
     */
    private static boolean createSDKInstance() {
        if (hCNetSDK == null) {
            synchronized (HCNetSDK.class) {
                String strDllPath = "";
                try {
                    //System.setProperty("jna.debug_load", "true");
                    if (osSelect.isWindows())
                        //win系统加载库路径
                        strDllPath = System.getProperty("user.dir") + "\\lib\\HCNetSDK.dll";

                    else if (osSelect.isLinux())
                        //Linux系统加载库路径
                        strDllPath = System.getProperty("user.dir") + "/lib/libhcnetsdk.so";
                    hCNetSDK = (HCNetSDK) Native.loadLibrary(strDllPath, HCNetSDK.class);
                } catch (Exception ex) {
                    System.out.println("loadLibrary: " + strDllPath + " Error: " + ex.getMessage());
                    return false;
                }
            }
        }
        return true;
    }

    public static void main(String args[]) throws InterruptedException {
        if (hCNetSDK == null) {
            if (!createSDKInstance()) {
                System.out.println("Load SDK fail");
                return;
            }
        }
        //linux系统建议调用以下接口加载组件库
        if (osSelect.isLinux()) {
            HCNetSDK.BYTE_ARRAY ptrByteArray1 = new HCNetSDK.BYTE_ARRAY(256);
            HCNetSDK.BYTE_ARRAY ptrByteArray2 = new HCNetSDK.BYTE_ARRAY(256);
            //这里是库的绝对路径，请根据实际情况修改，注意改路径必须有访问权限
            String strPath1 = System.getProperty("user.dir") + "/lib/libcrypto.so.1.1";
            String strPath2 = System.getProperty("user.dir") + "/lib/libssl.so.1.1";


            System.arraycopy(strPath1.getBytes(), 0, ptrByteArray1.byValue, 0, strPath1.length());
            ptrByteArray1.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(3, ptrByteArray1.getPointer());

            System.arraycopy(strPath2.getBytes(), 0, ptrByteArray2.byValue, 0, strPath2.length());
            ptrByteArray2.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(4, ptrByteArray2.getPointer());

            String strPathCom = System.getProperty("user.dir") + "/lib/";
            HCNetSDK.NET_DVR_LOCAL_SDK_PATH struComPath = new HCNetSDK.NET_DVR_LOCAL_SDK_PATH();
            System.arraycopy(strPathCom.getBytes(), 0, struComPath.sPath, 0, strPathCom.length());
            struComPath.write();
            hCNetSDK.NET_DVR_SetSDKInitCfg(2, struComPath.getPointer());
        }

        boolean initSuc = hCNetSDK.NET_DVR_Init();
        if (initSuc != true) {
            System.out.println("初始化失败");
        }
        hCNetSDK.NET_DVR_SetLogToFile(3, "./sdkLog", false);
        //设置语音对讲回调函数
        if (voiceDatacallback == null) {
            voiceDatacallback = new VoiceDataCallBack();
        }

        //设置语音回调函数
        if (cbVoiceDataCallBack == null) {
            cbVoiceDataCallBack = new cbVoiceDataCallBack_MR_V30();
        }

        AudioTest.login_V40("10.16.36.29", (short) 8000, "admin", "hik12345");

        //开启语音对讲
/*		AudioTest.startVoiceCom(lUserID);
		Thread.sleep(20000);
		AudioTest.stopVoiceCom(lUserID);*/

        //开启语音转发
        AudioTest.startVoiceG711Trans(lUserID);
        if (lVoiceHandle > -1) {
            AudioTest.stopVoiceG711Trans(lVoiceHandle);
        }
        Thread.sleep(1000);


        AudioTest.logout();
        //释放SDK
        hCNetSDK.NET_DVR_Cleanup();

    }


    static class VoiceDataCallBack implements HCNetSDK.FVoiceDataCallBack_V30 {
        public void invoke(int lVoiceComHandle, Pointer pRecvDataBuffer, int dwBufSize, byte byAudioFlag, int pUser) {
            //回调函数里保存语音对讲中双方通话语音数据
            System.out.println("语音对讲数据回调....");

        }


    }

    static class cbVoiceDataCallBack_MR_V30 implements HCNetSDK.FVoiceDataCallback_MR_V30 {
        public void invoke(int lVoiceComHandle, Pointer pRecvDataBuffer, int dwBufSize, byte byAudioFlag, Pointer pUser) {
            //语音回调函数，实现的是接收设备那边传过来的音频数据（g711编码），如果只需要平台发送音频到设备，不需要接收设备发送的音频，
            // 回调函数里什么都不实现
            //不影响业务功能
            System.out.println("-----=cbVoiceDataCallBack_MR_V30 enter---------");
            if (byAudioFlag == 1) {
                System.out.println("设备发过来的语音");
                //设备发送过来的语音数据
                try {
                    //将设备发送过来的语音数据写入文件
                    long offset = 0;
                    ByteBuffer buffers = pRecvDataBuffer.getByteBuffer(offset, dwBufSize);
                    byte[] bytes = new byte[dwBufSize];
                    buffers.rewind();
                    buffers.get(bytes);
                    outputStream.write(bytes);  //这里实现的是将设备发送的g711音频数据写入文件
                    //解码
                    if (pDecHandle == null) {
                        pDecHandle = hCNetSDK.NET_DVR_InitG711Decoder();
                    }
                    HCNetSDK.NET_DVR_AUDIODEC_PROCESS_PARAM struAudioParam = new HCNetSDK.NET_DVR_AUDIODEC_PROCESS_PARAM();
                    struAudioParam.in_buf = pRecvDataBuffer;
                    struAudioParam.in_data_size = dwBufSize;
                    HCNetSDK.BYTE_ARRAY ptrVoiceData = new HCNetSDK.BYTE_ARRAY(320);
                    ptrVoiceData.write();
                    struAudioParam.out_buf = ptrVoiceData.getPointer();
                    struAudioParam.out_frame_size = 320;
                    struAudioParam.g711_type = 0; //G711编码类型：0- U law，1- A law
                    struAudioParam.write();
                    if (!hCNetSDK.NET_DVR_DecodeG711Frame(pDecHandle, struAudioParam)) {
                        System.out.println("NET_DVR_DecodeG711Frame failed, error code:" + hCNetSDK.NET_DVR_GetLastError());
                    }
                    struAudioParam.read();
                    //将解码之后PCM音频数据写入文件
                    long offsetPcm = 0;
                    ByteBuffer buffersPcm = struAudioParam.out_buf.getByteBuffer(offsetPcm, struAudioParam.out_frame_size);
                    byte[] bytesPcm = new byte[struAudioParam.out_frame_size];
                    buffersPcm.rewind();
                    buffersPcm.get(bytesPcm);
                    outputStreamPcm.write(bytesPcm);  //这里实现的是将设备发送的pcm音频数据写入文件，（前面的代码实现的就是将g711解码成pcm音频）

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (byAudioFlag == 0) {
                System.out.println("客户端发送音频数据");
            }
        }
    }

        /**
         * 设备登录V40 与V30功能一致
         *
         * @param ip   设备IP
         * @param port SDK端口，默认设备的8000端口
         * @param user 设备用户名
         * @param psw  设备密码
         */
        public static boolean login_V40(String ip, short port, String user, String psw) {
            //注册
            HCNetSDK.NET_DVR_USER_LOGIN_INFO m_strLoginInfo = new HCNetSDK.NET_DVR_USER_LOGIN_INFO();//设备登录信息
            HCNetSDK.NET_DVR_DEVICEINFO_V40 m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V40();//设备信息

            String m_sDeviceIP = ip;//设备ip地址
            m_strLoginInfo.sDeviceAddress = new byte[HCNetSDK.NET_DVR_DEV_ADDRESS_MAX_LEN];
            System.arraycopy(m_sDeviceIP.getBytes(), 0, m_strLoginInfo.sDeviceAddress, 0, m_sDeviceIP.length());

            String m_sUsername = user;//设备用户名
            m_strLoginInfo.sUserName = new byte[HCNetSDK.NET_DVR_LOGIN_USERNAME_MAX_LEN];
            System.arraycopy(m_sUsername.getBytes(), 0, m_strLoginInfo.sUserName, 0, m_sUsername.length());

            String m_sPassword = psw;//设备密码
            m_strLoginInfo.sPassword = new byte[HCNetSDK.NET_DVR_LOGIN_PASSWD_MAX_LEN];
            System.arraycopy(m_sPassword.getBytes(), 0, m_strLoginInfo.sPassword, 0, m_sPassword.length());
            m_strLoginInfo.wPort = port;
            m_strLoginInfo.bUseAsynLogin = false; //是否异步登录：0- 否，1- 是
            m_strLoginInfo.write();
            lUserID = hCNetSDK.NET_DVR_Login_V40(m_strLoginInfo, m_strDeviceInfo);
            if (lUserID == -1) {
                System.out.println("登录失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
                return false;

            } else {
                System.out.println(ip + ":设备登录成功！");
                return true;
            }
        }

        /**
         * 设备注销
         */
        public static boolean logout() {

            if (hCNetSDK.NET_DVR_Logout(lUserID)) {
                System.out.println("注销成功");
            }

            return true;
        }




        /**
         * 开启语音对讲
         *
         * @param userID
         */
        public static boolean startVoiceCom(int userID) {

            if (voiceDatacallback == null) {
                voiceDatacallback = new VoiceDataCallBack();
            }
            int voiceChannel = 1; //语音通道号。对于设备本身的语音对讲通道，从1开始；对于设备的IP通道，为登录返回的
            // 起始对讲通道号(byStartDTalkChan) + IP通道索引 - 1，例如客户端通过NVR跟其IP Channel02所接前端IPC进行对讲，则dwVoiceChan=byStartDTalkChan + 1
            boolean bret = true;  //需要回调的语音数据类型：0- 编码后的语音数据，1- 编码前的PCM原始数据
            lVoiceComHandle = hCNetSDK.NET_DVR_StartVoiceCom_V30(userID, voiceChannel, bret, voiceDatacallback, null);
            if (lVoiceComHandle <= -1) {
                System.out.println("语音对讲开启失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
                return false;
            }
            System.out.println("语音对讲开始成功！");
            return true;

        }

        public static boolean stopVoiceCom(int userID) {
            if (!hCNetSDK.NET_DVR_StopVoiceCom(lVoiceComHandle)) {
                System.out.println("停止对讲失败，错误码为" + hCNetSDK.NET_DVR_GetLastError());
                return false;
            }
            System.out.println("语音对讲停止成功！");
            return true;

        }


        /**
         * 开启语音转发
         * 设备音频编码格式G711u
         *
         * @return
         */
        public static boolean startVoiceG711Trans(int userID) {

            //设置语音对讲函数回调
            file = new File(System.getProperty("user.dir") + "\\AudioFile\\DeviceSaveData.g7");  //保存回调函数的音频数据

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                outputStream = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            //保存解码后音频数据
            filePcm = new File(System.getProperty("user.dir") + "\\AudioFile\\DecodeSaveData.pcm");  //保存回调函数的音频数据

            if (!filePcm.exists()) {
                try {
                    filePcm.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            try {
                outputStreamPcm = new FileOutputStream(filePcm);
            } catch (FileNotFoundException e3) {
                // TODO Auto-generated catch block
                e3.printStackTrace();
            }
            int voiceChannel = 1; //语音通道号。对于设备本身的语音对讲通道，从1开始；对于设备的IP通道，为登录返回的
            // 起始对讲通道号(byStartDTalkChan) + IP通道索引 - 1，例如客户端通过NVR跟其IP Channel02所接前端IPC进行对讲，则dwVoiceChan=byStartDTalkChan + 1
            lVoiceHandle = hCNetSDK.NET_DVR_StartVoiceCom_MR_V30(userID, voiceChannel, cbVoiceDataCallBack, null);
            if (lVoiceHandle == -1) {
                System.out.println("语音转发启动失败,err=" + hCNetSDK.NET_DVR_GetLastError());
                return false;
            }

            //先测试接收设备发送音频数据
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            //以下代码是读取本地音频文件发送给设备
            FileInputStream Voicefile = null;
//            FileOutputStream Encodefile = null;
            int dataLength = 0;

            try {
                //创建从文件读取数据的FileInputStream流
                Voicefile = new FileInputStream(new File(System.getProperty("user.dir") + "\\AudioFile\\send2device.pcm"));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            fileEncode = new File(System.getProperty("user.dir") + "\\AudioFile\\EncodeData.g7");  //保存音频编码数据

            if (!fileEncode.exists()) {
                try {
                    fileEncode.createNewFile();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            try {
                outputStreamG711 = new FileOutputStream(fileEncode);
            } catch (FileNotFoundException e3) {
                // TODO Auto-generated catch block
                e3.printStackTrace();
            }

            try {

                //返回文件的总字节数
                dataLength = Voicefile.available();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            if (dataLength < 0) {
                System.out.println("input file dataSize < 0");
                return false;
            }

            HCNetSDK.BYTE_ARRAY ptrVoiceByte = new HCNetSDK.BYTE_ARRAY(dataLength);
            try {
                Voicefile.read(ptrVoiceByte.byValue);
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            ptrVoiceByte.write();

            int iEncodeSize = 0;
            HCNetSDK.NET_DVR_AUDIOENC_INFO enc_info = new HCNetSDK.NET_DVR_AUDIOENC_INFO();
            enc_info.write();
            Pointer encoder = hCNetSDK.NET_DVR_InitG711Encoder(enc_info);
            while ((dataLength - iEncodeSize) > 640) {
                HCNetSDK.BYTE_ARRAY ptrPcmData = new HCNetSDK.BYTE_ARRAY(640);
                System.arraycopy(ptrVoiceByte.byValue, iEncodeSize, ptrPcmData.byValue, 0, 640);
                ptrPcmData.write();

                HCNetSDK.BYTE_ARRAY ptrG711Data = new HCNetSDK.BYTE_ARRAY(320);
                ptrG711Data.write();

                HCNetSDK.NET_DVR_AUDIOENC_PROCESS_PARAM struEncParam = new HCNetSDK.NET_DVR_AUDIOENC_PROCESS_PARAM();
                struEncParam.in_buf = ptrPcmData.getPointer();
                struEncParam.out_buf = ptrG711Data.getPointer();
                struEncParam.out_frame_size = 320;
                struEncParam.g711_type = 0;//G711编码类型：0- U law，1- A law
                struEncParam.write();

                if (!hCNetSDK.NET_DVR_EncodeG711Frame(encoder, struEncParam)) {
                    System.out.println("NET_DVR_EncodeG711Frame failed, error code:" + hCNetSDK.NET_DVR_GetLastError());
                    hCNetSDK.NET_DVR_ReleaseG711Encoder(encoder);
                    //	hCNetSDK.NET_DVR_StopVoiceCom(lVoiceHandle);
                    return false;
                }
                struEncParam.read();
                ptrG711Data.read();

                long offsetG711 = 0;
                ByteBuffer buffersG711 = struEncParam.out_buf.getByteBuffer(offsetG711, struEncParam.out_frame_size);
                byte[] bytesG711 = new byte[struEncParam.out_frame_size];
                buffersG711.rewind();
                buffersG711.get(bytesG711);
                try {
                    outputStreamG711.write(bytesG711); //这里实现的是将pcm编码成的g711数据写入
                    // fileEncode = new File(System.getProperty("user.dir") + "\\AudioFile\\EncodeData.g7")
                    //这个文件;（编码数据是否保存对整体功能不影响）
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                iEncodeSize += 640;
                System.out.println("编码字节数：" + iEncodeSize);

                for (int i = 0; i < struEncParam.out_frame_size / 160; i++) {
                    HCNetSDK.BYTE_ARRAY ptrG711Send = new HCNetSDK.BYTE_ARRAY(160);
                    System.arraycopy(ptrG711Data.byValue, i * 160, ptrG711Send.byValue, 0, 160);
                    ptrG711Send.write();
                    if (!hCNetSDK.NET_DVR_VoiceComSendData(lVoiceHandle, ptrG711Send.byValue, 160)) {
                        System.out.println("NET_DVR_VoiceComSendData failed, error code:" + hCNetSDK.NET_DVR_GetLastError());
                    }

                    //需要实时速率发送数据
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
            return true;
        }

        public static void stopVoiceG711Trans(int lVoiceHandle) {
            if (pDecHandle != null) {
                hCNetSDK.NET_DVR_ReleaseG711Decoder(pDecHandle);
            }
            if (lVoiceHandle > -1) {
                hCNetSDK.NET_DVR_StopVoiceCom(lVoiceHandle);
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (outputStreamPcm != null) {
                try {
                    outputStreamPcm.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }





