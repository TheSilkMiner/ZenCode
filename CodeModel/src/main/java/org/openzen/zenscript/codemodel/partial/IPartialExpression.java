/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.partial;

import java.util.Collections;
import java.util.List;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.InvalidExpression;
import org.openzen.zenscript.codemodel.expression.LambdaClosure;
import org.openzen.zenscript.codemodel.member.IDefinitionMember;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public interface IPartialExpression {
	default List<StoredType> getAssignHints() {
		return Collections.emptyList();
	}
	
	Expression eval() throws CompileException;
	
	List<StoredType>[] predictCallTypes(TypeScope scope, List<StoredType> hints, int arguments) throws CompileException;
	
	List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<StoredType> hints, int arguments) throws CompileException;
	
	IPartialExpression getMember(CodePosition position, TypeScope scope, List<StoredType> hints, GenericName name) throws CompileException;
	
	Expression call(CodePosition position, TypeScope scope, List<StoredType> hints, CallArguments arguments) throws CompileException;
	
	TypeID[] getGenericCallTypes();
	
	/**
	 * Retrieves the (primary) member this expression refers to, or null if there is no primary target.
	 * 
	 * @return 
	 */
	default IDefinitionMember getMember() {
		return null;
	}
	
	default Expression assign(CodePosition position, TypeScope scope, Expression value) throws CompileException {
		return new InvalidExpression(position, value.type, CompileExceptionCode.CANNOT_ASSIGN, "This expression is not assignable");
	}
	
	default IPartialExpression capture(CodePosition position, LambdaClosure closure) throws CompileException {
		throw new CompileException(position, CompileExceptionCode.UNAVAILABLE_IN_CLOSURE, "expression not allowed in closure");
	}
}
