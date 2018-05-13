/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.member;

import java.util.Map;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.Modifiers;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.CastExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPriority;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Hoofdgebruiker
 */
public class CasterMember extends FunctionalMember implements ICasterMember {
	public final ITypeID toType;
	
	public CasterMember(CodePosition position, HighLevelDefinition definition, int modifiers, ITypeID toType) {
		super(position, definition, modifiers, "as", new FunctionHeader(toType));
		
		this.toType = toType;
	}
	
	@Override
	public String getInformalName() {
		return "caster to " + toType.toString();
	}

	@Override
	public void registerTo(TypeMembers type, TypeMemberPriority priority) {
		type.addCaster(this, priority);
	}

	@Override
	public String describe() {
		return "caster to " + toType.toString();
	}

	@Override
	public DefinitionMember instance(GlobalTypeRegistry registry, Map<TypeParameter, ITypeID> mapping) {
		return new CasterMember(position, definition, modifiers, toType.withGenericArguments(registry, mapping));
	}
	
	@Override
	public ITypeID getTargetType() {
		return toType;
	}

	@Override
	public Expression call(CodePosition position, Expression target, FunctionHeader instancedHeader, CallArguments arguments, TypeScope scope) {
		throw new UnsupportedOperationException("Cannot call a caster!");
	}

	@Override
	public Expression cast(CodePosition position, Expression value, boolean implicit) {
		return new CastExpression(position, value, this, implicit);
	}
	
	@Override
	public boolean isImplicit() {
		return Modifiers.isImplicit(modifiers);
	}

	@Override
	public <T> T accept(MemberVisitor<T> visitor) {
		return visitor.visitCaster(this);
	}
}
