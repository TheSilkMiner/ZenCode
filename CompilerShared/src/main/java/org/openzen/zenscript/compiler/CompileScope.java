/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.compiler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openzen.zencode.shared.CodePosition;
import org.openzen.zenscript.codemodel.annotations.AnnotationDefinition;
import org.openzen.zenscript.codemodel.definition.ExpansionDefinition;
import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.GenericName;
import org.openzen.zenscript.codemodel.type.GlobalTypeRegistry;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.codemodel.type.member.LocalMemberCache;
import org.openzen.zenscript.codemodel.type.member.TypeMemberPreparer;
import org.openzen.zenscript.codemodel.type.member.TypeMembers;

/**
 *
 * @author Hoofdgebruiker
 */
public class CompileScope implements TypeScope {
	private final GlobalTypeRegistry globalRegistry;
	private final List<ExpansionDefinition> expansions;
	private final LocalMemberCache cache;
	private final Map<String, AnnotationDefinition> annotations = new HashMap<>();
	
	public CompileScope(GlobalTypeRegistry globalRegistry, List<ExpansionDefinition> expansions, AnnotationDefinition[] annotations) {
		this.globalRegistry = globalRegistry;
		this.expansions = expansions;
		this.cache = new LocalMemberCache(globalRegistry, expansions);
		
		for (AnnotationDefinition annotation : annotations) {
			this.annotations.put(annotation.getAnnotationName(), annotation);
		}
	}

	@Override
	public GlobalTypeRegistry getTypeRegistry() {
		return globalRegistry;
	}

	@Override
	public LocalMemberCache getMemberCache() {
		return cache;
	}

	@Override
	public TypeMembers getTypeMembers(ITypeID type) {
		return cache.get(type);
	}

	@Override
	public AnnotationDefinition getAnnotation(String name) {
		return annotations.get(name);
	}

	@Override
	public ITypeID getType(CodePosition position, List<GenericName> name) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public ITypeID getThisType() {
		return null;
	}

	@Override
	public TypeMemberPreparer getPreparer() {
		return member -> {};
	}
}
