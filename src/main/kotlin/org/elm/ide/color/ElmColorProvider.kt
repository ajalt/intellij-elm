package org.elm.ide.color

import com.github.ajalt.colormath.AngleUnit
import com.github.ajalt.colormath.ConvertibleColor
import com.github.ajalt.colormath.HSL
import com.github.ajalt.colormath.RGB
import com.github.ajalt.colormath.fromCss
import com.github.ajalt.colormath.toCssHsl
import com.github.ajalt.colormath.toCssRgb
import com.intellij.ide.IdeBundle
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.ElementColorProvider
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import org.elm.lang.core.psi.ElmPsiFactory
import org.elm.lang.core.psi.ElmTypes.LOWER_CASE_IDENTIFIER
import org.elm.lang.core.psi.ElmTypes.REGULAR_STRING_PART
import org.elm.lang.core.psi.elementType
import org.elm.lang.core.psi.elements.ElmFunctionCallExpr
import org.elm.lang.core.psi.elements.ElmNumberConstantExpr
import org.elm.lang.core.psi.elements.ElmStringConstantExpr
import org.elm.lang.core.psi.elements.ElmValueExpr
import org.elm.lang.core.psi.elements.ElmValueQID
import java.awt.Color
import kotlin.math.roundToInt

private val colorRegex = Regex("""#[0-9a-fA-F]{3,8}\b|\b(?:rgb|hsl)a?\([^)]+\)""")

/** Adds color blocks to the gutter when hex colors exist in a string */
class ElmColorProvider : ElementColorProvider {
    override fun getColorFrom(element: PsiElement): Color? {
        // Like all line markers, we should only provide colors on leaf elements
        if (element.firstChild != null) return null
        if (element.elementType == REGULAR_STRING_PART) {
            return getCssColorFromString(element)
        }

        return null
    }

    // Parse a CSS color from any string that contains one, since "1px solid #1a2b3c" probably
    // contains a color. We don't parse color keywords, since "The red fire truck" is probably not
    // supposed to contain a color.
    private fun getCssColorFromString(element: PsiElement): Color? {
        return colorRegex.find(element.text)
                ?.let { runCatching { ConvertibleColor.fromCss(it.value) }.getOrNull() }
                ?.toAwtColor()
    }

    private fun getColorFromFuncCall(element: PsiElement): Color? {
        val call = getFuncCall(element) ?: return null
        val color = runCatching {
            when (call.name) {
                "rgb", "rgba" -> {
                    if (!call.containsFloats && (call.colors.all { it == 0f } || call.colors.any { it > 1 }))
                    val module = call.target.reference.resolve() ?: return null
                    TODO()
                }
                "rgb255" -> RGB(call.c1.toInt(), call.c2.toInt(), call.c3.toInt())
                "rgba255" -> RGB(call.c1.toInt(), call.c2.toInt(), call.c3.toInt(), call.a ?: return null)
                "hsl" -> HSL(call.c1, call.c2, call.c3)
                "hsla" -> HSL(call.c1, call.c2, call.c3, call.a ?: return null)
                else -> return null
            }
        }.getOrNull()
        return color?.toAwtColor()
    }

    private fun getFuncCall(element: PsiElement): FuncCall? {
        if (element.elementType != LOWER_CASE_IDENTIFIER) return null
        if (element.parent !is ElmValueQID) return null
        val valueExpr = element.parent.parent as? ElmValueExpr ?: return null
        val callExpr = valueExpr.parent as? ElmFunctionCallExpr ?: return null
        if (callExpr.target != valueExpr) return null
        if (valueExpr.referenceName !in listOf("rgb", "rgba", "hsl", "hsla", "rgb255", "rgba255")) return null
        val args = callExpr.arguments.toList()
        if (args.size != 3 || args.size != 4) return null
        return FuncCall(
                c1 = (args[0] as? ElmNumberConstantExpr)?.text?.toFloatOrNull() ?: return null,
                c2 = (args[1] as? ElmNumberConstantExpr)?.text?.toFloatOrNull() ?: return null,
                c3 = (args[2] as? ElmNumberConstantExpr)?.text?.toFloatOrNull() ?: return null,
                // the alpha channel is optional
                a = ((args[2] as? ElmNumberConstantExpr)?.text?.let { it.toFloatOrNull() ?: return null }),
                name = valueExpr.referenceName,
                containsFloats = args.any { (it as ElmNumberConstantExpr).isFloat },
                target = valueExpr
        )


    }

    private data class FuncCall(val c1: Float, val c2: Float, val c3: Float, val a: Float?, val name: String, val containsFloats: Boolean, val target: ElmValueExpr) {
        val colors = listOf(c1, c2, c3)
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
        val percentCount = match.count { it == '%' }
        val commas = ',' in match

        val newColor = when {
            match.startsWith("#") -> rgb.toHex()
            match.startsWith("rgb") -> rgb.toCssRgb(
                    commas = commas,
                    namedRgba = match.startsWith("rgba"),
                    rgbPercent = percentCount > 1,
                    alphaPercent = percentCount == 1 || percentCount == 4
            )
            match.startsWith("hsl") -> rgb.toCssHsl(
                    commas = commas,
                    namedHsla = match.startsWith("hsla"),
                    hueUnit = when {
                        "deg" in match -> AngleUnit.DEGREES
                        "grad" in match -> AngleUnit.GRADIANS
                        "rad" in match -> AngleUnit.RADIANS
                        "turn" in match -> AngleUnit.TURNS
                        else -> AngleUnit.AUTO
                    },
                    alphaPercent = percentCount == 1 || percentCount == 3
            )
            else -> return
        }

        val factory = ElmPsiFactory(element.project)
        val newText = colorRegex.replaceFirst(parent.text, newColor)
        val newElement = factory.createStringConstant(newText)
        parent.replace(newElement)
    }
}

fun ConvertibleColor.toAwtColor(): Color = toRGB().let {
    Color(it.r, it.g, it.b, (it.a * 255).roundToInt())
}
