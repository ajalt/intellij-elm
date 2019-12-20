package org.elm.lang.core.psi

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiBuilder
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiParserFacade
import com.intellij.psi.tree.IElementType
import com.intellij.util.IncorrectOperationException
import org.elm.lang.core.ElmFileType
import org.elm.lang.core.psi.ElmTypes.*
import org.elm.lang.core.psi.elements.*
import org.intellij.lang.annotations.Language


class ElmPsiFactory(private val project: Project) {
    companion object {
        /**
         * WARNING: this should only be called from the [ParserDefinition] hook
         * which takes [ASTNode]s from the [PsiBuilder] and emits [PsiElement].
         *
         * IMPORTANT: Must be kept in-sync with the BNF. The grammar rules are
         * written in CamelCase, but the IElementType constants that they correspond
         * to are generated by GrammarKit in ALL_CAPS. So if you have add a rule
         * named `FooBar` to the BNF, then you need to add a line here like:
         * `FOO_BAR -> return ElmFooBar(node)`. Don't forget to create the `ElmFooBar`
         * class.
         */
        fun createElement(node: ASTNode): PsiElement =
                when (node.elementType) {
                    ANONYMOUS_FUNCTION_EXPR -> ElmAnonymousFunctionExpr(node)
                    ANYTHING_PATTERN -> ElmAnythingPattern(node)
                    AS_CLAUSE -> ElmAsClause(node)
                    BIN_OP_EXPR -> ElmBinOpExpr(node)
                    CASE_OF_EXPR -> ElmCaseOfExpr(node)
                    CASE_OF_BRANCH -> ElmCaseOfBranch(node)
                    CHAR_CONSTANT_EXPR -> ElmCharConstantExpr(node)
                    CONS_PATTERN -> ElmConsPattern(node)
                    EXPOSED_OPERATOR -> ElmExposedOperator(node)
                    EXPOSED_TYPE -> ElmExposedType(node)
                    EXPOSED_VALUE -> ElmExposedValue(node)
                    EXPOSED_UNION_CONSTRUCTORS -> ElmExposedUnionConstructors(node)
                    EXPOSED_UNION_CONSTRUCTOR -> ElmExposedUnionConstructor(node)
                    EXPOSING_LIST -> ElmExposingList(node)
                    FIELD -> ElmField(node)
                    FIELD_ACCESS_EXPR -> ElmFieldAccessExpr(node)
                    FIELD_ACCESSOR_FUNCTION_EXPR -> ElmFieldAccessorFunctionExpr(node)
                    FIELD_TYPE -> ElmFieldType(node)
                    FUNCTION_CALL_EXPR -> ElmFunctionCallExpr(node)
                    FUNCTION_DECLARATION_LEFT -> ElmFunctionDeclarationLeft(node)
                    OPERATOR_DECLARATION_LEFT -> ElmOperatorDeclarationLeft(node) // TODO [drop 0.18] remove this line
                    OPERATOR_CONFIG -> ElmOperatorConfig(node) // TODO [drop 0.18] remove this line
                    GLSL_CODE_EXPR -> ElmGlslCodeExpr(node)
                    IF_ELSE_EXPR -> ElmIfElseExpr(node)
                    IMPORT_CLAUSE -> ElmImportClause(node)
                    INFIX_DECLARATION -> ElmInfixDeclaration(node)
                    INFIX_FUNC_REF -> ElmInfixFuncRef(node)
                    LET_IN_EXPR -> ElmLetInExpr(node)
                    LIST_EXPR -> ElmListExpr(node)
                    LIST_PATTERN -> ElmListPattern(node)
                    LOWER_PATTERN -> ElmLowerPattern(node)
                    LOWER_TYPE_NAME -> ElmLowerTypeName(node)
                    MODULE_DECLARATION -> ElmModuleDeclaration(node)
                    NEGATE_EXPR -> ElmNegateExpr(node)
                    NUMBER_CONSTANT_EXPR -> ElmNumberConstantExpr(node)
                    NULLARY_CONSTRUCTOR_ARGUMENT_PATTERN -> ElmNullaryConstructorArgumentPattern(node)
                    OPERATOR -> ElmOperator(node)
                    OPERATOR_AS_FUNCTION_EXPR -> ElmOperatorAsFunctionExpr(node)
                    TYPE_REF -> ElmTypeRef(node)
                    PARENTHESIZED_EXPR -> ElmParenthesizedExpr(node)
                    PATTERN -> ElmPattern(node)
                    PORT_ANNOTATION -> ElmPortAnnotation(node)
                    RECORD_EXPR -> ElmRecordExpr(node)
                    RECORD_BASE_IDENTIFIER -> ElmRecordBaseIdentifier(node)
                    RECORD_PATTERN -> ElmRecordPattern(node)
                    RECORD_TYPE -> ElmRecordType(node)
                    STRING_CONSTANT_EXPR -> ElmStringConstantExpr(node)
                    TUPLE_CONSTRUCTOR_EXPR -> ElmTupleConstructorExpr(node) // TODO [drop 0.18] remove this line
                    TUPLE_EXPR -> ElmTupleExpr(node)
                    TUPLE_PATTERN -> ElmTuplePattern(node)
                    TUPLE_TYPE -> ElmTupleType(node)
                    TYPE_ALIAS_DECLARATION -> ElmTypeAliasDeclaration(node)
                    TYPE_ANNOTATION -> ElmTypeAnnotation(node)
                    TYPE_DECLARATION -> ElmTypeDeclaration(node)
                    TYPE_EXPRESSION -> ElmTypeExpression(node)
                    TYPE_VARIABLE -> ElmTypeVariable(node)
                    UNION_VARIANT -> ElmUnionVariant(node)
                    UNION_PATTERN -> ElmUnionPattern(node)
                    UNIT_EXPR -> ElmUnitExpr(node)
                    UPPER_CASE_QID -> ElmUpperCaseQID(node)
                    VALUE_DECLARATION -> ElmValueDeclaration(node)
                    VALUE_EXPR -> ElmValueExpr(node)
                    VALUE_QID -> ElmValueQID(node)
                    else -> throw AssertionError("Unknown element type: " + node.elementType)
                }
    }

    fun createLowerCaseIdentifier(text: String): PsiElement =
            createFromText("$text = 42", LOWER_CASE_IDENTIFIER)
                    ?: error("Invalid lower-case identifier: `$text`")

    fun createUpperCaseIdentifier(text: String): PsiElement =
            createFromText<ElmTypeAliasDeclaration>("type alias $text = Int")
                    ?.upperCaseIdentifier
                    ?: error("Invalid upper-case identifier: `$text`")

    fun createUpperCaseQID(text: String): ElmUpperCaseQID =
            createFromText("module $text exposing (..)")
                    ?: error("Invalid upper-case QID: `$text`")

    fun createValueQID(text: String): ElmValueQID =
            createFromText("f = $text")
                    ?: error("Invalid value QID: `$text`")

    fun createStringConstant(text: String): ElmStringConstantExpr =
            createFromText("f = $text")
                    ?: error("Invalid string: `$text`")

    fun createAnythingPattern(): ElmAnythingPattern =
            createFromText("f _ = 1")
                    ?: error("Invalid anything pattern")

    fun createOperatorIdentifier(text: String): PsiElement =
            createFromText("f = x $text y", OPERATOR_IDENTIFIER)
                    ?: error("Invalid operator identifier: `$text`")

    fun createGlslExpr(text: String): ElmGlslCodeExpr =
            createFromText("f = x $text y")
                    ?: error("Invalid glsl expression: `$text`")

    fun createImport(moduleName: String, alias: String?): ElmImportClause {
        val asClause = if (alias != null) " as $alias" else ""
        return createFromText("import $moduleName$asClause")
                ?: error("Failed to create import of $moduleName")
    }

    fun createImportExposing(moduleName: String, exposedNames: List<String>): ElmImportClause =
            createFromText("import $moduleName exposing (${exposedNames.joinToString(", ")})")
                    ?: error("Failed to create import of $moduleName exposing $exposedNames")

    fun createCaseOfBranches(indent: String, patterns: List<String>): List<ElmCaseOfBranch> =
            patterns.joinToString("\n\n$indent", prefix = "foo = case 1 of\n\n$indent") { "$it ->\n$indent    " }
                    .let { createFromText<ElmValueDeclaration>(it) }
                    ?.descendantOfType<ElmCaseOfExpr>()?.branches
                    ?: error("Failed to create case of branches from $patterns")

    fun createLetInWrapper(indent: String, newDeclName: String, newDeclBody: String, bodyText: String): ElmLetInExpr {
        // NOTE: Assumes that each line in newDeclBody has had its indent normalized to match the indent of the first line.
        //       Each line of newDeclBody must start with a non-whitespace character.
        val newDeclBodyIndented = newDeclBody.lines().joinToString("\n") { "$indent        $it" }
        val code = """
        |foo =
        |${indent}let
        |${indent}    $newDeclName =
        |$newDeclBodyIndented
        |${indent}in
        |${indent}$bodyText
        """.trimMargin()
        return createFromText<ElmValueDeclaration>(code)
                ?.descendantOfType()
                ?: error("Failed to create let/in wrapper")
    }

    fun createNewline(): PsiElement = createWhitespace("\n")

    fun createWhitespace(ws: String): PsiElement =
            PsiParserFacade.SERVICE.getInstance(project).createWhiteSpaceFromText(ws)

    fun createElements(@Language("Elm") code: String): List<PsiElement> =
            PsiFileFactory.getInstance(project)
                    .createFileFromText("DUMMY.elm", ElmFileType, code)
                    .children.asList()

    private inline fun <reified T : PsiElement> createFromText(code: String): T? =
            PsiFileFactory.getInstance(project)
                    .createFileFromText("DUMMY.elm", ElmFileType, code)
                    .descendantOfType()

    private fun createFromText(code: String, elementType: IElementType): PsiElement? =
            PsiFileFactory.getInstance(project)
                    .createFileFromText("DUMMY.elm", ElmFileType, code)
                    .descendants.find { it.elementType == elementType }

    private fun error(msg: String): Nothing = throw IncorrectOperationException(msg)
}
