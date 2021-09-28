package com.arkivanov.gradle

import org.junit.Test
import kotlin.test.assertEquals

class TargetTest {

    @Test
    fun `all split targets defined`() {
        assertEquals(Target::class.sealedSubclasses.size, Target.LINUX_SPLIT_CLASSES.size + Target.MACOS_SPLIT_CLASSES.size)
    }
}
