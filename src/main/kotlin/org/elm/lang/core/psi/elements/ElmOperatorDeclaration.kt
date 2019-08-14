package org.elm.lang.core.psi.elements

import com.intellij.lang.ASTNode
import com.intellij.openapi.util.SimpleModificationTracker
import com.intellij.psi.PsiElement
import com.intellij.psi.stubs.IStubElementType
import org.elm.ide.icons.ElmIcons
import org.elm.lang.core.psi.*
import org.elm.lang.core.stubs.ElmOperatorDeclarationStub

// TODO [drop 0.18] delete this whole file

/**
 * A top-level operator declaration. 0.18 only
 */
class ElmOperatorDeclaration : ElmStubbedNamedElementImpl<ElmOperatorDeclarationStub>,
        ElmExposableTag, ElmDeclarationTag {

    constructor(node: ASTNode) :
            super(node, IdentifierCase.OPERATOR)

    constructor(stub: ElmOperatorDeclarationStub, stubType: IStubElementType<*, *>) :
            super(stub, stubType, IdentifierCase.OPERATOR)

    val modificationTracker = SimpleModificationTracker()

    override fun getIcon(flags: Int) =
            ElmIcons.FUNCTION

    val operatorIdentifier: PsiElement
        get() = findNotNullChildByType(ElmTypes.OPERATOR_IDENTIFIER)

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
        return if (includeParameters) descendantsOfType<ElmLowerPattern>() + this
        else listOf(this)
    }

    /** The `=` element. In a well-formed program, this will not be null */
    val eqElement: PsiElement? get() = findChildByType(ElmTypes.EQ)
}
