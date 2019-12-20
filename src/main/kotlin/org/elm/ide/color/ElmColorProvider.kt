package org.elm.ide.color

import com.github.ajalt.colormath.ColorMath
import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiElement
import org.elm.lang.core.psi.ElmTypes.REGULAR_STRING_PART
import org.elm.lang.core.psi.elementType
import org.elm.lang.core.psi.elements.ElmFunctionCallExpr
import org.elm.lang.core.psi.elements.ElmValueExpr
import java.awt.Color
import kotlin.math.roundToInt

private val colorRegex = Regex((ColorMath.cssKeywordColors.keys + """#[0-9a-fA-F]{3,8}\b|\b(?:rgb|hsl)a?\([^)]+\)""").joinToString("|"))

/** Adds color blocks to the gutter when hex colors exist in a string */
class ElmColorProvider : ElementColorProvider {
    override fun getColorFrom(element: PsiElement): Color? {
        // Like all line markers, we should only provide colors on leaf elements
        if (element.firstChild != null) return null
        // TODO elm-css rgb etc.
        if (element.elementType != REGULAR_STRING_PART) return null

        val callExpr = element.parent?.parent as? ElmFunctionCallExpr ?: return null

        if ((callExpr.target as? ElmValueExpr)?.referenceName == "style") {
           return getStyleArgColor(element, callExpr)
        }

        return null
    }

    // Parse a CSS color from the second argument of any function that looks like
    // Html.Attributes.style. We don't restrict it to that module, since there exist copies e.g. in
    // elm-css
    private fun getStyleArgColor(element: PsiElement, callExpr: ElmFunctionCallExpr) : Color?{
        // Only the second argument can contain colors
        val arguments = callExpr.arguments.toList()
        if (arguments.size != 2 || arguments[1] != element.parent) return null

        return colorRegex.find(element.text)
                ?.let { runCatching { ColorMath.parseCssColor(it.value) }.getOrNull() }
                ?.toRGB()
                ?.let { Color(it.r, it.g, it.b, (it.a * 255).roundToInt()) }
    }

    override fun setColorTo(element: PsiElement, color: Color) {
        return // TODO
    }
}
