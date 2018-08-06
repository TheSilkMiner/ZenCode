/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.context.TypeResolutionContext;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.parser.ParsedAnnotation;
import org.openzen.zenscript.parser.PrecompilationState;
import org.openzen.zenscript.parser.expression.ParsedExpression;
import org.openzen.zenscript.parser.type.IParsedType;

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
	
	private final int autoGetter;
	private final int autoSetter;
	
	private FieldMember compiled;
	private boolean isCompiled = false;
	
	public ParsedField(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			ParsedAnnotation[] annotations,
			String name,
			IParsedType type,
			ParsedExpression expression,
			boolean isFinal,
			int autoGetter,
			int autoSetter)
	{
		super(definition, annotations);
		
		this.position = position;
		this.modifiers = modifiers;
		this.name = name;
		this.type = type;
		this.expression = expression;
		this.isFinal = isFinal;
		this.autoGetter = autoGetter;
		this.autoSetter = autoSetter;
	}

	@Override
	public void linkTypes(TypeResolutionContext context) {
		compiled = new FieldMember(
				position,
				definition,
				modifiers | (isFinal ? Modifiers.FINAL : 0),
				name,
				context.getThisType(),
				type.compile(context),
				context.getTypeRegistry(),
				autoGetter,
				autoSetter,
				null);
	}

	@Override
	public FieldMember getCompiled() {
		return compiled;
	}

	@Override
	public void compile(BaseScope scope) {
		if (isCompiled)
			return;
		isCompiled = true;
		
		compiled.annotations = ParsedAnnotation.compileForMember(annotations, compiled, scope);
		
		if (expression != null) {
			Expression initializer = expression
					.compile(new ExpressionScope(scope, compiled.type))
					.eval()
					.castImplicit(position, scope, compiled.type);
			compiled.setInitializer(initializer);
			
			if (compiled.type == BasicTypeID.UNDETERMINED)
				compiled.type = initializer.type;
		} else if (compiled.type == BasicTypeID.UNDETERMINED) {
			throw new CompileException(position, CompileExceptionCode.PRECOMPILE_FAILED, "Could not infer type since no initializer is given");
		}
	}
}
