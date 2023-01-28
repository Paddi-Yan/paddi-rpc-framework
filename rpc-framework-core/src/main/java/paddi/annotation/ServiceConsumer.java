package paddi.annotation;

import java.lang.annotation.*;

/**
 * rpc service consumer, autowire the service implementation class
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 15:36:31
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Inherited
public @interface ServiceConsumer {

    /**
     * service version, default value is empty string
     */
    String version() default "";

    /**
     * service group, default value is empty string
     */
    String group() default "";
}
