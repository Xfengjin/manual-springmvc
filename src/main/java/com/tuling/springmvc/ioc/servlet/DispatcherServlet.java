package com.tuling.springmvc.ioc.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuling.springmvc.ioc.annotation.Controller;
import com.tuling.springmvc.ioc.annotation.RequestMapping;
import com.tuling.springmvc.ioc.annotation.ResponseBody;
import com.tuling.springmvc.ioc.context.WebApplicationContext;
import com.tuling.springmvc.ioc.handler.MappingHandler;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fengjin
 * @Slogan 致敬大师，致敬未来的你
 */
@WebServlet("DispatcherServlet")
public class DispatcherServlet extends HttpServlet {

    // 指定springmvc的容器
    private WebApplicationContext webApplicationContext;

    //创建集合  用于存放  映射关系    映射地址  与  控制器.方法，用于发送请求直接从该集合中进行匹配
    private Map<String, MappingHandler> handlerMap = new HashMap<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1、加载初始化参数 springmvc.xml 实际上就是拿到了webx.xml里面的contextConfiguration这个参数所指定的xml文件
        String contextConfiguration = this.getServletConfig().getInitParameter("contextConfiguration");

        // 2、创建springmvc容器
        webApplicationContext = new WebApplicationContext(contextConfiguration);

        // 3、初始化容器
        webApplicationContext.onRefresh();

        // 4、初始化请求与映射关系
        initHandlerMapping(webApplicationContext);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 进行分发处理
        doDispatcher(req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    public void doDispatcher(HttpServletRequest req, HttpServletResponse resp) {
        String uri = req.getRequestURI();
        try {
            if (!handlerMap.containsKey(uri)) {
                resp.getWriter().print("<h1>404 NOT  FOUND!</h1>");
            }
            // 从容器中的handler取出url 和 用户的请求地址进行匹配，找到满足条件的handler
            MappingHandler mappingHandler = handlerMap.get(uri);

            // TODO 方法调用之前，进行参数的注入

            // 调用目标方法
            Object result = mappingHandler.getMethod().invoke(mappingHandler.getController());

            if (result instanceof String) {
                // 跳转jsp
                String viewName = (String) result;
                // forward:/success.jsp
                if (viewName.contains(":")) {
                    String viewType=viewName.split(":")[0];
                    String viewPage=viewName.split(":")[1];
                    if (viewType.equals("forward")) {
                        // 转发
                        req.getRequestDispatcher(viewPage).forward(req, resp);
                    } else {
                        // redirect:/user.jsp 重定向
                        resp.sendRedirect(viewPage);
                    }
                } else {
                    // 默认就是转发
                    req.getRequestDispatcher(viewName).forward(req, resp);
                }
            } else {
                // 返回json格式的数据
                Method method = mappingHandler.getMethod();
                if (method.isAnnotationPresent(ResponseBody.class)) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    String value = objectMapper.writeValueAsString(result);
                    PrintWriter writer = resp.getWriter();
                    writer.print(value);
                    writer.flush();
                    writer.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 初始化处理请求映射关系
     * @param webApplicationContext
     */
    public void initHandlerMapping(WebApplicationContext webApplicationContext) {
        // 获取ioc容器
        Map<String, Object> iocMap = webApplicationContext.getIocMap();

        for (Map.Entry<String, Object> entry : iocMap.entrySet()) {
            // 获取bean
            Class<?> clazz = entry.getValue().getClass();
            if (clazz.isAnnotationPresent(Controller.class)) {
                // 获取bean中的所有方法，建立映射关系
                Method[] methods = clazz.getDeclaredMethods();

                for (Method method : methods) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        // 获取注解中的值
                        String url = method.getAnnotation(RequestMapping.class).value();
                        // 建立映射关系
                        MappingHandler mappingHandler = new MappingHandler(url, entry.getValue(), method);
                        handlerMap.put(url,mappingHandler);
                    }
                }
            }
        }
    }
}
