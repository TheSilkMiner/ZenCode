/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.linker.BaseScope;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.type.IParsedType;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedField extends ParsedDefinitionMember {
	private final CodePosition position;
	private final int modifiers;
	private final String name;
	private final IParsedType type;
	private final ParsedExpression expression;
	private final boolean isFinal;
	
	private FieldMember compiled;
	
	public ParsedField(CodePosition position, int modifiers, String name, IParsedType type, ParsedExpression expression, boolean isFinal) {
		this.position = position;
		this.modifiers = modifiers;
		this.name = name;
		this.type = type;
		this.expression = expression;
		this.isFinal = isFinal;
	}
	
	@Override
	public void linkInnerTypes(HighLevelDefinition definition) {
		
	}

	@Override
	public void linkTypes(BaseScope scope) {
		compiled = new FieldMember(position, modifiers, name, type.compile(scope), isFinal);
	}

	@Override
	public FieldMember getCompiled() {
		return compiled;
	}

	@Override
	public void compile(BaseScope scope) {
		if (expression != null) {
			Expression initializer = expression
					.compile(new ExpressionScope(scope, compiled.type))
					.eval()
					.castImplicit(position, scope, compiled.type);
			compiled.setInitializer(initializer);
		}
	}
}