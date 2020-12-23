package org.pulp.viewdsl.anno

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Argument(val value: Int = 0)//index