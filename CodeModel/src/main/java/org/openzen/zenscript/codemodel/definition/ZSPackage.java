/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.codemodel.definition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zenscript.shared.CompileException;
import org.openzen.zenscript.shared.CompileExceptionCode;
import org.openzen.zenscript.codemodel.HighLevelDefinition;
import org.openzen.zenscript.codemodel.partial.IPartialExpression;
import org.openzen.zenscript.codemodel.partial.PartialPackageExpression;
import org.openzen.zenscript.codemodel.partial.PartialTypeExpression;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.shared.CodePosition;
import org.openzen.zenscript.codemodel.scope.TypeScope;

/**
 *
 * @author Hoofdgebruiker
 */
public class ZSPackage {
	private final Map<String, ZSPackage> subPackages = new HashMap<>();
	private final Map<String, HighLevelDefinition> types = new HashMap<>();
	
	public IPartialExpression getMember(CodePosition position, GlobalTypeRegistry registry, GenericName name) {
		if (subPackages.containsKey(name.name) && name.arguments.isEmpty())
			return new PartialPackageExpression(position, subPackages.get(name.name));
		
		if (types.containsKey(name.name)) {
			if (types.get(name.name).genericParameters.size() != name.arguments.size())
				throw new CompileException(position, CompileExceptionCode.TYPE_ARGUMENTS_INVALID_NUMBER, "Invalid number of type arguments");
			
			return new PartialTypeExpression(position, registry.getForDefinition(types.get(name.name), name.arguments));
		}
		
		return null;
	}
	
	public HighLevelDefinition getImport(List<String> name, int depth) {
		if (depth >= name.size())
			return null;
		
		if (subPackages.containsKey(name.get(depth)))
			return subPackages.get(name.get(depth)).getImport(name, depth + 1);
		
		if (depth == name.size() - 1 && types.containsKey(name.get(depth)))
			return types.get(name.get(depth));
		
		return null;
	}
	
	public ITypeID getType(CodePosition position, TypeScope scope, List<GenericName> nameParts, int depth) {
		if (depth >= nameParts.size())
			return null;
		
		GenericName name = nameParts.get(depth);
		if (subPackages.containsKey(name.name) && name.arguments.isEmpty())
			return subPackages.get(name.name).getType(position, scope, nameParts, depth + 1);
		
		if (types.containsKey(name.name)) {
			ITypeID type = scope.getTypeRegistry().getForDefinition(types.get(name.name), name.arguments);
			depth++;
			while (depth < nameParts.size()) {
				GenericName innerName = nameParts.get(depth++);
				type = scope.getTypeMembers(type).getInnerType(position, innerName);
				if (type == null)
					return null;
			}
			
			return type;
		}
		
		return null;
	}
	
	public ZSPackage getOrCreatePackage(String name) {
		if (subPackages.containsKey(name))
			return subPackages.get(name);
		
		ZSPackage result = new ZSPackage();
		subPackages.put(name, result);
		return result;
	}
	
	public void register(HighLevelDefinition definition) {
		types.put(definition.name, definition);
	}
}