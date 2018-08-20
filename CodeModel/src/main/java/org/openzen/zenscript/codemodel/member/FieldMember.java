/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.GenericMapper;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.GetFieldExpression;
import org.openzen.zenscript.codemodel.expression.GetFunctionParameterExpression;
import org.openzen.zenscript.codemodel.expression.SetFieldExpression;
import org.openzen.zenscript.codemodel.expression.ThisExpression;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.member.ref.FieldMemberRef;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.statement.ExpressionStatement;
import org.openzen.zenscript.codemodel.statement.ReturnStatement;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.BuiltinID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;

/**
 *
 * @author Hoofdgebruiker
 */
public class FieldMember extends PropertyMember {
	public final String name;
	public Expression initializer;
	public final int autoGetterAccess;
	public final int autoSetterAccess;
	
	public final GetterMember autoGetter;
	public final SetterMember autoSetter;
	
	public FieldMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			String name,
			ITypeID thisType,
			ITypeID type,
			GlobalTypeRegistry registry,
			int autoGetterAccess,
			int autoSetterAccess,
			BuiltinID builtin)
	{
		super(position, definition, modifiers, type, builtin);
		
		this.name = name;
		this.autoGetterAccess = autoGetterAccess;
		this.autoSetterAccess = autoSetterAccess;
		
		ITypeID[] parameters = null;
		if (definition.genericParameters != null) {
			parameters = new ITypeID[definition.genericParameters.length];
			for (int i = 0; i < parameters.length; i++)
				parameters[i] = registry.getGeneric(definition.genericParameters[i]);
		}
		
		if (autoGetterAccess != 0) {
			this.autoGetter = new GetterMember(position, definition, autoGetterAccess, name, type, null);
			this.autoGetter.setBody(new ReturnStatement(position, new GetFieldExpression(
					position,
					new ThisExpression(position, thisType),
					new FieldMemberRef(this, null))));
		} else {
			this.autoGetter = null;
		}
		if (autoSetterAccess != 0) {
			this.autoSetter = new SetterMember(position, definition, autoSetterAccess, name, type, null);
			this.autoSetter.setBody(new ExpressionStatement(position, new SetFieldExpression(
					position,
					new ThisExpression(position, thisType),
					new FieldMemberRef(this, null),
					new GetFunctionParameterExpression(position, this.autoSetter.header.parameters[0]))));
		} else {
			this.autoSetter = null;
		}
	}
	
	private FieldMember(
			CodePosition position,
			HighLevelDefinition definition,
			int modifiers,
			String name,
			ITypeID type,
			int autoGetterAccess,
			int autoSetterAccess,
			GetterMember autoGetter,
			SetterMember autoSetter,
			BuiltinID builtin)
	{
		super(position, definition, modifiers, type, builtin);
		
		this.name = name;
		this.autoGetterAccess = autoGetterAccess;
		this.autoSetterAccess = autoSetterAccess;
		this.autoGetter = autoGetter;
		this.autoSetter = autoSetter;
	}
	
	public boolean hasAutoGetter() {
		return autoGetterAccess != 0;
	}
	
	public boolean hasAutoSetter() {
		return autoSetterAccess != 0;
	}
	
	public void setInitializer(Expression initializer) {
		this.initializer = initializer;
	}

	@Override
	public void registerTo(TypeMembers members, TypeMemberPriority priority, GenericMapper mapper) {
		members.addField(new FieldMemberRef(this, mapper), priority);
	}
	
	@Override
	public BuiltinID getBuiltin() {
		return builtin;
	}

	@Override
	public String describe() {
		return "field " + name;
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitField(this);
	}

	@Override
	public DefinitionMemberRef getOverrides() {
		return null;
	}

	@Override
	public void normalize(TypeScope scope) {
		type = type.getNormalized();
		if (initializer != null)
			initializer = initializer.normalize(scope);
	}

	@Override
	public boolean isAbstract() {
		return false;
	}
}
