package org.elm.ide.color

import com.intellij.util.ui.ColorIcon
import org.elm.lang.ElmTestBase
import org.intellij.lang.annotations.Language


// TODO: ElmLineMarkerProviderTestBase?
internal class ElmColorProviderTest : ElmTestBase() {
    fun `test value with other string content`() = doTest("""
import Html.Attributes
 
main =
    Html.Attributes.style "border" "1px solid #aabbcc"
""")

    // format test cases from https://developer.mozilla.org/en-US/docs/Web/CSS/color_value

    fun `test #f09`() = doFormatTest("#f09")
    fun `test #F09`() = doFormatTest("#F09")
    fun `test #ff0099`() = doFormatTest("#ff0099")
    fun `test #FF0099`() = doFormatTest("#FF0099")
    fun `test rgb(255,0,153)`() = doFormatTest("rgb(255,0,153)")
    fun `test rgb(255, 0, 153)`() = doFormatTest("rgb(255, 0, 153)")
    fun `test rgb(255, 0, 153_0)`() = doFormatTest("rgb(255, 0, 153.0)")
    fun `test rgb(100%,0%,60%)`() = doFormatTest("rgb(100%,0%,60%)")
    fun `test rgb(100%, 0%, 60%)`() = doFormatTest("rgb(100%, 0%, 60%)")
    fun `test rgb(255 0 153)`() = doFormatTest("rgb(255 0 153)")
    fun `test #f09f`() = doFormatTest("#f09f")
    fun `test #F09F`() = doFormatTest("#F09F")
    fun `test #ff0099ff`() = doFormatTest("#ff0099ff")
    fun `test #FF0099FF`() = doFormatTest("#FF0099FF")
    fun `test rgb(255, 0, 153, 1)`() = doFormatTest("rgb(255, 0, 153, 1)")
    fun `test rgb(255, 0, 153, 100%)`() = doFormatTest("rgb(255, 0, 153, 100%)")
    fun `test rgb(255 0 153 _ 1)`() = doFormatTest("rgb(255 0 153 / 1)")
    fun `test rgb(255 0 153 _ 100%)`() = doFormatTest("rgb(255 0 153 / 100%)")
    fun `test rgb(255, 0, 153_6, 1)`() = doFormatTest("rgb(255, 0, 153.6, 1)")
    fun `test rgb(1e2, _5e1, _5e0, +_25e2%)`() = doFormatTest("rgb(1e2, .5e1, .5e0, +.25e2%)")

    private fun doFormatTest(color: String) {
        doTest("""
        import Html.Attributes exposing (style)
        main = style "color" "$color"
        """.trimIndent())
    }

    private fun doTest(@Language("Elm") code: String) {
        configureByFileTree("""
        --@ Html/Attributes.elm
        module Html.Attributes exposing (..)
        type Attribute msg = Attribute
        style : String -> String -> Attribute msg
        style key value = Attribute
        
        --@ Main.elm
        {-caret-}
        """.trimIndent() + "\n" + code)
        val count = myFixture.findAllGutters().count { it.icon is ColorIcon }
        assertEquals(1, count)
    }
}
