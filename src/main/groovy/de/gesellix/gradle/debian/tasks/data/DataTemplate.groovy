package de.gesellix.gradle.debian.tasks.data

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.util.ConfigureUtil


class DataTemplate {

    String[] paths = null

    def mapper(Closure closure) {
        ConfigureUtil.configure(closure, mapper)
    }
}
