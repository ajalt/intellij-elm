package org.elm.ide.color

import com.github.ajalt.colormath.ColorMath
import com.github.ajalt.colormath.ConvertibleColor
import com.github.ajalt.colormath.HSL
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
import kotlin.math.PI
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
                else -> rgb.a.render()
            }
            if (rgb.a != 1f || name == "rgba") args += "$alphaSep$alpha"
            return "$name($args)"
        }

        val newValue = when {
            match.startsWith('#') -> rgb.toHex(withNumberSign = true) + if (rgb.a == 1f) "" else {
                (rgb.a * 255).roundToInt().toString(16).padStart(2, '0')
            }
            match.startsWith("rgb") -> renderFunc(
                    name = if (match.startsWith("rgba")) "rgba" else "rgb",
                    colors = listOf(rgb.r, rgb.g, rgb.b).map { n ->
                        when {
                            match.count { it == '%' } > 1 -> "${(n * 100 / 255f).roundToInt()}%"
                            else -> n.toString()
                        }
                    },
                    alphaPercent = match.count { it == '%' }.let { it == 1 || it == 4 }
            )
            match.startsWith("hsl") -> {
                val (h, s, l) = rgb.toHSL()
                val hue = when {
                    "grad" in match -> "${h.asGrad().render()}grad"
                    "rad" in match -> "${h.asRad().render()}rad"
                    "turn" in match -> "${h.asTurns().render()}turn"
                    else -> h.toString()
                }
                renderFunc(
                        name = if (match.startsWith("hsla")) "hsla" else "hsl",
                        colors = listOf(hue, "$s%", "$l%"),
                        alphaPercent = match.count { it == '%' } in listOf(1, 3)
                )
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

private fun Int.asGrad(): Float = this * 200 / 180f
private fun Int.asRad(): Float = (this * PI / 180).toFloat()
private fun Int.asTurns(): Float = this / 360f
private fun Float.render(): String = when {
    this == 1f -> "1"
    this == 0f -> "0"
    else -> String.format("%.2f", this).trim('0')
}
