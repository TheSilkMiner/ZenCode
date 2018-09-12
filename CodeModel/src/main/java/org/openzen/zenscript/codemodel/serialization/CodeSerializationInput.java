/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.serialization;

import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.FunctionHeader;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.context.ModuleContext;
import org.openzen.zenscript.codemodel.expression.CallArguments;
import org.openzen.zenscript.codemodel.expression.Expression;
import org.openzen.zenscript.codemodel.member.ref.DefinitionMemberRef;
import org.openzen.zenscript.codemodel.statement.Statement;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.context.StatementContext;
import org.openzen.zenscript.codemodel.context.TypeContext;
import org.openzen.zenscript.codemodel.generic.TypeParameter;
import org.openzen.zenscript.codemodel.member.EnumConstantMember;
import org.openzen.zenscript.codemodel.member.ref.VariantOptionRef;

/**
 *
 * @author Hoofdgebruiker
 */
public interface CodeSerializationInput {
	boolean readBool();
	
	int readByte();
	
	byte readSByte();
	
	short readShort();
	
	int readUShort();
	
	int readInt();
	
	int readUInt();
	
	long readLong();
	
	long readULong();
	
	float readFloat();
	
	double readDouble();
	
	char readChar();
	
	String readString();
	
	HighLevelDefinition readDefinition();
	
	DefinitionMemberRef readMember(TypeContext context, ITypeID type);
	
	EnumConstantMember readEnumConstant(TypeContext context);
	
	VariantOptionRef readVariantOption(TypeContext context, ITypeID type);
	
	ITypeID deserializeType(TypeContext context);
	
	CodePosition deserializePosition();
	
	FunctionHeader deserializeHeader(TypeContext context);
	
	CallArguments deserializeArguments(StatementContext context);
	
	Statement deserializeStatement(StatementContext context);
	
	Expression deserializeExpression(StatementContext context);
	
	TypeParameter deserializeTypeParameter(TypeContext context);
	
	TypeParameter[] deserializeTypeParameters(TypeContext context);
	
	void enqueueMembers(DecodingOperation operation);
	
	public void enqueueCode(DecodingOperation operation);
}