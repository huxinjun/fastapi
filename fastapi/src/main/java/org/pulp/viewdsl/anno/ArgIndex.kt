package org.pulp.viewdsl.anno

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ArgIndex(val value: Int)