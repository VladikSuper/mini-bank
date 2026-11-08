package com.example.bank.transaction;

import org.springframework.beans.factory.ObjectProvider;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Map;

public class TransactionalInvocationHandler implements InvocationHandler {

    private final Object bean;
    private final Class<?> beanClass;
    private final ObjectProvider<TransactionManager> txManagerProvider;

    public TransactionalInvocationHandler(Object bean,
                                          Class<?> beanClass,
                                          ObjectProvider<TransactionManager> txManagerProvider) {
        this.bean = bean;
        this.beanClass = beanClass;
        this.txManagerProvider = txManagerProvider;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method implMethod = beanClass.getMethod(method.getName(), method.getParameterTypes());

        if (!implMethod.isAnnotationPresent(MyTransactional.class)) {
            return callTarget(method, args);
        }

        TransactionManager tx = txManagerProvider.getObject();
        Map<String, BigDecimal> snapshot = tx.begin();
        try {
            return callTarget(method, args);
        } catch (RuntimeException e) {
            tx.rollback(snapshot);
            throw e;
        }
    }

    private Object callTarget(Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(bean, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
