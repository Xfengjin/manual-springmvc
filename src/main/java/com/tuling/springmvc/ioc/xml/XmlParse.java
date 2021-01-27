package com.tuling.springmvc.ioc.xml;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;

/**
 * @author fengjin
 * @Slogan 致敬大师，致敬未来的你
 */
public class XmlParse {

    private static final String ELEMENT_COMPONENT_SCAN = "component-scan";

    private static final String ELEMENT_BASE_PACKAGE = "base-package";

    public static String getBasePackage(String xml) {
        try {
            SAXReader saxReader = new SAXReader();
            InputStream resource = XmlParse.class.getClassLoader().getResourceAsStream(xml);
            // xml文档对象
            Document document = saxReader.read(resource);
            Element rootElement = document.getRootElement();
            Element element = rootElement.element(ELEMENT_COMPONENT_SCAN);
            Attribute attribute = element.attribute(ELEMENT_BASE_PACKAGE);
            String basePackage = attribute.getText();
            return basePackage;
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return "";
    }
}
