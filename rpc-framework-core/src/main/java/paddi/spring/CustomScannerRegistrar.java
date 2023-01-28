package paddi.spring;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;
import paddi.annotation.RpcScan;
import paddi.annotation.ServiceProvider;

/**
 * scan and filter specified annotation
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 15:32:50
 */
@Slf4j
public class CustomScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private static final String SPRING_BEAN_BASE_PACKAGE = "com.paddi";
    private static final String BASE_PACKAGE_ATTRIBUTE_NAME = "basePackage";
    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        //get the attributes and values of RpcScan annotation
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(RpcScan.class.getName()));
        String[] rpcScanBasePackages = new String[0];
        if(annotationAttributes != null) {
            rpcScanBasePackages = annotationAttributes.getStringArray(BASE_PACKAGE_ATTRIBUTE_NAME);
        }
        //scan the ServiceProvider annotation
        CustomScanner serviceProviderScanner = new CustomScanner(beanDefinitionRegistry, ServiceProvider.class);
        //scan the Component annotation
        CustomScanner springBeanScanner = new CustomScanner(beanDefinitionRegistry, Component.class);
        if(resourceLoader != null) {
            serviceProviderScanner.setResourceLoader(resourceLoader);
            springBeanScanner.setResourceLoader(resourceLoader);
        }
        int springBeanAmount = springBeanScanner.scan(SPRING_BEAN_BASE_PACKAGE);
        log.info("SpringBeanScanner扫描的数量: [{}]",springBeanAmount);
        int serviceProviderCount = serviceProviderScanner.scan(rpcScanBasePackages);
        log.info("ServiceProviderScanner扫描的数量: [{}]", serviceProviderCount);
    }
}
