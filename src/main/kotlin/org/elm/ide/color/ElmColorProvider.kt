package org.elm.ide.color

import com.github.ajalt.colormath.ColorMath
import com.github.ajalt.colormath.ConvertibleColor
import com.github.ajalt.colormath.RGB
import com.intellij.ide.IdeBundle
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import org.elm.lang.core.psi.ElmPsiFactory
import org.elm.lang.core.psi.ElmTypes.REGULAR_STRING_PART
import org.elm.lang.core.psi.elementType
import org.elm.lang.core.psi.elements.ElmStringConstantExpr
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
                ?.toAwtColor()
    }

    override fun setColorTo(element: PsiElement, color: Color) {
        if (element.firstChild != null) return
        if (element.elementType == REGULAR_STRING_PART) {
            val document = PsiDocumentManager.getInstance(element.project).getDocument(element.containingFile)
            CommandProcessor.getInstance().executeCommand(
                    element.project,
                    { setCssColorInString(element, color) },
                    // This is the message that the JavaColorProvider uses
                    IdeBundle.message("change.color.command.text"),
                    null,
                    document
            )
        }
    }

    private fun setCssColorInString(element: PsiElement, color: Color) {
        val parent = element.parent as? ElmStringConstantExpr ?: return
        val match = colorRegex.find(element.text)?.value ?: return
        val rgb = RGB(color.red, color.green, color.blue, color.alpha / 255f)
        fun renderFunc(name: String, colors: List<String>, alphaPercent: Boolean): String {
            val colorSep = if (',' in match) ", " else " "
            val alphaSep = if (',' in match) ", " else " / "
            var args = colors.joinToString(colorSep)
            val alpha = when {
                alphaPercent -> "${(rgb.a * 100).roundToInt()}%"
                else -> if (rgb.a == 1f) "1" else  rgb.a.toString().let {
                    it.take(it.indexOf('.') + 2)
                }
            }
            if (rgb.a != 1f || name == "rgba") args += "$alphaSep$alpha"
            return "$name($args)"
        }

        val newValue = when {
            match.startsWith('#') -> rgb.toHex(withNumberSign = true) + if (rgb.a == 1f) "" else {
                (rgb.a * 255).roundToInt().toString(16).padStart(2, '0')
            }
            match.startsWith("rgb") -> {
                val name = if (match.startsWith("rgba")) "rgba" else "rgb"
                val colorPercents = match.count { it == '%' } > 1
                val alphaPercent = match.count { it == '%' }.let { it == 1 || it == 4 }

                fun render(i: Int) = when {
                    colorPercents -> "${(i * 100 / 255f).roundToInt()}%"
                    else -> i.toString()
                }

                renderFunc(name, listOf(rgb.r, rgb.g, rgb.b).map { render(it) }, alphaPercent)
            }
            match.startsWith("hsl") -> {
                val hsl = rgb.toHSL()
                val name = if (match.startsWith("hsla")) "hsla" else "hsl"
                val colorSep = if (',' in match) ", " else " "
                val alphaSep = if (',' in match) ", " else " / "

                "TODO"
            }
            else -> return
        }

        val factory = ElmPsiFactory(element.project)
        val newText = colorRegex.replaceFirst(parent.text, newValue)
        val newElement = factory.createStringConstant(newText)
        parent.replace(newElement)
    }
}

fun ConvertibleColor.toAwtColor(): Color = toRGB().let {
    Color(it.r, it.g, it.b, (it.a * 255).roundToInt())
}
