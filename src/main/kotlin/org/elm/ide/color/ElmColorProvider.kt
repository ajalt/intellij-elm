package org.elm.ide.color

import com.github.ajalt.colormath.ColorMath
import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import org.elm.lang.core.psi.ElmTypes.REGULAR_STRING_PART
import org.elm.lang.core.psi.elementType
import java.awt.Color
import kotlin.math.roundToInt

private val colorRegex = Regex("""#[0-9a-fA-F]{3,8}\b|\b(?:rgb|hsl)a?\([^)]+\)""")

/** Adds color blocks to the gutter when hex colors exist in a string */
class ElmColorProvider : ElementColorProvider {
    override fun getColorFrom(element: PsiElement): Color? {
        // Like all line markers, we should only provide colors on leaf elements
        if (element.firstChild != null) return null
        // TODO elm-css rgb etc.
        if (element.elementType == REGULAR_STRING_PART) {
            return getCssColorFromString(element)
        }

        return null
    }

    // Parse a CSS color from any string that contains one, since "1px solid #1a2b3c" probably
    // contains a color. We don't parse color keywords, since "The red fire truck" is probably not
    // supposed to contain a color.
    private fun getCssColorFromString(element: PsiElement) : Color?{
        return colorRegex.find(element.text)
                ?.let { runCatching { ColorMath.parseCssColor(it.value) }.getOrNull() }
                ?.toRGB()
                ?.let { Color(it.r, it.g, it.b, (it.a * 255).roundToInt()) }
    }

    override fun setColorTo(element: PsiElement, color: Color) {
        return // TODO
    }
}
