package org.pulp.viewdsl.anno

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnClick(val value: String = "",
                         val interval: Long = 0)//milliSecond