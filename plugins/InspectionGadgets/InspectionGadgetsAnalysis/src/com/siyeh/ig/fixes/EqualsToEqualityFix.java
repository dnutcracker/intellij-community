// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.siyeh.ig.fixes;

import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.siyeh.InspectionGadgetsBundle;
import com.siyeh.ig.InspectionGadgetsFix;
import com.siyeh.ig.PsiReplacementUtil;
import com.siyeh.ig.psiutils.BoolUtils;
import com.siyeh.ig.psiutils.CommentTracker;
import com.siyeh.ig.psiutils.EqualityCheck;
import com.siyeh.ig.psiutils.ParenthesesUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Bas Leijdekkers
 */
public class EqualsToEqualityFix extends InspectionGadgetsFix {

  private final boolean myNegated;

  private EqualsToEqualityFix(boolean negated) {
    myNegated = negated;
  }

  @Nullable
  public static EqualsToEqualityFix buildFix(PsiMethodCallExpression expressionToFix, boolean negated) {
    if (expressionToFix.getParent() instanceof PsiExpressionStatement) {
      // replacing top level equals() call will produce red code
      return null;
    }
    return new EqualsToEqualityFix(negated);
  }

  @Nls
  @NotNull
  @Override
  public String getFamilyName() {
    return myNegated
           ? InspectionGadgetsBundle.message("not.equals.to.equality.quickfix")
           : InspectionGadgetsBundle.message("equals.to.equality.quickfix");
  }

  @Override
  protected void doFix(Project project, ProblemDescriptor descriptor) {
    final PsiMethodCallExpression call = PsiTreeUtil.getParentOfType(descriptor.getPsiElement(), PsiMethodCallExpression.class, false);
    EqualityCheck check = EqualityCheck.from(call);
    if (check == null) return;
    PsiExpression lhs = check.getLeft();
    PsiExpression rhs = check.getRight();
    final PsiElement parent = ParenthesesUtils.getParentSkipParentheses(call);
    final CommentTracker commentTracker = new CommentTracker();
    final String lhsText = commentTracker.text(lhs, ParenthesesUtils.EQUALITY_PRECEDENCE);
    final String rhsText = commentTracker.text(rhs, ParenthesesUtils.EQUALITY_PRECEDENCE);
    if (parent instanceof PsiExpression && BoolUtils.isNegation((PsiExpression)parent)) {
      PsiReplacementUtil.replaceExpression((PsiExpression)parent, lhsText + "!=" + rhsText, commentTracker);
    }
    else {
      PsiReplacementUtil.replaceExpression(call, lhsText + "==" + rhsText, commentTracker);
    }
  }
}
