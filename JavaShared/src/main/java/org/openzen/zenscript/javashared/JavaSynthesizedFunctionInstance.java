/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zenscript.javashared;

import org.openzen.zenscript.codemodel.type.TypeID;

/**
 *
 * @author Hoofdgebruiker
 */
public class JavaSynthesizedFunctionInstance {
	private final JavaSynthesizedFunction function;
	public final TypeID[] typeArguments;
	
	public JavaSynthesizedFunctionInstance(JavaSynthesizedFunction function, TypeID[] typeArguments) {
		this.function = function;
		this.typeArguments = typeArguments;
	}
	
	public JavaClass getCls() {
		return function.cls;
	}
	
	public String getMethod() {
		return function.method;
	}
}
