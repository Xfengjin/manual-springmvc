package com.tuling.springmvc.ioc.context;

import com.tuling.springmvc.ioc.annotation.Autowired;
import com.tuling.springmvc.ioc.annotation.Controller;
import com.tuling.springmvc.ioc.annotation.Service;
import com.tuling.springmvc.ioc.xml.XmlParse;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author fengjin
 * @Slogan 致敬大师，致敬未来的你
 *
 * springmvc 容器
 */
public class WebApplicationContext {

    // classpath: springmvc.xml
    private String contextConfiguration;

    // 定义集合， 用于存放bean的权限名|包名.类名
    private List<String> classNameList = new ArrayList<>();

    // 创建map 扮演ioc容器的角色， key==>beanName, value==>bean实例
    private Map<String, Object> iocMap = new ConcurrentHashMap<>();

    public WebApplicationContext() {
    }

    public WebApplicationContext(String contextConfiguration) {
        this.contextConfiguration = contextConfiguration;
    }

    /**
     * 初始化springmvc容器
     */
    public void onRefresh() {
        // 1.进行springmvc.xml的解析操作
        String basePackage = XmlParse.getBasePackage(contextConfiguration.split(":")[1]);

        String[] packages = basePackage.split(",");

        // 2.进行包扫描
        for (String currentPackage : packages) {
            doScanPackage(currentPackage.trim());
        }

        // 3.实例化容器中的bean
        doInstance();

        // 4.自动注入的操作
        doAutowired();
    }

    /**
     * 自动注入
     */
    public void doAutowired() {
        // 从ioc容器中取出所有的bean，判断是否使用了@Autowired这个注解，如果使用了，则需要进行依赖注入
        try {
            for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
                // 获取容器中的bean
                Object bean = entry.getValue();
                // 获取bean中的属性
                Field[] fields = bean.getClass().getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(Autowired.class)) {
                        // 获取bean中的value值，这个值就是bean的name
                        Autowired annotation = field.getAnnotation(Autowired.class);
                        String beanName = annotation.value();
                        // 取消检测机制（不推荐，会破坏java的封装性）
                        field.setAccessible(true);
                        field.set(beanName, iocMap.get(beanName));
                    }
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 实例化
     */
    public void doInstance() {
        for (String className : classNameList) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(Controller.class)) {
                    // 控制层
                    // 将首字母变成小写
                    String beanName = clazz.getSimpleName().substring(0, 1).toLowerCase() + clazz.getSimpleName().substring(1);
                    iocMap.put(beanName, clazz.newInstance());
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    // service层
                    Service annotation = clazz.getAnnotation(Service.class);
                    String beanName = annotation.value();
                    iocMap.put(beanName, clazz.newInstance());
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 扫描包
     */
    public void doScanPackage(String currentPackage) {
        // com.tuling.logic.controller ==> com/tuling/logic/controller
        URL url = this.getClass().getClassLoader().getResource("/" + currentPackage.replaceAll("\\.", "/"));
        String path = url.getFile();
        File files = new File(path);
        for (File file : files.listFiles()) {
            if (file.isDirectory()) {
                // 当前是一个文件目录
                doScanPackage(currentPackage + "." + file.getName());
            } else {
                // 文件目录下的文件，获取文件的全路径 UserController.class ==> com.tuling.logic.controller.UserController
                String className = currentPackage + "." + file.getName().replaceAll(".class", "");
            }
        }
    }

    public Map<String, Object> getIocMap() {
        return iocMap;
    }
}
