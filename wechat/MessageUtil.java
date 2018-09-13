

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * MessageUtil
 */
public class MessageUtil {

    private static Logger logger = LoggerFactory.getLogger(WeixinCallbackAction.class);

    public static Map<String, String> parseXml(HttpServletRequest request) {

        Map<String, String> map = new HashMap<>();
        InputStream inputStream = null;

        try {
            inputStream = request.getInputStream();
            SAXReader reader = new SAXReader();
            Document document = reader.read(inputStream);

            Element root = document.getRootElement();

            List<Element> elementList = root.elements();

            for (Element e : elementList) {
                map.put(e.getName(), e.getText());
            }
        } catch (Exception e) {
            logger.error("微信回调异常",e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                    inputStream = null;
                }catch (Exception e){
                    // Do Nothing
                }
            }
        }
        return map;
    }
}
