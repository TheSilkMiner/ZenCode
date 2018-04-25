/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.expression.NullExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionNull extends ParsedExpression {
	public ParsedExpressionNull(CodePosition position) {
		super(position);
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		return new NullExpression(position);
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}
}