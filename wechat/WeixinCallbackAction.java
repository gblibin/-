
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Created by libin on 2018/8/28.
 */
@Controller
@RequestMapping(value = "/wx")
public class WeixinCallbackAction implements ApplicationContextAware {

    private static Logger logger = LoggerFactory.getLogger(WeixinCallbackAction.class);

    /**微信请求参数名：微信加密签名*/
    public static final String REQUEST_SIGNATURE = "signature";
    /**微信请求参数名：时间戳*/
    public static final String REQUEST_TIMESTAMP = "timestamp";
    /**微信请求参数名：随机数*/
    public static final String REQUEST_NONCE = "nonce";
    /**微信请求参数名：随机字符串*/
    public static final String REQUEST_ECHOSTR = "echostr";


    private  ApplicationContext appCtx;

    @RequestMapping("/callback")
    public void wechatCallbackApi(HttpServletRequest request,HttpServletResponse response)
            throws ServletException, IOException {

        logger.info("接收到微信的回调请求...");

        PrintWriter out = response.getWriter();

        if (checkSignature(request)) {
            String echostr = request.getParameter(REQUEST_ECHOSTR);
            out.print(echostr);
        }
        invokeProcessors(request);
        out.close();
    }

    // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，d表示接入成功，否则接入失败
    private boolean checkSignature(HttpServletRequest request){
        // 微信加密签名
        String signature = request.getParameter(REQUEST_SIGNATURE);
        // 时间戳
        String timestamp = request.getParameter(REQUEST_TIMESTAMP);
        // 随机数
        String nonce = request.getParameter(REQUEST_NONCE);

        return SignUtil.checkSignature(signature, timestamp, nonce);
    }

    private void invokeProcessors(HttpServletRequest request){
        Map<String,String> weixinRequestParams = MessageUtil.parseXml(request);
        if(weixinRequestParams.isEmpty()){
            logger.info("解析微信事件异常或者无事件信息，忽略。");
          return;
        }

        logger.info("微信事件数据："+ weixinRequestParams);

        Map<String, WeixinEventProcessor>  processors = appCtx.getBeansOfType(WeixinEventProcessor.class);
        if(processors.isEmpty()){
            logger.info("没有配置微信事件监听者，忽略。");
            return;
        }

        logger.info("【开始】处理微信事件...");
        for(WeixinEventProcessor processor:processors.values()){
            processor.process(weixinRequestParams);
        }
        logger.info("【结束】处理微信事件...");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.appCtx = applicationContext;
    }
}
