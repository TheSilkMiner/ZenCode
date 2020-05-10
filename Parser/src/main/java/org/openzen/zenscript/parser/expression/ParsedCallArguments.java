/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.parser.expression;

import java.util.*;
import java.util.stream.Collectors;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zencode.shared.CompileException;
import org.openzen.zencode.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.*;
import org.openzen.zenscript.codemodel.generic.*;
import org.openzen.zenscript.lexer.ZSTokenType;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.type.member.TypeMemberGroup;
import org.openzen.zenscript.lexer.ZSTokenParser;
import org.openzen.zenscript.codemodel.scope.BaseScope;
import org.openzen.zenscript.codemodel.scope.ExpressionScope;
import org.openzen.zenscript.codemodel.type.InvalidTypeID;
import org.openzen.zenscript.codemodel.type.StoredType;
import org.openzen.zenscript.lexer.ParseException;
import org.openzen.zenscript.parser.type.IParsedType;

/**
 *
 * @author Hoofdgebruiker
 */
public class ParsedCallArguments {
	public static final ParsedCallArguments NONE = new ParsedCallArguments(null, Collections.emptyList());
	
	public static ParsedCallArguments parse(ZSTokenParser tokens) throws ParseException {
		List<IParsedType> typeArguments = IParsedType.parseTypeArgumentsForCall(tokens);
		
		tokens.required(ZSTokenType.T_BROPEN, "( expected");
		
		List<ParsedExpression> arguments = new ArrayList<>();
		try {
			if (tokens.optional(ZSTokenType.T_BRCLOSE) == null) {
				do {
					arguments.add(ParsedExpression.parse(tokens));
				} while (tokens.optional(ZSTokenType.T_COMMA) != null);
				tokens.required(ZSTokenType.T_BRCLOSE, ") expected");
			}
		} catch (ParseException ex) {
			tokens.logError(ex);
			tokens.recoverUntilToken(ZSTokenType.T_BRCLOSE);
		}
		
		return new ParsedCallArguments(typeArguments, arguments);
	}
	
	public static ParsedCallArguments parseForAnnotation(ZSTokenParser tokens) throws ParseException {
		List<IParsedType> typeArguments = IParsedType.parseTypeArgumentsForCall(tokens);
		
		List<ParsedExpression> arguments = new ArrayList<>();
		if (tokens.isNext(ZSTokenType.T_BROPEN)) {
			tokens.required(ZSTokenType.T_BROPEN, "( expected");
			try {
				if (tokens.optional(ZSTokenType.T_BRCLOSE) == null) {
					do {
						arguments.add(ParsedExpression.parse(tokens));
					} while (tokens.optional(ZSTokenType.T_COMMA) != null);
					tokens.required(ZSTokenType.T_BRCLOSE, ") expected");
				}
			} catch (ParseException ex) {
				tokens.logError(ex);
				tokens.recoverUntilToken(ZSTokenType.T_BRCLOSE);
			}
		}
		
		return new ParsedCallArguments(typeArguments, arguments);
	}
	
	private final List<IParsedType> typeArguments;
	public final List<ParsedExpression> arguments;
	
	public ParsedCallArguments(List<IParsedType> typeArguments, List<ParsedExpression> arguments) {
		this.typeArguments = typeArguments;
		this.arguments = arguments;
	}
	
	public CallArguments compileCall(
			CodePosition position, 
			ExpressionScope scope,
			StoredType[] genericParameters,
			TypeMemberGroup member) throws CompileException
	{
		List<FunctionHeader> possibleHeaders = member.getMethodMembers().stream()
				.map(method -> method.member.getHeader())
				.collect(Collectors.toList());
		return compileCall(position, scope, genericParameters, possibleHeaders);
	}
	
	public CallArguments compileCall(
			CodePosition position,
			ExpressionScope scope,
			StoredType[] typeArguments,
			List<FunctionHeader> candidateFunctions) throws CompileException
	{
		if (this.typeArguments != null) {
			typeArguments = new StoredType[this.typeArguments.size()];
			for (int i = 0; i < this.typeArguments.size(); i++)
				typeArguments[i] = this.typeArguments.get(i).compile(scope);
		}
		
		List<FunctionHeader> candidates = new ArrayList<>();
		for (FunctionHeader header : candidateFunctions) {
			if (isCompatibleWith(scope, header, typeArguments))
				candidates.add(header);
		}
		
		if (candidates.isEmpty()) {
			StringBuilder explanation = new StringBuilder();
			CallArguments arguments = compileCallNaive(position, scope);
			for (FunctionHeader candidate : candidateFunctions)
				explanation.append(candidate.explainWhyIncompatible(scope, arguments)).append("\n");
			throw new CompileException(position, CompileExceptionCode.CALL_NO_VALID_METHOD, "No compatible methods found: \n" + explanation.toString());
		}

		ExpressionScope innerScope = scope;
		if (candidates.size() == 1) {
			innerScope = scope.forCall(candidates.get(0));
		} else {
			int givenTypeArguments = typeArguments == null ? 0 : typeArguments.length;
			candidates = candidates.stream()
					.filter(candidate -> candidate.getNumberOfTypeParameters() == givenTypeArguments)
					.collect(Collectors.toList());
			
			if (candidates.isEmpty()) {
				throw new CompileException(position, CompileExceptionCode.CALL_NO_VALID_METHOD, "Could not determine call type parameters");
			}
		}
		
		List<StoredType>[] predictedTypes = new List[arguments.size()];
		for (int i = 0; i < predictedTypes.length; i++)
			predictedTypes[i] = new ArrayList<>();

		for (FunctionHeader header : candidates) {
			//TODO: this is wrong!
			boolean variadic = header.isVariadic();
   
			type_parameter_replacement:
            if(typeArguments != null && typeArguments.length > 0 && header.typeParameters.length == typeArguments.length) {
                final Map<TypeParameter, StoredType> types = new HashMap<>();
                for(int i = 0; i < header.typeParameters.length; i++) {
                    if(!header.typeParameters[i].matches(scope.getMemberCache(), typeArguments[i].type)) {
                        break type_parameter_replacement;
                    }
                    types.put(header.typeParameters[i], typeArguments[i]);
                }
                header = header.withGenericArguments(new GenericMapper(position, scope.getTypeRegistry(), types));
            }
			
			for (int i = 0; i < arguments.size(); i++) {
			    
				final StoredType parameterType = header.getParameterType(variadic, i);
				if (!predictedTypes[i].contains(parameterType))
					predictedTypes[i].add(parameterType);
			}
		}
		
		Expression[] cArguments = new Expression[arguments.size()];
		for (int i = 0; i < cArguments.length; i++) {
			IPartialExpression cArgument = arguments.get(i).compile(innerScope.withHints(predictedTypes[i]));
			cArguments[i] = cArgument.eval();
		}
		
		StoredType[] typeArguments2 = typeArguments;
		if (typeArguments2 == null || typeArguments2.length == 0) {
			for (FunctionHeader candidate : candidates) {
				if (candidate.typeParameters != null) {
					typeArguments2 = new StoredType[candidate.typeParameters.length];
					for (int i = 0; i < typeArguments2.length; i++) {
						if (innerScope.genericInferenceMap.get(candidate.typeParameters[i]) == null)
							typeArguments2[i] = new InvalidTypeID(position, CompileExceptionCode.TYPE_ARGUMENTS_NOT_INFERRABLE, "Could not infer type parameter " + candidate.typeParameters[i].name).stored();
						else
							typeArguments2[i] = innerScope.genericInferenceMap.get(candidate.typeParameters[i]);
					}

					break;
				}
			}
		}
		
		return new CallArguments(typeArguments2, cArguments);
	}
	
	
	public CallArguments compileCall(
			CodePosition position,
			ExpressionScope scope,
			StoredType[] typeArguments,
			FunctionHeader function) throws CompileException
	{
		ExpressionScope innerScope = scope.forCall(function);
		
		List<StoredType>[] predictedTypes = new List[arguments.size()];
		for (int i = 0; i < predictedTypes.length; i++) {
			predictedTypes[i] = new ArrayList<>();
			predictedTypes[i].add(function.parameters[i].type);
		}
		
		Expression[] cArguments = new Expression[arguments.size()];
		for (int i = 0; i < cArguments.length; i++) {
			IPartialExpression cArgument = arguments.get(i).compile(innerScope.withHints(predictedTypes[i]));
			cArguments[i] = cArgument.eval();
		}
		
		StoredType[] typeArguments2 = typeArguments;
		if (typeArguments2 == null) {
			if (function.typeParameters != null) {
				typeArguments2 = new StoredType[function.typeParameters.length];
				for (int i = 0; i < typeArguments2.length; i++) {
					if (innerScope.genericInferenceMap.get(function.typeParameters[i]) == null)
						throw new CompileException(position, CompileExceptionCode.TYPE_ARGUMENTS_NOT_INFERRABLE, "Could not infer type parameter " + function.typeParameters[i].name);
					else
						typeArguments2[i] = innerScope.genericInferenceMap.get(function.typeParameters[i]);
				}
			}
		}
		
		return new CallArguments(typeArguments2, cArguments);
	}
	
	private CallArguments compileCallNaive(CodePosition position, ExpressionScope scope) throws CompileException {
		Expression[] cArguments = new Expression[arguments.size()];
		for (int i = 0; i < cArguments.length; i++) {
			IPartialExpression cArgument = arguments.get(i).compile(scope);
			cArguments[i] = cArgument.eval();
		}
		return new CallArguments(StoredType.NONE, cArguments);
	}
	
	private boolean isCompatibleWith(BaseScope scope, FunctionHeader header, StoredType[] typeArguments) {
		if (!header.accepts(arguments.size()))
			return false;

		//TODO: This is wrong
		boolean variadic = header.isVariadic();
		for (int i = 0; i < arguments.size(); i++) {
			FunctionParameter parameter = header.getParameter(variadic, i);
			if (typeArguments == null && header.typeParameters != null && parameter.type.hasInferenceBlockingTypeParameters(header.typeParameters))
				return false;
			
			if (!arguments.get(i).isCompatibleWith(scope, header.getParameterType(variadic, i).getNormalized()))
				return false;
		}
		
		return true;
	}
}
