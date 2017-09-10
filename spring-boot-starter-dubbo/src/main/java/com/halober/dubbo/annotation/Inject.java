package com.halober.dubbo.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import com.alibaba.dubbo.config.annotation.Reference;

/**
 * 该注解会优先使用spring上下文找到的对象注入当前参数，如果spring上下文找不到时才会到dubbo中找并注入当前注解的参数
 * @author leige
 *
 */
@Retention(RUNTIME)
@Target({ FIELD, METHOD })
public @interface Inject {
	/**
	 * 如果从spring容器没有能够找到的话，使用该配置到dubbo中找
	 * @return
	 */
	Reference value() default @Reference();
}
