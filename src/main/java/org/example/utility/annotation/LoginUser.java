package org.example.utility.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER) // 파라미터에만 붙일 수 있음
@Retention(RetentionPolicy.RUNTIME) // 실행 중에도 어노테이션 정보가 유지됨
public @interface LoginUser {
}
