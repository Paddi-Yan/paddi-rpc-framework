package paddi.annotation;

import java.lang.annotation.*;

/**
 * rpc service provider annotation, autowire the service implementation class
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 15:33:21
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ServiceProvider {
    /**
     * service version, default value is empty string
     */
    String version() default "";

    /**
     * service group, default value is empty string
     */
    String group() default "";
}
