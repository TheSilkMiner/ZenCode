/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openzen.zenscript.parser.expression;

import java.util.List;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.OperatorType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.DefinitionMemberGroup;
import org.openzen.zenscript.linker.ExpressionScope;
import org.openzen.zenscript.shared.CodePosition;

/**
 *
 * @author Stanneke
 */
public class ParsedExpressionIndex extends ParsedExpression {
	private final ParsedExpression value;
	private final List<ParsedExpression> indexes;

	public ParsedExpressionIndex(CodePosition position, ParsedExpression value, List<ParsedExpression> indexes) {
		super(position);

		this.value = value;
		this.indexes = indexes;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) {
		return new PartialIndexedExpression(scope);
	}

	@Override
	public boolean hasStrongType() {
		return true;
	}
	
	private class PartialIndexedExpression implements IPartialExpression {
		private final ExpressionScope scope;
		private final Expression target;
		
		private PartialIndexedExpression(ExpressionScope scope) {
			this.scope = scope;
			target = value.compile(scope.withoutHints()).eval();
		}
		
		@Override
		public Expression eval() {
			DefinitionMemberGroup members = scope.getTypeMembers(target.type).getOrCreateGroup(OperatorType.INDEXGET);
			List<ITypeID>[] predictedTypes = members.predictCallTypes(scope, scope.hints, indexes.size());
			Expression[] arguments = new Expression[indexes.size()];
			for (int i = 0; i < arguments.length; i++)
				arguments[i] = indexes.get(i).compile(scope.createInner(predictedTypes[i], this::getLength)).eval();
			
			return members.call(position, scope, target, new CallArguments(arguments), false);
		}

		@Override
		public List<ITypeID>[] predictCallTypes(TypeScope scope, List<ITypeID> hints, int arguments) {
			return eval().predictCallTypes(scope, hints, arguments);
		}
		
		@Override
		public List<FunctionHeader> getPossibleFunctionHeaders(TypeScope scope, List<ITypeID> hints, int arguments) {
			return eval().getPossibleFunctionHeaders(scope, hints, arguments);
		}

		@Override
		public IPartialExpression getMember(CodePosition position, TypeScope scope, List<ITypeID> hints, GenericName name) {
			return eval().getMember(position, scope, hints, name);
		}

		@Override
		public Expression call(CodePosition position, TypeScope scope, List<ITypeID> hints, CallArguments arguments) {
			return eval().call(position, scope, hints, arguments);
		}
		
		@Override
		public Expression assign(CodePosition position, TypeScope scope, Expression value) {
			DefinitionMemberGroup members = scope.getTypeMembers(target.type).getOrCreateGroup(OperatorType.INDEXSET);
			List<ITypeID>[] predictedTypes = members.predictCallTypes(scope, this.scope.hints, indexes.size() + 1);
			
			Expression[] arguments = new Expression[indexes.size() + 1];
			for (int i = 0; i < arguments.length - 1; i++)
				arguments[i] = indexes.get(i).compile(this.scope.createInner(predictedTypes[i], this::getLength)).eval();
			arguments[indexes.size()] = value;
			
			return members.call(position, scope, target, new CallArguments(arguments), false);
		}
		
		@Override
		public List<ITypeID> getAssignHints() {
			DefinitionMemberGroup members = scope.getTypeMembers(target.type).getOrCreateGroup(OperatorType.INDEXSET);
			List<ITypeID>[] predictedTypes = members.predictCallTypes(scope, scope.hints, indexes.size() + 1);
			return predictedTypes[indexes.size()];
		}
		
		private Expression getLength(CodePosition position) {
			return target.getMember(position, scope, scope.hints, new GenericName("length")).eval();
		}
	}
}