package com.zhaow.restful.method.action;


import com.intellij.openapi.module.Module;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;

//PathAndQuery  AbsolutePath AbsoluteUri Query
public class ModuleHelper {
    // URL
    private static final String SCHEME = "http://"; //PROTOCOL
    private static final String HOST = "localhost";
    private static final String PORT = "8080"; // int
    public static String DEFAULT_URI = "http://" + HOST + ":" + PORT;
//    private static final String PATH = "http://localhost"+":"+PORT; // PATH or FILE

    /**
     * 缓存 mould fullUrl前缀, ServiceHostPrefix
     */
    private static Map<String, String> SERVICE_HOST_PREFIX_MAP = new HashMap<>();

    Module module;

    public static String getAUTHORITY() {
        return null;
    }

    PropertiesHandler propertiesHandler;

    public ModuleHelper(Module module) {
        this.module = module;
        propertiesHandler = new PropertiesHandler(module);
    }

    public static ModuleHelper create(Module module) {
        return new ModuleHelper(module);
    }

    public static String getServiceHostPrefix(Module module) {
        if (module == null) {
            return DEFAULT_URI;
        }

        String moduleName = module.getName();

        String prefix = SERVICE_HOST_PREFIX_MAP.get(moduleName);
        if (StringUtils.isNotBlank(prefix)) {
            return prefix;
        }

        PropertiesHandler propertiesHandler = new PropertiesHandler(module);;
        String port = propertiesHandler.getServerPort();

        if (StringUtils.isEmpty(port)) {
            port = PORT;
        }

        String contextPath = propertiesHandler.getContextPath();
        prefix = new StringBuilder(SCHEME).append(HOST).append(":").append(port).append(contextPath).toString();

        if (prefix != null) {
            SERVICE_HOST_PREFIX_MAP.put(moduleName, prefix);
        }

        return prefix;
    }

    // @NotNull
    // public String getServiceHostPrefix() {
    //     if (module == null) {
    //         return DEFAULT_URI;
    //     }
    //
    //     String port = propertiesHandler.getServerPort();
    //
    //     if (StringUtils.isEmpty(port)) {
    //         port = PORT;
    //     }
    //
    //     String contextPath = propertiesHandler.getContextPath();
    //     return new StringBuilder(SCHEME).append(HOST).append(":").append(port).append(contextPath).toString();
    //
    // }
}
