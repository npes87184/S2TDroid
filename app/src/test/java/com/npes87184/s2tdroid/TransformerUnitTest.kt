package com.npes87184.s2tdroid

import com.npes87184.s2tdroid.libs.Transformer
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Transformer local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class TransformerUnitTest {
    companion object {
        const val simplify = "hello! 龙傲天"
        const val traditional = "hello! 龍傲天"
    }

    @Test
    fun testToSimplify() {
        assertEquals(simplify, Transformer.toSimplify(traditional))
    }

    @Test
    fun testToTraditional() {
        assertEquals(traditional, Transformer.toTraditional(simplify))
    }
}