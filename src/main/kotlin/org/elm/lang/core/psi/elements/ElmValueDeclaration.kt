package org.elm.lang.core.psi.elements

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.utils.inlays.InlayHintsChecker.Companion.pattern
import org.elm.ide.icons.ElmIcons
import org.elm.lang.core.psi.*
import org.elm.lang.core.psi.ElmTypes.*
import org.elm.lang.core.stubs.ElmValueDeclarationStub


/**
 * A top-level value/function declaration and/or annotation.
 *
 * e.g.
 *  - `x = 42`
 *  - `f x = 2 * x`
 *  - `x : Int -> Int`
 *  - ```
 *    x : Int -> Int
 *    x a = a
 *    ```
 */
class ElmValueDeclaration : ElmStubbedElement<ElmValueDeclarationStub>,
        ElmNameIdentifierOwner, ElmExposableTag, ElmDocTarget, ElmDeclarationTag {

    constructor(node: ASTNode) :
            super(node)

    constructor(stub: ElmValueDeclarationStub, stubType: IStubElementType<*, *>) :
            super(stub, stubType)

    val modificationTracker = SimpleModificationTracker()

    override fun getIcon(flags: Int) =
            ElmIcons.FUNCTION

    /**
     * The 'body' of the declaration. This is the right-hand side which is bound
     * to the name(s) on the left-hand side.
     *
     * In a well-formed program, this will be non-null.
     */
    val expression: ElmExpressionTag?
        get() = findChildByClass(ElmExpressionTag::class.java)

    /**
     * Names that are declared on the left-hand side of the equals sign in a value
     * declaration. In the basic case, this is the name of the function/value itself.
     * Optionally may also include "parameters" to the function. Parameters are simple,
     * lower-case identifiers like you would normally expect in a function, but also
     * any destructured names caused by pattern matching.
     *
     * @param includeParameters include names declared as parameters to the function
     *                          (also includes destructured names). The default is `true`
     */
    fun declaredNames(includeParameters: Boolean = true): List<ElmNameIdentifierOwner> {
        return if (includeParameters) namedParameters + this
        else listOf(this)
    }

    // TODO docs
    val typeAnnotationIdentifier: PsiElement?
        get() = getIdentifiers().find {
            it.nextSiblings.withoutWsOrComments.firstOrNull()?.elementType == COLON
        }

    // TODO docs
    val functionIdentifier: PsiElement?
        get() {
            val ids = getIdentifiers()
            if (ids.size == 2) return ids[1]
            if (ids[0].nextSiblings.withoutWsOrComments.firstOrNull()?.elementType == COLON) return null
            return ids[0]
        }

    /**
     * All parameter names declared in this function.
     *
     * e.g. `a`, `b`, `c`, `d`, and `e` in `foo a (b, (c, d)) {e} = 42`
     */
    val namedParameters: Collection<ElmNameDeclarationPatternTag>
        get() = descendantsOfType()

    /**
     * Zero or more parameters to the function
     */
    val patterns: List<ElmFunctionParamTag>
        get() = directChildrenOfType()

    /** The type annotation for this function, or `null` if there isn't one. */
    val typeAnnotation: ElmTypeExpression?
        get() = stubDirectChildrenOfType<ElmTypeExpression>().firstOrNull()

    override val docComment: PsiComment?
        get() = (prevSiblings.withoutWs.firstOrNull() as? PsiComment)
                ?.takeIf { it.text.startsWith("{-|") }

    /** The `=` element. In a well-formed program, this will be null only if there is no declaration */
    val eqElement: PsiElement? get() = findChildByType(EQ)

    override fun getNameIdentifier(): PsiElement {
        // this will be the annotation name if there's no signature, but that's fine
        return findChildrenByType<PsiElement>(LOWER_CASE_IDENTIFIER).last()
    }

    override fun getName(): String =
            stub?.name ?: nameIdentifier.text

    override fun setName(name: String): PsiElement {
        val factory = ElmPsiFactory(project)
        getIdentifiers().forEach {
            it.replace(factory.createLowerCaseIdentifier(name))
        }
        return this
    }

    override fun getTextOffset() =
            nameIdentifier.textOffset

    override fun getPresentation() =
            org.elm.ide.presentation.getPresentation(this)

    private fun getIdentifiers() = findChildrenByType<PsiElement>(LOWER_CASE_IDENTIFIER)
}
