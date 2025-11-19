package ovh.bookexchange.api.filters;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class CustomParameterRequestWrapper extends HttpServletRequestWrapper {
    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */

    private final Map<String, String[]> customParamMap;
    public CustomParameterRequestWrapper(HttpServletRequest request) {
        super(request);
        customParamMap = new HashMap(request.getParameterMap());
    }

    protected void setParameter(String name, String value) {
        customParamMap.put(name, new String[] {value});
    }

    @Override
    public String getParameter(String name) {
        String[] param = customParamMap.get(name);
        if (param == null) {
            return null;
        }
        return param[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(customParamMap);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(customParamMap.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return customParamMap.get(name);
    }


}
