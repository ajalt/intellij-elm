package org.elm.ide.color

import com.github.ajalt.colormath.ConvertibleColor
import com.github.ajalt.colormath.HSL
import com.github.ajalt.colormath.RGB
import com.intellij.openapi.application.runWriteAction
import com.intellij.util.ui.ColorIcon
import org.elm.lang.ElmTestBase
import org.intellij.lang.annotations.Language


// TODO: ElmLineMarkerProviderTestBase?
internal class ElmColorProviderTest : ElmTestBase() {
    fun `test value with other string content`() = doGutterTest("""
main = ("border", "1px solid #aabbcc")
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
    fun `test hsl(270,60%,70%)`() = doFormatTest("hsl(270,60%,70%)")
    fun `test hsl(270, 60%, 70%)`() = doFormatTest("hsl(270, 60%, 70%)")
    fun `test hsl(270 60% 70%)`() = doFormatTest("hsl(270 60% 70%)")
    fun `test hsl(270deg, 60%, 70%)`() = doFormatTest("hsl(270deg, 60%, 70%)")
    fun `test hsl(4_71239rad, 60%, 70%)`() = doFormatTest("hsl(4.71239rad, 60%, 70%)")
    fun `test hsl(_75turn, 60%, 70%)`() = doFormatTest("hsl(.75turn, 60%, 70%)")
    fun `test hsl(270, 60%, 50%, _15)`() = doFormatTest("hsl(270, 60%, 50%, .15)")
    fun `test hsl(270, 60%, 50%, 15%)`() = doFormatTest("hsl(270, 60%, 50%, 15%)")
    fun `test hsl(270 60% 50% _ _15)`() = doFormatTest("hsl(270 60% 50% / .15)")
    fun `test hsl(270 60% 50% _ 15%)`() = doFormatTest("hsl(270 60% 50% / 15%)")

    fun `test write #f09`() = doWriteFormatTest("#f09", "#7b2d43")
    fun `test write #ff0099`() = doWriteFormatTest("#ff0099", "#7b2d43")
    fun `test write rgb(255,0,153)`() = doWriteFormatTest("rgb(255,0,153)", "rgb(123, 45, 67)")
    fun `test write rgb(255, 0, 153)`() = doWriteFormatTest("rgb(255, 0, 153)", "rgb(123, 45, 67)")
    fun `test write rgb(255, 0, 153_0)`() = doWriteFormatTest("rgb(255, 0, 153.0)", "rgb(123, 45, 67)")
    fun `test write rgb(100%,0%,60%)`() = doWriteFormatTest("rgb(100%,0%,60%)", "rgb(48%, 18%, 26%)")
    fun `test write rgb(100%, 0%, 60%)`() = doWriteFormatTest("rgb(100%, 0%, 60%)", "rgb(48%, 18%, 26%)")
    fun `test write rgb(255 0 153)`() = doWriteFormatTest("rgb(255 0 153)", "rgb(123 45 67)")
    fun `test write #f090`() = doWriteFormatTest("#f090", "#7b2d4300", RGB(123, 45, 67, 0f))
    fun `test write #ff009900`() = doWriteFormatTest("#ff00990", "#7b2d4300", RGB(123, 45, 67, 0f))
    fun `test write rgba(255, 0, 153, 1)`() = doWriteFormatTest("rgba(255, 0, 153, 1)", "rgba(123, 45, 67, 1)")
    fun `test write rgb(255, 0, 153, 100%)`() = doWriteFormatTest("rgb(255, 0, 153, 100%)", "rgb(123, 45, 67, 50%)", RGB(123, 45, 67, .5f))
    fun `test write rgb(255 0 153 _ 1)`() = doWriteFormatTest("rgb(255 0 153 / 1)", "rgb(123 45 67 / .5)", RGB(123, 45, 67, .5f))
    fun `test write rgb(255 0 153 _ 100%)`() = doWriteFormatTest("rgb(255 0 153 / 100%)", "rgb(123 45 67 / 50%)", RGB(123, 45, 67, .5f))
    fun `test write hsl(270,60%,70%)`() = doWriteFormatTest("hsl(270,60%,70%)","hsl(123, 45%, 67%, .5)", HSL(123, 45, 67, .5f))
    fun `test write hsl(270, 60%, 70%)`() = doWriteFormatTest("hsl(270, 60%, 70%)","hsl(123, 45%, 67%)", HSL(123, 45, 67))
    fun `test write hsl(270 60% 70%)`() = doWriteFormatTest("hsl(270 60% 70%)","hsl(123 45% 67%)", HSL(123, 45, 67))
    fun `test write hsl(270, 60%, 50%, _15)`() = doWriteFormatTest("hsl(270, 60%, 50%, .15)","hsl(123, 45%, 67%, .5)", HSL(123, 45, 67, .5f))
    fun `test write hsl(270, 60%, 50%, 15%)`() = doWriteFormatTest("hsl(270, 60%, 50%, 15%)","hsl(123, 45%, 67%, 50%)", HSL(123, 45, 67, .5f))
    fun `test write hsl(270 60% 50% _ _15)`() = doWriteFormatTest("hsl(270 60% 50% / .15)","hsl(123 45% 67% / .5)", HSL(123, 45, 67, .5f))
    fun `test write hsl(270 60% 50% _ 15%)`() = doWriteFormatTest("hsl(270 60% 50% / 15%)","hsl(123 45% 67% / 50%)", HSL(123, 45, 67, .5f))
    fun `test write hsl(270grad,60%,70%)`() = doWriteFormatTest("hsl(270grad,60%,70%)","hsl(136.67grad, 45%, 67%, .5)", HSL(123, 45, 67, .5f))
    fun `test write hsl(270rad,60%,70%)`() = doWriteFormatTest("hsl(270rad,60%,70%)","hsl(2.15rad, 45%, 67%, .5)", HSL(123, 45, 67, .5f))
    fun `test write hsl(270turn,60%,70%)`() = doWriteFormatTest("hsl(270turn,60%,70%)","hsl(.34turn, 45%, 67%, .5)", HSL(123, 45, 67, .5f))


    private fun doFormatTest(color: String) {
        doGutterTest("""
        import Html.Attributes exposing (style)
        main = style "color" "$color"
        """.trimIndent())
    }

    private fun doGutterTest(@Language("Elm") code: String) {
        InlineFile(code)
        val count = myFixture.findAllGutters().count { it.icon is ColorIcon }
        assertEquals(1, count)
    }


    private fun doWriteFormatTest(before: String, after: String, color: ConvertibleColor = RGB(123, 45, 67)) {
        doWriteTest(color, "main = \". $before {-caret-}.\"", "main = \". $after .\"")
    }

    private fun doWriteTest(color: ConvertibleColor, @Language("Elm") before: String, @Language("Elm") after: String) {
        InlineFile(before)
        val element = myFixture.file.findElementAt(myFixture.caretOffset - 1)
        requireNotNull(element)
        val awtColor = color.toAwtColor()
        runWriteAction {
            ElmColorProvider().setColorTo(element, awtColor)
        }
        myFixture.checkResult(after)
    }
}
