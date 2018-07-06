/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javasource.scope;

import org.openzen.zenscript.codemodel.scope.TypeScope;
import org.openzen.zenscript.codemodel.type.ITypeID;
import org.openzen.zenscript.javasource.JavaSourceImporter;
import org.openzen.zenscript.javasource.JavaSourceObjectTypeVisitor;
import org.openzen.zenscript.javasource.JavaSourceSyntheticHelperGenerator;
import org.openzen.zenscript.javasource.JavaSourceSyntheticTypeGenerator;
import org.openzen.zenscript.javasource.JavaSourceTypeVisitor;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSourceFileScope {
	public final JavaSourceImporter importer;
	public final JavaSourceSyntheticTypeGenerator typeGenerator;
	public final JavaSourceSyntheticHelperGenerator helperGenerator;
	public final String className;
	public final JavaSourceTypeVisitor typeVisitor;
	public final JavaSourceObjectTypeVisitor objectTypeVisitor;
	public final TypeScope semanticScope;
	public final boolean isInterface;
	
	public JavaSourceFileScope(
			JavaSourceImporter importer, 
			JavaSourceSyntheticTypeGenerator typeGenerator,
			JavaSourceSyntheticHelperGenerator helperGenerator,
			String className,
			TypeScope semanticScope,
			boolean isInterface)
	{
		this.importer = importer;
		this.typeGenerator = typeGenerator;
		this.helperGenerator = helperGenerator;
		this.className = className;
		this.semanticScope = semanticScope;
		this.isInterface = isInterface;
		
		typeVisitor = new JavaSourceTypeVisitor(importer, typeGenerator);
		objectTypeVisitor = typeVisitor.objectTypeVisitor;
	}
	
	public String type(ITypeID type) {
		return type.accept(typeVisitor);
	}
}