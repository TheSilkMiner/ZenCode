package org.openzen.zenscript.parser.expression;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.expression.MatchExpression;
import org.openzen.zenscript.codemodel.expression.switchvalue.SwitchValue;
import org.openzen.zenscript.codemodel.expression.switchvalue.VariantOptionSwitchValue;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.TypeID;

import java.util.List;

public class ParsedMatchExpression extends ParsedExpression {
	public final ParsedExpression value;
	public final List<Case> cases;

	public ParsedMatchExpression(CodePosition position, ParsedExpression value, List<Case> cases) {
		super(position);

		this.value = value;
		this.cases = cases;
	}

	@Override
	public IPartialExpression compile(ExpressionScope scope) throws CompileException {
		Expression cValue = value.compile(scope).eval();
		MatchExpression.Case[] cCases = new MatchExpression.Case[cases.size()];
		for (int i = 0; i < cases.size(); i++) {
			Case matchCase = cases.get(i);
			cCases[i] = matchCase.compile(cValue.type, scope);
		}

		TypeID result = cCases[0].value.type;
		for (int i = 1; i < cCases.length; i++) {
			TypeID oldResult = result;
			result = scope.getTypeMembers(result).union(cCases[i].value.type);
			if (result == null)
				throw new CompileException(position, CompileExceptionCode.TYPE_CANNOT_UNITE, "Matches have different types: " + oldResult + " and " + cCases[i].value.type);
		}

		return new MatchExpression(position, cValue, result, cCases);
	}

	@Override
	public boolean hasStrongType() {
		return false;
	}

	public static class Case {
		public final ParsedExpression name;
		public final ParsedExpression value;

		public Case(ParsedExpression name, ParsedExpression body) {
			this.name = name;
			this.value = body;
		}

		public MatchExpression.Case compile(TypeID valueType, ExpressionScope scope) throws CompileException {
			if (name == null) {
				ExpressionScope innerScope = scope.createInner(scope.hints, scope.getDollar());
				Expression value = this.value.compile(innerScope).eval();
				return new MatchExpression.Case(null, value);
			}

			SwitchValue switchValue = name.compileToSwitchValue(valueType, scope.withHint(valueType));
			ExpressionScope innerScope = scope.createInner(scope.hints, scope.getDollar());
			if (switchValue instanceof VariantOptionSwitchValue) {
				VariantOptionSwitchValue variantSwitchValue = (VariantOptionSwitchValue) switchValue;

				for (int i = 0; i < variantSwitchValue.parameters.length; i++)
					innerScope.addMatchingVariantOption(variantSwitchValue.parameters[i], i, variantSwitchValue);
			}

			Expression value = this.value.compile(innerScope).eval();
			return new MatchExpression.Case(switchValue, value);
		}
	}
}
