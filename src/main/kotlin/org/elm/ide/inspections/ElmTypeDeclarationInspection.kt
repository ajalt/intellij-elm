package org.elm.ide.inspections

import org.elm.lang.core.diagnostics.ElmDiagnostic
import org.elm.lang.core.psi.ElmPsiElement
import org.elm.lang.core.psi.elements.ElmTypeAliasDeclaration
import org.elm.lang.core.psi.elements.ElmTypeDeclaration
import org.elm.lang.core.types.typeExpressionInference
import org.elm.lang.core.types.variantInference

class ElmTypeDeclarationInspection : ElmDiagnosticBasedInspection() {
    override fun getElementDiagnostics(element: ElmPsiElement): Iterable<ElmDiagnostic> {
        return when (element) {
            is ElmTypeDeclaration -> element.typeExpressionInference().diagnostics + element.variantInference().diagnostics
            is ElmTypeAliasDeclaration -> element.typeExpressionInference().diagnostics
            else -> emptyList()
        }
    }
}
