package org.pulp.viewdsl.anno

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnLongClick(val value: String = "")//milliSecond