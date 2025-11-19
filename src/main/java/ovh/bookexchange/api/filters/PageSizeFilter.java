package ovh.bookexchange.api.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre chargé de gérer la taille maximale des pages.
 * Si elle dépasse la valeur renseignée dans le constructeur,
 * la requête est modifiée pour que la taille demandée soit la taille maximale.
 */
public class PageSizeFilter extends OncePerRequestFilter {
    private final int maxPageSize;
    private final int defaultPageSize;

    public PageSizeFilter(int maxPageSize, int defaultPageSize) {
        this.maxPageSize = maxPageSize;
        this.defaultPageSize = defaultPageSize;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getParameter("size") != null) {

            try {
                int size = Integer.parseInt(request.getParameter("size"));
                if (size > maxPageSize) {
                    filterChain.doFilter(getRequestWrapperWithParam(request, "size", String.valueOf(maxPageSize)), response);
                    return;
                }
            } catch (NumberFormatException e) {
                filterChain.doFilter(getRequestWrapperWithParam(request, "size", String.valueOf(defaultPageSize)), response);
                return;
                //C'est voulu de ne rien faire, ce n'est pas à ce middleware si de gérer ça.
                //C'est le problème d'un middleware intégré à spring (il met la valeur par défault du pageable dans ce càs).
            }
        }
        filterChain.doFilter(request, response);
    }

    private HttpServletRequest getRequestWrapperWithParam(HttpServletRequest request, String name, String value) {
        CustomParameterRequestWrapper requestWrapper = new CustomParameterRequestWrapper(request);
        requestWrapper.setParameter(name, value);
        return requestWrapper;
    }
}
