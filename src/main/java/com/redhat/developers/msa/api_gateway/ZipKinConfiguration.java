package com.redhat.developers.msa.api_gateway;

import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.ServerRequestInterceptor;
import com.github.kristofa.brave.ServerResponseInterceptor;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.HttpSpanCollector;
import com.github.kristofa.brave.servlet.BraveServletFilter;

@Configuration
public class ZipKinConfiguration {

    /**
     * Register the ZipKin Filter to intercept {@link ServerRequestInterceptor} and {@link ServerResponseInterceptor}
     * 
     * @return
     */
    @Bean
    public FilterRegistrationBean zipkinFilter() {
        Brave brave = getBrave();
        FilterRegistrationBean registration =
            new FilterRegistrationBean(new BraveServletFilter(brave.serverRequestInterceptor(), brave.serverResponseInterceptor(), new DefaultSpanNameProvider()));
        // Explicit mapping to avoid trace on readiness probe
        registration.addUrlPatterns("/api");
        return registration;
    }

    /**
     * The instance of {@link Brave} - A instrumentation library for Zipkin
     * 
     * @return
     */
    @Bean
    @Scope(value = "singleton")
    public Brave getBrave() {
        Brave brave = new Brave.Builder("api-gateway")
            .spanCollector(HttpSpanCollector.create(System.getenv("ZIPKIN_SERVER_URL"),
            		new EmptySpanCollectorMetricsHandler()))
            .build();
        return brave;
    }

}
