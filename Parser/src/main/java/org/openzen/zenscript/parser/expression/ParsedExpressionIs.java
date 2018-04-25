/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.IsExpression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedExpressionIs extends ParsedExpression {
	private final ParsedExpression expression;
	private final IParsedType type;
	
	public ParsedExpressionIs(CodePosition position, ParsedExpression expression, IParsedType type) {
		super(position);
		
		this.expression = expression;
		this.type = type;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		ITypeID isType = type.compile(scope);
		Expression expression = this.expression.compile(scope.withHint(isType)).eval();
		return new IsExpression(position, expression, isType);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
}