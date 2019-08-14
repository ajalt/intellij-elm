package org.elm.lang.core.psi.elements

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import com.intellij.psi.util.PsiTreeUtil
import org.elm.ide.icons.ElmIcons
import org.elm.lang.core.psi.*
import org.elm.lang.core.stubs.ElmPlaceholderStub


/**
 * A top-level destructuring declaration.
 *
 * e.g. `(x, y) = (0, 0)`
 */
// In Elm 0.19, this is only valid inside a let block
class ElmDestructuringDeclaration : ElmStubbedElement<ElmPlaceholderStub> {

    constructor(node: ASTNode) :
            super(node)

    constructor(stub: ElmPlaceholderStub, stubType: IStubElementType<*, *>) :
            super(stub, stubType)

    val modificationTracker = SimpleModificationTracker()

    override fun getIcon(flags: Int) =
            ElmIcons.FUNCTION

    /** The pattern if this declaration is binding multiple names. */
    val pattern: ElmPattern
        get() = findNotNullChildByClass(ElmPattern::class.java)

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
     */
    fun declaredNames(): Collection<ElmNameIdentifierOwner> {
        return pattern.descendantsOfType()
    }

    /** The `=` element. In a well-formed program, this will not be null */
    val eqElement: PsiElement? get() = findChildByType(ElmTypes.EQ)
}
