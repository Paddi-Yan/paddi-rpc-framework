package paddi.annotation;

import org.springframework.context.annotation.Import;
import paddi.spring.CustomScannerRegistrar;

import java.lang.annotation.*;

/**
 * scan custom annotation
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 15:31:45
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Import(CustomScannerRegistrar.class)
@Documented
public @interface RpcScan {
    String[] basePackage();
}
