package org.pulp.viewdsl.anno

@Target(AnnotationTarget.FIELD)//表示可以在函数中使用
@Retention(AnnotationRetention.RUNTIME)//表示运行时注解
annotation class Bind(val id: Int)