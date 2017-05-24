package com.nuliji.util;

import com.nuliji.service.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by gaojie on 2017/5/11.
 */
public class Configure {

    private static final Logger logger = LoggerFactory.getLogger(Configure.class);
    public static Properties payProperties = new Properties();

    static {
        loadProps();
    }

    synchronized static private void loadProps() {
        logger.debug("开始加载properties文件内容.......");
        InputStream in = null;
        try {
            in = Configure.class.getClassLoader().getResourceAsStream("jdbc.properties");
            payProperties.load(in);
        } catch (FileNotFoundException e) {
            logger.error("jdbc.properties文件未找到");
        } catch (IOException e) {
            logger.error("出现IOException");
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                logger.error("jdbc.properties文件流关闭出现异常");
            }
        }
        logger.debug("加载properties文件内容完成...........");
        logger.debug("properties文件内容：" + payProperties);
    }

    public static String payNotifyUrl() {
        return payProperties.getProperty("pay_notify");
    }

    public static String alipayPid() {
        return payProperties.getProperty("alipay_pid");
    }

    public static String alipayAppKey() {
        return payProperties.getProperty("alipay_app_key");
    }

    public static String alipayAppId() {
        return payProperties.getProperty("alipay_app_id");
    }

    public static String alipayAppPublicKey() {
        return payProperties.getProperty("alipay_app_public_key");
    }

    public static String weixinPid() {
        return payProperties.getProperty("weixin_pid");
    }

    public static String weixinAppKey() {
        return payProperties.getProperty("weixin_app_key");
    }

    public static String weixinAppId() {
        return payProperties.getProperty("weixin_app_id");
    }
}
