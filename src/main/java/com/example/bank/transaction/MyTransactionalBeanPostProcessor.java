package com.example.bank.transaction;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

@Component
public class MyTransactionalBeanPostProcessor implements BeanPostProcessor {

    private final Map<String, Class<?>> transactionalBeans = new HashMap<>();
    private final ObjectProvider<TransactionManager> txManagerProvider;

    public MyTransactionalBeanPostProcessor(ObjectProvider<TransactionManager> txManagerProvider) {
        this.txManagerProvider = txManagerProvider;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        for (Method method : bean.getClass().getMethods()) {
            if (method.isAnnotationPresent(MyTransactional.class)) {
                transactionalBeans.put(beanName, bean.getClass());
                break;
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        Class<?> beanClass = transactionalBeans.get(beanName);
        if (beanClass == null) {
            return bean;
        }

        TransactionalInvocationHandler handler =
                new TransactionalInvocationHandler(bean, beanClass, txManagerProvider);

        return Proxy.newProxyInstance(
                beanClass.getClassLoader(),
                beanClass.getInterfaces(),
                handler);
    }
}
