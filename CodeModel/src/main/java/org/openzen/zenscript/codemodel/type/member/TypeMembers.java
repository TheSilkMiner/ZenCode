/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.type.member;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.codemodel.CompareType;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.CheckNullExpression;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InterfaceCastExpression;
import org.openzen.zenscript.codemodel.expression.MakeConstExpression;
import org.openzen.zenscript.codemodel.expression.NotExpression;
import org.openzen.zenscript.codemodel.expression.NullExpression;
import org.openzen.zenscript.codemodel.expression.WrapOptionalExpression;
import org.openzen.zenscript.codemodel.member.CallerMember;
import org.openzen.zenscript.codemodel.member.CasterMember;
import org.openzen.zenscript.codemodel.member.ConstructorMember;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.FieldMember;
import org.openzen.zenscript.codemodel.member.ICallableMember;
import org.openzen.zenscript.codemodel.member.ICasterMember;
import org.openzen.zenscript.codemodel.member.IGettableMember;
import org.openzen.zenscript.codemodel.member.IIteratorMember;
import org.openzen.zenscript.codemodel.member.ImplementationMember;
import org.openzen.zenscript.codemodel.member.InnerDefinition;
import org.openzen.zenscript.codemodel.member.MethodMember;
import org.openzen.zenscript.codemodel.member.OperatorMember;
import org.openzen.zenscript.codemodel.member.SetterMember;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialMemberGroupExpression;
import org.openzen.zenscript.codemodel.partial.PartialStaticMemberGroupExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.type.BasicTypeID;
import org.openzen.zenscript.codemodel.type.DefinitionTypeID;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public final class TypeMembers {
	public static final int MODIFIER_OPTIONAL = 1;
	public static final int MODIFIER_CONST = 2;
	public static final int MODIFIER_SHARED = 4;
	public static final int MODIFIER_WEAK = 8;
	
	private final LocalMemberCache cache;
	public final ITypeID type;
	
	private final List<TypeMember<ICasterMember>> casters = new ArrayList<>();
	private final List<TypeMember<ImplementationMember>> implementations = new ArrayList<>();
	private final List<TypeMember<IIteratorMember>> iterators = new ArrayList<>();
	
	private final Map<String, EnumConstantMember> enumMembers = new HashMap<>();
	private final Map<String, DefinitionMemberGroup> members = new HashMap<>();
	private final Map<String, InnerDefinition> innerTypes = new HashMap<>();
	private final Map<OperatorType, DefinitionMemberGroup> operators = new HashMap<>();
	
	public TypeMembers(LocalMemberCache cache, ITypeID type) {
		this.cache = cache;
		this.type = type;
	}
	
	public LocalMemberCache getMemberCache() {
		return cache;
	}
	
	public GlobalTypeRegistry getTypeRegistry() {
		return cache.getRegistry();
	}
	
	public void copyMembersTo(CodePosition position, TypeMembers other, TypeMemberPriority priority) {
		other.casters.addAll(casters);
		
		for (Map.Entry<String, EnumConstantMember> entry : enumMembers.entrySet())
			other.addEnumMember(entry.getValue(), priority);
		for (Map.Entry<String, DefinitionMemberGroup> entry : members.entrySet())
			other.getOrCreateGroup(entry.getKey()).merge(position, entry.getValue(), priority);
		for (Map.Entry<String, InnerDefinition> entry : innerTypes.entrySet())
			other.innerTypes.put(entry.getKey(), entry.getValue());
		for (Map.Entry<OperatorType, DefinitionMemberGroup> entry : operators.entrySet())
			other.getOrCreateGroup(entry.getKey()).merge(position, entry.getValue(), priority);
	}
	
	public ITypeID union(ITypeID other) {
		if (type == other)
			return type;
		
		if (this.canCastImplicit(other))
			return other;
		if (cache.get(other).canCastImplicit(type))
			return type;
		
		return null;
	}
	
	public void addConstructor(ConstructorMember constructor, TypeMemberPriority priority) {
		getOrCreateGroup(OperatorType.CONSTRUCTOR).addMethod(constructor, priority);
	}
	
	public void addCaller(CallerMember caller, TypeMemberPriority priority) {
		getOrCreateGroup(OperatorType.CALL).addMethod(caller, priority);
	}
	
	public void addCaster(CasterMember caster, TypeMemberPriority priority) {
		for (int i = 0; i < casters.size(); i++) {
			if (casters.get(i).member.getTargetType() == caster.toType) {
				casters.set(i, casters.get(i).resolve(new TypeMember<>(priority, caster)));
				return;
			}
		}
		
		casters.add(new TypeMember<>(priority, caster));
	}
	
	public void addField(FieldMember member, TypeMemberPriority priority) {
		DefinitionMemberGroup group = getOrCreateGroup(member.name);
		group.setField(member, priority);
	}
	
	public void addGetter(IGettableMember member, TypeMemberPriority priority) {
		DefinitionMemberGroup group = getOrCreateGroup(member.getName());
		group.setGetter(member, priority);
	}
	
	public void addSetter(SetterMember member, TypeMemberPriority priority) {
		DefinitionMemberGroup group = getOrCreateGroup(member.name);
		group.setSetter(member, priority);
	}
	
	public void addMethod(MethodMember member, TypeMemberPriority priority) {
		DefinitionMemberGroup group = getOrCreateGroup(member.name);
		group.addMethod(member, priority);
	}
	
	public void addOperator(OperatorMember member, TypeMemberPriority priority) {
		DefinitionMemberGroup group = getOrCreateGroup(member.operator);
		group.addMethod(member, priority);
	}
	
	public void addOperator(OperatorType operator, ICallableMember member, TypeMemberPriority priority) {
		DefinitionMemberGroup group = getOrCreateGroup(operator);
		group.addMethod(member, priority);
	}
	
	public void addIterator(IIteratorMember iterator, TypeMemberPriority priority) {
		for (int i = 0; i < iterators.size(); i++) {
			if (iterators.get(i).member.getLoopVariableCount() == iterator.getLoopVariableCount()) {
				iterators.set(i, iterators.get(i).resolve(new TypeMember<>(priority, iterator)));
				return;
			}
		}
		
		iterators.add(new TypeMember<>(priority, iterator));
	}
	
	public void addImplementation(ImplementationMember member, TypeMemberPriority priority) {
		for (int i = 0; i < implementations.size(); i++) {
			if (implementations.get(i).member.type == member.type) {
				implementations.set(i, implementations.get(i).resolve(new TypeMember<>(priority, member)));
				return;
			}
		}
		
		implementations.add(new TypeMember<>(priority, member));
	}
	
	public void addInnerType(String name, InnerDefinition type) {
		innerTypes.put(name, type);
	}
	
	public DefinitionMemberGroup getOrCreateGroup(String name) {
		if (!members.containsKey(name))
			members.put(name, new DefinitionMemberGroup());
		
		return members.get(name);
	}
	
	public DefinitionMemberGroup getOrCreateGroup(OperatorType operator) {
		if (!operators.containsKey(operator))
			operators.put(operator, new DefinitionMemberGroup());
		
		return operators.get(operator);
	}
	
	public void addEnumMember(EnumConstantMember member, TypeMemberPriority priority) {
		if (enumMembers.containsKey(member.name))
			throw new CompileException(member.position, CompileExceptionCode.ENUM_VALUE_DUPLICATE, "Duplicate enum member " + member.name);
		
		enumMembers.put(member.name, member);
	}
	
	public EnumConstantMember getEnumMember(String name) {
		return enumMembers.get(name);
	}
	
	public Expression compare(CodePosition position, TypeScope scope, CompareType operator, Expression left, Expression right) {
		if (operator == CompareType.EQ) {
			DefinitionMemberGroup equal = getOrCreateGroup(OperatorType.EQUALS);
			for (TypeMember<ICallableMember> member : equal.getMethodMembers()) {
				if (member.member.getHeader().accepts(scope, right))
					return equal.call(position, scope, left, new CallArguments(right), false);
			}
		} else if (operator == CompareType.NE) {
			DefinitionMemberGroup equal = getOrCreateGroup(OperatorType.EQUALS);
			for (TypeMember<ICallableMember> member : equal.getMethodMembers()) {
				if (member.member.getHeader().accepts(scope, right))
					return new NotExpression(position, equal.call(position, scope, left, new CallArguments(right), false));
			}
		}
		
		DefinitionMemberGroup compare = getOrCreateGroup(OperatorType.COMPARE);
		return compare.callWithComparator(position, scope, left, new CallArguments(right), operator);
	}
	
	public Expression unary(CodePosition position, TypeScope scope, OperatorType operator, Expression value) {
		DefinitionMemberGroup members = getOrCreateGroup(operator);
		return members.call(position, scope, value, new CallArguments(), false);
	}
	
	public Expression ternary(CodePosition position, TypeScope scope, OperatorType operator, Expression a, Expression b, Expression c) {
		DefinitionMemberGroup members = getOrCreateGroup(operator);
		return members.call(position, scope, a, new CallArguments(b, c), false);
	}
	
	public IIteratorMember getIterator(int variables) {
		for (TypeMember<IIteratorMember> iterator : iterators)
			if (iterator.member.getLoopVariableCount() == variables)
				return iterator.member;
		
		return null;
	}
	
	public ITypeID[] getLoopTypes(int variables) {
		for (TypeMember<IIteratorMember> iterator : iterators)
			if (iterator.member.getLoopVariableCount() == variables)
				return iterator.member.getLoopVariableTypes();
		
		return null;
	}
	
	public boolean canCastImplicit(ITypeID toType) {
		if (toType == type)
			return true;
		if (toType == null)
			throw new NullPointerException();
		if (type == BasicTypeID.ANY || toType == BasicTypeID.ANY || toType == BasicTypeID.UNDETERMINED)
			return true;
		
		if (type == BasicTypeID.NULL && toType.isOptional())
			return true;
		if (toType.isOptional() && canCastImplicit(toType.unwrap()))
			return true;
		if (toType.isConst() && canCastImplicit(toType.unwrap()))
			return true;
		if (type.isOptional() && type.unwrap() == toType)
			return true;
		
		for (TypeMember<ICasterMember> caster : casters) {
			if (caster.member.isImplicit() && toType == caster.member.getTargetType())
				return true;
		}
		for (TypeMember<ImplementationMember> implementation : implementations) {
			if (implementation.member.type == toType)
				return true;
		}
		
		return false;
	}
	
	public Expression castImplicit(CodePosition position, Expression value, ITypeID toType) {
		if (toType == type || toType == BasicTypeID.UNDETERMINED)
			return value;
		if (toType == null)
			throw new NullPointerException();
		
		if (type == BasicTypeID.NULL && toType.isOptional())
			return new NullExpression(position, toType);
		if (toType.isOptional() && canCastImplicit(toType.unwrap()))
			return new WrapOptionalExpression(position, castImplicit(position, value, toType.unwrap()), toType);
		if (toType.isConst() && canCastImplicit(toType.unwrap()))
			return new MakeConstExpression(position, castImplicit(position, value, toType.unwrap()), toType);
		if (type.isOptional() && type.unwrap() == toType)
			return new CheckNullExpression(position, value);
		
		for (TypeMember<ICasterMember> caster : casters) {
			if (caster.member.isImplicit() && toType == caster.member.getTargetType())
				return caster.member.cast(position, value, toType);
		}
		for (TypeMember<ImplementationMember> implementation : implementations) {
			if (implementation.member.type == toType)
				return new InterfaceCastExpression(position, value, toType);
		}
		
		throw new CompileException(position, CompileExceptionCode.INVALID_CAST, "Could not cast " + toString() + " to " + toType);
	}
	
	public Expression castExplicit(CodePosition position, Expression value, ITypeID toType, boolean optional) {
		if (this.canCastImplicit(toType))
			return castImplicit(position, value, toType);
		
		for (TypeMember<ICasterMember> caster : casters)
			if (toType == caster.member.getTargetType())
				return caster.member.cast(position, value, toType);
		
		throw new CompileException(position, CompileExceptionCode.INVALID_CAST, "Cannot cast " + toString() + " to " + toType + ", even explicitly");
	}
	
	public boolean hasMember(String name) {
		return members.containsKey(name);
	}
	
	public IPartialExpression getMemberExpression(CodePosition position, Expression target, GenericName name, boolean allowStatic) {
		if (members.containsKey(name.name) && name.arguments.isEmpty())
			return new PartialMemberGroupExpression(position, target, members.get(name.name), allowStatic);
		
		return null;
	}
	
	public IPartialExpression getStaticMemberExpression(CodePosition position, GenericName name) {
		if (members.containsKey(name.name))
			return new PartialStaticMemberGroupExpression(position, members.get(name.name));
		if (innerTypes.containsKey(name.name))
			return new PartialTypeExpression(position, innerTypes.get(name.name).instance(cache.getRegistry(), name.arguments));
		
		return null;
	}
	
	public boolean hasInnerType(String name) {
		return innerTypes.containsKey(name);
	}
	
	public DefinitionTypeID getInnerType(CodePosition position, GenericName name) {
		if (!innerTypes.containsKey(name.name))
			throw new CompileException(position, CompileExceptionCode.NO_SUCH_INNER_TYPE, "No such inner type in " + type + ": " + name.name);
		
		return innerTypes.get(name.name).instance(cache.getRegistry(), name.arguments);
	}
	
	@Override
	public String toString() {
		return type.toString();
	}
}