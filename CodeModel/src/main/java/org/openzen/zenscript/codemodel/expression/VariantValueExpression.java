/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.expression;

import java.util.List;
import org.openzen.zenscript.codemodel.definition.VariantDefinition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class VariantValueExpression extends Expression {
	public final VariantDefinition.Option option;
	public final Expression[] arguments;
	
	public VariantValueExpression(CodePosition position, ITypeID variantType, VariantDefinition.Option option) {
		this(position, variantType, option, null);
	}
	
	public VariantValueExpression(CodePosition position, ITypeID variantType, VariantDefinition.Option option, Expression[] arguments) {
		super(position, variantType, arguments == null ? null : multiThrow(position, arguments));
		
		this.option = option;
		this.arguments = null;
	}
	
	public int getNumberOfArguments() {
		return arguments == null ? 0 : arguments.length;
	}
	
	@Override
	public Expression call(CodePosition position, TypeScope scope, List<ITypeID> hints, CallArguments arguments) {
		if (arguments != null)
			return super.call(position, scope, hints, arguments);
		
		return new VariantValueExpression(position, type, option, arguments.arguments);
	}
	
	@Override
	public <T> T accept(ExpressionVisitor<T> visitor) {
		return visitor.visitVariantValue(this);
	}
}