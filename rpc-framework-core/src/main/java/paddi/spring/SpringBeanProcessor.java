package paddi.spring;

import com.paddi.extension.ExtensionLoader;
import com.paddi.factory.SingletonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import paddi.annotation.ServiceConsumer;
import paddi.annotation.ServiceProvider;
import paddi.config.RpcServiceConfig;
import paddi.provider.ServicesProvider;
import paddi.provider.impl.ZkServicesProvider;
import paddi.proxy.RpcClientProxy;
import paddi.remoting.transport.RpcRequestTransport;

import java.lang.reflect.Field;

/**
 * call this method before creating the bean to sea if the class is annotated
 * @Author: Paddi-Yan
 * @Project: paddi-rpc-framework
 * @CreatedTime: 2023年01月28日 16:18:10
 */
@Slf4j
@Component
public class SpringBeanProcessor implements BeanPostProcessor {
    private final ServicesProvider servicesProvider;
    private final RpcRequestTransport rpcClient;

    public SpringBeanProcessor() {
        servicesProvider = SingletonFactory.getInstance(ZkServicesProvider.class);
        rpcClient = ExtensionLoader.getExtensionLoader(RpcRequestTransport.class).getExtension("netty");
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if(bean.getClass().isAnnotationPresent(ServiceProvider.class)) {
            log.info("[{}] is annotated with [{}]", bean.getClass().getName(), ServicesProvider.class.getCanonicalName());
            //get ServiceProvider annotation
            ServiceProvider serviceProvider = bean.getClass().getAnnotation(ServiceProvider.class);
            //build RpcServiceProperties
            RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                                                     .group(serviceProvider.group())
                                                     .version(serviceProvider.version())
                                                     .service(bean).build();
            servicesProvider.publishService(rpcServiceConfig);
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = bean.getClass();
        Field[] declaredFields = targetClass.getDeclaredFields();
        for(Field declaredField : declaredFields) {
            ServiceConsumer serviceConsumer = declaredField.getAnnotation(ServiceConsumer.class);
            if(serviceConsumer != null) {
                RpcServiceConfig rpcServiceConfig = RpcServiceConfig.builder()
                                                         .group(serviceConsumer.group())
                                                         .version(serviceConsumer.version()).build();
                RpcClientProxy clientProxy = new RpcClientProxy(rpcClient, rpcServiceConfig);
                declaredField.setAccessible(true);
                try {
                    declaredField.set(bean, clientProxy);
                } catch(IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return bean;
    }
}
